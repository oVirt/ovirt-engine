package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetUnregisteredBlockStorageDomainsParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public abstract class ImportSanStorageModel extends SanStorageModelBase {

    protected ListModel<StorageDomain> storageDomains;

    @Override
    protected String getListName() {
        return "ImportSanStorageModel"; //$NON-NLS-1$
    }

    @Override
    protected void initializeItems(List<LunModel> newLuns, List<SanTargetModel> newTargets) {
    }

    public ListModel<StorageDomain> getStorageDomains() {
        return storageDomains;
    }

    public void setStorageDomains(ListModel<StorageDomain> storageDomains) {
        this.storageDomains = storageDomains;
    }

    protected void addStorageDomains(ArrayList<StorageDomain> storageDomains) {
        Set<StorageDomain> allStorageDomains = new HashSet<>();
        allStorageDomains.addAll(getStorageDomains().getItems());
        allStorageDomains.addAll(storageDomains);
        getStorageDomains().setItems(new ArrayList<>(allStorageDomains));
    }

    protected void postGetUnregisteredStorageDomains(List<StorageDomain> storageDomains, List<StorageServerConnections> connections) {
        // Override if needed
    }

    protected void getUnregisteredStorageDomains(List<StorageServerConnections> connections) {
        VDS vds = getContainer().getHost().getSelectedItem();

        Frontend.getInstance().runQuery(QueryType.GetUnregisteredBlockStorageDomains,
                new GetUnregisteredBlockStorageDomainsParameters(vds.getId(), getType(), connections),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    Pair<List<StorageDomain>, List<StorageServerConnections>> returnValuePair =
                            returnValue.getReturnValue();

                    ArrayList<StorageDomain> storageDomains =
                            (ArrayList<StorageDomain>) returnValuePair.getFirst();
                    ArrayList<StorageServerConnections> connections1 =
                            (ArrayList<StorageServerConnections>) returnValuePair.getSecond();

                    if (storageDomains != null) {
                        addStorageDomains(storageDomains);
                    }

                    postGetUnregisteredStorageDomains(storageDomains, connections1);
                }));
    }

    @Override
    public boolean validate() {
        boolean isValid = getStorageDomains().getSelectedItems() != null &&
                !getStorageDomains().getSelectedItems().isEmpty();

        if (!isValid) {
            getInvalidityReasons().add(ConstantsManager.getInstance().getConstants().noStorageDomainsSelectedInvalidReason());
        }
        setIsValid(isValid);

        return getIsValid();
    }
}
