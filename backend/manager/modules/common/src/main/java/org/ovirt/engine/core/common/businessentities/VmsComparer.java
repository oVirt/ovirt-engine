package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

public class VmsComparer implements java.util.Comparator<VM>, Serializable {
    private static final long serialVersionUID = 2773040834879205191L;

    @Override
    public int compare(VM o1, VM o2) {
        return Compare(o1, o2);
    }

    public int Compare(VM x, VM y) {
        return x.getPriority() - y.getPriority();
    }

    public VmsComparer() {
    }
}
