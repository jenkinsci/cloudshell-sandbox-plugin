/*   Copyright 2013, MANDIANT, Eric Lordahl
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.jenkinsci.plugins.cloudshell;

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import net.sf.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class SandboxAPIProxy {
    public static final String BLUEPRINT_CONFLICT_ERROR = "Blueprint has conflicting resources";
    protected final CsServerDetails serverDetails;

    public class SandboxApiException extends Exception {

        public SandboxApiException(String message) {
            super(message);
        }

    }

    public class ReserveBluePrintConflictException extends SandboxApiException {
        private String bluePrintIdentifier;

        public ReserveBluePrintConflictException(String bluePrintIdentifier, String message) {
            super(message);
        }

        public String getBluePrintIdentifier() {
            return bluePrintIdentifier;
        }
    }

    public SandboxAPIProxy(String serverAddress, String user, String pw, int port, String domain, boolean ignoreSSL)
	{
        this.serverDetails=new CsServerDetails(serverAddress,user,pw, domain, ignoreSSL);
    }

    public SandboxAPIProxy(CsServerDetails serverDetails){
        this.serverDetails=serverDetails;
    }

    public String WaitForSetup(String sandBoxId, int waitForSetup, boolean ignoreSSL, BuildListener listener)
            throws SandboxApiException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        try {

            listener.getLogger().println("Waiting for setup...");
            WaitForSandBox(sandBoxId, "Ready", waitForSetup, ignoreSSL, listener);

            return sandBoxId;
        }
        catch (Exception e)
        {
            throw e;
        }
    }

    public String GetSandBoxDetails(AbstractBuild<?,?> build, String sandboxId, boolean ignoreSSL, BuildListener listener)
            throws SandboxApiException, UnsupportedEncodingException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return SandboxDetails(sandboxId, ignoreSSL).toString();
    }

    public String StartBluePrint(AbstractBuild<?,?> build, String bluePrintName, String sandBoxName, String duration, boolean waitForSetup, boolean ignoreSSL, BuildListener listener)
            throws SandboxApiException, UnsupportedEncodingException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        RestResponse response = HTTPWrapper.InvokeLogin(GetBaseUrl(), this.serverDetails.user, this.serverDetails.pw, this.serverDetails.domain, ignoreSSL);
        String url = GetBaseUrl() + "/v1/blueprints/"+ URLEncoder.encode(bluePrintName, "UTF-8") +"/start";
        RestResponse result = HTTPWrapper.ExecutePost(url, response.getContent(), sandBoxName, duration, ignoreSSL);
        JSONObject j = JSONObject.fromObject(result.getContent());
        if (j.containsKey("errorCategory")) {
            String message = j.get("message").toString();
            if (message.equals(BLUEPRINT_CONFLICT_ERROR)){
                throw new ReserveBluePrintConflictException(bluePrintName,message);
            }
            listener.getLogger().println("Error starting sandbox. Response was: " + result.getContent() + " HTTP Code: " + result.getHttpCode());
            throw new SandboxApiException(bluePrintName);
        }
        String newSb = j.getString("id");
        return newSb;
    }

    public void StopBluePrint(String sandboxId, boolean waitForComplete, boolean ignoreSSL, BuildListener listener) throws SandboxApiException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        RestResponse response = HTTPWrapper.InvokeLogin(GetBaseUrl(), this.serverDetails.user, this.serverDetails.pw, "Global",ignoreSSL);
        String url = GetBaseUrl() + "/v1/sandboxes/" + sandboxId + "/stop";
        RestResponse result = HTTPWrapper.ExecutePost(url, response.getContent(), null, null, ignoreSSL);
       /* if (response.getHttpCode() != 200) {
            throw new RuntimeException("Failed to stop: "
                    + response.getHttpCode());
        }*/
        JSONObject j = JSONObject.fromObject(result.getContent());
        if (j.containsKey("errorCategory")) {
            throw new SandboxApiException("Failed to stop blueprint: " + j);
        }

        try
        {
            if (waitForComplete)
            {
                WaitForSandBox(sandboxId, "Ended", 300, ignoreSSL, listener);
            }
            listener.getLogger().println("SandBox Stopped: ");
            listener.getLogger().println(sandboxId);
        }
        catch (Exception e)
        {
            listener.getLogger().println(j);
        }
    }

    private String GetSandBoxStatus(String sb, boolean ignoreSSL) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return SandboxDetails(sb, ignoreSSL).getString("state");
    }

    private void WaitForSandBox(String sandboxId, String status, int timeoutSec, boolean ignoreSSL, BuildListener listener) throws SandboxApiException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        long startTime = System.currentTimeMillis();

        String sandboxStatus = GetSandBoxStatus(sandboxId, ignoreSSL);
        while (!sandboxStatus.equals(status) && (System.currentTimeMillis()-startTime) < timeoutSec*1000)
        {
            if (sandboxStatus.equals("Error"))
            {
                listener.getLogger().println("There was an error setting up the sandbox. Aborting");
                throw new SandboxApiException("Sandbox status is: Error");
            }
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sandboxStatus = GetSandBoxStatus(sandboxId, ignoreSSL);
        }
    }

    private JSONObject SandboxDetails(String sb, boolean ignoreSSL) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        RestResponse response= HTTPWrapper.InvokeLogin(GetBaseUrl(), this.serverDetails.user, this.serverDetails.pw, "Global", ignoreSSL);
        String url = GetBaseUrl() + "/v1/sandboxes/" + sb;
        RestResponse result = HTTPWrapper.ExecuteGet(url, response.getContent(), ignoreSSL);

        JSONObject j = JSONObject.fromObject(result.getContent());

        if (j.toString().contains("errorCategory")) {
            throw new RuntimeException("Failed to get sandbox details: " + j);
        }
        return j;
    }

    private String GetBaseUrl()
    {
        return this.serverDetails.serverAddress + "/Api";
    }

}
