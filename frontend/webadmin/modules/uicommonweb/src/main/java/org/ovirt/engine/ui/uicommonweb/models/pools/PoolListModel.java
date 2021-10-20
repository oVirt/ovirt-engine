package org.ovirt.engine.ui.uicommonweb.models.pools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmPoolParameters;
import org.ovirt.engine.core.common.action.VmPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor;
import org.ovirt.engine.ui.uicommonweb.builders.vm.PoolUnitToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.UnitToGraphicsDeviceParamsBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.VmSpecificUnitToVmBuilder;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ListWithSimpleDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchStringMapping;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ExistingPoolModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.NewPoolModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmBasedWidgetSwitchModeCommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmHighPerformanceConfigurationModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.inject.Inject;

public class PoolListModel extends ListWithSimpleDetailsModel<Void, VmPool> {

    private UICommand privateNewCommand;

    private VmPool privateCurrentPool;

    public UICommand getNewCommand() {
        return privateNewCommand;
    }

    private void setNewCommand(UICommand value) {
        privateNewCommand = value;
    }

    private UICommand privateEditCommand;

    @Override
    public UICommand getEditCommand() {
        return privateEditCommand;
    }

    private void setEditCommand(UICommand value) {
        privateEditCommand = value;
    }

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand() {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value) {
        privateRemoveCommand = value;
    }

    private final PoolGeneralModel generalModel;

    public PoolGeneralModel getGeneralModel() {
        return generalModel;
    }

    private final PoolVmListModel vmListModel;

    public PoolVmListModel getVmListModel() {
        return vmListModel;
    }

    private final PermissionListModel<VmPool> permissionListModel;

    public PermissionListModel<VmPool> getPermissionListModel() {
        return permissionListModel;
    }

