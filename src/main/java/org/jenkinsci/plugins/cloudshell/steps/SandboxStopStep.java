package org.jenkinsci.plugins.cloudshell.steps;

import com.google.inject.Inject;
import hudson.AbortException;
import hudson.Extension;
import hudson.model.Result;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
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
            stepsCommon.StopSandbox(listener, step.reservationId, getContext());
            return null;
        }
    }
}
