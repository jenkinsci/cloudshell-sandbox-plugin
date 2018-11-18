package org.jenkinsci.plugins.cloudshell.steps;

import com.google.inject.Inject;
import hudson.Extension;
import hudson.Util;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class SandboxStartStep extends AbstractStepImpl {

    public final String name;
    public final int duration;
    public final int timeout;

    @CheckForNull
    private String sandboxDomain;
    @CheckForNull
    private String sandboxName;
    @CheckForNull
    private String params;

    @DataBoundConstructor
    public SandboxStartStep(@Nonnull String name, int duration, int timeout) {
        this.name = name;
        this.duration = duration;
        this.timeout = timeout;
    }


    @CheckForNull
    public String getParams() {
        return params;
    }

    @DataBoundSetter
    public void setParams(@CheckForNull String params) {
        this.params = Util.fixNull(params);
    }

    @CheckForNull
    public String getSandboxName() {
        return sandboxName;
    }

    @DataBoundSetter
    public void setSandboxName(@CheckForNull String sandboxName) {
        this.sandboxName = Util.fixNull(sandboxName);
    }

    @CheckForNull
    public String getSandboxDomain() {
        return sandboxDomain;
    }

    @DataBoundSetter
    public void setSandboxDomain(@CheckForNull String sandboxDomain) {
        this.sandboxDomain = Util.fixNull(sandboxDomain);
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
            return stepsCommon.startSandbox(listener, step.name, step.duration ,step.params, step.sandboxName, step.timeout, step.sandboxDomain);
        }

        private static final long serialVersionUID = 1L;
    }
}
