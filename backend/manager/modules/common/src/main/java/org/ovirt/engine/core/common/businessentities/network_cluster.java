package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;

@Entity
@Table(name = "network_cluster")
@TypeDef(name = "guid", typeClass = GuidType.class)
@NamedQueries(value = { @NamedQuery(name = "delete_network_cluster",
                                    query = "delete from network_cluster n where n.clusterId = :cluster_id and n.networkId = :network_id") })
public class network_cluster implements Serializable {
    private static final long serialVersionUID = -4900811332744926545L;

    @Id
    @Column(name = "cluster_id")
    @Type(type = "guid")
    private Guid clusterId;

    @Column(name = "network_id")
    @Type(type = "guid")
    private Guid networkId;

    @Column(name = "status")
    private Integer status = 0;

    @Column(name = "is_display")
    private Boolean isDisplay = false;

    /**
     * A cluster network can be tagged as monitored. Monitored network have implications on automated actions taken on a
     * host during monitoring.
     */
    private boolean required;

    public network_cluster() {
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((isDisplay == null) ? 0 : isDisplay.hashCode());
        //FIXME: remove cluster from hashCode calculation - breaks the tests when working in JDBC template mode
        /*
        result = prime * result + ((cluster == null) ? 0 : cluster.hashCode());
         */
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((networkId == null) ? 0 : networkId.hashCode());
        result = prime * result + ((clusterId == null) ? 0 : clusterId.hashCode());
        result = prime * result + (required ? 11 : 13);
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
        network_cluster other = (network_cluster) obj;
        if (isDisplay == null) {
            if (other.isDisplay != null)
                return false;
        } else if (!isDisplay.equals(other.isDisplay))
            return false;
        if (status == null) {
            if (other.status != null)
                return false;
        } else if (!status.equals(other.status))
            return false;
        if (networkId == null) {
            if (other.networkId != null)
                return false;
        } else if (!networkId.equals(other.networkId))
            return false;
        if (clusterId == null) {
            if (other.clusterId != null)
                return false;
        } else if (!clusterId.equals(other.clusterId))
            return false;
        if (required != other.required) {
            return false;
        }
        return true;
    }


    public network_cluster(Guid cluster_id, Guid network_id, int status, boolean isDisplay, boolean required) {
        clusterId = cluster_id;
        networkId = network_id;
        this.status = status;
        this.isDisplay = isDisplay;
        this.required = required;
    }

    public Guid getcluster_id() {
        return clusterId;
    }

    public void setcluster_id(Guid value) {
        clusterId = value;
    }

    public Guid getnetwork_id() {
        return networkId;
    }

    public void setnetwork_id(Guid value) {
        networkId = value;
    }

    public int getstatus() {
        return this.status;
    }

    public void setstatus(int value) {
        this.status = value;
    }

    public boolean getis_display() {
        return this.isDisplay;
    }

    public void setis_display(boolean value) {
        this.isDisplay = value;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
