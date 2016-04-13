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
import org.jenkinsci.plugins.cloudshell.CsServer;
import org.jenkinsci.plugins.cloudshell.VariableInjectionAction;
import org.jenkinsci.plugins.cloudshell.action.SandboxLaunchAction;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;

public class StartSandbox extends CloudShellBuildStep {

	private final String bpName;
	private final String sbName;
	private final String sbDuration;

	@DataBoundConstructor
	public StartSandbox(String bpName, String sbName, String sbDuration) {
		this.bpName = bpName;
		this.sbName = sbName;
		this.sbDuration = sbDuration;
	}

	public String getBpName() {
		return bpName;
	}

	public String getSbName() {
		return sbName;
	}

	public String getSbDuration() {
		return sbDuration;
	}


	public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener, CsServer server) {
		return StartSandBox(build, launcher, listener, server);
	}

	private boolean StartSandBox(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener, CsServer server)  {
		String id = server.StartBluePrint(build,bpName, sbName, sbDuration, listener);
		build.addAction(new VariableInjectionAction("SANDBOX_ID",id));
		SandboxLaunchAction launchAction = new SandboxLaunchAction();
		build.addAction(launchAction);
		launchAction.started(server,id);
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