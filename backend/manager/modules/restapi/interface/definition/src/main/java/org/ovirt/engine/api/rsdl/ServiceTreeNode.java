package org.ovirt.engine.api.rsdl;

import java.util.ArrayList;
import java.util.List;

/**
 * A node in the 'Service' tree, which contains:
 * 1) The name of the resource class
 *      e.g: VmDisksResource
 * 2) The path section leading to it
 *      e.g: 'diskattachments'
 * 3) Name of getter-method for this service at this service's parent.
 *      e.g: 'getDiskAttachments'
 * 4) A list of Nodes, which are the child 'Services'.
 * 5) A list of 'actions', which are the actions of this service,
 *    consisting of the JAX-RS methods and special actions, e.g: 'start'
 */
public class ServiceTreeNode {

    private String name;
    private String path;

    //only for 'collection' services (such as VmsService) a reference
    //to the sub-service representing the single-entity-context (such
    //as VmService) is kept. When irrelevant, this will be 'null'.
    private ServiceTreeNode son;

    private List<ServiceTreeNode> subServices = new ArrayList<>();
    private List<String> actions = new ArrayList<>();

    //Name of getter-method for this service at this service's parent.
    private String getter;

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
    public ServiceTreeNode getSon() {
        return son;
    }
    public void setSon(ServiceTreeNode son) {
        this.son = son;
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
    public String getGetter() {
        return getter;
    }
    public void setGetter(String getter) {
        this.getter = getter;
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

    public boolean isCollection() {
        return getSon()!=null;
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
        public Builder getter(String getter) {
            node.setGetter(getter);
            return this;
        }
        public ServiceTreeNode build() {
            if (node.subServices!=null) {
                node.setSon(node.getSubService("{id}"));
            }
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
        .append(tabs).append("getter: ").append(getter).append("\n")
        .append(tabs).append("son: ").append(son==null ? "" : son.getName()).append("\n")
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
