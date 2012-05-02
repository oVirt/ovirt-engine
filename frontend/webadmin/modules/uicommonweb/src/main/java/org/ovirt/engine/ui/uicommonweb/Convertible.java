package org.ovirt.engine.ui.uicommonweb;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class Convertible {

    private EntityModel subject;

    public Convertible(EntityModel subject) {
        this.subject = subject;
    }

    public int Integer() {
        return Integer.parseInt(subject.getEntity().toString());
    }
}
