package org.jenkinsci.plugins.cloudshell.steps;

import com.quali.cloudshell.QsServerDetails;
import com.quali.cloudshell.SandboxApiGateway;
import com.google.inject.Inject;
import hudson.Extension;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.cloudshell.CloudShellConfig;
import org.jenkinsci.plugins.cloudshell.Loggers.QsJenkinsTaskLogger;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

public class SandboxStartStep extends AbstractStepImpl {

    public final String name;

    public final int duration;

    @DataBoundConstructor
    public SandboxStartStep(@Nonnull String name, @Nonnull int duration) {
        this.name = name;
        this.duration = duration;
    }

    @Extension
    public static final class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(SandboxStartStepExecution.class);
        }

        @Override public String getFunctionName() {
            return "startSandbox";
        }

        @Override public String getDisplayName() { return "starts a CloudShell Sandbox";
        }
    }

    public static class SandboxStartStepExecution extends AbstractSynchronousStepExecution<String> {

        @Inject
        private transient SandboxStartStep step;

        @StepContextParameter
        private transient TaskListener listener;

        @Override
        protected String run() throws Exception {
            listener.getLogger().println("CloudShell Starting!");
            CloudShellConfig.DescriptorImpl descriptorImpl =
                    (CloudShellConfig.DescriptorImpl) Jenkins.getInstance().getDescriptor(CloudShellConfig.class);
            QsServerDetails server = descriptorImpl.getServer();
            QsJenkinsTaskLogger logger = new QsJenkinsTaskLogger(listener);
            SandboxApiGateway gateway = new SandboxApiGateway(logger, server);
            String sandboxId = gateway.startBlueprint(step.name, step.duration, true, null);
            return sandboxId;
        }

        private static final long serialVersionUID = 1L;
    }
}
