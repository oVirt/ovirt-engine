package org.ovirt.engine.core.common.vdscommands;

import java.util.Set;

public class UserOverriddenNicValues {
    private Set<String> labels;

    public UserOverriddenNicValues() {
    }

    public UserOverriddenNicValues(Set<String> labels) {
        setLabels(labels);
    }

    public Set<String> getLabels() {
        return labels;
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }
}
