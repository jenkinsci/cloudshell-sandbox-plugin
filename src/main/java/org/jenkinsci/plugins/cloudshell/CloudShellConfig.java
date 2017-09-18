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
import com.quali.cloudshell.QsServerDetails;
import com.quali.cloudshell.SandboxApiGateway;
import com.quali.cloudshell.qsExceptions.SandboxApiException;
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
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.cloudshell.CloudShellBuildStep.CSBuildStepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

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
		QsServerDetails serverDetails = getDescriptor().getServer();
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

        private QsServerDetails server;

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

		public FormValidation doTestConnection(
				@QueryParameter("serverAddress") final String serverAddress,
				@QueryParameter("user") final String user,
				@QueryParameter("pw") final String pw,
				@QueryParameter("domain") final String domain,
				@QueryParameter("ignoreSSL") final boolean ignoreSSL ){
					try {
						SandboxApiGateway gateway = new SandboxApiGateway(null, new QsServerDetails(serverAddress, user, pw, domain, ignoreSSL));
						gateway.TryLogin();
					} catch (SandboxApiException | KeyStoreException | KeyManagementException | NoSuchAlgorithmException e) {
						return FormValidation.error(e.getMessage());
					} catch (UnknownHostException e) {
						return FormValidation.error("Unknown Host: " + e.getMessage());
					} catch (IOException e) {
						return FormValidation.error(e.getMessage());
					}
            return FormValidation.ok("Test completed successfully");
		}

		public FormValidation doCheckPw(@QueryParameter String value) {
			if(value.isEmpty())
				return FormValidation.errorWithMarkup("Password cannot be empty");
			else
				return FormValidation.ok();
		}

		public FormValidation doCheckUser(@QueryParameter String value) {
			if(value.isEmpty())
				return FormValidation.errorWithMarkup("User cannot be empty");
			else
				return FormValidation.ok();
		}

		public FormValidation doCheckDomain(@QueryParameter String value) {
			if(value.isEmpty())
				return FormValidation.errorWithMarkup("Domain cannot be empty");
			else
				return FormValidation.ok();
		}


		public FormValidation doCheckServerAddress(@QueryParameter String value) {
//			String regex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
			String regex = "^(https?)://[-a-zA-Z0-9-_.:]*[0-9]";
			if(value.matches(regex))
				return FormValidation.ok();
			else
				return FormValidation.errorWithMarkup("Invalid server address, see help for more details");
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
            server = new QsServerDetails(
                    formData.getString("serverAddress"),
                    formData.getString("user"),
                    formData.getString("pw"),
					formData.getString("domain"),
                    Boolean.parseBoolean(formData.getString("ignoreSSL"))
                    );
            save();
            return super.configure(req,formData);
        }

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		public String getServerAddress() {
	return server.serverAddress;
}
        public String getUser() {
            return server.user;
        }
        public String getPw() {
            return server.pw;
        }
		public String getDomain() {
			return server.domain;
		}
        public boolean getIgnoreSSL() {
            return server.ignoreSSL;
        }
        public QsServerDetails getServer() {return server;}

	}
}
