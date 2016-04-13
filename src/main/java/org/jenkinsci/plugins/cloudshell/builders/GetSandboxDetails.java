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

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.jenkinsci.plugins.cloudshell.CloudShellBuildStep;
import org.jenkinsci.plugins.cloudshell.CsServer;
import org.kohsuke.stapler.DataBoundConstructor;

public class GetSandboxDetails extends CloudShellBuildStep {

	private final String SbId;

	@DataBoundConstructor
	public GetSandboxDetails(String sbId) {
		SbId = sbId;
	}

	public String getSbId() {
		return SbId;
	}

	public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener, CsServer server) {
		EnvVars env = null;
		try {
			env = build.getEnvironment(listener);
		}
		catch (Exception e) {
		}

		env.overrideAll(build.getBuildVariables());

		String expandedSbId = env.expand(SbId);

		server.GetSandBoxDetails(expandedSbId, listener);

		return true;
	}

	private boolean convert(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener)  {
		return true;
	}

	@Extension
	public static final class startSandboxDescriptor extends CSBuildStepDescriptor {

		public startSandboxDescriptor() {
			load();
		}
		@Override
		public String getDisplayName() {
			return "Get Sandbox Details";
		}

	}	
}