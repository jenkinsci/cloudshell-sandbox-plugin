package org.jenkinsci.plugins.cloudshell.publisher;

import com.quali.cloudshell.QsServerDetails;
import com.quali.cloudshell.SandboxApiGateway;
import com.quali.cloudshell.qsExceptions.SandboxApiException;
import com.quali.cloudshell.qsExceptions.TeardownFailedException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import org.jenkinsci.plugins.cloudshell.Loggers.QsJenkinsTaskLogger;
import org.jenkinsci.plugins.cloudshell.action.SandboxLaunchAction;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Serializable;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

/**
 * Post-build step that allow stop all matched container
 *
 * @author magnayn
 */
public class CloudShellPublisherControl extends Recorder implements Serializable {

    @DataBoundConstructor
    public CloudShellPublisherControl() {
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public BuildStepDescriptor getDescriptor() {
        return super.getDescriptor();
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        List<SandboxLaunchAction> sandboxLaunchActions = build.getActions(SandboxLaunchAction.class);
        QsJenkinsTaskLogger logger = new QsJenkinsTaskLogger(listener);

        for (SandboxLaunchAction sandboxItem : sandboxLaunchActions) {
            QsServerDetails serverDetails = sandboxItem.getServerDetails();
            for (String sandboxId : sandboxItem.getRunning()) {
                try {
                    SandboxApiGateway sandboxApiGateway = new SandboxApiGateway(logger, serverDetails);
                    sandboxApiGateway.StopSandbox(sandboxId, true);
                } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException | SandboxApiException e) {
                    e.printStackTrace();
                }
            }

            for (String sandboxId : sandboxItem.getRunning()) {
                try {
                    SandboxApiGateway sandboxApiGateway = new SandboxApiGateway(logger, serverDetails);
                    sandboxApiGateway.VerifyTeardownSucceeded(sandboxId);
                } catch (TeardownFailedException e) {
                    listener.getLogger().println("Teardown ended with erroes, see sandbox:  " + sandboxId);
                    build.setResult(Result.FAILURE);
                } catch (SandboxApiException e) {
                    listener.error("Failed to stop sandbox:  " + e.getMessage() + ". \n" + Arrays.toString(e.getStackTrace()));
                    build.setResult(Result.FAILURE);
                }
            }
        }

        return true;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Stop CloudShell sandboxes";
        }
    }
}


