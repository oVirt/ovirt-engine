package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.Guid;

import java.util.Objects;

/**
 * Entity corresponding to <strong>vm_icons</strong> database table.
 */
public class VmIcon implements BusinessEntity<Guid> {

    private Guid id;
    private String dataUrl;

    public VmIcon () {
    }

    public VmIcon(Guid id, String dataUrl) {
        this.id = id;
        this.dataUrl = dataUrl;
    }

    public String getDataUrl() {
        return dataUrl;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "VmIcon{" +
                "id=" + id +
                ", dataUrl='" + printDataUrl(dataUrl) + '\'' +
                '}';
    }

    private static String printDataUrl(String dataUrl) {
        if (dataUrl == null) {
            return "null";
        }
        final int maxLength = 32;
        if (dataUrl.length() > maxLength) {
            return dataUrl.substring(0, maxLength) + '…';
        }
        return dataUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VmIcon vmIcon = (VmIcon) o;
        return Objects.equals(id, vmIcon.id) &&
                Objects.equals(dataUrl, vmIcon.dataUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dataUrl);
    }
}
