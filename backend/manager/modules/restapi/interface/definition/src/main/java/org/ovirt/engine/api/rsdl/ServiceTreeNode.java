package org.ovirt.engine.api.rsdl;

import java.util.ArrayList;
import java.util.List;

/**
 * A node in the 'Service' tree, which contains:
 * 1) The name of the service class
 *      e.g: VmDisksResource
 * 2) The path section leading to it
 *      e.g: 'vms'
 * 3) A list of Nodes, which are the child 'Services'.
 * 4) A list of actions, which are the actions of this service,
 *    consisting of the HTTP methods and the 'actions' (e.g VM start)
 * @author oliel
 */
public class ServiceTreeNode {

    private String name;
    private String path;
    private List<ServiceTreeNode> subServices = new ArrayList<>();
    private List<String> actions = new ArrayList<>();

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public List<ServiceTreeNode> getSubServices() {
        return subServices;
    }
    public void setSubServices(List<ServiceTreeNode> subServices) {
        this.subServices = subServices;
    }
    public List<String> getActions() {
        return actions;
    }
    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    /**
     * Checks whether or not this node contains the provided
     * action (purposely ignores case when comparing action names).
     */
    public boolean containsAction(String actionName) {
        if (actions!=null) {
            for (String action : actions) {
                if (action.toLowerCase().equals(actionName.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsSubService(String path) {
        if (subServices!=null) {
            for (ServiceTreeNode node : subServices) {
                if (node.getPath().equals(path)) {
                    return true;
                }
            }
        }
        return false;
    }

    public ServiceTreeNode getSubService(String path) {
        if (subServices!=null) {
            for (ServiceTreeNode node : subServices) {
                if (node.getPath().equals(path)) {
                    return node;
                }
            }
        }
        return null;
    }

    public static class Builder {
        ServiceTreeNode node;
        public Builder() {
            node = new ServiceTreeNode();
        }
        public Builder name(String name) {
            node.setName(name);
            return this;
        }
        public Builder path(String path) {
            node.setPath(path);
            return this;
        }
        public Builder subCollections(List<ServiceTreeNode> subCollections) {
            node.setSubServices(subCollections);
            return this;
        }
        public Builder actions(List<String> actions) {
            node.setActions(actions);
            return this;
        }
        public ServiceTreeNode build() {
            return node;
        }
    }

    @Override
    public String toString() {
        return toString(0);
    }

    private String toString(int tabNum) {
        StringBuilder builder = new StringBuilder();
        String tabs = getTabs(tabNum);
        builder.append(tabs).append("name: ").append(name).append("\n")
        .append(tabs).append("path: ").append(path).append("\n")
        .append(tabs).append("actions: ").append(printActions()).append("\n")
        .append(tabs).append("sub-services:\n").append(printSubServices(++tabNum)).append("\n");
        return builder.toString();
    }

    private String getTabs(int tabNum) {
        StringBuilder builder = new StringBuilder("");
        for (int i=0; i<tabNum; i++) {
            builder.append("\t");
        }
        return builder.toString();
    }

    private String printSubServices(int tabNum) {
        StringBuilder builder = new StringBuilder();
        for (ServiceTreeNode node : subServices) {
            builder.append(node.toString(tabNum));
        }
        return builder.toString();
    }

    private Object printActions() {
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<actions.size(); i++) {
            builder.append(actions.get(i)).append(",");
        }
        String actions = builder.toString();
        if (actions.length()>0) {
            actions = actions.substring(0, actions.length()-1);
        }
        return actions;
    }
}
