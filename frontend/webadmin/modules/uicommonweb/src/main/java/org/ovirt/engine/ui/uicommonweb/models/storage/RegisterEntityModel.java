package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportEntityData;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

import java.util.ArrayList;
import java.util.List;

public abstract class RegisterEntityModel extends Model {

    private UICommand cancelCommand;
    private ListModel<ImportEntityData> entities;
    private ListModel<VDSGroup> cluster;
    private Guid storageDomainId;

    public RegisterEntityModel() {
        setEntities(new ListModel());
        setCluster(new ListModel());
    }

    protected abstract void onSave();

    @Override
    public void initialize() {
        super.initialize();

        // Create and set commands
        UICommand onSaveCommand = new UICommand("OnSave", this); //$NON-NLS-1$
        onSaveCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        onSaveCommand.setIsDefault(true);
        getCommands().add(onSaveCommand);
        getCommands().add(getCancelCommand());

        updateClusters();
    }

    private void updateClusters() {
        AsyncDataProvider.getDataCentersByStorageDomain(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                ArrayList<StoragePool> storagePools = (ArrayList<StoragePool>) returnValue;
                StoragePool storagePool = storagePools.size() > 0 ? storagePools.get(0) : null;
                if (storagePool == null) {
                    return;
                }

                AsyncDataProvider.getClusterByServiceList(new AsyncQuery(target, new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) returnValue;

                        for (ImportEntityData entityData : entities.getItems()) {
                            List<VDSGroup> filteredClusters = AsyncDataProvider.filterByArchitecture(clusters, entityData.getArchType());
                            entityData.getCluster().setItems(filteredClusters);
                            entityData.getCluster().setSelectedItem(Linq.firstOrDefault(filteredClusters));
                        }

                        getCluster().setItems(clusters);
                        getCluster().setSelectedItem(Linq.firstOrDefault(clusters));
                    }
                }), storagePool.getId(), true, false);
            }
        }), storageDomainId);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            onSave();
        }
    }

    protected void cancel() {
        getCancelCommand().execute();
    }

    @Override
    public UICommand getCancelCommand() {
        return cancelCommand;
    }

    public void setCancelCommand(UICommand cancelCommand) {
        this.cancelCommand = cancelCommand;
    }

    public ListModel<ImportEntityData> getEntities() {
        return entities;
    }

    public void setEntities(ListModel<ImportEntityData> entities) {
        this.entities = entities;
    }

    public ListModel<VDSGroup> getCluster() {
        return cluster;
    }

    private void setCluster(ListModel<VDSGroup> value) {
        cluster = value;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }
}
