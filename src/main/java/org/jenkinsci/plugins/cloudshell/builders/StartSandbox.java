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
package org.jenkinsci.plugins.cloudshell.builders;

import com.quali.cloudshell.QsExceptions.ReserveBluePrintConflictException;
import com.quali.cloudshell.QsExceptions.SandboxApiException;
import com.quali.cloudshell.QsServerDetails;
import com.quali.cloudshell.SandboxApiGateway;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.jenkinsci.plugins.cloudshell.CloudShellBuildStep;
import org.jenkinsci.plugins.cloudshell.Loggers.QsJenkinsTaskLogger;
import org.jenkinsci.plugins.cloudshell.VariableInjectionAction;
import org.jenkinsci.plugins.cloudshell.action.SandboxLaunchAction;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class StartSandbox extends CloudShellBuildStep {

	private final String blueprintName;
	private final String sandboxDuration;
	private final int maxWaitForSandboxAvailability;
	private QsJenkinsTaskLogger logger;

	@DataBoundConstructor
	public StartSandbox(String blueprintName, String sandboxDuration, int maxWaitForSandboxAvailability) {
		this.blueprintName = blueprintName;
		this.sandboxDuration = sandboxDuration;
		this.maxWaitForSandboxAvailability = maxWaitForSandboxAvailability;
	}

	public String getBlueprintName() {
		return blueprintName;
	}

	public String getSandboxDuration() {
		return sandboxDuration;
	}

	public int getMaxWaitForSandboxAvailability() {
		return maxWaitForSandboxAvailability;
	}

	public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener, QsServerDetails server) throws Exception {
		logger = new QsJenkinsTaskLogger(listener);
		return TryToReserveWithTimeout(build, launcher, listener, server, maxWaitForSandboxAvailability);
	}

	private boolean TryToReserveWithTimeout(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener, QsServerDetails server,
											long timeout_minutes) throws Exception {

		long startTime = System.currentTimeMillis();
		while ((System.currentTimeMillis()-startTime) <= timeout_minutes * 60 * 1000 ){

			try {
				return StartSandBox(build,launcher,listener,server);
			}
			catch (ReserveBluePrintConflictException ce){
				listener.getLogger().println("Waiting for sandbox to become available...");
			}
			catch (Exception e){
				throw e;
			}
			Thread.sleep(30*1000);

		}

		return  false;
	}


	private boolean StartSandBox(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener, QsServerDetails qsServerDetails) throws UnsupportedEncodingException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, SandboxApiException {
		SandboxApiGateway gateway = new SandboxApiGateway(logger, qsServerDetails);
        String sandboxId = gateway.startBlueprint(blueprintName, Integer.parseInt(sandboxDuration), true, null);
        String sandboxDetails = gateway.GetSandboxDetails(sandboxId);
        addSandboxToBuildActions(build, qsServerDetails, sandboxId, sandboxDetails);
		return true;
	}

    private void addSandboxToBuildActions(AbstractBuild<?, ?> build, QsServerDetails serverDetails, String id, String sandboxDetails) {
        build.addAction(new VariableInjectionAction("SANDBOX_ID",id));
		build.addAction(new VariableInjectionAction("SANDBOX_DETAILS",sandboxDetails));
        SandboxLaunchAction launchAction = new SandboxLaunchAction(serverDetails);
        build.addAction(launchAction);
        launchAction.started(id);
    }


    @Extension
	public static final class startSandboxDescriptor extends CSBuildStepDescriptor {

		public startSandboxDescriptor() {
			load();
		}

		@Override
		public String getDisplayName() {
			return "Start Sandbox";
		}

	}	
}