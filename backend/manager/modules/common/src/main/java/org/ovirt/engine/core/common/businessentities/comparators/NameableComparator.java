package org.ovirt.engine.core.common.businessentities.comparators;

import java.io.Serializable;
import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.Nameable;

public class NameableComparator implements Comparator<Nameable>, Serializable {
    private static final long serialVersionUID = -1826247211038825447L;

    @Override
    public int compare(Nameable o1, Nameable o2) {
        return o1.getName().compareTo(o2.getName());
    }
}
