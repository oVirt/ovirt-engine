package org.ovirt.engine.core.common.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NicLabel;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.annotation.ConfiguredRange;
import org.ovirt.engine.core.compat.Guid;

public class HostSetupNetworksParameters extends VdsActionParameters {

    private static final long serialVersionUID = 6819278948636850828L;

    /*
     * This field cannot be validated via bean validation due to validation conflict: {@link NetworkAttachment} in this
     * class is present as new entity and entity to be updated, both requiring different conflicting validations. So
     * {@link NetworkAttachment} will be validated manually in {@link org.ovirt.engine.core.bll.CommandBase#validate}
     */
    private List<NetworkAttachment> networkAttachments;

    private Set<Guid> removedNetworkAttachments;

    private List<CreateOrUpdateBond> createOrUpdateBonds;

    private Set<Guid> removedBonds;

    private Set<String> removedUnmanagedNetworks;

    @Valid
    private Set<NicLabel> labels;

    private Set<String> removedLabels;

    private boolean rollbackOnFailure = true;

    private boolean commitOnSuccess;

    @ConfiguredRange(min = 1, maxConfigValue = ConfigValues.NetworkConnectivityCheckTimeoutInSeconds,
            message = "VALIDATION_CONNECTIVITY_TIMEOUT_INVALID")
    private Integer conectivityTimeout;

    HostSetupNetworksParameters() {
    }

    public HostSetupNetworksParameters(Guid hostId) {
        super(hostId);
        setNetworkAttachments(new ArrayList<NetworkAttachment>());
        setRemovedNetworkAttachments(new HashSet<Guid>());
        setCreateOrUpdateBonds(new ArrayList<CreateOrUpdateBond>());
        setRemovedBonds(new HashSet<Guid>());
        setRemovedUnmanagedNetworks(new HashSet<String>());
        setLabels(new HashSet<NicLabel>());
        setRemovedLabels(new HashSet<String>());
    }

    public HostSetupNetworksParameters(HostSetupNetworksParameters other) {
        super(other.getVdsId());
        setNetworkAttachments(other.networkAttachments);
        setRemovedNetworkAttachments(other.removedNetworkAttachments);
        setCreateOrUpdateBonds(other.createOrUpdateBonds);
        setRemovedBonds(other.removedBonds);
        setRemovedUnmanagedNetworks(other.removedUnmanagedNetworks);
        setLabels(other.labels);
        setRemovedLabels(other.removedLabels);
        setRollbackOnFailure(other.rollbackOnFailure);
        setConectivityTimeout(other.conectivityTimeout);
        setCommitOnSuccess(other.commitOnSuccess);

    }

    public boolean isEmptyRequest() {
        return networkAttachments.isEmpty() &&
            removedNetworkAttachments.isEmpty() &&
            createOrUpdateBonds.isEmpty() &&
            removedBonds.isEmpty() &&
            removedUnmanagedNetworks.isEmpty() &&
            labels.isEmpty() &&
            removedLabels.isEmpty();
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

    public List<CreateOrUpdateBond> getCreateOrUpdateBonds() {
        return createOrUpdateBonds;
    }

    public void setCreateOrUpdateBonds(List<CreateOrUpdateBond> createOrUpdateBonds) {
        this.createOrUpdateBonds = createOrUpdateBonds;
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

    public void setLabels(Set<NicLabel> labels) {
        this.labels = labels;
    }

    public Set<NicLabel> getLabels() {
        return labels;
    }

    public void setRemovedLabels(Set<String> removedLabels) {
        this.removedLabels = removedLabels;
    }

    public Set<String> getRemovedLabels() {
        return removedLabels;
    }

    public boolean isCommitOnSuccess() {
        return commitOnSuccess;
    }

    public void setCommitOnSuccess(boolean commitOnSuccess) {
        this.commitOnSuccess = commitOnSuccess;
    }
}
