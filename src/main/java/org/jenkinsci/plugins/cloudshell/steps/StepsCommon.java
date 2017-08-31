package org.jenkinsci.plugins.cloudshell.steps;

import com.quali.cloudshell.SandboxApiGateway;
import com.quali.cloudshell.qsExceptions.ReserveBluePrintConflictException;
import com.quali.cloudshell.qsExceptions.SandboxApiException;
import com.quali.cloudshell.qsExceptions.TeardownFailedException;
import hudson.model.Result;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.cloudshell.CloudShellConfig;
import org.jenkinsci.plugins.cloudshell.Loggers.QsJenkinsTaskLogger;
import org.jenkinsci.plugins.workflow.steps.StepContext;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class StepsCommon {
    public String StartSandbox(TaskListener listener, String name, int duration, StepContext context, String parameters, String sandboxName, int timeout)
            throws SandboxApiException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, InterruptedException {

        SandboxApiGateway gateway = getSandboxApiGateway(listener);

        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis()-startTime) <= timeout * 60 * 1000 ){
            try {
                return gateway.StartBlueprint(name, duration, true, (sandboxName.isEmpty()) ? null : sandboxName, parseParams(parameters));
            }
            catch (ReserveBluePrintConflictException ce){
                listener.getLogger().println("Waiting for sandbox to become available...");
            }
            Thread.sleep(30*1000);
        }
        return null;
    }


    private Map<String, String> parseParams(String params) throws SandboxApiException {
        if (params != null && !params.isEmpty()) {
            Map<String, String> map = new HashMap<>();
            String[] parameters = params.split(";");
            for (String param: parameters) {
                String[] split = param.trim().split("=");
                if (split.length < 2) throw new SandboxApiException("Failed to parse blueprint parameters");
                map.put(split[0], split[1]);
            }
            return map;
        }
        return null;
    }

    public void StopSandbox(TaskListener listener, String sandboxId, StepContext context){
        listener.getLogger().println("Sandbox plugin:  Sandbox Cleanup in progress");
        try {
            SandboxApiGateway gateway = getSandboxApiGateway(listener);
            gateway.StopSandbox(sandboxId, true);
            gateway.VerifyTeardownSucceeded(sandboxId);
        } catch (TeardownFailedException e) {
            listener.error("Teardown ended with erroes, see sandbox:  " + sandboxId);
            context.setResult(Result.FAILURE);
        } catch (SandboxApiException | IOException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
           listener.error("Failed to stop sandbox:  " + e.getMessage() + ". \n" + Arrays.toString(e.getStackTrace()));
           context.setResult(Result.FAILURE);
        }
    }

    private SandboxApiGateway getSandboxApiGateway(TaskListener listener) throws SandboxApiException {
        CloudShellConfig.DescriptorImpl descriptorImpl =
                (CloudShellConfig.DescriptorImpl) Jenkins.getInstance().getDescriptor(CloudShellConfig.class);
        return new SandboxApiGateway(
                new QsJenkinsTaskLogger(listener),
                descriptorImpl.getServer());
    }
}