    @Inject
    public PoolListModel(final PoolGeneralModel poolGeneralModel, final PoolVmListModel poolVmListModel,
            final PermissionListModel<VmPool> permissionListModel) {
        this.generalModel = poolGeneralModel;
        this.vmListModel = poolVmListModel;
        this.permissionListModel = permissionListModel;

        setDetailList();
        setTitle(ConstantsManager.getInstance().getConstants().poolsTitle());
        setApplicationPlace(WebAdminApplicationPlaces.poolMainPlace);

        setDefaultSearchString(SearchStringMapping.POOLS_DEFAULT_SEARCH + ":"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.VDC_POOL_OBJ_NAME, SearchObjects.VDC_POOL_PLU_OBJ_NAME });
        setAvailableInModes(ApplicationMode.VirtOnly);

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        updateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
    }

    private void setDetailList() {
        List<HasEntity<VmPool>> list = new ArrayList<>();
        list.add(generalModel);
        list.add(vmListModel);
        list.add(permissionListModel);
        setDetailModels(list);
    }

    @Override
    public boolean isSearchStringMatch(String searchString) {
        return searchString.trim().toLowerCase().startsWith("pool"); //$NON-NLS-1$
    }

    @Override
    protected void syncSearch() {
        SearchParameters tempVar = new SearchParameters(applySortOptions(getSearchString()), SearchType.VmPools, isCaseSensitiveSearch());
        tempVar.setMaxCount(getSearchPageSize());
        super.syncSearch(QueryType.Search, tempVar);
    }

    @Override
    public boolean supportsServerSideSorting() {
        return true;
    }

    public void newEntity() {
        if (getWindow() != null) {
            return;
        }

        PoolModel model = new PoolModel(new NewPoolModelBehavior());
        model.setIsNew(true);
        model.setCustomPropertiesKeysList(AsyncDataProvider.getInstance().getCustomPropertiesList());
        model.setIsAdvancedModeLocalStorageKey("wa_pool_dialog");  //$NON-NLS-1$
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newPoolTitle());
        model.setHelpTag(HelpTag.new_pool);
        model.setHashName("new_pool"); //$NON-NLS-1$
        model.getVmType().setSelectedItem(VmType.Desktop);
        model.initialize();

        VmBasedWidgetSwitchModeCommand switchModeCommand = new VmBasedWidgetSwitchModeCommand();
        switchModeCommand.init(model);
        model.getCommands().add(switchModeCommand);

        UICommand command = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
        model.getCommands().add(command);

        model.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$
    }

    public void edit() {
        final VmPool pool = getSelectedItem();

        if (getWindow() != null) {
            return;
        }

        final PoolListModel poolListModel = this;

        Frontend.getInstance().runQuery(QueryType.GetVmDataByPoolId,
                new IdQueryParameters(pool.getVmPoolId()),
                new AsyncQuery<QueryReturnValue>(result -> {
                    final VM vm = result.getReturnValue();

                    final ExistingPoolModelBehavior behavior = new ExistingPoolModelBehavior(vm, pool);
                    behavior.getPoolModelBehaviorInitializedEvent().addListener((ev, sender, args) -> {
                        final PoolModel model = behavior.getModel();

                        for (EntityModel<VmPoolType> item : model.getPoolType().getItems()) {
                            if (item.getEntity() == pool.getVmPoolType()) {
                                model.getPoolType().setSelectedItem(item);
                                break;
                            }
                        }
                        String cdImage = null;

                        if (vm != null) {
                            model.getDataCenterWithClustersList().setSelectedItem(null);
                            model.getDataCenterWithClustersList().setSelectedItem(Linq.firstOrNull(model.getDataCenterWithClustersList()
                                    .getItems(),
                                    new Linq.DataCenterWithClusterPredicate(vm.getStoragePoolId(), vm.getClusterId())));

                            model.getTemplateWithVersion().setIsChangeable(false);
                            cdImage = vm.getIsoPath();
                            model.getVmType().setSelectedItem(vm.getVmType());
                        } else {
                            model.getDataCenterWithClustersList()
                                    .setSelectedItem(Linq.firstOrNull(model.getDataCenterWithClustersList().getItems()));
                        }

                        model.getDataCenterWithClustersList().setIsChangeable(vm == null);

                        boolean hasCd = !StringHelper.isNullOrEmpty(cdImage);
                        model.getCdImage().setIsChangeable(hasCd);
                        model.getCdAttached().setEntity(hasCd);

                        model.getProvisioning().setIsChangeable(false);
                        model.getStorageDomain().setIsChangeable(false);
                    });

                    PoolModel model = new PoolModel(behavior);
                    model.setCustomPropertiesKeysList(AsyncDataProvider.getInstance().getCustomPropertiesList());
                    model.setIsAdvancedModeLocalStorageKey("wa_pool_dialog");  //$NON-NLS-1$
                    setWindow(model);

                    VmBasedWidgetSwitchModeCommand switchModeCommand = new VmBasedWidgetSwitchModeCommand();
                    switchModeCommand.init(model);
                    model.getCommands().add(switchModeCommand);

                    UICommand command = UICommand.createDefaultOkUiCommand("OnSave", poolListModel); //$NON-NLS-1$
                    model.getCommands().add(command);

                    model.getCommands().add(UICommand.createCancelUiCommand("Cancel", poolListModel)); //$NON-NLS-1$

                    model.setTitle(ConstantsManager.getInstance().getConstants().editPoolTitle());
                    model.setHelpTag(HelpTag.edit_pool);
                    model.setHashName("edit_pool"); //$NON-NLS-1$
                    model.initialize();
                    model.getName().setEntity(pool.getName());
                    model.getDescription().setEntity(pool.getVmPoolDescription());
                    model.getComment().setEntity(pool.getComment());
                    model.getPoolStateful().setEntity(pool.isStateful());
                    model.getAssignedVms().setEntity(pool.getAssignedVmsCount());
                    model.getPrestartedVms().setEntity(pool.getPrestartedVms());
                    model.setPrestartedVmsHint("0-" + pool.getAssignedVmsCount()); //$NON-NLS-1$
                    model.getMaxAssignedVmsPerUser().setEntity(pool.getMaxAssignedVmsPerUser());

                }));
    }

    private List<StoragePool> asList(Object returnValue) {
        if (returnValue instanceof ArrayList) {
            return (ArrayList<StoragePool>) returnValue;
        }

        if (returnValue instanceof StoragePool) {
            List<StoragePool> res = new ArrayList<>();
            res.add((StoragePool) returnValue);
            return res;
        }

        throw new IllegalArgumentException("Expected ArrayList of storage_pools or a storage_pool. Given " + returnValue.getClass().getName()); //$NON-NLS-1$
    }

    public void remove() {
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removePoolsTitle());
        model.setHelpTag(HelpTag.remove_pool);
        model.setHashName("remove_pool"); //$NON-NLS-1$

        ArrayList<String> list = new ArrayList<>();
        for (VmPool item : getSelectedItems()) {
            list.add(item.getName());
        }
        model.setItems(list);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public void onRemove() {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            VmPool pool = (VmPool) item;
            list.add(new VmPoolParametersBase(pool.getVmPoolId()));
        }

        model.startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.RemoveVmPool, list,
                result -> {
                    ConfirmationModel localModel = (ConfirmationModel) result.getState();
                    localModel.stopProgress();
                    cancel();
                }, model);
    }

    public void onSave() {
        final PoolModel model = (PoolModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        if (!model.getIsNew() && getSelectedItem() == null) {
            cancel();
            return;
        }

        if (!model.validate()) {
            return;
        }

        setCurrentPool(model.getIsNew() ? new VmPool() : (VmPool) Cloner.clone(getSelectedItem()));
        final String name = model.getName().getEntity();

        // Check name unicitate.
        AsyncDataProvider.getInstance().isPoolNameUnique(new AsyncQuery<>(
                        isUnique -> {
                            if ((model.getIsNew() && !isUnique)
                                    || (!model.getIsNew() && !isUnique
                                    && name.compareToIgnoreCase(getCurrentPool().getName()) != 0)) {
                                model.getName()
                                        .getInvalidityReasons()
                                        .add(ConstantsManager.getInstance()
                                                .getConstants()
                                                .nameMustBeUniqueInvalidReason());
                                model.getName().setIsValid(false);
                                model.setValidTab(TabName.GENERAL_TAB, false);
                                model.fireValidationCompleteEvent();
                                return;
                            }
                            String selectedCpu = model.getCustomCpu().getSelectedItem();
                            if (selectedCpu != null && !selectedCpu.isEmpty() && !model.getCustomCpu()
                                    .getItems()
                                    .contains(selectedCpu)) {
                                ConfirmationModel confirmModel = new ConfirmationModel();
                                confirmModel.setTitle(ConstantsManager.getInstance()
                                        .getConstants()
                                        .vmUnsupportedCpuTitle());
                                confirmModel.setMessage(ConstantsManager.getInstance()
                                        .getConstants()
                                        .vmUnsupportedCpuMessage());
                                confirmModel.setHelpTag(HelpTag.edit_unsupported_cpu);
                                confirmModel.setHashName("edit_unsupported_cpu"); //$NON-NLS-1$

                                confirmModel.getCommands()
                                        .add(new UICommand("OnSave_Phase2", PoolListModel.this) //$NON-NLS-1$
                                                .setTitle(ConstantsManager.getInstance().getConstants().ok())
                                                .setIsDefault(true));

                                confirmModel.getCommands()
                                        .add(UICommand.createCancelUiCommand("CancelConfirmation", //$NON-NLS-1$
                                                PoolListModel.this));

                                setConfirmWindow(confirmModel);
                            } else {
                                if (model.getVmType().getSelectedItem() == VmType.HighPerformance) {
                                    displayHighPerformanceConfirmationPopup();
                                } else {
                                    savePoolPostValidation();
                                }
                            }
                        }),
                name);
    }

    public void savePoolPostValidation() {

        final PoolModel model = (PoolModel) getWindow();

        VmPool pool = getCurrentPool();

        setConfirmWindow(null);

        // Save changes.
        pool.setName(model.getName().getEntity());
        pool.setVmPoolDescription(model.getDescription().getEntity());
        pool.setClusterId(model.getSelectedCluster().getId());
        pool.setComment(model.getComment().getEntity());
        pool.setStateful(model.getPoolStateful().getEntity());
        pool.setPrestartedVms(model.getPrestartedVms().getEntity());
        pool.setMaxAssignedVmsPerUser(model.getMaxAssignedVmsPerUser().getEntity());
        pool.setAutoStorageSelect(model.getDisksAllocationModel().getDiskAllocationTargetEnabled().getEntity());

        EntityModel<VmPoolType> poolTypeSelectedItem = model.getPoolType().getSelectedItem();
        pool.setVmPoolType(poolTypeSelectedItem.getEntity());

        if (model.getSpiceProxyEnabled().getEntity()) {
            pool.setSpiceProxy(model.getSpiceProxy().getEntity());
        }

        VM vm = buildVmOnSave(model);
        vm.setVmInit(model.getVmInitModel().buildCloudInitParameters(model));

        vm.setUseLatestVersion(model.getTemplateWithVersion().getSelectedItem().isLatest());
        vm.setStateless(false);
        vm.setInstanceTypeId(model.getInstanceTypes().getSelectedItem().getId());

        AddVmPoolParameters param =
                new AddVmPoolParameters(pool, vm, model.getNumOfDesktops().getEntity());

        param.setStorageDomainId(Guid.Empty);
        param.setDiskInfoDestinationMap(model.getDisksAllocationModel()
                .getImageToDestinationDomainMap());
        param.setConsoleEnabled(model.getIsConsoleDeviceEnabled().getEntity());
        param.setVirtioScsiEnabled(model.getIsVirtioScsiEnabled().getEntity());
        param.setSeal(model.getIsSealed().getEntity());

        param.setSoundDeviceEnabled(model.getIsSoundcardEnabled().getEntity());
        param.setTpmEnabled(model.getTpmEnabled().getEntity());
        param.setRngDevice(model.getIsRngEnabled().getEntity() ? model.generateRngDevice() : null);

        if(model.getIsHeadlessModeEnabled().getEntity()) {
            param.getVmStaticData().setDefaultDisplayType(DisplayType.none);
       }
        BuilderExecutor.build(model, param, new UnitToGraphicsDeviceParamsBuilder());

        param.getVmStaticData().setUseHostCpuFlags(model.getHostCpu().getEntity());
        param.getVmStaticData().setCpuPinning(model.getCpuPinning().getEntity());

        if (model.getQuota().getSelectedItem() != null) {
            vm.setQuotaId(model.getQuota().getSelectedItem().getId());
        }

        model.startProgress();

        if (model.getIsNew()) {
            if (model.getIcon().getEntity().isCustom()) {
                param.setVmLargeIcon(model.getIcon().getEntity().getIcon());
            }
            // Although only one action is invoked by this call, runMultipleAction() is necessary here.
            // In contrast to runAction(), runMultipleAction() allows to call the callback immediately
            // after the command is started, not waiting till it finishes.
            Frontend.getInstance().runMultipleAction(ActionType.AddVmPool,
                    new ArrayList<>(Collections.singletonList(param)),
                    result -> {
                        cancel();
                        stopProgress();
                    },
                    this);
        } else {
            Frontend.getInstance().runMultipleAction(ActionType.UpdateVmPool,
                    new ArrayList<>(Collections.singletonList(param)),
                    result -> {
                        cancel();
                        stopProgress();
                    },
                    this);
        }
    }

    protected static VM buildVmOnSave(PoolModel model) {
        VM vm = new VM();
        BuilderExecutor.build(model, vm.getStaticData(), new PoolUnitToVmBaseBuilder());
        BuilderExecutor.build(model, vm, new VmSpecificUnitToVmBuilder());
        return vm;
    }

    public void cancel() {
        setWindow(null);
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.selectedItemPropertyChanged(sender, e);
        updateActionAvailability();
    }

    private void updateActionAvailability() {
        getEditCommand().setIsExecutionAllowed(getSelectedItem() != null && getSelectedItems() != null
                && getSelectedItems().size() == 1 && hasVms(getSelectedItem()));

        getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0);
    }

    private boolean hasVms(Object selectedItem) {
        if (selectedItem instanceof VmPool) {
            return ((VmPool) selectedItem).getAssignedVmsCount() != 0;
        }
        return false;
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewCommand()) {
            newEntity();
        }
        if (command == getEditCommand()) {
            edit();
        }
        if (command == getRemoveCommand()) {
            remove();
        }
        if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
        if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            onSave();
        }
        if ("OnSave_Phase2".equals(command.getName())) { //$NON-NLS-1$
            savePoolPostValidation();
        }
        if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        }
        if ("CancelConfirmation".equals(command.getName())) { //$NON-NLS-1$
            cancelConfirmation();
        }
    }

    @Override
    protected String getListName() {
        return "PoolListModel"; //$NON-NLS-1$
    }

    public VmPool getCurrentPool() {
        return privateCurrentPool;
    }

    public void setCurrentPool(VmPool privateCurrentPool) {
        this.privateCurrentPool = privateCurrentPool;
    }

    private void cancelConfirmation() {
        setConfirmWindow(null);
    }

    protected void displayHighPerformanceConfirmationPopup() {
        final PoolModel model = (PoolModel) getWindow();

        if (model == null || model.getProgress() != null) {
            return;
        }

        VmHighPerformanceConfigurationModel confirmModel = new VmHighPerformanceConfigurationModel();

        // Handle CPU Pinning topology
        final boolean isVmAssignedToSpecificHosts = !model.getIsAutoAssign().getEntity();
        final boolean isVmCpuPinningSet = model.getCpuPinning().getIsChangable()
                && model.getCpuPinning().getEntity() != null && !model.getCpuPinning().getEntity().isEmpty();
        confirmModel.addRecommendationForCpuPinning(isVmAssignedToSpecificHosts, isVmCpuPinningSet);

        // Handle Huge Pages
        KeyValueModel keyValue = model.getCustomPropertySheet();
        final boolean isVmHugePagesSet = keyValue != null && keyValue.getUsedKeys().contains("hugepages"); //$NON-NLS-1$
        confirmModel.addRecommendationForHugePages(isVmHugePagesSet);

        // Handle KSM (Kernel Same Page Merging)
        confirmModel.addRecommendationForKsm(model.getSelectedCluster().isEnableKsm(), model.getSelectedCluster().getName());

        if (!confirmModel.getRecommendationsList().isEmpty()) {
            confirmModel.setTitle(ConstantsManager.getInstance()
                    .getConstants()
                    .configurationChangesForHighPerformancePoolTitle());
            confirmModel.setHelpTag(HelpTag.configuration_changes_for_high_performance_pool);
            confirmModel.setHashName("configuration_changes_for_high_performance_pool"); //$NON-NLS-1$

            confirmModel.getCommands().add(new UICommand("OnSave_Phase2", PoolListModel.this) //$NON-NLS-1$
                    .setTitle(ConstantsManager.getInstance().getConstants().ok())
                    .setIsDefault(true));

            confirmModel.getCommands()
                    .add(UICommand.createCancelUiCommand("CancelConfirmation", PoolListModel.this)); //$NON-NLS-1$

            setConfirmWindow(null);
            setConfirmWindow(confirmModel);
        } else {
            savePoolPostValidation();
        }
    }
}
