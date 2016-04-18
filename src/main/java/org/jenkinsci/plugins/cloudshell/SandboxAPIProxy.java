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

    public SandboxAPIProxy(String serverAddress, String user, String pw, int port, String domain)
	{
        this.serverDetails=new CsServerDetails(serverAddress,user,pw, domain, port);
    }

    public SandboxAPIProxy(CsServerDetails serverDetails){
        this.serverDetails=serverDetails;
    }

    public String StartBluePrint(AbstractBuild<?,?> build, String bluePrintName, String sandBoxName, String duration, boolean waitForSetup, BuildListener listener)
            throws SandboxApiException
    {
        String token = HTTPWrapper.InvokeLogin(GetBaseUrl(), this.serverDetails.user, this.serverDetails.pw, this.serverDetails.domain);
        String url = GetBaseUrl() + "/v1/blueprints/"+ bluePrintName +"/start";

        String result = HTTPWrapper.ExecutePost(url, token, sandBoxName, duration);
        JSONObject j = JSONObject.fromObject(result);
        if (j.containsKey("errorCategory")) {
            String message = j.get("message").toString();
            if (message.equals(BLUEPRINT_CONFLICT_ERROR)){
                throw new ReserveBluePrintConflictException(bluePrintName,message);
            }
            throw new SandboxApiException(bluePrintName);
        }
        try
        {
            listener.getLogger().println("SandBox Created: ");
            String newSb = j.getString("id");
            listener.getLogger().println(newSb);
            if (waitForSetup)
            {
                WaitForSandBox(newSb, "Ready", 300);
            }
            return newSb;
        }
        catch (Exception e)
        {
            listener.getLogger().println(j);
        }
        return null;
    }

    public void StopBluePrint(String sandboxId, boolean waitForComplete, BuildListener listener) throws SandboxApiException {
        String token = HTTPWrapper.InvokeLogin(GetBaseUrl(), this.serverDetails.user, this.serverDetails.pw, "Global");
        String url = GetBaseUrl() + "/v1/sandboxes/" + sandboxId + "/stop";
        String result = HTTPWrapper.ExecutePost(url, token, null, null);
        JSONObject j = JSONObject.fromObject(result);
        if (j.containsKey("errorCategory")) {
            throw new SandboxApiException("Failed to stop blueprint: " + j);
        }

        try
        {
            if (waitForComplete)
            {
                WaitForSandBox(sandboxId, "Ended", 300);
            }
            listener.getLogger().println("SandBox Stopped: ");
            listener.getLogger().println(sandboxId);
        }
        catch (Exception e)
        {
            listener.getLogger().println(j);
        }
    }

    private String GetSandBoxStatus(String sb)
    {
        return SandboxDetails(sb).getString("state");
    }

    private void WaitForSandBox(String sandboxId, String status, int timeoutSec) throws SandboxApiException {
        long startTime = System.currentTimeMillis();

        String sandboxStatus = GetSandBoxStatus(sandboxId);
        while (!sandboxStatus.equals(status) && (System.currentTimeMillis()-startTime) < timeoutSec*1000)
        {
            if (sandboxStatus.equals("Error"))
            {
                throw new SandboxApiException("Sandbox status is: Error");
            }
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private JSONObject SandboxDetails(String sb)
    {
        String token = HTTPWrapper.InvokeLogin(GetBaseUrl(), this.serverDetails.user, this.serverDetails.pw, "Global");
        String url = GetBaseUrl() + "/v1/sandboxes/" + sb;
        String result = HTTPWrapper.ExecuteGet(url, token);
        JSONObject j = JSONObject.fromObject(result);

        if (j.toString().contains("errorCategory")) {
            throw new RuntimeException("Failed to get sandbox details: " + j);
        }
        return j;
    }

    private String GetBaseUrl()
    {
        return "http://" + this.serverDetails.serverAddress + ":" + this.serverDetails.port + "/Api";
    }

}
