package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public abstract class ImportVmModel extends ListWithDetailsModel {

    private final ClusterListModel<Void> cluster;
    private ListModel<CpuProfile> cpuProfiles;
    private final QuotaListModel<Void> clusterQuota;
    private ArchitectureType targetArchitecture;
    protected StoragePool storagePool;
    private UICommand closeCommand;
    private List<VM> vmsFromDB;

    public abstract void executeImport(IFrontendMultipleActionAsyncCallback callback);
    public abstract boolean validate();
    public abstract void init(final List<VM> externalVms, final Guid dataCenterId);
    protected final IEventListener<EventArgs> clusterChangedListener = new IEventListener<EventArgs>() {

        @Override
        public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
            if (getClusterQuota().getIsAvailable()) {
                Frontend.getInstance().runQuery(QueryType.GetAllRelevantQuotasForCluster,
                    new IdQueryParameters(getCluster().getSelectedItem().getId()),
                    new AsyncQuery<QueryReturnValue>(returnValue -> {
                                ArrayList<Quota> quotaList = returnValue.getReturnValue();
                                getClusterQuota().setItems(quotaList);
                                if (quotaList.isEmpty()
                                        && QuotaEnforcementTypeEnum.HARD_ENFORCEMENT.equals(storagePool.getQuotaEnforcementType())) {
                                    setMessage(ConstantsManager.getInstance()
                                            .getConstants()
                                            .missingQuotaClusterEnforceMode());
                                } else if (getMessage() != null
                                        && getMessage().equals(ConstantsManager.getInstance()
                                                .getConstants()
                                                .missingQuotaClusterEnforceMode())) {
                                    setMessage("");
                                }
                            }));
            }
            fetchCpuProfiles(getCluster().getSelectedItem().getId());
        }
    };

    ImportVmModel(final ClusterListModel<Void> cluster, final QuotaListModel clusterQuota) {
        this.cluster = cluster;
        this.clusterQuota = clusterQuota;
        setCpuProfiles(new ListModel<CpuProfile>());
    }

    private void fetchCpuProfiles(Guid clusterId) {
        Frontend.getInstance().runQuery(QueryType.GetCpuProfilesByClusterId,
                new IdQueryParameters(clusterId),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    List<CpuProfile> cpuProfiles = returnValue.getReturnValue();
                    getCpuProfiles().setItems(cpuProfiles);
                }));
    }

    protected void setDetailList(HasEntity<?> ... models) {
        setDetailModels(Arrays.asList(models));
    }

    public ArchitectureType getTargetArchitecture() {
        return targetArchitecture;
    }

    public void setTargetArchitecture(ArchitectureType targetArchitecture) {
        this.targetArchitecture = targetArchitecture;
    }

    public ClusterListModel<Void> getCluster() {
        return cluster;
    }

    public ListModel<CpuProfile> getCpuProfiles() {
        return cpuProfiles;
    }

    private void setCpuProfiles(ListModel<CpuProfile> value) {
        cpuProfiles = value;
    }

    public QuotaListModel<Void> getClusterQuota() {
        return clusterQuota;
    }

    public StoragePool getStoragePool() {
        return storagePool;
    }

    public void setStoragePool(StoragePool storagePool) {
        this.storagePool = storagePool;
    }

    public void setItems(final AsyncCallback<QueryReturnValue> callback, final List<VM>  externalVms) {
        Frontend.getInstance().runQuery(QueryType.Search,
                new SearchParameters(createSearchPattern(externalVms), SearchType.VM),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    vmsFromDB = returnValue.getReturnValue();

                    Set<String> existingNames = vmsFromDB
                            .stream()
                            .filter(vm -> vm.getStoragePoolId().equals(getStoragePool().getId()))
                            .map(VM::getName)
                            .collect(Collectors.toSet());

                    List<ImportVmData> vmDataList = new ArrayList<>();
                    for (VM vm : externalVms) {
                        ImportVmData vmData = new ImportVmData(vm);
                        if (vmsFromDB.contains(vm)) {
                            vmData.setExistsInSystem(true);
                            vmData.getClone().setEntity(true);
                            vmData.getClone().setIsChangeable(false);
                            vmData.getClone().setChangeProhibitionReason(ConstantsManager.getInstance()
                                    .getConstants()
                                    .importVMThatExistsInSystemMustClone());
                        }

                        vmData.setNameExistsInSystem(existingNames.contains(vm.getName()));

                        vmDataList.add(vmData);
                    }
                    setItems(vmDataList);
                    callback.onSuccess(returnValue);
                }));
    }

    private boolean isNameExistsInTheSystem(String vmName) {
        return vmsFromDB.stream().anyMatch(vm -> vm.getName().equals(vmName));
    }

    private String createSearchPattern(Collection<VM> vms) {
        String vm_guidKey = "ID = "; //$NON-NLS-1$
        String vm_nameKey = "NAME = "; //$NON-NLS-1$
        String orKey = " or "; //$NON-NLS-1$
        String prefix = "VM: "; //$NON-NLS-1$

        StringJoiner sj = new StringJoiner(orKey, prefix, "");
        vms.forEach(vm -> {
            sj.add(vm_guidKey + vm.getId().toString());
            sj.add(vm_nameKey + vm.getName());
        });

        return sj.toString();
    }

    protected boolean validateNames() {
        boolean valid = true;
        for (ImportVmData importVmData : (Iterable<ImportVmData>) getItems()) {
            if (!validateName(importVmData)) {
                valid = false;
            }
        }

        if (!valid) {
            onPropertyChanged(new PropertyChangedEventArgs("InvalidVm")); //$NON-NLS-1$
        }
        return valid;
    }

    private boolean validateName(final ImportVmData data) {
        final int maxNameLength = getMaxNameLength();
        VmImportGeneralModel model = (VmImportGeneralModel) getDetailModels().get(0);
        EntityModel<String> vmName = new EntityModel<>(data.getVm().getName());
        vmName.validateEntity(
                new IValidation[] {
                        new NotEmptyValidation(),
                        new LengthValidation(maxNameLength),
                        new I18NNameValidation(),
                        uniqueNameValidation(data, vmName)
                });

        data.setError(vmName.getIsValid() ? null : ConstantsManager.getInstance().getConstants().invalidName());

        // Updating the 'name' model in general sub-tab
        model.getName().setInvalidityReasons(vmName.getInvalidityReasons());
        model.getName().setIsValid(vmName.getIsValid());

        return vmName.getIsValid();
    }

    private IValidation uniqueNameValidation(ImportVmData data, EntityModel<String> vmName) {
        return value -> (isNameExistsInTheSystem(vmName.getEntity()) || !isVmNameUnique(data))?
               ValidationResult.fail(ConstantsManager.getInstance().getConstants().nameMustBeUniqueInvalidReason())
               : ValidationResult.ok();
    }

    private boolean isVmNameUnique(ImportVmData data) {
        return getItems().stream().noneMatch(item -> data != item &&
                data.getVm().getName().equals(((ImportVmData) item).getVm().getName()));
    }

    protected int getMaxNameLength() {
        return AsyncDataProvider.getInstance().getMaxVmNameLength();
    }

    @Override
    protected Object provideDetailModelEntity(Object selectedItem) {
        return selectedItem;
    }

    protected void showCloseMessage(String message) {
        setMessage(message);
        getCommands().clear();
        getCommands().add(closeCommand);
        stopProgress();
    }

    public void setCloseCommand(UICommand closeCommand) {
        this.closeCommand = closeCommand;
    }
}
