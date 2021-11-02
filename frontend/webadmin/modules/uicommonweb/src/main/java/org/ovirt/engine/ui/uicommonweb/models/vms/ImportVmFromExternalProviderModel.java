package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericNameableComparator;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.dataprovider.ImagesDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.SortedListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public abstract class ImportVmFromExternalProviderModel extends ImportVmModel {
    public static final String ON_DISK_LOAD = "OnDiskLoad"; //$NON-NLS-1$

    private Map<Guid, ArrayList<Quota>> storageQuotaMap;
    private ListModel<StorageDomain> storage;
    private ListModel<VolumeType> allocation;
    private final Map<String, ImportDiskData> diskImportDataMap = new HashMap<>();
    private final VmImportGeneralModel vmImportGeneralModel;
    private VmImportDiskListModel importDiskListModel;
    protected VmImportInterfaceListModel importInterfaceListModel;
    private List<VnicProfileView> networkProfiles;
    private SortedListModel<RepoImage> iso;
    private EntityModel<Boolean> attachDrivers;
    private String winWithoutVirtioMessage;
    private boolean sourceIsNotKvm;

    protected ImportVmFromExternalProviderModel(VmImportGeneralModel vmImportGeneralModel, VmImportDiskListModel importDiskListModel,
            VmImportInterfaceListModel vmImportInterfaceListModel, final ClusterListModel<Void> cluster, final QuotaListModel clusterQuota) {
        super(cluster, clusterQuota);
        this.vmImportGeneralModel = vmImportGeneralModel;
        this.importDiskListModel = importDiskListModel;
        this.importInterfaceListModel = vmImportInterfaceListModel;
        setStorage(new ListModel<StorageDomain>());
        setAllocation(new ListModel<VolumeType>());
        getAllocation().setItems(Arrays.asList(null, VolumeType.Sparse, VolumeType.Preallocated));
        sourceIsNotKvm = true;
        setIso(new SortedListModel<>(new LexoNumericNameableComparator()));
        setAttachDrivers(new EntityModel<>(false));
        vmImportGeneralModel.getOperatingSystems().getSelectedItemChangedEvent().addListener((ev, sender, args) -> updateWindowsWarningMessage());
        getClusterQuota().setIsAvailable(false);
        setDetailList(vmImportGeneralModel, importInterfaceListModel, importDiskListModel);
    }

    private void updateWindowsWarningMessage() {
        setWinWithoutVirtioMessage(""); //$NON-NLS-1$
        Integer selectedOS = vmImportGeneralModel.getOperatingSystems().getSelectedItem();
        if (selectedOS == null) {
            return;
        }

        boolean attachDrivers = getAttachDrivers().getEntity();
        boolean someDriverSelected = getIso().getSelectedItem() != null;
        boolean isWindows = AsyncDataProvider.getInstance().isWindowsOsType(selectedOS);

        if (isWindows && sourceIsNotKvm &&  (!attachDrivers || !someDriverSelected)) {
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

    @Override
    public void init(final List<VM> externalVms, final Guid dataCenterId) {
        setCloseCommand(new UICommand(null, this)
        .setTitle(ConstantsManager.getInstance().getConstants().close())
        .setIsDefault(true)
        .setIsCancel(true));

        initIsoAndAttachDriversFields(externalVms);
        setTargetArchitecture(externalVms);
        withDataCenterLoaded(dataCenterId, returnValue -> setItems(r -> doInit(), externalVms));
    }

    private void initIsoAndAttachDriversFields(final List<VM> externalVms) {
        sourceIsNotKvm = externalVms != null && externalVms.size() > 0 && externalVms.get(0).getOrigin() != OriginType.KVM;

        getIso().setIsAvailable(sourceIsNotKvm);
        getAttachDrivers().setIsAvailable(sourceIsNotKvm);
        if (sourceIsNotKvm) {
            setAttachDrivers(new EntityModel<>(false));
            getIso().setIsChangeable(false);

            getAttachDrivers().getEntityChangedEvent().addListener((ev, sender, args) -> {
                getIso().setIsChangeable(getAttachDrivers().getEntity());
                updateWindowsWarningMessage();
            });

            getIso().getSelectedItemChangedEvent().addListener((ev, sender, args) -> updateWindowsWarningMessage());
        }
    }

    private void withDataCenterLoaded(Guid dataCenterId, final AsyncCallback<StoragePool> callback) {
        AsyncDataProvider.getInstance().getDataCenterById(new AsyncQuery<>(returnValue -> {
            setStoragePool(returnValue);
            callback.onSuccess(returnValue);
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
        AsyncDataProvider.getInstance().getVnicProfilesByDcId(new AsyncQuery<>(returnValue -> {
            networkProfiles = returnValue;
            initNetworksList();
            initClusterAndStorage(dataCenter);
        }), dataCenter.getId());
     }

    private void initClusterAndStorage(StoragePool dataCenter) {
        AsyncDataProvider.getInstance().getClusterByServiceList(new AsyncQuery<>(clusters -> {
            ArchitectureType targetArch = getTargetArchitecture();
            if (targetArch != null) {
                clusters = AsyncDataProvider.getInstance().filterByArchitecture(clusters, targetArch);
            }
            getCluster().setItems(clusters);
            getCluster().setSelectedItem(Linq.firstOrNull(clusters));

            // get storage domains
            AsyncDataProvider.getInstance().getStorageDomainList(new AsyncQuery<>(
                            storageDomains -> {
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
                            }),
            getStoragePool().getId());
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
            vm.getInterfaces().forEach(this::addNetworkImportData);
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
        ImagesDataProvider.getISOImagesList(new AsyncQuery<>(
                        images -> {
                            getIso().setItems(images);
                            getIso().setSelectedItem(tryToFindVirtioTools(images));
                        }),
                getStoragePool().getId(),
                false);
    }

    private RepoImage tryToFindVirtioTools(List<RepoImage> isos) {
        for (RepoImage iso : isos) {
            if (iso.getRepoImageId().startsWith("virtio-win")) { //$NON-NLS-1$
                return iso;
            }
        }
        return isos.isEmpty() ? null : isos.get(0);
    }

    private void initQuotaForStorageDomains() {
        List<QueryType> queryTypeList = new ArrayList<>();
        List<QueryParametersBase> queryParamsList = new ArrayList<>();
        for (StorageDomain storage : getStorage().getItems()) {
            queryTypeList.add(QueryType.GetAllRelevantQuotasForStorage);
            queryParamsList.add(new IdQueryParameters(storage.getId()));
        }
        storageQuotaMap = new HashMap<>();
        Frontend.getInstance().runMultipleQueries(queryTypeList,
                queryParamsList,
                result -> {
                    Iterator<QueryReturnValue> returnValuesIter = result.getReturnValues().iterator();
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

    public SortedListModel<RepoImage> getIso() {
        return iso;
    }

    public void setIso(SortedListModel<RepoImage> iso) {
        this.iso = iso;
    }

    public EntityModel<Boolean> getAttachDrivers() {
        return attachDrivers;
    }

    public void setAttachDrivers(EntityModel<Boolean> attachTools) {
        this.attachDrivers = attachTools;
    }

    protected void updateNetworkInterfacesForVm(List<VmNetworkInterface> interfaces) {
        for (VmNetworkInterface iface : interfaces) {
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
