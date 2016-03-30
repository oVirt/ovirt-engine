package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.action.ChangeVMClusterParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmCommonUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister.ConsoleContext;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor;
import org.ovirt.engine.ui.uicommonweb.builders.vm.CommonUnitToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.CpuSharesVmBaseToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.DedicatedVmForVdsVmBaseToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.FullUnitToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.KernelParamsVmBaseToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.UnitToGraphicsDeviceParamsBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.UsbPolicyVmBaseToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.VmIconUnitAndVmToParameterBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.VmSpecificUnitToVmBuilder;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.ConsolesFactory;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.configure.UserPortalPermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.CloneVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DataCenterWithCluster;
import org.ovirt.engine.ui.uicommonweb.models.vms.RunOnceModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModelNetworkAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.vms.UserPortalExistingVmModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.UserPortalNewTemplateVmModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.UserPortalNewVmModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.UserPortalRunOnceModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UserPortalVmEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UserPortalVmSnapshotListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmAppListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmBasedWidgetSwitchModeCommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGuestContainerListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGuestInfoModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceCreatingManager;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmMonitorModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmNextRunConfigurationModel;
import org.ovirt.engine.ui.uicommonweb.place.UserPortalApplicationPlaces;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;

import com.google.inject.Inject;

public class UserPortalListModel extends AbstractUserPortalListModel {
    private final UIConstants constants = ConstantsManager.getInstance().getConstants();
    public static final EventDefinition searchCompletedEventDefinition;
    private Event<EventArgs> privateSearchCompletedEvent;

    /** The edited VM could be different than the selected VM in the grid
     *  when the VM has next-run configuration */
    private VM editedVm;

    VmInterfaceCreatingManager defaultNetworkCreatingManager = new VmInterfaceCreatingManager(new VmInterfaceCreatingManager.PostVnicCreatedCallback() {
        @Override
        public void vnicCreated(Guid vmId, UnitVmModel unitVmModel) {
            if (getWindow() != null) {
                getWindow().stopProgress();
            }
            cancel();
            updateActionAvailability();
            executeDiskModifications(vmId, unitVmModel);
        }

        @Override
        public void queryFailed() {
            stopProgress(UserPortalListModel.this);
            cancel();
        }
    });

    @Override
    public Event<EventArgs> getSearchCompletedEvent() {
        return privateSearchCompletedEvent;
    }

    private void setSearchCompletedEvent(Event<EventArgs> value) {
        privateSearchCompletedEvent = value;
    }

    private UICommand cloneVmCommand;

    public UICommand getCloneVmCommand() {
        return cloneVmCommand;
    }

    public void setCloneVmCommand(UICommand cloneVmCommand) {
        this.cloneVmCommand = cloneVmCommand;
    }

    private UICommand privateNewVmCommand;

    public UICommand getNewVmCommand() {
        return privateNewVmCommand;
    }

