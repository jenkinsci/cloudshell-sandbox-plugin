package org.jenkinsci.plugins.cloudshell.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import hudson.model.Action;
import org.jenkinsci.plugins.cloudshell.CsServer;

public class SandboxLaunchAction implements Action, Serializable, Cloneable{

    private transient List<Item> running = new ArrayList<>();

    public static class Item {
        public final CsServer server;
        public final String id;

        public Item(CsServer client, String id) {
            this.server = client;
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Item item = (Item) o;

            if (!server.equals(item.server)) return false;
            if (!id.equals(item.id)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = server.hashCode();
            result = 31 * result + id.hashCode();
            return result;
        }
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

    public void started(CsServer server, String sandboxId) {
        running.add( new Item(server, sandboxId) );
    }

    public void stopped(CsServer server, String sandboxId) {
        running.remove( new Item(server, sandboxId) );
    }

    public Iterable<Item> getRunning() {
        return running;
    }
}
