package org.ovirt.engine.core.common.businessentities.network.pseudo;

import org.ovirt.engine.core.common.businessentities.IVdcQueryable;

public class NetworkLabel implements IVdcQueryable {

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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NetworkLabel other = (NetworkLabel) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