    private void setNewVmCommand(UICommand value) {
        privateNewVmCommand = value;
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

    private UICommand privateSaveCommand;

    public UICommand getSaveCommand() {
        return privateSaveCommand;
    }

    private void setSaveCommand(UICommand value) {
        privateSaveCommand = value;
    }

    private UICommand privateRunOnceCommand;

    public UICommand getRunOnceCommand() {
        return privateRunOnceCommand;
    }

    private void setRunOnceCommand(UICommand value) {
        privateRunOnceCommand = value;
    }

    private UICommand privateChangeCdCommand;

    public UICommand getChangeCdCommand() {
        return privateChangeCdCommand;
    }

    private void setChangeCdCommand(UICommand value) {
        privateChangeCdCommand = value;
    }

    private UICommand privateNewTemplateCommand;

    public UICommand getNewTemplateCommand() {
        return privateNewTemplateCommand;
    }

    private void setNewTemplateCommand(UICommand value) {
        privateNewTemplateCommand = value;
    }

    private final VmGeneralModel vmGeneralModel;
    private final VmGuestInfoModel vmGuestInfoModel;
    private final UserPortalVmSnapshotListModel vmSnapshotListModel;
    private final VmMonitorModel vmMonitorModel;
    private final VmDiskListModel vmDiskListModel;
    private final VmInterfaceListModel vmInterfaceListModel;
    private final UserPortalPermissionListModel permissionListModel;
    private final UserPortalVmEventListModel vmEventListModel;
    private final VmAppListModel<VM> vmAppListModel;
    private final PoolGeneralModel poolGeneralModel;
    private final PoolDiskListModel poolDiskListModel;
    private final PoolInterfaceListModel poolInterfaceListModel;
    private final VmGuestContainerListModel vmGuestContainerListModel;

    private VM privatetempVm;

    public VM gettempVm() {
        return privatetempVm;
    }

    public void settempVm(VM value) {
        privatetempVm = value;
    }

    private StorageDomain privatestorageDomain;

    public StorageDomain getstorageDomain() {
        return privatestorageDomain;
    }

    public void setstorageDomain(StorageDomain value) {
        privatestorageDomain = value;
    }

    static {
        searchCompletedEventDefinition = new EventDefinition("SearchCompleted", UserPortalListModel.class); //$NON-NLS-1$
    }

    @Inject
    public UserPortalListModel(final VmGeneralModel vmGeneralModel, final VmGuestInfoModel vmGuestInfoModel,
            final UserPortalVmSnapshotListModel userPortalVmSnapshotListModel, final VmMonitorModel vmMonitorModel,
            final VmDiskListModel vmDiskListModel, final VmInterfaceListModel vmInterfaceListModel,
            final UserPortalPermissionListModel userPortalPermissionListModel,
            final UserPortalVmEventListModel userPortalVmEventListModel, final VmAppListModel<VM> vmAppListModel,
            final PoolGeneralModel poolGeneralModel, final PoolDiskListModel poolDiskListModel,
            final PoolInterfaceListModel poolInterfaceListModel, VmGuestContainerListModel vmVmGuestContainerListModel) {
        this.vmGeneralModel = vmGeneralModel;
        this.vmGuestInfoModel = vmGuestInfoModel;
        this.vmSnapshotListModel = userPortalVmSnapshotListModel;
        this.vmMonitorModel = vmMonitorModel;
        this.vmDiskListModel = vmDiskListModel;
        this.vmInterfaceListModel = vmInterfaceListModel;
        this.permissionListModel = userPortalPermissionListModel;
        this.vmEventListModel = userPortalVmEventListModel;
        this.vmAppListModel = vmAppListModel;
        this.poolGeneralModel = poolGeneralModel;
        this.poolDiskListModel = poolDiskListModel;
        this.poolInterfaceListModel = poolInterfaceListModel;
        this.vmGuestContainerListModel = vmVmGuestContainerListModel;
        setDetailList();

        setApplicationPlace(UserPortalApplicationPlaces.extendedVirtualMachineSideTabPlace);
        setSearchCompletedEvent(new Event<>(searchCompletedEventDefinition));

        setNewVmCommand(new UICommand("NewVm", this)); //$NON-NLS-1$
        setCloneVmCommand(new UICommand("CloneVm", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setSaveCommand(new UICommand("Save", this)); //$NON-NLS-1$
        setRunOnceCommand(new UICommand("RunOnce", this)); //$NON-NLS-1$
        setChangeCdCommand(new UICommand("ChangeCD", this)); //$NON-NLS-1$
        setNewTemplateCommand(new UICommand("NewTemplate", this)); //$NON-NLS-1$
        setTitle(ConstantsManager.getInstance().getConstants().virtualMachinesTitle());

        updateActionAvailability();

        consolesFactory = new ConsolesFactory(ConsoleContext.UP_EXTENDED, this);
    }

    private void setDetailList() {
        vmGeneralModel.setIsAvailable(false);
        vmSnapshotListModel.setIsAvailable(false);
        vmMonitorModel.setIsAvailable(false);
        vmDiskListModel.setIsAvailable(false);
        vmInterfaceListModel.setIsAvailable(false);
        poolGeneralModel.setIsAvailable(false);
        poolDiskListModel.setIsAvailable(false);
        poolInterfaceListModel.setIsAvailable(false);
        permissionListModel.setIsAvailable(true);
        vmEventListModel.setIsAvailable(true);
        vmAppListModel.setIsAvailable(true);
        vmGuestInfoModel.setIsAvailable(true);
        vmGuestContainerListModel.setIsAvailable(true);

        List<HasEntity<? /* extends VmOrPool */>> list = new ArrayList<>();
        list.add(vmGeneralModel);
        list.add(poolGeneralModel);
        list.add(vmInterfaceListModel);
        list.add(poolInterfaceListModel);
        list.add(vmDiskListModel);
        list.add(poolDiskListModel);
        list.add(vmSnapshotListModel);
        list.add(permissionListModel);
        list.add(vmEventListModel);
        list.add(vmAppListModel);
        list.add(vmMonitorModel);
        list.add(vmGuestInfoModel);
        list.add(vmGuestContainerListModel);

        setDetailModels((List) list);
    }

    @Override
    public void setItems(Collection value) {
        if (items != value) {
            itemsChanging(value, items);
            items = value;
            getItemsChangedEvent().raise(this, EventArgs.EMPTY);
            onPropertyChanged(new PropertyChangedEventArgs("Items")); //$NON-NLS-1$

            UserPortalItemModel selectedItem = getSelectedItem();
            if (items != null && selectedItem != null) {
                for (Object object : items) {
                    UserPortalItemModel itemModel = (UserPortalItemModel) object;
                    if (itemModel.getEntity().equals(selectedItem.getEntity())) {
                        this.selectedItem = itemModel;
                        break;
                    }
                }
            }
            onSelectedItemChanged();
        }
    }

    @Override
    protected void syncSearch() {
        super.syncSearch();
        VdcQueryParametersBase queryParameters = new VdcQueryParametersBase();
        queryParameters.setRefresh(getIsQueryFirstTime());
        Frontend.getInstance().runQuery(VdcQueryType.GetAllVmsAndVmPools, queryParameters,
                new AsyncQuery(this, new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        ArrayList<VM> vms = new ArrayList<>();
                        ArrayList<VmPool> pools = new ArrayList<>();

                        VdcQueryReturnValue retValue = (VdcQueryReturnValue) returnValue;
                        if (retValue != null && retValue.getSucceeded()) {
                            List<Object> list = (ArrayList<Object>) retValue.getReturnValue();
                            if (list != null) {
                                for (Object object : list) {
                                    if (object instanceof VM) {
                                        vms.add((VM) object);
                                    } else if (object instanceof VmPool) {
                                        pools.add((VmPool) object);
                                    }
                                }
                            }
                        }

                        onVmAndPoolLoad(vms, pools);
                    }
                }));
    }

    @Override
    protected void updateDetailsAvailability() {
        super.updateDetailsAvailability();

        UserPortalItemModel item = getSelectedItem();

        vmGeneralModel.setIsAvailable(item != null && !item.isPool());
        vmSnapshotListModel.setIsAvailable(item != null && !item.isPool());
        vmMonitorModel.setIsAvailable(item != null && !item.isPool());
        vmDiskListModel.setIsAvailable(item != null && !item.isPool());
        vmInterfaceListModel.setIsAvailable(item != null && !item.isPool());
        vmEventListModel.setIsAvailable(item != null && !item.isPool());

        poolGeneralModel.setIsAvailable(item != null && item.isPool());
        poolDiskListModel.setIsAvailable(item != null && item.isPool());
        poolInterfaceListModel.setIsAvailable(item != null && item.isPool());
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewVmCommand()) {
            newInternal();
        }
        else if (command == getCloneVmCommand()) {
            cloneVm();
        }
        else if (command == getEditCommand()) {
            edit();
        }
        else if (command == getRemoveCommand()) {
            remove();
        }
        else if (command == getSaveCommand()) {
            onSave();
        }
        else if (command == getRunOnceCommand()) {
            runOnce();
        }
        else if (command == getChangeCdCommand()) {
            changeCD();
        }
        else if (command == getNewTemplateCommand()) {
            newTemplate();
        }
        else if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        }
        else if ("OnRunOnce".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
        else if ("OnChangeCD".equals(command.getName())) { //$NON-NLS-1$
            onChangeCD();
        }
        else if ("OnNewTemplate".equals(command.getName())) { //$NON-NLS-1$
            onNewTemplate();
        }
        else if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            onSave();
        }
        else if (command.getName().equals("closeVncInfo")) { //$NON-NLS-1$
            setWindow(null);
        }
        else if ("OnClone".equals(command.getName())) { //$NON-NLS-1$
            onClone();
        }
        else if ("CancelConfirmation".equals(command.getName())) { //$NON-NLS-1$
            stopProgress(UserPortalListModel.this);
            setConfirmWindow(null);
        }
        else if ("updateExistingVm".equals(command.getName())) { // $NON-NLS-1$
            VmNextRunConfigurationModel model = (VmNextRunConfigurationModel) getConfirmWindow();
            updateExistingVm(UserPortalListModel.this, model.getApplyCpuLater().getEntity());
            setConfirmWindow(null);
        }
        else if ("postVmNameUniqueCheck".equals(command.getName())) { // $NON-NLS-1$
            postVmNameUniqueCheck();
            setConfirmWindow(null);
        }
    }

    private void cloneVm() {
        final UserPortalItemModel vm = getSelectedItem();
        if (vm == null) {
            return;
        }

        CloneVmModel model = new CloneVmModel(vm.getVM(), constants);
        setWindow(model);

        model.initialize();
        model.setTitle(ConstantsManager.getInstance().getConstants().cloneVmTitle());

        model.setHelpTag(HelpTag.clone_vm);
        model.setHashName("clone_vm"); //$NON-NLS-1$

        UICommand okCommand = UICommand.createDefaultOkUiCommand("OnClone", this); //$NON-NLS-1$
        model.getCommands().add(okCommand);
        UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(cancelCommand);
    }

    private void onClone() {
        ((CloneVmModel) getWindow()).onClone(this, true);
    }

    private void newTemplate() {
        UserPortalItemModel selectedItem = getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        VM vm = (VM) selectedItem.getEntity();
        UnitVmModel windowModel = new UnitVmModel(new UserPortalNewTemplateVmModelBehavior(vm), this);
        setWindow(windowModel);
        windowModel.setTitle(ConstantsManager.getInstance().getConstants().newTemplateTitle());
        windowModel.setHelpTag(HelpTag.new_template);
        windowModel.setHashName("new_template"); //$NON-NLS-1$
        windowModel.setIsNew(true);
        windowModel.getVmType().setSelectedItem(vm.getVmType());
        windowModel.initialize(null);
        windowModel.getIsTemplatePublic().setEntity(false);

        windowModel.getCommands().add(
                UICommand.createDefaultOkUiCommand("OnNewTemplate", this)); //$NON-NLS-1$)

        windowModel.getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$

        windowModel.setCustomPropertiesKeysList(AsyncDataProvider.getInstance().getCustomPropertiesList());
    }

    private void onNewTemplate() {
        UserPortalItemModel selectedItem = getSelectedItem();
        if (selectedItem == null) {
            cancel();
            return;
        }

        UnitVmModel model = (UnitVmModel) getWindow();

        if (!model.validate(false)) {
            model.setIsValid(false);
        }
        else if (model.getIsSubTemplate().getEntity()) {
            postNameUniqueCheck(this);
        }
        else {
            model.startProgress();
            String name = model.getName().getEntity();

            // Check name unicitate.
            AsyncDataProvider.getInstance().isTemplateNameUnique(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {

                            UserPortalListModel userPortalListModel = (UserPortalListModel) target;
                            boolean isNameUnique = (Boolean) returnValue;
                            if (!isNameUnique) {

                                UnitVmModel vmModel = (UnitVmModel) userPortalListModel.getWindow();
                                vmModel.getName().getInvalidityReasons().clear();
                                vmModel
                                        .getName()
                                        .getInvalidityReasons()
                                        .add(ConstantsManager.getInstance()
                                                .getConstants()
                                                .nameMustBeUniqueInvalidReason());
                                vmModel.getName().setIsValid(false);
                                vmModel.setIsValid(false);
                                vmModel.fireValidationCompleteEvent();
                                stopProgress(target);
                            }
                            else {
                                userPortalListModel.postNameUniqueCheck(userPortalListModel);
                            }

                        }
                    }),
                    name, model.getSelectedDataCenter() == null ? null : model.getSelectedDataCenter().getId());
        }
    }

    private void postNameUniqueCheck(UserPortalListModel userPortalListModel) {
        UnitVmModel model = (UnitVmModel) userPortalListModel.getWindow();
        UserPortalItemModel selectedItem = userPortalListModel.getSelectedItem();
        VM vm = (VM) selectedItem.getEntity();

        VM newVm = buildVmOnNewTemplate(model, vm);

        newVm.setMigrationDowntime(vm.getMigrationDowntime());

        AddVmTemplateParameters addVmTemplateParameters =
                new AddVmTemplateParameters(newVm,
                        model.getName().getEntity(),
                        model.getDescription().getEntity());

        addVmTemplateParameters.setPublicUse(model.getIsTemplatePublic().getEntity());

        addVmTemplateParameters.setDiskInfoDestinationMap(model.getDisksAllocationModel()
                .getImageToDestinationDomainMap());
        addVmTemplateParameters.setSoundDeviceEnabled(model.getIsSoundcardEnabled().getEntity());
        addVmTemplateParameters.setConsoleEnabled(model.getIsConsoleDeviceEnabled().getEntity());
        addVmTemplateParameters.setCopyVmPermissions(model.getCopyPermissions().getEntity());
        addVmTemplateParameters.setUpdateRngDevice(true);
        addVmTemplateParameters.setRngDevice(model.getIsRngEnabled().getEntity() ? model.generateRngDevice() : null);
        BuilderExecutor.build(model, addVmTemplateParameters, new UnitToGraphicsDeviceParamsBuilder());
        if (model.getIsSubTemplate().getEntity()) {
            addVmTemplateParameters.setBaseTemplateId(model.getBaseTemplate().getSelectedItem().getId());
            addVmTemplateParameters.setTemplateVersionName(model.getTemplateVersionName().getEntity());
        }

        Frontend.getInstance().runAction(VdcActionType.AddVmTemplate, addVmTemplateParameters,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        stopProgress(result.getState());
                        cancel();
                    }
                }, this);
    }

    protected static VM buildVmOnNewTemplate(UnitVmModel model, VM vm) {
        VM tempVar = new VM();
        tempVar.setId(vm.getId());

        BuilderExecutor.build(model, tempVar.getStaticData(), new CommonUnitToVmBaseBuilder<VmStatic>());
        BuilderExecutor.build(vm.getStaticData(), tempVar.getStaticData(),
                new KernelParamsVmBaseToVmBaseBuilder(),
                new UsbPolicyVmBaseToVmBaseBuilder(),
                new CpuSharesVmBaseToVmBaseBuilder(),
                new DedicatedVmForVdsVmBaseToVmBaseBuilder());
        return tempVar;
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    private void runOnce() {
        UserPortalItemModel selectedItem = getSelectedItem();
        if (selectedItem == null || selectedItem.getEntity() == null) {
            return;
        }

        VM vm = (VM) selectedItem.getEntity();

        // populating VMInit
        AsyncQuery getVmInitQuery = new AsyncQuery();
        getVmInitQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                RunOnceModel runOnceModel = new UserPortalRunOnceModel((VM) result,
                        UserPortalListModel.this);
                setWindow(runOnceModel);
                runOnceModel.init();
            }
        };

        AsyncDataProvider.getInstance().getVmById(getVmInitQuery, vm.getId());
    }

    private void updateActionAvailability() {
        UserPortalItemModel selectedItem = getSelectedItem();

        getEditCommand().setIsExecutionAllowed(selectedItem != null && !selectedItem.isPool());

        getRemoveCommand().setIsExecutionAllowed(selectedItem != null
                && !selectedItem.isPool()
                && VdcActionUtils.canExecute(new ArrayList<>(Arrays.asList(new VM[]{(VM) selectedItem.getEntity()})),
                VM.class,
                VdcActionType.RemoveVm));

        getRunOnceCommand().setIsExecutionAllowed(selectedItem != null
                && !selectedItem.isPool()
                && VdcActionUtils.canExecute(new ArrayList<>(Arrays.asList(new VM[]{(VM) selectedItem.getEntity()})),
                VM.class,
                VdcActionType.RunVmOnce));

        getCloneVmCommand().setIsExecutionAllowed(selectedItem != null
                && !selectedItem.isPool()
                && VdcActionUtils.canExecute(new ArrayList<>(Arrays.asList(new VM[]{(VM) selectedItem.getEntity()})),
                VM.class,
                VdcActionType.CloneVm));

        getChangeCdCommand().setIsExecutionAllowed(selectedItem != null
                && !selectedItem.isPool()
                && VdcActionUtils.canExecute(new ArrayList<>(Arrays.asList(new VM[]{
                (VM) selectedItem.getEntity()})),
                VM.class,
                VdcActionType.ChangeDisk));

        getNewTemplateCommand().setIsExecutionAllowed(selectedItem != null
                && !selectedItem.isPool()
                && VdcActionUtils.canExecute(new ArrayList<>(Arrays.asList(new VM[]{
                (VM) selectedItem.getEntity()})),
                VM.class,
                VdcActionType.AddVmTemplate));
    }

    private void newInternal() {
        UnitVmModel model = new UnitVmModel(new UserPortalNewVmModelBehavior(), this);
        model.getVmType().setSelectedItem(VmType.Server);
        model.setTitle(ConstantsManager.getInstance()
                .getConstants().newVmTitle());
        model.setHelpTag(HelpTag.new_vm);
        model.setHashName("new_vm"); //$NON-NLS-1$
        model.setIsNew(true);
        model.setCustomPropertiesKeysList(AsyncDataProvider.getInstance().getCustomPropertiesList());
        model.setIsAdvancedModeLocalStorageKey("up_vm_dialog");  //$NON-NLS-1$
        setWindow(model);

        model.initialize(null);

        VmBasedWidgetSwitchModeCommand switchModeCommand = new VmBasedWidgetSwitchModeCommand();
        switchModeCommand.init(model);
        model.getCommands().add(switchModeCommand);

        model.getProvisioning().setEntity(true);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    private void edit() {
        UserPortalItemModel selectedItem = getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        if (getWindow() != null) {
            return;
        }

        VM vm = (VM) selectedItem.getEntity();

        // populating VMInit
        AsyncQuery getVmInitQuery = new AsyncQuery();
        getVmInitQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                editedVm = (VM) result;
                vmInitLoaded(editedVm);
            }
        };
        if (vm.isNextRunConfigurationExists()) {
            AsyncDataProvider.getInstance().getVmNextRunConfiguration(getVmInitQuery, vm.getId());
        } else {
            AsyncDataProvider.getInstance().getVmById(getVmInitQuery, vm.getId());
        }
    }

    private void vmInitLoaded(VM vm) {
        UnitVmModel model = new UnitVmModel(new UserPortalExistingVmModelBehavior(vm), this);

        model.setTitle(ConstantsManager.getInstance()
                .getConstants().editVmTitle());
        model.setHelpTag(HelpTag.edit_vm);
        model.setHashName("edit_vm"); //$NON-NLS-1$
        model.getVmType().setSelectedItem(vm.getVmType());
        model.setCustomPropertiesKeysList(AsyncDataProvider.getInstance().getCustomPropertiesList());
        model.setIsAdvancedModeLocalStorageKey("up_vm_dialog");  //$NON-NLS-1$

        setWindow(model);

        model.initialize(null);

        VmBasedWidgetSwitchModeCommand switchModeCommand = new VmBasedWidgetSwitchModeCommand();
        switchModeCommand.init(model);
        model.getCommands().add(switchModeCommand);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    private void remove() {
        if (getConfirmWindow() != null) {
            return;
        }

        ConfirmationModel confirmModel = new ConfirmationModel();
        setConfirmWindow(confirmModel);

        confirmModel.setTitle(ConstantsManager.getInstance().getConstants().removeVirtualMachineTitle());
        confirmModel.setHelpTag(HelpTag.remove_virtual_machine);
        confirmModel.setHashName("remove_virtual_machine"); //$NON-NLS-1$

        ArrayList<String> list = new ArrayList<>();
        for (VM vm : getSelectedVms()) {
            list.add(vm.getName());
        }

        confirmModel.setItems(list);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        getConfirmWindow().getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        getConfirmWindow().getCommands().add(tempVar2);
    }

    private void onRemove() {
        getConfirmWindow().startProgress();

        List<VdcActionParametersBase> paramsList = new ArrayList<>();
        for (VM vm : getSelectedVms()) {
            paramsList.add(new RemoveVmParameters(vm.getId(), false));
        }

        Frontend.getInstance().runMultipleActions(VdcActionType.RemoveVm, paramsList,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        ConfirmationModel model =
                                (ConfirmationModel) ((UserPortalListModel) result.getState()).getConfirmWindow();
                        model.stopProgress();
                        cancel();
                    }
                },
                this);

    }

    private List<VM> getSelectedVms() {
        List<VM> vms = new ArrayList<>();
        if (getSelectedItems() == null) {
            return vms;
        }

        for (Object selectedItem : getSelectedItems()) {
            UserPortalItemModel itemModel = (UserPortalItemModel) selectedItem;
            VM vm = itemModel.getVM();
            if (vm != null) {
                vms.add(vm);
            }
        }

        return vms;
    }

    private void changeCD() {
        UserPortalItemModel selectedItem = getSelectedItem();
        if (selectedItem == null || selectedItem.getEntity() == null) {
            return;
        }

        final VM vm = (VM) selectedItem.getEntity();

        AttachCdModel model = new AttachCdModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().changeCDTitle());
        model.setHelpTag(HelpTag.change_cd);
        model.setHashName("change_cd"); //$NON-NLS-1$

        ArrayList<String> defaultImages =
                new ArrayList<>(Arrays.asList(new String[]{ConstantsManager.getInstance().getConstants().noCds()}));
        model.getIsoImage().setItems(defaultImages);
        model.getIsoImage().setSelectedItem(Linq.firstOrNull(defaultImages));

        AsyncQuery getImagesQuery = new AsyncQuery();
        getImagesQuery.setModel(this);

        getImagesQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model1, Object result) {
                UserPortalListModel userPortalListModel = (UserPortalListModel) model1;
                AttachCdModel _attachCdModel = (AttachCdModel) userPortalListModel.getWindow();
                List<String> images = (List<String>) result;
                images.add(0, ConsoleModel.getEjectLabel());
                _attachCdModel.getIsoImage().setItems(images);
                if (_attachCdModel.getIsoImage().getIsChangable()) {
                    String selectedIso = Linq.firstOrNull(images, new Linq.IPredicate<String>() {
                        @Override
                        public boolean match(String s) {
                            return vm.getCurrentCd().equals(s);
                        }
                    });
                    _attachCdModel.getIsoImage().setSelectedItem(selectedIso == null ? ConsoleModel.getEjectLabel() : selectedIso);
                }
            }
        };

        AsyncDataProvider.getInstance().getIrsImageList(getImagesQuery, vm.getStoragePoolId());

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnChangeCD", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    private void onChangeCD() {
        UserPortalItemModel selectedItem = getSelectedItem();
        if (selectedItem == null || selectedItem.getEntity() == null) {
            cancel();
            return;
        }

        VM vm = (VM) selectedItem.getEntity();
        AttachCdModel model = (AttachCdModel) getWindow();
        model.startProgress();
        String isoName =
                model.getIsoImage().getSelectedItem().equals(ConsoleModel.getEjectLabel()) ? "" //$NON-NLS-1$
                        : model.getIsoImage().getSelectedItem();

        Frontend.getInstance().runAction(VdcActionType.ChangeDisk, new ChangeDiskCommandParameters(vm.getId(), isoName),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        stopProgress(result.getState());
                        cancel();
                    }
                }, this);
    }

    private void onSave() {

        final UnitVmModel model = (UnitVmModel) getWindow();
        UserPortalItemModel selectedItem = getSelectedItem();
        if (!model.getIsNew() && selectedItem.getEntity() == null) {
            cancel();
            return;
        }

        settempVm(model.getIsNew() ? new VM() : (VM) Cloner.clone(selectedItem.getEntity()));

        if (!model.validate()) {
            return;
        }

        model.startProgress();
        // Check name uniqueness.
        AsyncDataProvider.getInstance().isVmNameUnique(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        UserPortalListModel userPortalListModel = (UserPortalListModel) target;
                        boolean isNameUnique = (Boolean) returnValue;
                        String newName = model.getName().getEntity();
                        String currentName = userPortalListModel.gettempVm().getName();
                        if (!isNameUnique && newName.compareToIgnoreCase(currentName) != 0) {
                            UnitVmModel unitModel = (UnitVmModel) userPortalListModel.getWindow();
                            unitModel.getName().getInvalidityReasons().clear();
                            unitModel
                                    .getName()
                                    .getInvalidityReasons()
                                    .add(ConstantsManager.getInstance().getConstants().nameMustBeUniqueInvalidReason());
                            unitModel.getName().setIsValid(false);
                            unitModel.setIsValid(false);
                            unitModel.setValidTab(TabName.GENERAL_TAB, false);
                            unitModel.fireValidationCompleteEvent();
                            stopProgress(target);
                        } else {
                            String selectedCpu = model.getCustomCpu().getSelectedItem();
                            if (selectedCpu != null && !selectedCpu.isEmpty()  && !model.getCustomCpu().getItems().contains(selectedCpu)) {
                                ConfirmationModel confirmModel = new ConfirmationModel();
                                confirmModel.setTitle(ConstantsManager.getInstance().getConstants().vmUnsupportedCpuTitle());
                                confirmModel.setMessage(ConstantsManager.getInstance().getConstants().vmUnsupportedCpuMessage());
                                confirmModel.setHelpTag(HelpTag.edit_unsupported_cpu);
                                confirmModel.setHashName("edit_unsupported_cpu"); //$NON-NLS-1$

                                confirmModel.getCommands().add(new UICommand("postVmNameUniqueCheck", UserPortalListModel.this) //$NON-NLS-1$
                                        .setTitle(ConstantsManager.getInstance().getConstants().ok())
                                        .setIsDefault(true));

                                confirmModel.getCommands().add(UICommand.createCancelUiCommand("CancelConfirmation", UserPortalListModel.this)); //$NON-NLS-1$

                                setConfirmWindow(confirmModel);
                            } else {
                                userPortalListModel.postVmNameUniqueCheck();
                            }
                        }

                    }
                }),
                model.getName().getEntity(),
                model.getSelectedDataCenter() == null ? null : model.getSelectedDataCenter().getId());
    }

    private void stopProgress(Object target) {
        if (target instanceof UserPortalListModel) {
            Model window = ((UserPortalListModel) target).getWindow();
            if (window != null) {
                window.stopProgress();
            }
        }
    }

    public void postVmNameUniqueCheck() {
        final UnitVmModel model = (UnitVmModel) getWindow();

        // Save changes.
        buildVmOnSave(model, gettempVm());

        gettempVm().setCpuPinning(model.getCpuPinning().getEntity());

        gettempVm().setVmInit(model.getVmInitModel().buildCloudInitParameters(model));

        if (model.getIsNew()) {
            saveNewVm(model);
        }
        else {
            final VM selectedItem = (VM) getSelectedItem().getEntity();
            gettempVm().setUseLatestVersion(model.getTemplateWithVersion().getSelectedItem().isLatest());

            if (!selectedItem.isHostedEngine() && selectedItem.isRunningOrPaused()) {
                AsyncDataProvider.getInstance().getVmChangedFieldsForNextRun(editedVm, gettempVm(), getUpdateVmParameters(false), new AsyncQuery(this,
                        new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object thisModel, Object returnValue) {
                        List<String> changedFields = ((VdcQueryReturnValue)returnValue).getReturnValue();
                        if (!changedFields.isEmpty()) {
                            VmNextRunConfigurationModel confirmModel = new VmNextRunConfigurationModel();
                            confirmModel.setTitle(ConstantsManager.getInstance().getConstants().editNextRunConfigurationTitle());
                            confirmModel.setHelpTag(HelpTag.edit_next_run_configuration);
                            confirmModel.setHashName("edit_next_run_configuration"); //$NON-NLS-1$
                            confirmModel.setChangedFields(changedFields);
                            confirmModel.setCpuPluggable(VmCommonUtils.isCpusToBeHotplugged(selectedItem, gettempVm()));
                            boolean isMemoryHotUnplugSupported =
                                    AsyncDataProvider.getInstance().isMemoryHotUnplugSupported(gettempVm());
                            confirmModel.setMemoryPluggable(VmCommonUtils.isMemoryToBeHotplugged(
                                    selectedItem, gettempVm(), isMemoryHotUnplugSupported));
                            confirmModel.getCommands().add(new UICommand("updateExistingVm", UserPortalListModel.this) //$NON-NLS-1$
                            .setTitle(ConstantsManager.getInstance().getConstants().ok())
                            .setIsDefault(true));

                            confirmModel.getCommands().add(UICommand.createCancelUiCommand("CancelConfirmation", UserPortalListModel.this)); //$NON-NLS-1$

                            setConfirmWindow(confirmModel);
                        }
                        else {
                            updateExistingVm((UserPortalListModel)thisModel, false);
                        }
                    }
                }));
            }
            else {
                updateExistingVm(this, false);
            }
        }
    }

    private void saveNewVm(final UnitVmModel model) {
        setstorageDomain(model.getStorageDomain().getSelectedItem());

        VM vm = gettempVm();
        vm.setUseLatestVersion(model.getTemplateWithVersion().getSelectedItem().isLatest());

        AddVmParameters parameters = new AddVmParameters(vm);
        parameters.setDiskInfoDestinationMap(model.getDisksAllocationModel().getImageToDestinationDomainMap());
        parameters.setMakeCreatorExplicitOwner(true);
        parameters.setCopyTemplatePermissions(model.getCopyPermissions().getEntity());
        parameters.setSoundDeviceEnabled(model.getIsSoundcardEnabled().getEntity());
        parameters.setConsoleEnabled(model.getIsConsoleDeviceEnabled().getEntity());
        parameters.setVmLargeIcon(model.getIcon().getEntity().getIcon());
        setRngDeviceToParams(model, parameters);
        BuilderExecutor.build(model, parameters, new UnitToGraphicsDeviceParamsBuilder());

        if (!StringHelper.isNullOrEmpty(model.getVmId().getEntity())) {
            parameters.setVmId(new Guid(model.getVmId().getEntity()));
        }

        Frontend.getInstance().runAction(
                model.getProvisioning().getEntity() ? VdcActionType.AddVmFromTemplate : VdcActionType.AddVm,
                        parameters,
                        new UnitVmModelNetworkAsyncCallback(model, defaultNetworkCreatingManager),
                        this);
    }

    private void updateExistingVm(UserPortalListModel userPortalListModel, final boolean applyCpuChangesLater) {
        final UnitVmModel model = (UnitVmModel) getWindow();
        UserPortalItemModel selectedItem = userPortalListModel.getSelectedItem();

        Guid oldClusterID = ((VM) selectedItem.getEntity()).getClusterId();
        Guid newClusterID = model.getSelectedCluster().getId();
        if (oldClusterID.equals(newClusterID) == false) {
            Frontend.getInstance().runAction(VdcActionType.ChangeVMCluster,
                                             new ChangeVMClusterParameters(
                                                     newClusterID,
                                                     gettempVm().getId(),
                                                     model.getCustomCompatibilityVersion().getSelectedItem()),
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void executed(FrontendActionAsyncResult result) {
                            VdcReturnValueBase returnValueBase = result.getReturnValue();
                            if (returnValueBase != null && returnValueBase.getSucceeded()) {
                                VmManagementParametersBase param = getUpdateVmParameters(applyCpuChangesLater);
                                Frontend.getInstance()
                                    .runAction(VdcActionType.UpdateVm,
                                            param,
                                            new UnitVmModelNetworkAsyncCallback(model,
                                                    defaultNetworkCreatingManager,
                                                    gettempVm().getId()),
                                            this);
                            }
                            else {
                                getWindow().stopProgress();
                            }
                        }
                    }, this);
        }
        else {
            VmManagementParametersBase param = getUpdateVmParameters(applyCpuChangesLater);
            Frontend.getInstance().runAction(VdcActionType.UpdateVm,
                    param,
                    new UnitVmModelNetworkAsyncCallback(model, defaultNetworkCreatingManager, gettempVm().getId()),
                    this);
        }
    }

    private VmManagementParametersBase getUpdateVmParameters(boolean applyCpuChangesLater) {
        UnitVmModel model = (UnitVmModel) getWindow();
        VmManagementParametersBase params = new VmManagementParametersBase(gettempVm());

        BuilderExecutor.build(
                new Pair<>((UnitVmModel) getWindow(), gettempVm()),
                params,
                new VmIconUnitAndVmToParameterBuilder());
        params.setSoundDeviceEnabled(model.getIsSoundcardEnabled().getEntity());
        params.setConsoleEnabled(model.getIsConsoleDeviceEnabled().getEntity());
        setRngDeviceToParams(model, params);
        params.setApplyChangesLater(applyCpuChangesLater);
        BuilderExecutor.build(model, params, new UnitToGraphicsDeviceParamsBuilder());

        return params;
    }

    private void setRngDeviceToParams(UnitVmModel model, VmManagementParametersBase parameters) {
        parameters.setUpdateRngDevice(true);
        parameters.setRngDevice(model.getIsRngEnabled().getEntity() ? model.generateRngDevice() : null);
    }

    protected static void buildVmOnSave(UnitVmModel model, VM vm) {
        BuilderExecutor.build(model, vm.getStaticData(), new FullUnitToVmBaseBuilder<VmStatic>());
        BuilderExecutor.build(model, vm, new VmSpecificUnitToVmBuilder());
    }

    private void updateDataCenterWithCluster() {
        UnitVmModel model = (UnitVmModel) getWindow();
        UserPortalItemModel selectedItem = getSelectedItem();
        final VM vm = (VM) selectedItem.getEntity();
        DataCenterWithCluster selectedDataCenterWithCluster = null;

        for (DataCenterWithCluster candidate : model.getDataCenterWithClustersList().getItems()) {

            if (model.getIsNew()) {
                selectedDataCenterWithCluster = candidate;
                break;
            }

            if (candidate.getDataCenter().getId().equals(vm.getStoragePoolId())
                    && candidate.getCluster().getId().equals(vm.getClusterId())) {
                selectedDataCenterWithCluster = candidate;
                break;
            }
        }

        if (!model.getIsNew() && selectedDataCenterWithCluster == null) {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, final Object loadedDataCenter) {
                    UserPortalListModel userPortalListModel = (UserPortalListModel) model;
                    final UnitVmModel unitModel = (UnitVmModel) userPortalListModel.getWindow();

                    AsyncDataProvider.getInstance().getClusterById(new AsyncQuery(this, new INewAsyncCallback() {

                        @Override
                        public void onSuccess(Object model, Object loadedCluster) {
                            DataCenterWithCluster newItem =
                                    new DataCenterWithCluster((StoragePool) loadedDataCenter,
                                            (Cluster) loadedCluster);
                            unitModel.getDataCenterWithClustersList().setItems(Arrays.asList(newItem));
                            unitModel.getDataCenterWithClustersList().setSelectedItem(newItem);
                        }
                    }), vm.getClusterId());
                }
            };
            AsyncDataProvider.getInstance().getDataCenterById(_asyncQuery, vm.getStoragePoolId());
        } else {
            model.getDataCenterWithClustersList().setSelectedItem(selectedDataCenterWithCluster);
        }

        model.getDataCenterWithClustersList().setIsChangeable(vm.getStatus() == VMStatus.Down);
    }

    private void vmModel_DefaultHost_ItemsChanged() {
        UnitVmModel model = (UnitVmModel) getWindow();
        if (!model.getIsNew()) {
            UserPortalItemModel selectedItem = getSelectedItem();
            VM vm = (VM) selectedItem.getEntity();
            VDS host = null;

            for (VDS item : model.getDefaultHost().getItems()) {
                if (vm.getDedicatedVmForVdsList().contains(item.getId())) {
                    host = item;
                    break;
                }
            }
            if (host == null) {
                model.getIsAutoAssign().setEntity(true);
            }
            else {
                model.getDefaultHost().setSelectedItems(new ArrayList<>(Arrays.asList(host)));
                model.getIsAutoAssign().setEntity(false);
            }
        }
    }

    private void vmModel_DisplayProtocol_ItemsChanged() {
        UnitVmModel model = (UnitVmModel) getWindow();
        if (!model.getIsNew()) {
            UserPortalItemModel selectedItem = getSelectedItem();
            DisplayType displayType = ((VM) selectedItem.getEntity()).getDefaultDisplayType();
            if (model.getDisplayType().getItems().contains(displayType)) {
                model.getDisplayType().setSelectedItem(displayType);
            }
        }
    }

    private Integer cachedMaxPriority;

    private void vmModel_Priority_ItemsChanged() {
        UnitVmModel model = (UnitVmModel) getWindow();
        if (!model.getIsNew()) {
            if (cachedMaxPriority == null) {
                AsyncDataProvider.getInstance().getMaxVmPriority(new AsyncQuery(model,
                        new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object target, Object returnValue) {
                                cachedMaxPriority = (Integer) returnValue;
                                updatePriority((UnitVmModel) target);
                            }
                        }));
            } else {
                updatePriority(model);
            }
        }
    }

    private void updatePriority(UnitVmModel model) {
        UserPortalItemModel selectedItem = getSelectedItem();
        VM vm = (VM) selectedItem.getEntity();
        int roundPriority = AsyncDataProvider.getInstance().getRoundedPriority(vm.getPriority(), cachedMaxPriority);
        EntityModel<Integer> priority = null;

        for (EntityModel<Integer> a : model.getPriority().getItems()) {
            int p = a.getEntity();
            if (p == roundPriority) {
                priority = a;
                break;
            }
        }
        ((UnitVmModel) model.getWindow()).getPriority().setSelectedItem(priority);
    }

    private void vmModel_TimeZone_ItemsChanged() {
        UnitVmModel model = (UnitVmModel) getWindow();
        if (!model.getIsNew()) {
            UserPortalItemModel selectedItem = getSelectedItem();
            VM vm = (VM) selectedItem.getEntity();

            if (!StringHelper.isNullOrEmpty(vm.getTimeZone())) {
                model.getTimeZone().setSelectedItem(Linq.firstOrNull(model.getTimeZone().getItems(),
                        new Linq.TimeZonePredicate(vm.getTimeZone())));
            }
        }
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        UnitVmModel model = (UnitVmModel) getWindow();
        if (ev.matchesDefinition(itemsChangedEventDefinition) && sender == model.getDataCenterWithClustersList()) {
            updateDataCenterWithCluster();
        }
        else if (ev.matchesDefinition(itemsChangedEventDefinition) && sender == model.getDefaultHost()) {
            vmModel_DefaultHost_ItemsChanged();
        }
        else if (ev.matchesDefinition(itemsChangedEventDefinition) && sender == model.getDisplayType()) {
            vmModel_DisplayProtocol_ItemsChanged();
        }
        else if (ev.matchesDefinition(itemsChangedEventDefinition) && sender == model.getPriority()) {
            vmModel_Priority_ItemsChanged();
        }
        else if (ev.matchesDefinition(itemsChangedEventDefinition) && sender == model.getTimeZone()) {
            vmModel_TimeZone_ItemsChanged();
        }
    }

    @Override
    protected boolean fetchLargeIcons() {
        return false;
    }

    @Override
    protected String getListName() {
        return "UserPortalListModel"; //$NON-NLS-1$
    }

    @Override
    protected ConsoleContext getConsoleContext() {
        return ConsoleContext.UP_EXTENDED;
    }

    protected void executeDiskModifications(Guid vmId, UnitVmModel model) {
        // this is done on the background - the window is not visible anymore
        gettempVm().setId(vmId);
        model.getInstanceImages().executeDiskModifications(gettempVm());
    }
}
