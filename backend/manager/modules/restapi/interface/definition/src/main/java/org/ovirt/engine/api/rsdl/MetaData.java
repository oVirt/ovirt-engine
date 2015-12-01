package org.ovirt.engine.api.rsdl;

import java.util.LinkedList;
import java.util.List;

public class MetaData {

    public MetaData() {
        super();
        this.actions = new LinkedList<>();
    }

    private List<Action> actions;

    public List<Action> getActions() {
        return actions;
    }
    public void setActions(List<Action> actions) {
        this.actions = actions;
    }
    public void addAction(Action action) {
        this.actions.add(action);
    }

}
