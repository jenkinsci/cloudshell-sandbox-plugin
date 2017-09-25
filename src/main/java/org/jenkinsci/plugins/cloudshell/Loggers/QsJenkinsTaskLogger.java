package org.jenkinsci.plugins.cloudshell.Loggers;


import com.quali.cloudshell.logger.QsLogger;
import hudson.model.TaskListener;

public class QsJenkinsTaskLogger extends QsLogger {

    private final TaskListener listener;

    public QsJenkinsTaskLogger(TaskListener listener) {
        this.listener = listener;
    }

    @Override
    public void debug(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void info(String s) {
        listener.getLogger().println(s);
    }

    @Override
    public void error(String s) { listener.error(s);}
}
