package org.jenkinsci.plugins.cloudshell.Loggers;


import com.quali.cloudshell.logger.QsLogger;
import hudson.model.TaskListener;

public class QsJenkinsTaskLogger extends QsLogger {

    private final TaskListener listener;

    public QsJenkinsTaskLogger(TaskListener listener) {
        this.listener = listener;
    }

    @Override
    public void Debug(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void Info(String s) {
        listener.getLogger().println(s);
    }

    @Override
    public void Error(String s) { listener.error(s);}
}
