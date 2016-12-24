package org.jenkinsci.plugins.cloudshell.steps;

import com.google.inject.Inject;
import hudson.Extension;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

public class SandboxStartStep extends AbstractStepImpl {

    public final String name;
    public final String params;
    public final int maxDuration;

    @DataBoundConstructor
    public SandboxStartStep(@Nonnull String name, String params, @Nonnull int maxDuration) {
        this.name = name;
        this.params = params;
        this.maxDuration = maxDuration;
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
            StepsCommon stepsCommon = new StepsCommon();
            return stepsCommon.StartSandbox(listener, step.name, step.maxDuration, getContext(),step.params);
        }

        private static final long serialVersionUID = 1L;
    }
}
