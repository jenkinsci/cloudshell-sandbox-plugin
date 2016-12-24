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

import com.quali.cloudshell.QsServerDetails;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.jenkinsci.plugins.cloudshell.CloudShellBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

public class ExecuteComponentCommand extends CloudShellBuildStep {

	private final String sandboxId;
	private final String componentId;
	private final String commandName;

	@DataBoundConstructor
	public ExecuteComponentCommand(String sandboxId, String componentId, String commandName) {
		this.componentId = componentId;
		this.commandName = commandName;
		this.sandboxId = sandboxId;
	}

	public String getSandboxId() {
		return sandboxId;
	}

	public String getCommandName() {
		return commandName;
	}

	public String getComponentId() {
		return componentId;
	}

	public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener, QsServerDetails server) throws Exception {
		return true;
	}

    @Extension
	public static final class startSandboxDescriptor extends CSBuildStepDescriptor {

		public startSandboxDescriptor() {
			load();
		}

		@Override
		public String getDisplayName() {
			return "Execute Component Command";
		}

	}	
}