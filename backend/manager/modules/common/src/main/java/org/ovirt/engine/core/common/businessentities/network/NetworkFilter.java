package org.ovirt.engine.core.common.businessentities.network;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class NetworkFilter implements Queryable, BusinessEntity<Guid>, Nameable {

    private static final long serialVersionUID = 479750365026775507L;

    public static final String VDSM_NO_MAC_SPOOFING = "vdsm-no-mac-spoofing";

    private String name;
    /**
     * Assuming libvirt's backward compatibility support, {@code version} infer to the minimal version this filter is being first supported
     */
    private Version version;
    private Guid id;

    public NetworkFilter() {
    }

    public NetworkFilter(Guid id) {
        this.id = id;
    }

    public NetworkFilter(String name, Version version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
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
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NetworkFilter that = (NetworkFilter) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(version, that.version) &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, id);
    }
}
