package org.ovirt.engine.core.common.vdscommands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.action.CreateOrUpdateBond;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class HostSetupNetworksVdsCommandParameters extends VdsIdAndVdsVDSCommandParametersBase {
    private List<HostNetwork> networks;
    private Set<String> removedNetworks;
    private List<CreateOrUpdateBond> createOrUpdateBond;
    private Set<String> removedBonds;
    private boolean rollbackOnFailure;
    private int connectivityTimeout;
    private boolean managementNetworkChanged;

    public HostSetupNetworksVdsCommandParameters(VDS host,
            List<HostNetwork> networks,
            Set<String> removedNetworks,
            List<CreateOrUpdateBond> createOrUpdateBond,
            Set<String> removedBonds) {
        super(host);
        this.networks = (networks == null) ? new ArrayList<HostNetwork>() : networks;
        this.removedNetworks = (removedNetworks == null) ? new HashSet<String>() : removedNetworks;
        this.createOrUpdateBond = (createOrUpdateBond
                == null) ? new ArrayList<CreateOrUpdateBond>() : createOrUpdateBond;
        this.removedBonds = (removedBonds == null) ? new HashSet<String>() : removedBonds;
    }

    public HostSetupNetworksVdsCommandParameters() {
    }

    public List<HostNetwork> getNetworks() {
        return networks;
    }

    public void setNetworks(List<HostNetwork> networks) {
        this.networks = networks;
    }

    public Set<String> getRemovedNetworks() {
        return removedNetworks;
    }

    public void setRemovedNetworks(Set<String> removedNetworks) {
        this.removedNetworks = removedNetworks;
    }

    public List<CreateOrUpdateBond> getCreateOrUpdateBonds() {
        return createOrUpdateBond;
    }

    public void setCreateOrUpdateBonds(List<CreateOrUpdateBond> createOrUpdateBond) {
        this.createOrUpdateBond = createOrUpdateBond;
    }

    public Set<String> getRemovedBonds() {
        return removedBonds;
    }

    public void setRemovedBonds(Set<String> removedBonds) {
        this.removedBonds = removedBonds;
    }

    public boolean isRollbackOnFailure() {
        return rollbackOnFailure;
    }

    public int getConnectivityTimeout() {
        return connectivityTimeout;
    }

    public void setRollbackOnFailure(boolean checkConnectivity) {
        this.rollbackOnFailure = checkConnectivity;
    }

    public void setConnectivityTimeout(int connectivityTimeout) {
        this.connectivityTimeout = connectivityTimeout;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("rollbackOnFailure", isRollbackOnFailure())
                .append("connectivityTimeout", getConnectivityTimeout())
                .append("networks", Entities.collectionToString(getNetworks(), "\t\t"))
                .append("removedNetworks", getRemovedNetworks())
                .append("bonds", Entities.collectionToString(getCreateOrUpdateBonds(), "\t\t"))
                .append("removedBonds", getRemovedBonds());
    }

    public boolean isManagementNetworkChanged() {
        return managementNetworkChanged;
    }

    public void setManagementNetworkChanged(boolean isManagementNetworkChanged) {
        this.managementNetworkChanged = isManagementNetworkChanged;
    }
}
