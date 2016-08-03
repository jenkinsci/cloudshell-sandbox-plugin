/*
 * The MIT License
 *
 * Copyright (c) 2013-2014, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.cloudshell.steps;

import com.google.inject.Inject;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.cloudshell.CloudShellBuildStep;
import org.jenkinsci.plugins.cloudshell.CloudShellConfig;
import org.jenkinsci.plugins.cloudshell.CsServerDetails;
import org.jenkinsci.plugins.cloudshell.SandboxAPIProxy;
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

        @Override public String getDisplayName() {
            return "starts a cloudshell sandbox";
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
            CsServerDetails server = descriptorImpl.getServer();
            SandboxAPIProxy sandboxAPIProxy = new SandboxAPIProxy(server);
            String start = sandboxAPIProxy.Start(step.name, "Jenkins", String.valueOf(step.duration), true, null);
            listener.getLogger().println("CloudShell Started: " + start);
            return start;
        }

        private static final long serialVersionUID = 1L;
    }
}
