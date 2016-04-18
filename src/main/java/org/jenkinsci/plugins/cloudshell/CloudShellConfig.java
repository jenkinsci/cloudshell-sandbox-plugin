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

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.Launcher;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Items;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.cloudshell.CloudShellBuildStep.CSBuildStepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class CloudShellConfig extends Builder {

	private final CloudShellBuildStep buildStep;

	@DataBoundConstructor
	public CloudShellConfig(final CloudShellBuildStep buildStep)
	{
		this.buildStep = buildStep;
    }

	public CloudShellBuildStep getBuildStep() {
		return buildStep;
	}

	@Override
	public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener)  {
		CsServerDetails serverDetails = getDescriptor().getServer();
        try
        {
            return buildStep.perform(build, launcher, listener, serverDetails);

        } catch (Exception e) {
            listener.getLogger().println(e);
		}
        return false;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl)super.getDescriptor();
	}

	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private CsServerDetails server;

        public DescriptorImpl() {
            load();
        }

		@Initializer(before=InitMilestone.PLUGINS_STARTED)
        public static void addAliases() {
			Items.XSTREAM2.addCompatibilityAlias(
					"org.jenkinsci.plugins.cloudshell.CloudShellConfig",
					CloudShellConfig.class
			);
		}

		@Override
		public String getDisplayName() {
			return "CloudShell Build Step";
		}

		public DescriptorExtensionList<CloudShellBuildStep, CSBuildStepDescriptor> getBuildSteps() {
			return CloudShellBuildStep.all();
		}

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            server = new CsServerDetails(
                    formData.getString("serverAddress"),
                    formData.getString("user"),
                    formData.getString("pw"),
					formData.getString("domain"),
					formData.getInt("port")
                    );
            save();
            return super.configure(req,formData);
        }

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

//        public String getServerAddress() {
//            return server.serverAddress;
//        }

		public String getServerAddress() {
	return server.serverAddress;
}
        public String getUser() {
            return server.user;
        }
        public String getPw() {
            return server.pw;
        }
        public int getPort() {
            return server.port;
        }
		public String getDomain() {
			return server.domain;
		}
        public CsServerDetails getServer() {return server;}

	}
}
