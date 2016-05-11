package org.jenkinsci.plugins.cloudshell.publisher;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import org.jenkinsci.plugins.cloudshell.CsServerDetails;
import org.jenkinsci.plugins.cloudshell.SandboxAPIProxy;
import org.jenkinsci.plugins.cloudshell.action.SandboxLaunchAction;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.Serializable;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
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

        for (SandboxLaunchAction sandboxItem : sandboxLaunchActions) {
            for (String sandboxId : sandboxItem.getRunning()) {
                try {
                    CsServerDetails serverDetails = sandboxItem.getServerDetails();
                    new SandboxAPIProxy(serverDetails).StopBluePrint(sandboxId,true, serverDetails.ignoreSSL, listener);
                } catch (SandboxAPIProxy.SandboxApiException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (KeyManagementException e) {
                    e.printStackTrace();
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

        @Override
        public String getDisplayName() {
            return "Stop CloudShell sandboxes";
        }
    }
}


