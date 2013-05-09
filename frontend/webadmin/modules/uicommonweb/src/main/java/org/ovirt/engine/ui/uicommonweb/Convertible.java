package org.ovirt.engine.ui.uicommonweb;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class Convertible {

    private EntityModel subject;

    public Convertible(EntityModel subject) {
        this.subject = subject;
    }

    public int integer() {
        return Integer.parseInt(subject.getEntity().toString());
    }

    public Integer nullableInteger() {

        return subject.getEntity() != null
            ? Integer.parseInt(subject.getEntity().toString())
            : null;
    }

    public Short nullableShort() {

        return subject.getEntity() != null
            ? Short.parseShort(subject.getEntity().toString())
            : null;
    }
}
