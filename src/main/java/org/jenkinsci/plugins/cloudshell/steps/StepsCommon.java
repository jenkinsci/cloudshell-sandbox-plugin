package org.jenkinsci.plugins.cloudshell.steps;

import com.quali.cloudshell.QsExceptions.SandboxApiException;
import com.quali.cloudshell.QsServerDetails;
import com.quali.cloudshell.SandboxApiGateway;
import hudson.EnvVars;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.cloudshell.CloudShellBuildStep;
import org.jenkinsci.plugins.cloudshell.CloudShellConfig;
import org.jenkinsci.plugins.cloudshell.Loggers.QsJenkinsTaskLogger;
import org.jenkinsci.plugins.workflow.steps.EnvironmentExpander;
import org.jenkinsci.plugins.workflow.steps.StepContext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StepsCommon {
    public String StartSandbox(TaskListener listener, String name, int duration, StepContext context, String parameters) throws SandboxApiException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, InterruptedException {
        listener.getLogger().println("CloudShell Starting!");
        SandboxApiGateway gateway = getSandboxApiGateway(listener);

        String sandboxName = null;
        EnvVars envVars = context.get(EnvVars.class);
        String jobName = envVars.get("JOB_NAME");
        if (jobName != null && !jobName.isEmpty())
        {
            sandboxName = jobName + "_" + java.util.UUID.randomUUID().toString().substring(0, 5);;
        }
        String sandboxId = gateway.StartBlueprint(name, duration, true, sandboxName, parseParams(parameters));
        return sandboxId;
    }

    private Map<String, String> parseParams(String params) throws SandboxApiException {
        if (!params.isEmpty()) {
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

    public void StopSandbox(TaskListener listener, String sandboxId) throws SandboxApiException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        listener.getLogger().println("CloudShell Stop Starting!");
        SandboxApiGateway gateway = getSandboxApiGateway(listener);
        try {
            gateway.StopSandbox(sandboxId, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SandboxApiGateway getSandboxApiGateway(TaskListener listener) {
        CloudShellConfig.DescriptorImpl descriptorImpl =
                (CloudShellConfig.DescriptorImpl) Jenkins.getInstance().getDescriptor(CloudShellConfig.class);
        QsServerDetails server = descriptorImpl.getServer();
        QsJenkinsTaskLogger logger = new QsJenkinsTaskLogger(listener);
        return new SandboxApiGateway(logger, server);
    }
}
