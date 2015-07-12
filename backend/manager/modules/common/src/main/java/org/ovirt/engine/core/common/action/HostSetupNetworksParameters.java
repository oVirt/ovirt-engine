package org.ovirt.engine.core.common.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.annotation.ConfiguredRange;
import org.ovirt.engine.core.compat.Guid;


public class HostSetupNetworksParameters extends VdsActionParameters {

    private static final long serialVersionUID = 6819278948636850828L;

    /*This field cannot be validated via bean validation due to validation conflict:
    {@link NetworkAttachment} in this class is present as new entity and entity to be removed, both requiring different
    conflicting validations. So {@link NetworkAttachment} will be validated manually in
     {@link org.ovirt.engine.core.bll.CommandBase#canDoAction}*/
    private List<NetworkAttachment> networkAttachments;

    private Set<Guid> removedNetworkAttachments;

    private List<Bond> bonds;

    private Set<Guid> removedBonds;

    private Set<String> removedUnmanagedNetworks;

    private boolean rollbackOnFailure = true;

    @ConfiguredRange(min = 1, maxConfigValue = ConfigValues.NetworkConnectivityCheckTimeoutInSeconds,
            message = "VALIDATION.CONNECTIVITY.TIMEOUT.INVALID")
    private Integer conectivityTimeout;

    HostSetupNetworksParameters() {
    }

    public HostSetupNetworksParameters(Guid hostId) {
        super(hostId);
        setNetworkAttachments(new ArrayList<NetworkAttachment>());
        setRemovedNetworkAttachments(new HashSet<Guid>());
        setBonds(new ArrayList<Bond>());
        setRemovedBonds(new HashSet<Guid>());
        setRemovedUnmanagedNetworks(new HashSet<String>());
    }

    public boolean rollbackOnFailure() {
        return rollbackOnFailure;
    }

    public void setRollbackOnFailure(boolean rollbackOnFailure) {
        this.rollbackOnFailure = rollbackOnFailure;
    }

    public Integer getConectivityTimeout() {
        return conectivityTimeout;
    }

    public void setConectivityTimeout(Integer conectivityTimeout) {
        this.conectivityTimeout = conectivityTimeout;
    }

    public List<NetworkAttachment> getNetworkAttachments() {
        return networkAttachments;
    }

    public void setNetworkAttachments(List<NetworkAttachment> networkAttachments) {
        this.networkAttachments = networkAttachments;
    }

    public Set<Guid> getRemovedNetworkAttachments() {
        return removedNetworkAttachments;
    }

    public void setRemovedNetworkAttachments(Set<Guid> removedNetworkAttachments) {
        this.removedNetworkAttachments = removedNetworkAttachments;
    }

    public List<Bond> getBonds() {
        return bonds;
    }

    public void setBonds(List<Bond> bonds) {
        this.bonds = bonds;
    }

    public Set<Guid> getRemovedBonds() {
        return removedBonds;
    }

    public void setRemovedBonds(Set<Guid> removedBonds) {
        this.removedBonds = removedBonds;
    }

    public Set<String> getRemovedUnmanagedNetworks() {
        return removedUnmanagedNetworks;
    }

    public void setRemovedUnmanagedNetworks(Set<String> removedUnmanagedNetworks) {
        this.removedUnmanagedNetworks = removedUnmanagedNetworks;
    }
}

