package org.jenkinsci.plugins.cloudshell.steps;

import com.quali.cloudshell.Constants;
import com.quali.cloudshell.QsServerDetails;
import com.quali.cloudshell.SandboxApiGateway;
import com.quali.cloudshell.qsExceptions.InvalidApiCallException;
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

public class StepsCommon {

    String startSandbox(TaskListener listener, String name, int duration, String parameters, String sandboxName, int timeout, String sandboxDomain, int sandboxTimeout)
            throws SandboxApiException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, InterruptedException {

            if (sandboxTimeout == 0)
                sandboxTimeout = Constants.CONNECT_TIMEOUT_SECONDS;

            if (sandboxDomain == null || sandboxDomain.isEmpty())
            {
                return InitiateBlueprintStart(name, duration, parameters, sandboxName, timeout, getSandboxApiGateway(listener, sandboxTimeout));
            }
            return InitiateBlueprintStart(name, duration, parameters, sandboxName, timeout, getSandboxApiGateway(listener, sandboxDomain, sandboxTimeout));
        }

    private String InitiateBlueprintStart(String name, int duration, String parameters, String sandboxName, int timeout, SandboxApiGateway gateway) throws SandboxApiException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        return gateway.TryStartBlueprint(name,
                duration,
               true,
               (sandboxName == null || sandboxName.isEmpty()) ? null : sandboxName,
               gateway.TryParseBlueprintParams(parameters),
               timeout);
    }

    void stopSandbox(TaskListener listener, String sandboxId, StepContext context, int timeout){
        if (timeout == 0)
            timeout = Constants.CONNECT_TIMEOUT_SECONDS;
        listener.getLogger().println("Sandbox plugin:  Sandbox Cleanup in progress");
        try {
            SandboxApiGateway gateway = getSandboxApiGateway(listener, timeout);
            gateway.StopSandbox(sandboxId, true);
            try {
                gateway.VerifyTeardownSucceeded(sandboxId);
            } catch (InvalidApiCallException e) {
                listener.getLogger().println("Teardown process cannot be verified, please use newer version of CloudShell to support this feature.");
            }
        } catch (TeardownFailedException e) {
            listener.error("Teardown ended with erroes, see sandbox:  " + sandboxId);
            context.setResult(Result.FAILURE);
        } catch (SandboxApiException | IOException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
           listener.error("Failed to stop sandbox:  " + e.getMessage() + ". \n" + Arrays.toString(e.getStackTrace()));
           context.setResult(Result.FAILURE);
        }
    }


    private QsServerDetails GetCloudShellServerConfig() {
        CloudShellConfig.DescriptorImpl descriptorImpl =
                (CloudShellConfig.DescriptorImpl) Jenkins.getInstance().getDescriptor(CloudShellConfig.class);
        return descriptorImpl.getServer();
    }

    private SandboxApiGateway getSandboxApiGateway(TaskListener listener, int sandboxTimeout) throws SandboxApiException {
        QsServerDetails server = GetCloudShellServerConfig();
        return new SandboxApiGateway(
                new QsJenkinsTaskLogger(listener),
                server, sandboxTimeout);
    }

    private SandboxApiGateway getSandboxApiGateway(TaskListener listener, String domain, int sandboxTimeout) throws SandboxApiException {
        QsServerDetails server = GetCloudShellServerConfig();
        QsServerDetails tempQsServerDetails = new QsServerDetails(server.serverAddress, server.user, server.pw, domain, server.ignoreSSL);
        return new SandboxApiGateway(new QsJenkinsTaskLogger(listener), tempQsServerDetails, sandboxTimeout);
    }
}
