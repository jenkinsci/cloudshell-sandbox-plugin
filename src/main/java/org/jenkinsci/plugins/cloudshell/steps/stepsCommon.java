package org.jenkinsci.plugins.cloudshell.steps;

import com.quali.cloudshell.QsExceptions.SandboxApiException;
import com.quali.cloudshell.QsServerDetails;
import com.quali.cloudshell.SandboxApiGateway;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.cloudshell.CloudShellConfig;
import org.jenkinsci.plugins.cloudshell.Loggers.QsJenkinsTaskLogger;

import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;


public class StepsCommon {

    public String StartSandbox(TaskListener listener, String name, int duration, String buildName) throws SandboxApiException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, UnsupportedEncodingException {
        listener.getLogger().println("CloudShell Starting!");
        CloudShellConfig.DescriptorImpl descriptorImpl =
                (CloudShellConfig.DescriptorImpl) Jenkins.getInstance().getDescriptor(CloudShellConfig.class);
        QsServerDetails server = descriptorImpl.getServer();
        QsJenkinsTaskLogger logger = new QsJenkinsTaskLogger(listener);
        SandboxApiGateway gateway = new SandboxApiGateway(logger, server);
        String sandboxName = buildName + "_" + java.util.UUID.randomUUID().toString().substring(0, 5);;
        String sandboxId = gateway.startBlueprint(name, duration, true, sandboxName);
        return sandboxId;
    }

    public void StopSandbox(TaskListener listener, String sandboxId) throws SandboxApiException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        listener.getLogger().println("CloudShell Stop Starting!");
        CloudShellConfig.DescriptorImpl descriptorImpl =
                (CloudShellConfig.DescriptorImpl) Jenkins.getInstance().getDescriptor(CloudShellConfig.class);
        QsServerDetails server = descriptorImpl.getServer();
        QsJenkinsTaskLogger logger = new QsJenkinsTaskLogger(listener);
        SandboxApiGateway gateway = new SandboxApiGateway(logger, server);
        gateway.StopSandbox(sandboxId, true);
    }


}
