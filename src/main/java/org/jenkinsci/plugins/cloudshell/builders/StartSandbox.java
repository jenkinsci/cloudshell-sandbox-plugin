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
	private final String sandboxName;
	private final String sandboxDuration;
	private final boolean waitForSetup;

	@DataBoundConstructor
	public StartSandbox(String blueprintName, String sandboxName, String sandboxDuration, boolean waitForSetup) {
		this.blueprintName = blueprintName;
		this.sandboxName = sandboxName;
		this.sandboxDuration = sandboxDuration;
		this.waitForSetup = waitForSetup;
	}

	public String getBlueprintName() {
		return blueprintName;
	}

	public String getSandboxName() {
		return sandboxName;
	}

	public String getSandboxDuration() {
		return sandboxDuration;
	}

	public boolean getSwaitForSetup() {
		return waitForSetup;
	}


	public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener, CsServerDetails server) throws SandboxAPIProxy.SandboxApiException {
		return StartSandBox(build, launcher, listener, server);
	}

	private boolean StartSandBox(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener, CsServerDetails serverDetails) throws SandboxAPIProxy.SandboxApiException {
		SandboxAPIProxy proxy = new SandboxAPIProxy(serverDetails);
		String id = proxy.StartBluePrint(build, blueprintName, sandboxName, sandboxDuration, waitForSetup, listener);
		build.addAction(new VariableInjectionAction("SANDBOX_ID",id));
		SandboxLaunchAction launchAction = new SandboxLaunchAction(serverDetails);
		build.addAction(launchAction);
		launchAction.started(id);
		return true;
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