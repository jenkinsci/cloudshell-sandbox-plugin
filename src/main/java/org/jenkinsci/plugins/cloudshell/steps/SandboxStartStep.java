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
    public final int duration;

    @DataBoundConstructor
    public SandboxStartStep(@Nonnull String name, @Nonnull int maxDuration) {
        this.name = name;
        this.duration = maxDuration;
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
            //TODO: send build name
            return stepsCommon.StartSandbox(listener, step.name, step.duration, null);
        }

        private static final long serialVersionUID = 1L;
    }
}
