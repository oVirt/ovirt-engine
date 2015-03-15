package org.ovirt.engine.core.common.action;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.compat.Guid;

public class ExtendSANStorageDomainParameters extends StorageDomainParametersBase {
    private static final long serialVersionUID = 1051216804598069936L;
    private ArrayList<String> privateLunIds;

    public ArrayList<String> getLunIds() {
        return privateLunIds == null ? new ArrayList<String>() : privateLunIds;
    }

    public void setLunIds(ArrayList<String> value) {
        privateLunIds = value;
    }

    private ArrayList<LUNs> privateLunsList;

    public ArrayList<LUNs> getLunsList() {
        return privateLunsList == null ? new ArrayList<LUNs>() : privateLunsList;
    }

    public void setLunsList(ArrayList<LUNs> value) {
        privateLunsList = value;
    }

    private boolean force;

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public ExtendSANStorageDomainParameters(Guid storageDomainId, ArrayList<String> lunIds) {
        super(storageDomainId);
        setLunIds(lunIds);
    }

    public ExtendSANStorageDomainParameters(Guid storageDomainId, ArrayList<String> lunIds, boolean force) {
        this(storageDomainId, lunIds);
        setForce(force);
    }

    public ExtendSANStorageDomainParameters() {
    }
}
