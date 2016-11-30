package org.jenkinsci.plugins.cloudshell.steps;

import com.quali.cloudshell.QsServerDetails;
import com.quali.cloudshell.SandboxApiGateway;
import com.google.inject.Inject;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.cloudshell.CloudShellConfig;
import org.jenkinsci.plugins.cloudshell.Loggers.QsJenkinsTaskLogger;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

public class SandboxStopStep extends AbstractStepImpl {

    public final String reservationId;

    @DataBoundConstructor
    public SandboxStopStep(@Nonnull String reservationId) {
        this.reservationId = reservationId;
    }

    @Extension
    public static final class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(SandboxStopStepExecution.class);
        }

        @Override public String getFunctionName() {
            return "stopSandbox";
        }

        @Override public String getDisplayName() {
            return "stops a cloudshell sandbox";
        }
    }

    public static class SandboxStopStepExecution extends AbstractSynchronousStepExecution<Void> {

        private static final long serialVersionUID = 1L;

        @Inject
        private transient SandboxStopStep step;

        @StepContextParameter
        private transient TaskListener listener;

        @Override
        protected Void run() throws Exception {
            StepsCommon stepsCommon = new StepsCommon();
            stepsCommon.StopSandbox(listener, step.reservationId);
            return null;
        }
    }
}
