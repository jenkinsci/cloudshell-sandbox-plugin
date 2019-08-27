package org.jenkinsci.plugins.cloudshell.steps;

import com.google.inject.Inject;
import hudson.Extension;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;

public class SandboxStopStep extends AbstractStepImpl {

    public final String sandboxId;

    private int teardownTimeout;

    @DataBoundConstructor
    public SandboxStopStep(@Nonnull String sandboxId) {
        this.sandboxId = sandboxId;
    }

    public int getTeardownTimeout() {
        return teardownTimeout;
    }

    @DataBoundSetter
    public void setTeardownTimeout(int teardownTimeout) {
        this.teardownTimeout = teardownTimeout;
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
            return "Stops a CloudShell Sandbox";
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
            stepsCommon.stopSandbox(listener, step.sandboxId, getContext(), step.teardownTimeout*60);
            return null;
        }
    }
}
