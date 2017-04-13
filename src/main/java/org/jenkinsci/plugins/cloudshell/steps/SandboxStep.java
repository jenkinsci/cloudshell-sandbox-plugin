/*
 * The MIT License
 *
 * Copyright 2015 Jesse Glick.
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
import com.quali.cloudshell.qsExceptions.SandboxApiException;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.TaskListener;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class SandboxStep extends AbstractStepImpl {

    public final String name;
    public final int maxDuration;
    public final String params;

    @DataBoundConstructor
    public SandboxStep(@Nonnull String name, @Nonnull int maxDuration, String params) {
        this.name = name;
        this.maxDuration = maxDuration;
        this.params = params;
    }

    public String getName() {
    return name;
}

    public int getMaxDuration() {
        return maxDuration;
    }

    public static class Execution extends AbstractStepExecutionImpl {

        private static final long serialVersionUID = 1;

        @Inject(optional = true)
        private transient SandboxStep step;

        @StepContextParameter
        private transient TaskListener listener;

        private String sandboxId;

        @Override
        public boolean start() throws Exception {
            StepsCommon stepsCommon = new StepsCommon();
            StepContext context = getContext();
            context.newBodyInvoker().
                    withContext(CreateSandbox(stepsCommon, context)).
                    withContext(EnvironmentExpander.merge(getContext().get(EnvironmentExpander.class), new ExpanderImpl(sandboxId))).
                    withCallback(new Callback(sandboxId, listener)).
                    start();
            return false;
        }

        @Override
        public void stop(@Nonnull Throwable throwable) throws Exception {
            listener.getLogger().println("Aborting CloudShell Sandbox!");
            if (sandboxId != null && !sandboxId.isEmpty())
            {
                StepsCommon stepsCommon = new StepsCommon();
                stepsCommon.StopSandbox(listener, sandboxId);
            }

        }

        private boolean CreateSandbox(StepsCommon stepsCommon, StepContext context) throws
                SandboxApiException,
                NoSuchAlgorithmException,
                KeyStoreException,
                KeyManagementException,
                IOException, InterruptedException {

            sandboxId = stepsCommon.StartSandbox(listener, step.name, step.maxDuration, context, step.params);
            return false;
        }

        private static class Callback extends BodyExecutionCallback {

            private static final long serialVersionUID = 1;
            private final String sandboxId;
            private TaskListener listener;

            Callback(String sandboxId, TaskListener listener) {
                this.sandboxId = sandboxId;
                this.listener = listener;
            }

            private void stopSandbox(StepContext context) {
                StepsCommon stepsCommon = new StepsCommon();
                try {
                    stepsCommon.StopSandbox(listener, sandboxId);
                } catch (SandboxApiException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSuccess(StepContext context, Object result) {
                stopSandbox(context);
                context.onSuccess(result);
            }

            @Override
            public void onFailure(StepContext context, Throwable t) {
                stopSandbox(context);
                context.onFailure(t);
            }

        }
    }

    private static final class ExpanderImpl extends EnvironmentExpander {
        private static final long serialVersionUID = 1;
        private final Map<String,String> overrides;
        private ExpanderImpl(String sandboxId) {
            this.overrides = new HashMap<>();
            this.overrides.put("SANDBOX_ID", sandboxId);
        }
        @Override public void expand(EnvVars env) throws IOException, InterruptedException {
            env.overrideAll(overrides);
        }
    }


    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(Execution.class);
        }

        @Override
        public String getFunctionName() {
            return "withSandbox";
        }

        @Override
        public String getDisplayName() {
            return "Use sandbox in a specific scope";
        }

        @Override
        public boolean takesImplicitBlockArgument() {
            return true;
        }

        @Override
        public Step newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            //TODO: throw nice errors if needed
            String name = formData.getString("name");
            int duration = Integer.parseInt(formData.getString("maxDuration"));
            String params = formData.getString("params");
            return new SandboxStep(name, duration, params);
        }

    }
}
