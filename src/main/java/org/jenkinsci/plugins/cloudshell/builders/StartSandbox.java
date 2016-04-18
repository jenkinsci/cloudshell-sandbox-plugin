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

import com.iwombat.foundation.uuid.UUID;
import com.iwombat.util.GUIDUtil;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.jenkinsci.plugins.cloudshell.CloudShellBuildStep;
import org.jenkinsci.plugins.cloudshell.CsServerDetails;
import org.jenkinsci.plugins.cloudshell.SandboxAPIProxy;
import org.jenkinsci.plugins.cloudshell.VariableInjectionAction;
import org.jenkinsci.plugins.cloudshell.action.SandboxLaunchAction;
import org.kohsuke.stapler.DataBoundConstructor;

public class StartSandbox extends CloudShellBuildStep {

	private final String blueprintName;
	private final String sandboxDuration;
	private final int maxWaitForSandboxAvailability;

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

	public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener, CsServerDetails server) throws SandboxAPIProxy.SandboxApiException, InterruptedException {
        return TryToReserveWithTimeout(build, launcher, listener, server, maxWaitForSandboxAvailability);
	}

	private boolean TryToReserveWithTimeout(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener, CsServerDetails server,
											long timeout_minutes) throws SandboxAPIProxy.SandboxApiException, InterruptedException {

		long startTime = System.currentTimeMillis();

		while ((System.currentTimeMillis()-startTime) <= timeout_minutes * 60 * 1000 ){

			try {
				return StartSandBox(build,launcher,listener,server);
			}
			catch (SandboxAPIProxy.ReserveBluePrintConflictException ce){
				listener.getLogger().println("Waiting for sandbox to become available...");
			}
			catch (Exception e){
				throw e;
			}
			Thread.sleep(30*1000);

		}

		return  false;
	}


	private boolean StartSandBox(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener, CsServerDetails serverDetails) throws SandboxAPIProxy.SandboxApiException {
		SandboxAPIProxy proxy = new SandboxAPIProxy(serverDetails);
        String sandboxName = build.getFullDisplayName() + "_" + java.util.UUID.randomUUID().toString().substring(0,5);
		String id = proxy.StartBluePrint(build, blueprintName, sandboxName, sandboxDuration, true, listener);
        listener.getLogger().println("Created Sandbox: " + sandboxName);
        listener.getLogger().println("Sandbox Id: " + id);
        addSandboxToBuildActions(build, serverDetails, id);
        int maxSetup = Integer.parseInt(sandboxDuration)*60;
        proxy.WaitForSetup(id,maxSetup,listener);
		return true;
	}

    private void addSandboxToBuildActions(AbstractBuild<?, ?> build, CsServerDetails serverDetails, String id) {
        build.addAction(new VariableInjectionAction("SANDBOX_ID",id));
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