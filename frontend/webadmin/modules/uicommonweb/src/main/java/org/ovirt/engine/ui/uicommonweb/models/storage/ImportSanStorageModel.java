package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetUnregisteredBlockStorageDomainsParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public abstract class ImportSanStorageModel extends SanStorageModel {

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
        ArrayList<StorageDomain> allStorageDomains = new ArrayList<>();
        allStorageDomains.addAll(getStorageDomains().getItems());
        allStorageDomains.addAll(storageDomains);
        getStorageDomains().setItems(allStorageDomains);
    }

    protected void postGetUnregisteredStorageDomains(List<StorageDomain> storageDomains, List<StorageServerConnections> connections) {
        // Override if needed
    }

    protected void getUnregisteredStorageDomains(List<StorageServerConnections> connections) {
        VDS vds = getContainer().getHost().getSelectedItem();

        Frontend.getInstance().runQuery(VdcQueryType.GetUnregisteredBlockStorageDomains,
                new GetUnregisteredBlockStorageDomainsParameters(vds.getId(), getType(), connections),
                new AsyncQuery(getContainer(), new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        VdcQueryReturnValue vdcQueryReturnValue = (VdcQueryReturnValue) returnValue;
                        Pair<List<StorageDomain>, List<StorageServerConnections>> returnValuePair =
                                vdcQueryReturnValue.getReturnValue();

                        ArrayList<StorageDomain> storageDomains =
                                (ArrayList<StorageDomain>) returnValuePair.getFirst();
                        ArrayList<StorageServerConnections> connections =
                                (ArrayList<StorageServerConnections>) returnValuePair.getSecond();

                        if (storageDomains != null) {
                            addStorageDomains(storageDomains);
                        }

                        postGetUnregisteredStorageDomains(storageDomains, connections);
                    }
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
