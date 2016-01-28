package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
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
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

public abstract class ImportVmFromExternalProviderModel extends ImportVmModel {
    public static final String ON_DISK_LOAD = "OnDiskLoad"; //$NON-NLS-1$

    private Map<Guid, ArrayList<Quota>> storageQuotaMap;
    private ListModel<StorageDomain> storage;
    private ListModel<VolumeType> allocation;
    private final Map<String, ImportDiskData> diskImportDataMap = new HashMap<>();
    private final VmImportGeneralModel vmImportGeneralModel;
    private VmImportDiskListModel importDiskListModel;
    private VmImportInterfaceListModel importInterfaceListModel;
    private List<VnicProfileView> networkProfiles;
    private ListModel<String> iso;
    private EntityModel<Boolean> attachDrivers;
    private String winWithoutVirtioMessage;

    protected ImportVmFromExternalProviderModel(VmImportGeneralModel vmImportGeneralModel, VmImportDiskListModel importDiskListModel,
            VmImportInterfaceListModel vmImportInterfaceListModel, final ClusterListModel<Void> cluster, final QuotaListModel clusterQuota) {
        super(cluster, clusterQuota);
        this.vmImportGeneralModel = vmImportGeneralModel;
        this.importDiskListModel = importDiskListModel;
        this.importInterfaceListModel = vmImportInterfaceListModel;
        setStorage(new ListModel<StorageDomain>());
        setAllocation(new ListModel<VolumeType>());
        getAllocation().setItems(Arrays.asList(VolumeType.Sparse, VolumeType.Preallocated));
        setIso(new ListModel<String>());
        getIso().setIsChangeable(false);
        setAttachDrivers(new EntityModel<>(false));
        getAttachDrivers().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                getIso().setIsChangeable(getAttachDrivers().getEntity());
                updateWindowsWarningMessage();
            }
        });

        getIso().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                updateWindowsWarningMessage();
            }
        });

        vmImportGeneralModel.getOperatingSystems().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                updateWindowsWarningMessage();
            }
        });

        getClusterQuota().setIsAvailable(false);
        setDetailList(vmImportGeneralModel, vmImportInterfaceListModel, importDiskListModel);
    }

    private void updateWindowsWarningMessage() {
        setWinWithoutVirtioMessage(""); //$NON-NLS-1$
        Integer selectedOS = vmImportGeneralModel.getOperatingSystems().getSelectedItem();
        if (selectedOS == null) {
            return;
        }

        boolean attachDrivers = getAttachDrivers().getEntity();
        boolean someDriverSelected = !StringUtils.isEmpty(getIso().getSelectedItem());
        boolean isWindows = AsyncDataProvider.getInstance().isWindowsOsType(selectedOS);

        if (isWindows && (!attachDrivers || !someDriverSelected)) {
            setWinWithoutVirtioMessage(ConstantsManager.getInstance()
                    .getConstants().missingVirtioDriversForWindows());
        }
    }

    private final Map<String, ImportNetworkData> networkImportDataMap = new HashMap<>();

    public List<VnicProfileView> getNetworkProfiles() {
        return networkProfiles;
    }

    public SearchableListModel getImportDiskListModel() {
        return importDiskListModel;
    }

    public SearchableListModel getImportNetworkInterfaceListModel() {
        return importInterfaceListModel;
    }

    public void init(final List<VM> externalVms, final Guid dataCenterId) {
        setCloseCommand(new UICommand(null, this)
        .setTitle(ConstantsManager.getInstance().getConstants().close())
        .setIsDefault(true)
        .setIsCancel(true));

        setTargetArchitecture(externalVms);
        withDataCenterLoaded(dataCenterId, new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                setItems(new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        doInit();
                    }
                }, externalVms);
            }
        });
    }

    private void withDataCenterLoaded(Guid dataCenterId, final INewAsyncCallback callback) {
        AsyncDataProvider.getInstance().getDataCenterById(new AsyncQuery(null, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                setStoragePool((StoragePool) returnValue);
                callback.onSuccess(model, returnValue);
            }
        }), dataCenterId);
    }

    protected void setTargetArchitecture(List<VM> externalVms) {
        setTargetArchitecture(externalVms.iterator().next().getClusterArch());
    }

    protected void doInit() {
        final StoragePool dataCenter = getStoragePool();
        if (dataCenter == null) {
            return;
        }

        setStoragePool(dataCenter);
        getClusterQuota().setIsAvailable(dataCenter.getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED);
        getCluster().getSelectedItemChangedEvent().addListener(clusterChangedListener);

        // get cluster
        getCluster().setItems(null);
        AsyncDataProvider.getInstance().getVnicProfilesByDcId(new AsyncQuery(ImportVmFromExternalProviderModel.this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                networkProfiles = (List<VnicProfileView>) returnValue;
                initNetworksList();
                initClusterAndStorage(dataCenter);
            }
        }), dataCenter.getId());
     }

    private void initClusterAndStorage(StoragePool dataCenter) {
        AsyncDataProvider.getInstance().getClusterByServiceList(new AsyncQuery(ImportVmFromExternalProviderModel.this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                List<Cluster> clusters = (List<Cluster>) returnValue;

                ArchitectureType targetArch = getTargetArchitecture();
                if (targetArch != null) {
                    clusters = AsyncDataProvider.getInstance().filterByArchitecture(clusters, targetArch);
                }
                getCluster().setItems(clusters);
                getCluster().setSelectedItem(Linq.firstOrNull(clusters));

                // get storage domains
                AsyncDataProvider.getInstance().getStorageDomainList(new AsyncQuery(ImportVmFromExternalProviderModel.this,
                        new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        List<StorageDomain> storageDomains = (List<StorageDomain>) returnValue;
                        // filter storage domains
                        List<StorageDomain> filteredStorageDomains = new ArrayList<>();
                        for (StorageDomain domain : storageDomains) {
                            if (Linq.isDataActiveStorageDomain(domain)) {
                                filteredStorageDomains.add(domain);
                            }
                        }

                        getStorage().setItems(filteredStorageDomains);
                        if (getClusterQuota().getIsAvailable()) {
                            initQuotaForStorageDomains();
                        } else {
                            initDisksStorageDomainsList();
                        }
                    }

                }),
                getStoragePool().getId());
            }
        }),
        dataCenter.getId(), true, false);
    }

    protected void addDiskImportData(String alias,
            ArrayList<StorageDomain> storageDomains,
            VolumeType volumeType, EntityModel collapseSnapshots) {
        ImportDiskData data = new ImportDiskData();
        data.setCollapseSnapshot(collapseSnapshots);
        data.setAllStorageDomains(new ArrayList<StorageDomain>());
        data.setStorageDomains(storageDomains);
        data.setVolumeType(volumeType);
        data.setStorageQuotaList(storageQuotaMap);
        diskImportDataMap.put(alias, data);
    }

    protected void addNetworkImportData(VmNetworkInterface iface) {
        networkImportDataMap.put(
                iface.getVmName() + "$" + iface.getName(), //$NON-NLS-1$
                new ImportNetworkData(networkProfiles));
    }

    protected void initNetworksList() {
        for (Object item : getItems()) {
            ImportVmData importVmData = (ImportVmData) item;
            VM vm = importVmData.getVm();
            for (VmNetworkInterface iface : vm.getInterfaces()) {
                addNetworkImportData(iface);
            }
        }
    }

    protected void initDisksStorageDomainsList() {
        for (Object item : getItems()) {
            ImportVmData importVmData = (ImportVmData) item;
            VM vm = importVmData.getVm();

            for (Disk disk : vm.getDiskMap().values()) {
                DiskImage diskImage = (DiskImage) disk;
                addDiskImportData(
                        diskImage.getDiskAlias(),
                        new ArrayList<StorageDomain>(),
                        diskImage.getVolumeType(),
                        importVmData.getCollapseSnapshots());
            }
        }
        initIsoImages();
        postInitDisks();
    }

    private void postInitDisks() {
        stopProgress();
    }

    private void initIsoImages() {
        AsyncDataProvider.getInstance().getIrsImageList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        List<String> images = (List<String>) returnValue;
                        getIso().setItems(images);
                        getIso().setSelectedItem(tryToFindVirtioTools(images));
                    }
                }),
                getStoragePool().getId(),
                false);
    }

    private String tryToFindVirtioTools(List<String> isos) {
        for (String iso : isos) {
            if (iso.startsWith("virtio-win")) { //$NON-NLS-1$
                return iso;
            }
        }
        return isos.isEmpty() ? null : isos.get(0);
    }

    private void initQuotaForStorageDomains() {
        List<VdcQueryType> queryTypeList = new ArrayList<>();
        List<VdcQueryParametersBase> queryParamsList = new ArrayList<>();
        for (StorageDomain storage : getStorage().getItems()) {
            queryTypeList.add(VdcQueryType.GetAllRelevantQuotasForStorage);
            queryParamsList.add(new IdQueryParameters(storage.getId()));
        }
        storageQuotaMap = new HashMap<>();
        Frontend.getInstance().runMultipleQueries(queryTypeList,
                queryParamsList,
                new IFrontendMultipleQueryAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleQueryAsyncResult result) {
                        Iterator<VdcQueryReturnValue> returnValuesIter = result.getReturnValues().iterator();
                        Iterator<StorageDomain> domainsIter = getStorage().getItems().iterator();
                        boolean noQuota = true;
                        while(domainsIter.hasNext()) {
                            ArrayList<Quota> quotaList = returnValuesIter.next().getReturnValue();
                            noQuota = noQuota && quotaList.isEmpty();
                            storageQuotaMap.put(
                                    domainsIter.next().getId(),
                                    quotaList);
                        }
                        if (noQuota
                                && QuotaEnforcementTypeEnum.HARD_ENFORCEMENT.equals(storagePool.getQuotaEnforcementType())) {
                            showCloseMessage(ConstantsManager.getInstance()
                                    .getConstants().missingQuotaStorageEnforceMode());
                        }
                        initDisksStorageDomainsList();
                    }
                });
    }

    @Override
    protected String getListName() {
        return "ImportVmFromExternalProviderModel"; //$NON-NLS-1$
    }

    public ListModel<StorageDomain> getStorage() {
        return storage;
    }

    public void setStorage(ListModel<StorageDomain> storage) {
        this.storage = storage;
    }

    public ListModel<VolumeType> getAllocation() {
        return allocation;
    }

    public void setAllocation(ListModel<VolumeType> allocation) {
        this.allocation = allocation;
    }

    public ImportDiskData getDiskImportData(String alias) {
        ImportDiskData importData = diskImportDataMap.get(alias);
        if (importData != null) {
            if (storage.getSelectedItem() == null) {
                importData.setSelectedStorageDomain(storage.getSelectedItem());
            }
        }
        return importData;
    }

    public ImportNetworkData getNetworkImportData(VmNetworkInterface iface) {
        return networkImportDataMap.get(iface.getVmName() + "$" + iface.getName()); //$NON-NLS-1$
    }

    @Override
    public boolean validate() {
        return true;
    }

    public ListModel<String> getIso() {
        return iso;
    }

    public void setIso(ListModel<String> iso) {
        this.iso = iso;
    }

    public EntityModel<Boolean> getAttachDrivers() {
        return attachDrivers;
    }

    public void setAttachDrivers(EntityModel<Boolean> attachTools) {
        this.attachDrivers = attachTools;
    }

    protected void updateNetworkInterfacesForVm(VM vm) {
        for (VmNetworkInterface iface : vm.getInterfaces()) {
            ImportNetworkData importNetworkData = getNetworkImportData(iface);
            VnicProfileView profile = importNetworkData.getSelectedNetworkProfile();
            if (profile != null) {
                iface.setNetworkName(profile.getNetworkName());
                iface.setVnicProfileName(profile.getName());
            }
        }
    }

    public String getWinWithoutVirtioMessage() {
        return winWithoutVirtioMessage;
    }

    public void setWinWithoutVirtioMessage(String value) {
        if (!Objects.equals(winWithoutVirtioMessage, value)) {
            winWithoutVirtioMessage = value;
            onPropertyChanged(new PropertyChangedEventArgs("WinWithoutVirtioMessage")); //$NON-NLS-1$
        }
    }
}
