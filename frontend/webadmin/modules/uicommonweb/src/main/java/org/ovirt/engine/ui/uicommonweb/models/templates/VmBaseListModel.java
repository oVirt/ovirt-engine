package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.CpuPinningPolicy;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.IconUtils;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor;
import org.ovirt.engine.ui.uicommonweb.builders.vm.FullUnitToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.UnitToGraphicsDeviceParamsBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.VmSpecificUnitToVmBuilder;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModelChain;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModelChain.ConfirmationModelChainItem;
import org.ovirt.engine.ui.uicommonweb.models.ListWithSimpleDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.vms.AbstractDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.AttachDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ExportOvaModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ExportVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.HasDiskWindow;
import org.ovirt.engine.ui.uicommonweb.models.vms.InstanceImagesModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModelNetworkAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmBasedWidgetSwitchModeCommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmHighPerformanceConfigurationModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceCreatingManager;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public abstract class VmBaseListModel<E, T> extends ListWithSimpleDetailsModel<E, T> implements HasDiskWindow {

    public static final String DISK_WINDOW = "DiskWindow"; //$NON-NLS-1$

    protected static final String IS_ADVANCED_MODEL_LOCAL_STORAGE_KEY = "wa_vm_dialog"; //$NON-NLS-1$

    private VM privatecurrentVm;
    private UnitVmModel currentVmModel;

    public VM getcurrentVm() {
        return privatecurrentVm;
    }

    public void setcurrentVm(VM value) {
        privatecurrentVm = value;
    }

    public UnitVmModel getCurrentVmModel() {
        return currentVmModel;
    }

    public void setCurrentVmModel(UnitVmModel currentVmModel) {
        this.currentVmModel = currentVmModel;
    }

    VmInterfaceCreatingManager addVmFromBlankTemplateNetworkManager =
            new VmInterfaceCreatingManager(new VmInterfaceCreatingManager.PostVnicCreatedCallback() {
                @Override
                public void vnicCreated(Guid vmId, UnitVmModel unitVmModel) {
                    executeDiskModifications(vmId, unitVmModel);
                }

                @Override
                public void queryFailed() {
                    // do nothing
                }
            });

    protected VmInterfaceCreatingManager defaultNetworkCreatingManager =
            new VmInterfaceCreatingManager(new VmInterfaceCreatingManager.PostVnicCreatedCallback() {
                @Override
                public void vnicCreated(Guid vmId, UnitVmModel unitVmModel) {
                    getWindow().stopProgress();
                    cancel();
                    fireModelChangeRelevantForActionsEvent();
                    executeDiskModifications(vmId, unitVmModel);
                }

                @Override
                public void queryFailed() {
                    getWindow().stopProgress();
                    cancel();
                }
            });

    protected void executeDiskModifications(Guid vmId, UnitVmModel model) {
        // this is done on the background - the window is not visible anymore
        getcurrentVm().setId(vmId);
        model.getInstanceImages().executeDiskModifications(getcurrentVm());
    }

    protected void postExportOvaGetHosts(List<VDS> hosts) {
        ExportOvaModel model = (ExportOvaModel) getWindow();
        model.getProxy().setItems(hosts);
        model.getProxy().setSelectedItem(Linq.firstOrNull(hosts));

        model.getCommands().add(UICommand.createDefaultOkUiCommand("OnExportOva", this)); //$NON-NLS-1$
        model.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$

        model.stopProgress();
    }

    protected void export() {
        T selectedEntity = getSelectedItem();
        if (selectedEntity == null) {
            return;
        }

        if (getWindow() != null) {
            return;
        }

        ExportVmModel model = new ExportVmModel();
        setWindow(model);
        model.startProgress();
        setupExportModel(model);
        postExportGetSnapshots(selectedEntity);
    }

    protected void postExportGetSnapshots(T selectedEntity) {
        // Override if fetching snapshots is needed
        postExportInitStorageDomains(selectedEntity);
    }

    protected void postExportInitStorageDomains(T selectedEntity) {
        AsyncDataProvider.getInstance().getStorageDomainList(new AsyncQuery<>(
                storageDomains -> {
                    List<StorageDomain> filteredStorageDomains = new ArrayList<>();
                    for (StorageDomain a : storageDomains) {
                        if (a.getStorageDomainType() == StorageDomainType.ImportExport) {
                            filteredStorageDomains.add(a);
                        }
                    }

                    postExportGetStorageDomainList(filteredStorageDomains);
                }), extractStoragePoolIdNullSafe(selectedEntity));

        // check, if the VM has a disk which doesn't allow snapshot
        sendWarningForNonExportableDisks(selectedEntity);
    }

    private void postExportGetStorageDomainList(List<StorageDomain> storageDomains) {
        ExportVmModel model = (ExportVmModel) getWindow();
        model.getStorage().setItems(storageDomains);
        model.getStorage().setSelectedItem(Linq.firstOrNull(storageDomains));

        boolean noActiveStorage = true;
        for (StorageDomain a : storageDomains) {
            if (a.getStatus() == StorageDomainStatus.Active) {
                noActiveStorage = false;
                break;
            }
        }

        if (entitiesSelectedOnDifferentDataCenters()) {
            model.getCollapseSnapshots().setIsChangeable(false);
            model.getForceOverride().setIsChangeable(false);

            model.setMessage(entityResideOnSeveralDCsMakeSureTheExportedVMResideOnSameDcMsg());

            UICommand tempVar = new UICommand("Cancel", this); //$NON-NLS-1$
            tempVar.setTitle(ConstantsManager.getInstance().getConstants().close());
            tempVar.setIsDefault(true);
            tempVar.setIsCancel(true);
            model.getCommands().add(tempVar);
            model.stopProgress();
        } else if (storageDomains.isEmpty()) {
            model.getCollapseSnapshots().setIsChangeable(false);
            model.getForceOverride().setIsChangeable(false);

            model.setMessage(thereIsNoExportDomainBackupEntityAttachExportDomainToVmsDcMsg());

            UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
            tempVar2.setTitle(ConstantsManager.getInstance().getConstants().close());
            tempVar2.setIsDefault(true);
            tempVar2.setIsCancel(true);
            model.getCommands().add(tempVar2);
            model.stopProgress();
        } else if (noActiveStorage) {
            model.getCollapseSnapshots().setIsChangeable(false);
            model.getForceOverride().setIsChangeable(false);

            model.setMessage(ConstantsManager.getInstance()
                    .getConstants()
                    .theRelevantExportDomainIsNotActivePleaseActivateItMsg());

            UICommand tempVar3 = new UICommand("Cancel", this); //$NON-NLS-1$
            tempVar3.setTitle(ConstantsManager.getInstance().getConstants().close());
            tempVar3.setIsDefault(true);
            tempVar3.setIsCancel(true);
            model.getCommands().add(tempVar3);
            model.stopProgress();
        } else {
            showWarningOnExistingEntities(model, getEntityExportDomain());

            UICommand tempVar4 = UICommand.createDefaultOkUiCommand("OnExport", this); //$NON-NLS-1$
            model.getCommands().add(tempVar4);
            UICommand tempVar5 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
            model.getCommands().add(tempVar5);
        }
    }

    protected void showWarningOnExistingEntities(final ExportVmModel model, final QueryType getVmOrTemplateQuery) {
        Guid storageDomainId = model.getStorage().getSelectedItem().getId();
        AsyncDataProvider.getInstance().getDataCentersByStorageDomain(new AsyncQuery<>(
                storagePools -> postShowWarningOnExistingVms(model, storagePools, getVmOrTemplateQuery)), storageDomainId);
    }

    private void postShowWarningOnExistingVms(final ExportVmModel exportModel,
            List<StoragePool> storagePools,
            QueryType getVmOrTemplateQuery) {
        StoragePool storagePool = storagePools.size() > 0 ? storagePools.get(0) : null;

        if (storagePool != null) {

            Guid storageDomainId = exportModel.getStorage().getSelectedItem().getId();
            GetAllFromExportDomainQueryParameters tempVar =
                    new GetAllFromExportDomainQueryParameters(storagePool.getId(), storageDomainId);
            Frontend.getInstance().runQuery(getVmOrTemplateQuery, tempVar, new AsyncQuery<>((AsyncCallback<QueryReturnValue>) returnValue -> {
                ExportVmModel windowModel = (ExportVmModel) getWindow();
                List<T> foundVms = new ArrayList<>();

                if (returnValue != null) {
                    Iterable<T> iterableReturnValue = asIterableReturnValue(returnValue.getReturnValue());

                    for (T selectedItem1 : getSelectedItems()) {
                        for (T returnValueItem : iterableReturnValue) {
                            if (entititesEqualsNullSafe(returnValueItem, selectedItem1)) {
                                foundVms.add(selectedItem1);
                                break;
                            }
                        }
                    }
                }

                if (foundVms.size() != 0) {
                    windowModel.setMessage(composeEntityOnStorage(composeExistingVmsWarningMessage(foundVms)));
                }

                exportModel.stopProgress();
            }));
        } else {
            exportModel.stopProgress();
        }
    }

    private String composeExistingVmsWarningMessage(List<T> existingVms) {
        final List<String> list = new ArrayList<>();
        for (T t : existingVms) {
            list.add(extractNameFromEntity(t));
        }

        return String.join(", ", list); //$NON-NLS-1$
    }

    protected void setupExportModel(ExportVmModel model) {
        // no-op by default. Override if needed.
    }

    protected void setupExportOvaModel(ExportOvaModel model) {
        // no-op by default. Override if needed.
    }

    protected void setupNewVmModel(UnitVmModel model,
            VmType vmtype,
            List<UICommand> uiCommands) {
        model.setTitle(ConstantsManager.getInstance().getConstants().newVmTitle());
        model.setHelpTag(HelpTag.new_vm);
        model.setHashName("new_vm"); //$NON-NLS-1$
        model.setIsNew(true);
        model.getVmType().setSelectedItem(vmtype);
        model.setCustomPropertiesKeysList(AsyncDataProvider.getInstance().getCustomPropertiesList());
        model.setIsAdvancedModeLocalStorageKey(IS_ADVANCED_MODEL_LOCAL_STORAGE_KEY);

        setWindow(model);
        model.initialize();

        VmBasedWidgetSwitchModeCommand switchModeCommand = new VmBasedWidgetSwitchModeCommand();
        switchModeCommand.init(model);
        model.getCommands().add(switchModeCommand);

        model.getProvisioning().setEntity(true);

        for (UICommand uicommand : uiCommands) {
            model.getCommands().add(uicommand);
        }
        model.initForemanProviders(null);
    }

    protected void validateVm(final UnitVmModel model, final String vmName) {

        if (!model.validate()) {
            return;
        }

        AsyncDataProvider.getInstance().
                isVmNameUnique(new AsyncQuery<>(returnValue -> {
                    if (!returnValue && vmName.compareToIgnoreCase(getcurrentVm().getName()) != 0) {
                        model.getName()
                                .getInvalidityReasons()
                                .add(ConstantsManager.getInstance().getConstants().nameMustBeUniqueInvalidReason());
                        model.getName().setIsValid(false);
                        model.setValidTab(TabName.GENERAL_TAB, false);
                        model.fireValidationCompleteEvent();
                    } else {
                        model.getName()
                                .getInvalidityReasons().clear();
                        model.getName().setIsValid(true);
                        model.setValidTab(TabName.GENERAL_TAB, true);
                        onSaveVM(model);
                    }
                }

                        ),
                        vmName,
                        model.getSelectedDataCenter() == null ? null : model.getSelectedDataCenter().getId());
    }

    protected void onSaveVM(UnitVmModel model) {
        // Save changes.
        buildVmOnSave(model, getcurrentVm());

        getcurrentVm().setCpuPinning(model.getCpuPinning().getEntity());

        getcurrentVm().setUseHostCpuFlags(model.getHostCpu().getEntity());

        getcurrentVm().setVmInit(model.getVmInitModel().buildCloudInitParameters(model));

        if (!model.getProviders().getItems().iterator().next().equals(model.getProviders().getSelectedItem())) {
            getcurrentVm().setProviderId(model.getProviders().getSelectedItem().getId());
        }

        ConfirmationModelChain chain = new ConfirmationModelChain();
        chain.addConfirmation(createConfirmHighPerformanceVm(model));
        chain.addConfirmation(new TpmDataRemovalConfirmation(model, getcurrentVm().getId()));
        chain.addConfirmation(new NvramDataRemovalConfirmation(model, getcurrentVm().getId()));
        chain.execute(this, () -> saveOrUpdateVM(model));
    }

    protected void saveOrUpdateVM(final UnitVmModel model) {
        setConfirmWindow(null);

        if (!model.validate()) {
            return;
        }

        if (model.getIsNew()) {
            newVM(model);
        } else if (model.getIsClone()) {
            cloneVM(model);
        } else {
            updateVM(model);
        }
    }

    protected void cancelConfirmation() {
        setConfirmWindow(null);
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        fireModelChangeRelevantForActionsEvent();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        fireModelChangeRelevantForActionsEvent();
    }

    @Override
    protected void selectedItemPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.selectedItemPropertyChanged(sender, e);

        if (e.propertyName.equals("status")) { //$NON-NLS-1$
            fireModelChangeRelevantForActionsEvent();
        }
    }

    private void newVM(UnitVmModel model) {
        ConfirmationModelChain chain = new ConfirmationModelChain();
        chain.addConfirmation(new ChipsetDependentVmDeviceChangesConfirmation(model));
        chain.execute(this, () -> saveNewVm(model));
    }

    private void saveNewVm(final UnitVmModel model) {
        if (model.getProgress() != null) {
            return;
        }

        model.startProgress();

        VM vm = getcurrentVm();
        if (!StringHelper.isNullOrEmpty(model.getVmId().getEntity())) {
            vm.setId(new Guid(model.getVmId().getEntity()));
        }
        vm.setUseLatestVersion(model.getTemplateWithVersion().getSelectedItem().isLatest());
        AddVmParameters parameters = new AddVmParameters(vm);
        parameters.setDiskInfoDestinationMap(model.getDisksAllocationModel().getImageToDestinationDomainMap());
        parameters.setSeal(model.getIsSealed().getEntity());
        parameters.setConsoleEnabled(model.getIsConsoleDeviceEnabled().getEntity());
        parameters.setCopyTemplatePermissions(model.getCopyPermissions().getEntity());
        parameters.setSoundDeviceEnabled(model.getIsSoundcardEnabled().getEntity());
        parameters.setTpmEnabled(model.getTpmEnabled().getEntity());
        parameters.setVirtioScsiEnabled(model.getIsVirtioScsiEnabled().getEntity());
        parameters.setVmLargeIcon(IconUtils.filterPredefinedIcons(model.getIcon().getEntity().getIcon()));
        parameters.setAffinityGroups(model.getAffinityGroupList().getSelectedItems());
        parameters.setAffinityLabels(model.getLabelList().getSelectedItems());
        setVmWatchdogToParams(model, parameters);
        setRngDeviceToParams(model, parameters);
        if (model.getIsHeadlessModeEnabled().getEntity()) {
            parameters.getVmStaticData().setDefaultDisplayType(DisplayType.none);
        }
        BuilderExecutor.build(model, parameters, new UnitToGraphicsDeviceParamsBuilder());
        if (!StringHelper.isNullOrEmpty(model.getVmId().getEntity())) {
            parameters.setVmId(new Guid(model.getVmId().getEntity()));
        }

        if (model.getIsClone()) {
            parameters.setVmId(Guid.Empty);
        }
        addVm(model, parameters, vm);
    }

    private void addVm(UnitVmModel model, AddVmParameters parameters, VM vm) {
        if (!model.getSelectedCluster().isManaged()) {
            addVmToKubevirt(model, parameters, vm);
            return;
        }
        Frontend.getInstance().runAction(
                model.getProvisioning().getEntity() ? ActionType.AddVmFromTemplate : ActionType.AddVm,
                        parameters,
                        createUnitVmModelNetworkAsyncCallback(vm, model),
                        this);
    }

    private void addVmToKubevirt(UnitVmModel model, AddVmParameters parameters, VM vm) {
        InstanceImagesModel instanceImagesModel = model.getInstanceImages();
        List<DiskVmElement> disksToAttach = new ArrayList<>();
        instanceImagesModel.getItems().forEach(instanceImageLineModel -> {
            AbstractDiskModel diskModel = instanceImageLineModel.getDiskModel().getEntity();
            boolean isAttachDiskModel = diskModel instanceof AttachDiskModel;
            if (!isAttachDiskModel) {
                return;
            }
            DiskVmElement dve = new DiskVmElement(diskModel.getDisk().getId(), vm.getId());
            dve.setBoot(diskModel.getIsBootable().getEntity());
            dve.setDiskInterface(diskModel.getDiskInterface().getSelectedItem());
            dve.setReadOnly(diskModel.isReadOnly());
            disksToAttach.add(dve);
        });
        parameters.setDisksToAttach(disksToAttach);

        Frontend.getInstance()
                .runAction(
                        ActionType.AddVmToKubevirt,
                        parameters,
                        result -> {
                            model.stopProgress();
                            cancel();
                        },
                        this);
    }

    protected void updateVM(UnitVmModel model){
        // no-op by default. Override if needed.
    }

    protected void cloneVM(UnitVmModel model){
        // no-op by default. Override if needed.
    }

    protected void cancel() {
        // no-op by default. Override if needed.
    }

    protected UnitVmModelNetworkAsyncCallback createUnitVmModelNetworkAsyncCallback(VM vm, UnitVmModel model) {
        if (!model.getIsClone() && vm.getVmtGuid().equals(Guid.Empty)) {
            return new UnitVmModelNetworkAsyncCallback(model, addVmFromBlankTemplateNetworkManager) {
                @Override
                public void executed(FrontendActionAsyncResult result) {
                    getWindow().stopProgress();
                    ActionReturnValue returnValue = result.getReturnValue();
                    if (returnValue != null && returnValue.getSucceeded()) {
                        setWindow(null);
                        fireModelChangeRelevantForActionsEvent();
                    } else {
                        cancel();
                    }
                    super.executed(result);
                }
            };
        }

        return new UnitVmModelNetworkAsyncCallback(model, defaultNetworkCreatingManager);
    }

    public static void buildVmOnSave(UnitVmModel model, VM vm) {
        BuilderExecutor.build(model, vm.getStaticData(),
                new FullUnitToVmBaseBuilder());
        BuilderExecutor.build(model, vm, new VmSpecificUnitToVmBuilder());
    }

    protected void setVmWatchdogToParams(final UnitVmModel model, VmManagementParametersBase updateVmParams) {
        VmWatchdogType wdModel = model.getWatchdogModel().getSelectedItem();
        updateVmParams.setUpdateWatchdog(true);
        if (wdModel != null) {
            VmWatchdog vmWatchdog = new VmWatchdog();
            vmWatchdog.setAction(model.getWatchdogAction().getSelectedItem());
            vmWatchdog.setModel(wdModel);
            updateVmParams.setWatchdog(vmWatchdog);
        }
    }

    protected void setRngDeviceToParams(UnitVmModel model, VmManagementParametersBase parameters) {
        parameters.setUpdateRngDevice(true);
        parameters.setRngDevice(model.getIsRngEnabled().getEntity() ? model.generateRngDevice() : null);
    }

    protected abstract String composeEntityOnStorage(String entities);

    protected abstract Iterable<T> asIterableReturnValue(Object returnValue);

    protected abstract boolean entititesEqualsNullSafe(T e1, T e2);

    protected abstract String extractNameFromEntity(T entity);

    protected abstract Guid extractStoragePoolIdNullSafe(T entity);

    protected abstract boolean entitiesSelectedOnDifferentDataCenters();

    protected abstract String entityResideOnSeveralDCsMakeSureTheExportedVMResideOnSameDcMsg();

    protected abstract String thereIsNoExportDomainBackupEntityAttachExportDomainToVmsDcMsg();

    protected abstract QueryType getEntityExportDomain();

    protected abstract void sendWarningForNonExportableDisks(T entity);

    private Model diskModel;

    public void setDiskWindow(Model value) {
        if (diskModel != value) {
            diskModel = value;
            onPropertyChanged(new PropertyChangedEventArgs(DISK_WINDOW));
        }
    }

    public Model getDiskWindow() {
        return diskModel;
    }

    @Override
    public Map<String, Model> getWindowProperties() {
        Map<String, Model> map = super.getWindowProperties();
        map.put(DISK_WINDOW, getDiskWindow());
        return map;
    }

    @Override
    public void setWindowProperty(String propName, Model value) {
        if (DISK_WINDOW.equals(propName)) {
            setDiskWindow(value);
        } else {
            super.setWindowProperty(propName, value);
        }
    }

    protected ConfirmationModelChainItem createConfirmHighPerformanceVm(UnitVmModel model) {
        return new ConfirmationModelChainItem() {

            private VmHighPerformanceConfigurationModel confirmModel;

            @Override
            public void init(Runnable callback) {
                confirmModel = new VmHighPerformanceConfigurationModel();

                if (model.getCpuPinningPolicy().getSelectedItem().getPolicy() == CpuPinningPolicy.NONE) {
                    // Handle CPU Pinning topology
                    final boolean isVmAssignedToSpecificHosts = !model.getIsAutoAssign().getEntity();
                    final boolean isVmCpuPinningSet =
                            model.getCpuPinning().getIsChangable()
                                    && model.getCpuPinning().getEntity() != null
                                    && !model.getCpuPinning().getEntity().isEmpty();
                    confirmModel.addRecommendationForCpuPinning(isVmAssignedToSpecificHosts, isVmCpuPinningSet);

                    // Handle NUMA
                    final boolean isVmVirtNumaSet =
                            model.getNumaEnabled().getEntity() && model.getNumaNodeCount().getEntity() > 0;
                    final boolean isVmVirtNumaPinned =
                            model.getVmNumaNodes() != null
                                    && !model.getVmNumaNodes().isEmpty()
                                    && model.getVmNumaNodes()
                                            .stream()
                                            .filter(x -> !x.getVdsNumaNodeList().isEmpty())
                                            .count() > 0;
                    confirmModel.addRecommendationForVirtNumaSetAndPinned(isVmVirtNumaSet, isVmVirtNumaPinned);
                }

                // Handle Huge Pages
                KeyValueModel keyValue = model.getCustomPropertySheet();
                final boolean isVmHugePagesSet = keyValue != null && keyValue.getUsedKeys().contains("hugepages"); //$NON-NLS-1$
                confirmModel.addRecommendationForHugePages(isVmHugePagesSet);

                // Handle KSM (Kernel Same Page Merging)
                confirmModel.addRecommendationForKsm(model.getSelectedCluster().isEnableKsm(),
                        model.getSelectedCluster().getName());

                confirmModel.setTitle(
                        ConstantsManager.getInstance().getConstants().configurationChangesForHighPerformanceVmTitle());
                confirmModel.setHelpTag(HelpTag.configuration_changes_for_high_performance_vm);
                confirmModel.setHashName("configuration_changes_for_high_performance_vm"); //$NON-NLS-1$
                callback.run();
            }

            @Override
            public boolean isRequired() {
                return getcurrentVm().getVmType() == VmType.HighPerformance &&
                        !confirmModel.getRecommendationsList().isEmpty() &&
                        !model.isVmAttachedToPool();
            }

            @Override
            public ConfirmationModel getConfirmation() {
                return confirmModel;
            }
        };
    }

    protected class TpmDataRemovalConfirmation implements ConfirmationModelChainItem {

        private UnitVmModel model;

        private Guid id;

        private boolean required;

        public TpmDataRemovalConfirmation(UnitVmModel model, Guid id) {
            this.model = model;
            this.id = id;
        }

        @Override
        public void init(Runnable callback) {
            if (isConfirmTpmRequired()) {
                loadTpm(callback);
            } else {
                callback.run();
            }
        }

        private void loadTpm(Runnable callback) {
            Frontend.getInstance()
                    .runQuery(QueryType.HasTpmData,
                            new IdQueryParameters(id),
                            new AsyncQuery<>((AsyncCallback<QueryReturnValue>) returnValue -> {
                                required = returnValue.getReturnValue();
                                callback.run();
                            }));
        }

        private boolean isConfirmTpmRequired() {
            return id != null && model.getTpmOriginallyEnabled()
                    && !model.getTpmEnabled().getEntity();
        }

        @Override
        public boolean isRequired() {
            return required;
        }

        @Override
        public ConfirmationModel getConfirmation() {
            ConfirmationModel confirmModel = new ConfirmationModel();
            confirmModel.setTitle(ConstantsManager.getInstance().getConstants().confirmTpmDataRemovalTitle());
            confirmModel.setMessage(ConstantsManager.getInstance()
                    .getConstants()
                    .confirmTpmDataRemovalMessage());
            return confirmModel;
        }
    }

    protected class NvramDataRemovalConfirmation implements ConfirmationModelChainItem {

        private UnitVmModel model;

        private Guid id;

        private boolean required;

        public NvramDataRemovalConfirmation(UnitVmModel model, Guid id) {
            this.model = model;
            this.id = id;
        }

        @Override
        public void init(Runnable callback) {
            if (isConfirmNvramRequired()) {
                loadNvram(callback);
            } else {
                callback.run();
            }
        }

        private void loadNvram(Runnable callback) {
            Frontend.getInstance()
                    .runQuery(QueryType.HasNvramData,
                            new IdQueryParameters(id),
                            new AsyncQuery<>((AsyncCallback<QueryReturnValue>) returnValue -> {
                                required = returnValue.getReturnValue();
                                callback.run();
                            }));
        }

        private boolean isConfirmNvramRequired() {
            return id != null && model.getSecureBootOriginallyEnabled()
                    && !model.secureBootEnabled();
        }

        @Override
        public boolean isRequired() {
            return required;
        }

        @Override
        public ConfirmationModel getConfirmation() {
            ConfirmationModel confirmModel = new ConfirmationModel();
            confirmModel.setTitle(ConstantsManager.getInstance().getConstants().confirmNvramDataRemovalTitle());
            confirmModel.setMessage(ConstantsManager.getInstance()
                    .getConstants()
                    .confirmNvramDataRemovalMessage());
            return confirmModel;
        }
    }

    class ChipsetDependentVmDeviceChangesConfirmation implements ConfirmationModelChainItem {

        private UnitVmModel model;

        public ChipsetDependentVmDeviceChangesConfirmation(UnitVmModel model) {
            this.model = model;
        }

        @Override
        public boolean isRequired() {
            Cluster cluster = model.getSelectedCluster();

            if (cluster.getArchitecture().getFamily() != ArchitectureType.x86) {
                return false;
            }

            if (model.getTemplateWithVersion().getSelectedItem().getTemplateVersion().getClusterId() == null) {
                return false;
            }

            BiosType templateBiosType =
                    model.getTemplateWithVersion().getSelectedItem().getTemplateVersion().getBiosType();

            return model.getBiosType().getSelectedItem().getChipsetType() != templateBiosType.getChipsetType();
        }

        @Override
        public ConfirmationModel getConfirmation() {
            ConfirmationModel confirmModel = new ConfirmationModel();
            confirmModel.setTitle(
                    ConstantsManager.getInstance().getConstants().chipsetDependentVmDeviceChangesTitle());
            confirmModel.setMessage(
                    ConstantsManager.getInstance().getConstants().chipsetDependentVmDeviceChangesMessage());
            return confirmModel;
        }
    }
}
