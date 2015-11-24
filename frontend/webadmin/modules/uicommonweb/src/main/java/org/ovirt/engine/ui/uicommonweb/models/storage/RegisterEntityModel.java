package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportEntityData;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;

public abstract class RegisterEntityModel<T> extends Model {

    private UICommand cancelCommand;
    private ListModel<ImportEntityData<T>> entities;
    private ListModel<Cluster> cluster;
    private EntityModel<Map<Guid, List<Quota>>> clusterQuotasMap;
    private EntityModel<Map<Guid, Quota>> diskQuotaMap;
    private ListModel<Quota> storageQuota;
    private Guid storageDomainId;
    private StoragePool storagePool;

    public RegisterEntityModel() {
        setEntities(new ListModel<ImportEntityData<T>>());
        setCluster(new ListModel<Cluster>());

        setClusterQuotasMap(new EntityModel<Map<Guid, List<Quota>>>());
        getClusterQuotasMap().setEntity(new HashMap<Guid, List<Quota>>());
        setDiskQuotaMap(new EntityModel<Map<Guid, Quota>>());
        getDiskQuotaMap().setEntity(new HashMap<Guid, Quota>());
        setStorageQuota(new ListModel<Quota>());
    }

    protected abstract void onSave();

    @Override
    public void initialize() {
        super.initialize();

        // Create and set commands
        UICommand onSaveCommand = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
        getCommands().add(onSaveCommand);
        getCommands().add(getCancelCommand());

        updateClusters();
    }

    private void updateClusters() {
        AsyncDataProvider.getInstance().getDataCentersByStorageDomain(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                ArrayList<StoragePool> storagePools = (ArrayList<StoragePool>) returnValue;
                storagePool = storagePools.size() > 0 ? storagePools.get(0) : null;
                if (storagePool == null) {
                    return;
                }

                AsyncDataProvider.getInstance().getClusterByServiceList(new AsyncQuery(target, new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        ArrayList<Cluster> clusters = (ArrayList<Cluster>) returnValue;

                        for (ImportEntityData<T> entityData : entities.getItems()) {
                            List<Cluster> filteredClusters = AsyncDataProvider.getInstance().filterByArchitecture(clusters, entityData.getArchType());
                            entityData.getCluster().setItems(filteredClusters);
                            entityData.getCluster().setSelectedItem(Linq.firstOrNull(filteredClusters));
                        }

                        getCluster().setItems(clusters);
                        getCluster().setSelectedItem(Linq.firstOrNull(clusters));

                        updateClusterQuota(clusters);
                        updateStorageQuota();
                    }
                }), storagePool.getId(), true, false);

            }
        }), storageDomainId);
    }

    private void updateStorageQuota() {
        if (!isQuotaEnabled()) {
            return;
        }

        AsyncDataProvider.getInstance().getAllRelevantQuotasForStorageSorted(new AsyncQuery(
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        List<Quota> quotas = (List<Quota>)returnValue;
                        quotas = (quotas != null) ? quotas : new ArrayList<Quota>();

                        getStorageQuota().setItems(quotas);
                        getStorageQuota().setSelectedItem(Linq.firstOrNull(quotas));
                    }
                }), storageDomainId, null);
    }

    private void updateClusterQuota(ArrayList<Cluster> clusters) {
        if (!isQuotaEnabled()) {
            return;
        }

        List<VdcQueryType> queries = new ArrayList<>();
        List<VdcQueryParametersBase> params = new ArrayList<>();
        for (Cluster cluster : clusters) {
            queries.add(VdcQueryType.GetAllRelevantQuotasForCluster);
            params.add(new IdQueryParameters(cluster.getId()));
        }

        Frontend.getInstance().runMultipleQueries(queries, params, new IFrontendMultipleQueryAsyncCallback() {
            @Override
            public void executed(FrontendMultipleQueryAsyncResult result) {
                Map<Guid, List<Quota>> clusterQuotasMap = new HashMap<>();
                for (int i = 0; i < result.getReturnValues().size(); i++) {
                    List<Quota> quotas = result.getReturnValues().get(i).getReturnValue();
                    Guid clusterId = ((IdQueryParameters) result.getParameters().get(i)).getId();

                    clusterQuotasMap.put(clusterId, quotas);
                }
                getClusterQuotasMap().setEntity(clusterQuotasMap);
            }
        });
    }

    public void selectQuotaByName(String name, ListModel<Quota> listModel) {
        for (Quota quota : listModel.getItems()) {
            if (quota.getQuotaName().equals(name)) {
                listModel.setSelectedItem(quota);
                break;
            }
        }
    }

    public List<String> getQuotaNames(List<Quota> quotas) {
        List<String> names = new ArrayList<>();
        if (quotas != null) {
            for (Quota quota : quotas) {
                names.add(quota.getQuotaName());
            }
        }
        return names;
    }

    public Quota getQuotaByName(String name, List<Quota> quotas) {
        for (Quota quota : quotas) {
            if (quota.getQuotaName().equals(name)) {
                return quota;
            }
        }
        return null;
    }

    protected void updateDiskQuotas(List<Disk> disks) {
        for (Disk disk : disks) {
            Quota quota = getDiskQuotaMap().getEntity().get(disk.getId());
            if (quota == null) {
                quota = getStorageQuota().getSelectedItem();
            }
            if (quota != null) {
                ((DiskImage) disk).setQuotaId(quota.getId());
            }
        }
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

    public ListModel<ImportEntityData<T>> getEntities() {
        return entities;
    }

    public void setEntities(ListModel<ImportEntityData<T>> entities) {
        this.entities = entities;
    }

    public ListModel<Cluster> getCluster() {
        return cluster;
    }

    private void setCluster(ListModel<Cluster> value) {
        cluster = value;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public EntityModel<Map<Guid, List<Quota>>> getClusterQuotasMap() {
        return clusterQuotasMap;
    }

    public void setClusterQuotasMap(EntityModel<Map<Guid, List<Quota>>> clusterQuotasMap) {
        this.clusterQuotasMap = clusterQuotasMap;
    }

    public EntityModel<Map<Guid, Quota>> getDiskQuotaMap() {
        return diskQuotaMap;
    }

    public void setDiskQuotaMap(EntityModel<Map<Guid, Quota>> diskQuotaMap) {
        this.diskQuotaMap = diskQuotaMap;
    }

    public ListModel<Quota> getStorageQuota() {
        return storageQuota;
    }

    public void setStorageQuota(ListModel<Quota> storageQuota) {
        this.storageQuota = storageQuota;
    }

    public boolean isQuotaEnabled() {
        return storagePool.getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED;
    }
}
