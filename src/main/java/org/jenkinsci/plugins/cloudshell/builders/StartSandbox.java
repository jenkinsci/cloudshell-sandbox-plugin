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

import com.google.gson.Gson;
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

public class StartSandbox extends CloudShellBuildStep {

	private final String blueprintName;
	private final String sandboxDuration;
	private final String params;
	private final String sandboxDomain;
	private final int maxWaitForSandboxAvailability;
	private final String sandboxName;

	@DataBoundConstructor
	public StartSandbox(String blueprintName, String sandboxDuration, String sandboxDomain, int maxWaitForSandboxAvailability, String params, String sandboxName) {
		this.blueprintName = blueprintName;
		this.sandboxDuration = sandboxDuration;
		this.sandboxDomain = sandboxDomain;
		this.maxWaitForSandboxAvailability = maxWaitForSandboxAvailability;
		this.params = params;
		this.sandboxName = sandboxName;
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
	public String getParams() { return params; }
	public String getSandboxName() {
		return sandboxName;
	}
	public String getSandboxDomain() {
		return sandboxDomain;
	}

	public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener, QsServerDetails server) throws Exception {
		SandboxApiGateway gateway = new SandboxApiGateway(new QsJenkinsTaskLogger(listener), server);
		String sandboxId = gateway.TryStartBlueprint(blueprintName,
				Integer.parseInt(sandboxDuration),
				true,
				(sandboxName == null || sandboxName.isEmpty()) ? null : sandboxName,
				gateway.TryParseBlueprintParams(params),
				maxWaitForSandboxAvailability);

		Gson gson = new Gson();
		String sandboxDetails = gson.toJson(gateway.GetSandboxDetails(sandboxId));
		addSandboxToBuildActions(build, server, sandboxId, sandboxDetails);
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
	public static final class StartSandboxDescriptor extends CSBuildStepDescriptor {

		public StartSandboxDescriptor() {
			load();
		}

		@Override
		public String getDisplayName() {
			return "Start Sandbox";
		}

	}	
}