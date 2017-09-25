package org.jenkinsci.plugins.cloudshell.action;

import com.quali.cloudshell.QsServerDetails;
import hudson.model.Action;

import java.io.Serializable;
import java.util.ArrayList;

public class sandboxLaunchAction implements Action, Serializable {

    private QsServerDetails serverDetails;
    private ArrayList<String> runningSandboxes = new ArrayList<String>();

    public sandboxLaunchAction(QsServerDetails serverDetails){

        this.serverDetails = serverDetails;
    }

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return null;
    }

    public QsServerDetails getServerDetails(){
        return this.serverDetails;
    }

    public void started(String sandboxId) {
        runningSandboxes.add( sandboxId );
    }

    public Iterable<String> getRunning() {
        return runningSandboxes;
    }
}
