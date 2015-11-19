package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

/**
 * Entity corresponding to <strong>vm_icon_defaults</strong> database table.
 */
public class VmIconDefault implements BusinessEntity<Guid> {

    private Guid id;
    private int osId;
    private Guid smallIconId;
    private Guid largeIconId;

    public VmIconDefault() {
    }

    public VmIconDefault(Guid id, int osId, Guid smallIconId, Guid largeIconId) {
        this.id = id;
        this.osId = osId;
        this.smallIconId = smallIconId;
        this.largeIconId = largeIconId;
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public int getOsId() {
        return osId;
    }

    public void setOsId(int osId) {
        this.osId = osId;
    }

    public Guid getSmallIconId() {
        return smallIconId;
    }

    public void setSmallIconId(Guid smallIconId) {
        this.smallIconId = smallIconId;
    }

    public Guid getLargeIconId() {
        return largeIconId;
    }

    public void setLargeIconId(Guid largeIconId) {
        this.largeIconId = largeIconId;
    }

    @Override
    public String toString() {
        return "VmIconDefault{" +
                "id=" + id +
                ", osId=" + osId +
                ", smallIconId=" + smallIconId +
                ", largeIconId=" + largeIconId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VmIconDefault)) {
            return false;
        }
        VmIconDefault that = (VmIconDefault) o;
        return Objects.equals(osId, that.osId)
                && Objects.equals(id, that.id)
                && Objects.equals(smallIconId, that.smallIconId)
                && Objects.equals(largeIconId, that.largeIconId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                osId,
                smallIconId,
                largeIconId
        );
    }
}
