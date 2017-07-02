package org.ovirt.engine.core.common.businessentities.network.pseudo;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.Queryable;

public class NetworkLabel implements Queryable {

    private static final long serialVersionUID = -2906392515766833212L;
    private String id;

    public NetworkLabel() {
    }

    public NetworkLabel(String label) {
        this.id = label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NetworkLabel)) {
            return false;
        }
        NetworkLabel other = (NetworkLabel) obj;
        return Objects.equals(id, other.id);
    }
}
