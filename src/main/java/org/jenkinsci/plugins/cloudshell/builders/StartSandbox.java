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
import com.quali.cloudshell.Constants;
import com.quali.cloudshell.QsServerDetails;
import com.quali.cloudshell.SandboxApiGateway;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.jenkinsci.plugins.cloudshell.CloudShellBuildStep;
import org.jenkinsci.plugins.cloudshell.Loggers.QsJenkinsTaskLogger;
import org.jenkinsci.plugins.cloudshell.VariableInjectionAction;
import org.jenkinsci.plugins.cloudshell.action.SandboxLaunchAction;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.CheckForNull;

public class StartSandbox extends CloudShellBuildStep {

	private final String blueprintName;
	private final String sandboxDuration;
	private final int maxWaitForSandboxAvailability;
	private int setupTimeout;

	@CheckForNull
	private String sandboxDomain;
	@CheckForNull
	private String sandboxName;
	@CheckForNull
	private String params;

	@DataBoundConstructor
	public StartSandbox(String blueprintName, String sandboxDuration, int maxWaitForSandboxAvailability, int setupTimeout) {
		this.blueprintName = blueprintName;
		this.sandboxDuration = sandboxDuration;
		this.maxWaitForSandboxAvailability = maxWaitForSandboxAvailability;
		this.setupTimeout = setupTimeout;
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
	public int getSetupTimeout() {
		return setupTimeout;
	}

	@CheckForNull
	public String getParams() {
		return params;
	}

	@DataBoundSetter
	public void setParams(@CheckForNull String params) {
		this.params = Util.fixNull(params);
	}

	@CheckForNull
	public String getSandboxName() {
		return sandboxName;
	}

	@DataBoundSetter
	public void setSandboxName(@CheckForNull String sandboxName) {
		this.sandboxName = Util.fixNull(sandboxName);
	}

	@CheckForNull
	public String getSandboxDomain() {
		return sandboxDomain;
	}

	@DataBoundSetter
	public void setSandboxDomain(@CheckForNull String sandboxDomain) {
		this.sandboxDomain = Util.fixNull(sandboxDomain);
	}

	public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener, QsServerDetails server) throws Exception {

        assert sandboxDomain != null;
        if (!sandboxDomain.isEmpty()) {
			server = new QsServerDetails(server.serverAddress, server.user, server.pw, sandboxDomain, server.ignoreSSL);
		}

		if (setupTimeout == 0)
			setupTimeout = Constants.CONNECT_TIMEOUT_SECONDS;
		else
			setupTimeout = setupTimeout*60;

		SandboxApiGateway gateway = new SandboxApiGateway(new QsJenkinsTaskLogger(listener), server, setupTimeout);
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