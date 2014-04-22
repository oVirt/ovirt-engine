package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.AddVmFromScratchParameters;
import org.ovirt.engine.core.common.action.AddVmFromTemplateParameters;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.action.ChangeVMClusterParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
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
import org.ovirt.engine.ui.uicommonweb.builders.vm.FullUnitToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.KernelParamsVmBaseToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.UsbPolicyVmBaseToVmBaseBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.VmSpecificUnitToVmBuilder;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.ConsoleModelsCache;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.VmConsoles;
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
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceCreatingManager;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmMonitorModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmNextRunConfigurationModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSessionsModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;
import org.ovirt.engine.ui.uicompat.ObservableCollection;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class UserPortalListModel extends AbstractUserPortalListModel {

    private final UIConstants constants = ConstantsManager.getInstance().getConstants();
    public static final EventDefinition searchCompletedEventDefinition;
    private Event privateSearchCompletedEvent;

    /** The edited VM could be different than the selected VM in the grid
     *  when the VM has next-run configuration */
    private VM editedVm;

    VmInterfaceCreatingManager defaultNetworkCreatingManager = new VmInterfaceCreatingManager(new VmInterfaceCreatingManager.PostVnicCreatedCallback() {
        @Override
        public void vnicCreated(Guid vmId) {
            if (getWindow() != null) {
                getWindow().stopProgress();
            }
            cancel();
            updateActionAvailability();
        }

        @Override
        public void queryFailed() {
            stopProgress(UserPortalListModel.this);
            cancel();
        }
    });

    public Event getSearchCompletedEvent()
    {
        return privateSearchCompletedEvent;
    }

    private void setSearchCompletedEvent(Event value)
    {
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

    public UICommand getNewVmCommand()
    {
        return privateNewVmCommand;
    }

    private void setNewVmCommand(UICommand value)
    {
        privateNewVmCommand = value;
    }

    private UICommand privateEditCommand;

    @Override
    public UICommand getEditCommand()
    {
        return privateEditCommand;
    }

    private void setEditCommand(UICommand value)
    {
        privateEditCommand = value;
    }

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand()
    {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value)
    {
        privateRemoveCommand = value;
    }

    private UICommand privateSaveCommand;

    public UICommand getSaveCommand()
    {
        return privateSaveCommand;
    }

    private void setSaveCommand(UICommand value)
    {
        privateSaveCommand = value;
    }

    private UICommand privateRunOnceCommand;

    public UICommand getRunOnceCommand()
    {
        return privateRunOnceCommand;
    }

    private void setRunOnceCommand(UICommand value)
    {
        privateRunOnceCommand = value;
    }

    private UICommand privateChangeCdCommand;

    public UICommand getChangeCdCommand()
    {
        return privateChangeCdCommand;
    }

    private void setChangeCdCommand(UICommand value)
    {
        privateChangeCdCommand = value;
    }

    private UICommand privateNewTemplateCommand;

    public UICommand getNewTemplateCommand()
    {
        return privateNewTemplateCommand;
    }

    private void setNewTemplateCommand(UICommand value)
    {
        privateNewTemplateCommand = value;
    }

    private EntityModel vmGeneralModel;
    private EntityModel vmSessionsModel;
    private ListModel vmSnapshotListModel;
    private EntityModel vmMonitorModel;
    private ListModel vmDiskListModel;
    private ListModel vmInterfaceListModel;
    private ListModel permissionListModel;
    private ListModel vmEventListModel;
    private ListModel vmAppListModel;
    private EntityModel poolGeneralModel;
    private ListModel poolDiskListModel;
    private ListModel poolInterfaceListModel;
    private ArrayList<VM> privatevms;

    public ArrayList<VM> getvms()
    {
        return privatevms;
    }

    public void setvms(ArrayList<VM> value)
    {
        privatevms = value;
    }

    private ArrayList<VmPool> privatepools;

    public ArrayList<VmPool> getpools()
    {
        return privatepools;
    }

    public void setpools(ArrayList<VmPool> value)
    {
        privatepools = value;
    }

    private VM privatetempVm;

    public VM gettempVm()
    {
        return privatetempVm;
    }

    public void settempVm(VM value)
    {
        privatetempVm = value;
    }

    private StorageDomain privatestorageDomain;

    public StorageDomain getstorageDomain()
    {
        return privatestorageDomain;
    }

    public void setstorageDomain(StorageDomain value)
    {
        privatestorageDomain = value;
    }

    static
    {
        searchCompletedEventDefinition = new EventDefinition("SearchCompleted", UserPortalListModel.class); //$NON-NLS-1$
    }

    public UserPortalListModel()
    {
        setSearchCompletedEvent(new Event(searchCompletedEventDefinition));

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

        consoleModelsCache = new ConsoleModelsCache(ConsoleContext.UP_EXTENDED, this);
    }

    @Override
    public void setItems(Collection value)
    {
        if (items != value)
        {
            itemsChanging(value, items);
            items = value;
            getItemsChangedEvent().raise(this, EventArgs.EMPTY);
            onPropertyChanged(new PropertyChangedEventArgs("Items")); //$NON-NLS-1$

            UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
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
    protected void syncSearch()
    {
        super.syncSearch();
        VdcQueryParametersBase queryParameters = new VdcQueryParametersBase();
        queryParameters.setRefresh(getIsQueryFirstTime());
        Frontend.getInstance().runQuery(VdcQueryType.GetAllVmsAndVmPools, queryParameters,
                new AsyncQuery(this, new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        UserPortalListModel userPortalListModel = (UserPortalListModel) model;
                        ArrayList<VM> vms = new ArrayList<VM>();
                        ArrayList<VmPool> pools = new ArrayList<VmPool>();

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

                        userPortalListModel.setvms(vms);
                        userPortalListModel.setpools(pools);
                        userPortalListModel.onVmAndPoolLoad();
                    }
                }));
    }

    @Override
    protected void initDetailModels()
    {
        super.initDetailModels();

        vmGeneralModel = new VmGeneralModel();
        vmGeneralModel.setIsAvailable(false);

        vmSessionsModel = new VmSessionsModel();
        vmSessionsModel.setIsAvailable(false);

        vmSnapshotListModel = new UserPortalVmSnapshotListModel();
        vmSnapshotListModel.setIsAvailable(false);

        vmMonitorModel = new VmMonitorModel();
        vmMonitorModel.setIsAvailable(false);

        vmDiskListModel = new VmDiskListModel();
        vmDiskListModel.setIsAvailable(false);

        vmInterfaceListModel = new VmInterfaceListModel();
        vmInterfaceListModel.setIsAvailable(false);

        permissionListModel = new UserPortalPermissionListModel();
        permissionListModel.setIsAvailable(false);

        vmEventListModel = new UserPortalVmEventListModel();
        vmEventListModel.setIsAvailable(false);

        vmAppListModel = new VmAppListModel();
        vmAppListModel.setIsAvailable(false);

        poolGeneralModel = new PoolGeneralModel();
        poolGeneralModel.setIsAvailable(false);

        poolDiskListModel = new PoolDiskListModel();
        poolDiskListModel.setIsAvailable(false);

        poolInterfaceListModel = new PoolInterfaceListModel();
        poolInterfaceListModel.setIsAvailable(false);

        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
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
        list.add(vmSessionsModel);

        setDetailModels(list);

        permissionListModel.setIsAvailable(true);
        vmEventListModel.setIsAvailable(true);
        vmAppListModel.setIsAvailable(true);
        vmSessionsModel.setIsAvailable(true);
    }

    @Override
    protected Object provideDetailModelEntity(Object selectedItem)
    {
        // Each item in this list model is not a business entity,
        // therefore select an Entity property to provide it to
        // the detail models.

        EntityModel model = (EntityModel) selectedItem;
        if (model == null)
        {
            return null;
        }

        return model.getEntity();
    }

    @Override
    protected void updateDetailsAvailability()
    {
        super.updateDetailsAvailability();

        UserPortalItemModel item = (UserPortalItemModel) getSelectedItem();

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
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (command == getNewVmCommand())
        {
            newInternal();
        }
        else if (command == getCloneVmCommand())
        {
            cloneVm();
        }
        else if (command == getEditCommand())
        {
            edit();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }
        else if (command == getSaveCommand())
        {
            onSave();
        }
        else if (command == getRunOnceCommand())
        {
            runOnce();
        }
        else if (command == getChangeCdCommand())
        {
            changeCD();
        }
        else if (command == getNewTemplateCommand())
        {
            newTemplate();
        }
        else if ("OnRemove".equals(command.getName())) //$NON-NLS-1$
        {
            onRemove();
        }
        else if ("OnRunOnce".equals(command.getName())) //$NON-NLS-1$
        {
            cancel();
        }
        else if ("OnChangeCD".equals(command.getName())) //$NON-NLS-1$
        {
            onChangeCD();
        }
        else if ("OnNewTemplate".equals(command.getName())) //$NON-NLS-1$
        {
            onNewTemplate();
        }
        else if ("OnSave".equals(command.getName())) //$NON-NLS-1$
        {
            onSave();
        }
        else if (command.getName().equals("closeVncInfo")) { //$NON-NLS-1$
            setWindow(null);
        }
        else if ("OnClone".equals(command.getName())) { //$NON-NLS-1$
            onClone();
        }
        else if ("CancelConfirmation".equals(command.getName())) //$NON-NLS-1$
        {
            stopProgress(UserPortalListModel.this);
            setConfirmWindow(null);
        }
        else if ("updateExistingVm".equals(command.getName())) { // $NON-NLS-1$
            VmNextRunConfigurationModel model = (VmNextRunConfigurationModel) getConfirmWindow();
            updateExistingVm(UserPortalListModel.this, model.getApplyCpuLater().getEntity());
            setConfirmWindow(null);
        }
    }

    private void cloneVm() {
        final UserPortalItemModel vm = (UserPortalItemModel) getSelectedItem();
        if (vm == null) {
            return;
        }

        CloneVmModel model = new CloneVmModel(vm.getVM(), constants);
        setWindow(model);

        model.initialize();
        model.setTitle(ConstantsManager.getInstance().getConstants().cloneVmTitle());

        model.setHelpTag(HelpTag.clone_vm);
        model.setHashName("clone_vm"); //$NON-NLS-1$

        UICommand okCommand = new UICommand("OnClone", this); //$NON-NLS-1$
        okCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        okCommand.setIsDefault(true);
        model.getCommands().add(okCommand);
        UICommand cancelCommand = new UICommand("Cancel", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        model.getCommands().add(cancelCommand);
    }

    private void onClone() {
        ((CloneVmModel) getWindow()).onClone(this, true);
    }

    private void newTemplate()
    {
        UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
        if (selectedItem == null)
        {
            return;
        }

        VM vm = (VM) selectedItem.getEntity();
        UnitVmModel windowModel = new UnitVmModel(new UserPortalNewTemplateVmModelBehavior(vm));
        setWindow(windowModel);
        windowModel.setTitle(ConstantsManager.getInstance().getConstants().newTemplateTitle());
        windowModel.setHelpTag(HelpTag.new_template);
        windowModel.setHashName("new_template"); //$NON-NLS-1$
        windowModel.setIsNew(true);
        windowModel.getVmType().setSelectedItem(vm.getVmType());
        windowModel.initialize(null);
        windowModel.getIsTemplatePublic().setEntity(false);

        windowModel.getCommands().add(
                new UICommand("OnNewTemplate", this) //$NON-NLS-1$)
                .setTitle(ConstantsManager.getInstance().getConstants().ok())
                .setIsDefault(true));

        windowModel.getCommands().add(
                new UICommand("Cancel", this) //$NON-NLS-1$
                .setTitle(ConstantsManager.getInstance().getConstants().cancel())
                .setIsCancel(true));
    }

    private void onNewTemplate()
    {
        UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
        if (selectedItem == null)
        {
            cancel();
            return;
        }

        UnitVmModel model = (UnitVmModel) getWindow();

        if (!model.validate())
        {
            model.setIsValid(false);
        }
        else if (model.getIsSubTemplate().getEntity()) {
            postNameUniqueCheck(this);
        }
        else
        {
            model.startProgress(null);
            String name = model.getName().getEntity();

            // Check name unicitate.
            AsyncDataProvider.getInstance().isTemplateNameUnique(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {

                            UserPortalListModel userPortalListModel = (UserPortalListModel) target;
                            boolean isNameUnique = (Boolean) returnValue;
                            if (!isNameUnique)
                            {

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
                                stopProgress(target);
                            }
                            else
                            {
                                userPortalListModel.postNameUniqueCheck(userPortalListModel);
                            }

                        }
                    }),
                    name);
        }
    }

    private void postNameUniqueCheck(UserPortalListModel userPortalListModel)
    {
        UnitVmModel model = (UnitVmModel) userPortalListModel.getWindow();
        UserPortalItemModel selectedItem = (UserPortalItemModel) userPortalListModel.getSelectedItem();
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

        BuilderExecutor.build(model, tempVar.getStaticData(), new CommonUnitToVmBaseBuilder());
        BuilderExecutor.build(vm.getStaticData(), tempVar.getStaticData(),
                              new KernelParamsVmBaseToVmBaseBuilder(),
                              new UsbPolicyVmBaseToVmBaseBuilder());
        return tempVar;
    }

    @Override
    protected void onSelectedItemChanged()
    {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    private void runOnce()
    {
        UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
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

    private void updateActionAvailability()
    {
        UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();

        getEditCommand().setIsExecutionAllowed(selectedItem != null && !selectedItem.isPool());

        getRemoveCommand().setIsExecutionAllowed(selectedItem != null
                && !selectedItem.isPool()
                && VdcActionUtils.canExecute(new ArrayList<VM>(Arrays.asList(new VM[]{(VM) selectedItem.getEntity()})),
                VM.class,
                VdcActionType.RemoveVm));

        getRunOnceCommand().setIsExecutionAllowed(selectedItem != null
                && !selectedItem.isPool()
                && VdcActionUtils.canExecute(new ArrayList<VM>(Arrays.asList(new VM[]{(VM) selectedItem.getEntity()})),
                VM.class,
                VdcActionType.RunVmOnce));

        getCloneVmCommand().setIsExecutionAllowed(selectedItem != null
                && !selectedItem.isPool()
                && VdcActionUtils.canExecute(new ArrayList<VM>(Arrays.asList(new VM[]{(VM) selectedItem.getEntity()})),
                VM.class,
                VdcActionType.CloneVm));

        getChangeCdCommand().setIsExecutionAllowed(selectedItem != null
                                                           && !selectedItem.isPool()
                                                           && VdcActionUtils.canExecute(new ArrayList<VM>(Arrays.asList(new VM[] {(VM) selectedItem.getEntity()})),
                                                                                        VM.class,
                                                                                        VdcActionType.ChangeDisk));

        getNewTemplateCommand().setIsExecutionAllowed(selectedItem != null
                                                              && !selectedItem.isPool()
                                                              && VdcActionUtils.canExecute(new ArrayList<VM>(Arrays.asList(new VM[] {(VM) selectedItem.getEntity()})),
                                                                                           VM.class,
                                                                                           VdcActionType.AddVmTemplate));
    }

    private void newInternal()
    {
        UnitVmModel model = new UnitVmModel(new UserPortalNewVmModelBehavior());
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

        UICommand tempVar = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private void edit()
    {
        UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
        if (selectedItem == null)
        {
            return;
        }

        if (getWindow() != null)
        {
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
        UnitVmModel model = new UnitVmModel(new UserPortalExistingVmModelBehavior(vm));

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

        UICommand tempVar = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
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

        ArrayList<String> list = new ArrayList<String>();
        for (VM vm : getSelectedVms()) {
            list.add(vm.getName());
        }

        confirmModel.setItems(list);

        UICommand tempVar = new UICommand("OnRemove", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        getConfirmWindow().getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        getConfirmWindow().getCommands().add(tempVar2);
    }

    private void onRemove() {
        getConfirmWindow().startProgress(null);

        List<VdcActionParametersBase> paramsList = new ArrayList<VdcActionParametersBase>();
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
        List<VM> vms = new ArrayList<VM>();
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

    private void changeCD()
    {
        UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
        if (selectedItem == null || selectedItem.getEntity() == null)
        {
            return;
        }

        final VM vm = (VM) selectedItem.getEntity();

        AttachCdModel model = new AttachCdModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().changeCDTitle());
        model.setHelpTag(HelpTag.change_cd);
        model.setHashName("change_cd"); //$NON-NLS-1$

        ArrayList<String> defaultImages =
                new ArrayList<String>(Arrays.asList(new String[] { ConstantsManager.getInstance()
                        .getConstants()
                        .noCds() }));
        model.getIsoImage().setItems(defaultImages);
        model.getIsoImage().setSelectedItem(Linq.firstOrDefault(defaultImages));

        AsyncQuery getImagesQuery = new AsyncQuery();
        getImagesQuery.setModel(this);

        getImagesQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model1, Object result)
            {
                UserPortalListModel userPortalListModel = (UserPortalListModel) model1;
                AttachCdModel _attachCdModel = (AttachCdModel) userPortalListModel.getWindow();
                List<String> images = (List<String>) result;
                images.add(0, ConsoleModel.getEjectLabel());
                _attachCdModel.getIsoImage().setItems(images);
                if (_attachCdModel.getIsoImage().getIsChangable())
                {
                    String selectedIso = Linq.firstOrDefault(images, new Linq.IPredicate<String>() {
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

        UICommand tempVar = new UICommand("OnChangeCD", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private void onChangeCD()
    {
        UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
        if (selectedItem == null || selectedItem.getEntity() == null)
        {
            cancel();
            return;
        }

        VM vm = (VM) selectedItem.getEntity();
        AttachCdModel model = (AttachCdModel) getWindow();
        model.startProgress(null);
        String isoName =
                (model.getIsoImage().getSelectedItem().equals(ConsoleModel.getEjectLabel())) ? "" //$NON-NLS-1$
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

    private void onSave()
    {

        final UnitVmModel model = (UnitVmModel) getWindow();
        UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
        if (!model.getIsNew() && selectedItem.getEntity() == null)
        {
            cancel();
            return;
        }

        settempVm(model.getIsNew() ? new VM() : (VM) Cloner.clone(selectedItem.getEntity()));

        if (!model.validate())
        {
            return;
        }

        model.startProgress(null);
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
                            stopProgress(target);
                        } else {
                            userPortalListModel.postVmNameUniqueCheck(userPortalListModel);
                        }

                    }
                }),
                model.getName().getEntity());
    }

    private void stopProgress(Object target) {
        if (target instanceof UserPortalListModel) {
            Model window = ((UserPortalListModel) target).getWindow();
            if (window != null) {
                window.stopProgress();
            }
        }
    }

    public void postVmNameUniqueCheck(final UserPortalListModel userPortalListModel)
    {
        final UnitVmModel model = (UnitVmModel) getWindow();

        // Save changes.
        buildVmOnSave(model, gettempVm());

        gettempVm().setCpuPinning(model.getCpuPinning().getEntity());

        gettempVm().setVmInit(model.getVmInitModel().buildCloudInitParameters(model));

        if (model.getIsNew())
        {
            if (gettempVm().getVmtGuid().equals(Guid.Empty))
            {
                AddVmFromScratchParameters parameters =
                        new AddVmFromScratchParameters(gettempVm(),
                                new ArrayList<DiskImage>(),
                                Guid.Empty);
                parameters.setMakeCreatorExplicitOwner(true);
                parameters.setSoundDeviceEnabled(model.getIsSoundcardEnabled().getEntity());
                parameters.setConsoleEnabled(model.getIsConsoleDeviceEnabled().getEntity());
                setRngDeviceToParams(model, parameters);
                Frontend.getInstance().runAction(VdcActionType.AddVmFromScratch, parameters, new UnitVmModelNetworkAsyncCallback(model, defaultNetworkCreatingManager), this);
            }
            else
            {
                setstorageDomain(model.getStorageDomain().getSelectedItem());

                if (model.getProvisioning().getEntity())
                {
                    AsyncQuery _asyncQuery = new AsyncQuery();
                    _asyncQuery.setModel(this);
                    _asyncQuery.asyncCallback = new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object model, Object result)
                        {
                            UserPortalListModel userPortalListModel1 = (UserPortalListModel) model;
                            final UnitVmModel unitVmModel = (UnitVmModel) userPortalListModel1.getWindow();

                            VM vm = gettempVm();
                            vm.setUseLatestVersion(constants.latestTemplateVersionName().equals(unitVmModel.getTemplate().getSelectedItem().getTemplateVersionName()));

                            AddVmFromTemplateParameters param = new AddVmFromTemplateParameters(vm,
                                    unitVmModel.getDisksAllocationModel().getImageToDestinationDomainMap(),
                                    Guid.Empty);
                            param.setMakeCreatorExplicitOwner(true);
                            param.setCopyTemplatePermissions(unitVmModel.getCopyPermissions().getEntity());

                            param.setSoundDeviceEnabled(unitVmModel.getIsSoundcardEnabled().getEntity());
                            param.setConsoleEnabled(unitVmModel.getIsConsoleDeviceEnabled().getEntity());
                            setRngDeviceToParams(unitVmModel, param);
                            Frontend.getInstance().runAction(VdcActionType.AddVmFromTemplate, param, new UnitVmModelNetworkAsyncCallback(unitVmModel, defaultNetworkCreatingManager), this);
                        }
                    };
                    AsyncDataProvider.getInstance().getTemplateDiskList(_asyncQuery, gettempVm().getVmtGuid());
                }
                else
                {
                    VM vm = gettempVm();
                    vm.setUseLatestVersion(constants.latestTemplateVersionName().equals(model.getTemplate().getSelectedItem().getTemplateVersionName()));

                    VmManagementParametersBase param = new VmManagementParametersBase(vm);
                    param.setDiskInfoDestinationMap(model.getDisksAllocationModel().getImageToDestinationDomainMap());
                    param.setMakeCreatorExplicitOwner(true);
                    param.setCopyTemplatePermissions(model.getCopyPermissions().getEntity());

                    param.setSoundDeviceEnabled(model.getIsSoundcardEnabled().getEntity());
                    param.setConsoleEnabled(model.getIsConsoleDeviceEnabled().getEntity());
                    setRngDeviceToParams(model, param);
                    Frontend.getInstance().runAction(VdcActionType.AddVm, param, new UnitVmModelNetworkAsyncCallback(model, defaultNetworkCreatingManager), this);
                }
            }
        }
        else
        {
            final VM selectedItem = (VM) ((UserPortalItemModel) userPortalListModel.getSelectedItem()).getEntity();
            gettempVm().setUseLatestVersion(constants.latestTemplateVersionName().equals(model.getTemplate().getSelectedItem().getTemplateVersionName()));

            if (selectedItem.isRunningOrPaused()) {
                AsyncDataProvider.getInstance().isNextRunConfigurationChanged(editedVm, gettempVm(), getUpdateVmParameters(false), new AsyncQuery(this,
                        new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object thisModel, Object returnValue) {
                        if (((VdcQueryReturnValue)returnValue).<Boolean> getReturnValue()) {
                            VmNextRunConfigurationModel confirmModel = new VmNextRunConfigurationModel();
                            confirmModel.setTitle(ConstantsManager.getInstance().getConstants().editNextRunConfigurationTitle());
                            confirmModel.setHelpTag(HelpTag.edit_next_run_configuration);
                            confirmModel.setHashName("edit_next_run_configuration"); //$NON-NLS-1$
                            confirmModel.setCpuPluggable(selectedItem.getCpuPerSocket() == gettempVm().getCpuPerSocket() &&
                                    selectedItem.getNumOfSockets() != gettempVm().getNumOfSockets());
                            confirmModel.getCommands().add(new UICommand("updateExistingVm", UserPortalListModel.this) //$NON-NLS-1$
                            .setTitle(ConstantsManager.getInstance().getConstants().ok())
                            .setIsDefault(true));

                            confirmModel.getCommands().add(new UICommand("CancelConfirmation", UserPortalListModel.this) //$NON-NLS-1$
                            .setTitle(ConstantsManager.getInstance().getConstants().cancel())
                            .setIsCancel(true));

                            setConfirmWindow(confirmModel);
                        }
                        else {
                            updateExistingVm(userPortalListModel, false);
                        }
                    }
                }));
            }
            else {
                updateExistingVm(userPortalListModel, false);
            }
        }
    }

    private void updateExistingVm(UserPortalListModel userPortalListModel, final boolean applyCpuChangesLater) {
        final UnitVmModel model = (UnitVmModel) getWindow();
        UserPortalItemModel selectedItem = (UserPortalItemModel) userPortalListModel.getSelectedItem();

        Guid oldClusterID = ((VM) selectedItem.getEntity()).getVdsGroupId();
        Guid newClusterID = model.getSelectedCluster().getId();
        if (oldClusterID.equals(newClusterID) == false)
        {
            Frontend.getInstance().runAction(VdcActionType.ChangeVMCluster, new ChangeVMClusterParameters(newClusterID,
                    gettempVm().getId()),
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void executed(FrontendActionAsyncResult result) {
                            VmManagementParametersBase param = getUpdateVmParameters(applyCpuChangesLater);

                            Frontend.getInstance().runAction(VdcActionType.UpdateVm, param, new UnitVmModelNetworkAsyncCallback(model, defaultNetworkCreatingManager, gettempVm().getId()), this);
                        }
                    }, this);
        }
        else
        {
            VmManagementParametersBase param = getUpdateVmParameters(applyCpuChangesLater);
            Frontend.getInstance().runAction(VdcActionType.UpdateVm, param, new UnitVmModelNetworkAsyncCallback(model, defaultNetworkCreatingManager, gettempVm().getId()), this);
        }
    }

    private VmManagementParametersBase getUpdateVmParameters(boolean applyCpuChangesLater) {
        UnitVmModel model = (UnitVmModel) getWindow();
        VmManagementParametersBase params = new VmManagementParametersBase(gettempVm());

        params.setSoundDeviceEnabled(model.getIsSoundcardEnabled().getEntity());
        params.setConsoleEnabled(model.getIsConsoleDeviceEnabled().getEntity());
        setRngDeviceToParams(model, params);
        params.setApplyChangesLater(applyCpuChangesLater);
        setRngDeviceToParams(model, params);

        return params;
    }

    private void setRngDeviceToParams(UnitVmModel model, VmManagementParametersBase parameters) {
        parameters.setUpdateRngDevice(true);
        parameters.setRngDevice(model.getIsRngEnabled().getEntity() ? model.generateRngDevice() : null);
    }

    protected static void buildVmOnSave(UnitVmModel model, VM vm) {
        BuilderExecutor.build(model, vm.getStaticData(), new FullUnitToVmBaseBuilder());
        BuilderExecutor.build(model, vm, new VmSpecificUnitToVmBuilder());
    }

    private void updateDataCenterWithCluster() {
        UnitVmModel model = (UnitVmModel) getWindow();
        UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
        final VM vm = (VM) selectedItem.getEntity();
        DataCenterWithCluster selectedDataCenterWithCluster = null;

        for (DataCenterWithCluster candidate : model.getDataCenterWithClustersList().getItems()) {

            if (model.getIsNew()) {
                selectedDataCenterWithCluster = candidate;
                break;
            }

            if (candidate.getDataCenter().getId().equals(vm.getStoragePoolId())
                    && candidate.getCluster().getId().equals(vm.getVdsGroupId())) {
                selectedDataCenterWithCluster = candidate;
                break;
            }
        }

        if (!model.getIsNew() && selectedDataCenterWithCluster == null) {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, final Object loadedDataCenter)
                {
                    UserPortalListModel userPortalListModel = (UserPortalListModel) model;
                    final UnitVmModel unitModel = (UnitVmModel) userPortalListModel.getWindow();

                    AsyncDataProvider.getInstance().getClusterById(new AsyncQuery(this, new INewAsyncCallback() {

                        @Override
                        public void onSuccess(Object model, Object loadedCluster) {
                            DataCenterWithCluster newItem =
                                    new DataCenterWithCluster((StoragePool) loadedDataCenter,
                                            (VDSGroup) loadedCluster);
                            unitModel.getDataCenterWithClustersList().setItems(Arrays.asList(newItem));
                            unitModel.getDataCenterWithClustersList().setSelectedItem(newItem);
                        }
                    }), vm.getVdsGroupId());
                }
            };
            AsyncDataProvider.getInstance().getDataCenterById(_asyncQuery, vm.getStoragePoolId());
        } else {
            model.getDataCenterWithClustersList().setSelectedItem(selectedDataCenterWithCluster);
        }

        model.getDataCenterWithClustersList().setIsChangable(vm.getStatus() == VMStatus.Down);
    }

    private void vmModel_DefaultHost_ItemsChanged()
    {
        UnitVmModel model = (UnitVmModel) getWindow();
        if (!model.getIsNew())
        {
            UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
            VM vm = (VM) selectedItem.getEntity();
            VDS host = null;

            for (VDS item : model.getDefaultHost().getItems())
            {
                if (item.getId().equals(((vm.getDedicatedVmForVds() != null) ? vm.getDedicatedVmForVds()
                        : Guid.Empty)))
                {
                    host = item;
                    break;
                }
            }
            if (host == null)
            {
                model.getIsAutoAssign().setEntity(true);
            }
            else
            {
                model.getDefaultHost().setSelectedItem(host);
                model.getIsAutoAssign().setEntity(false);
            }
        }
    }

    private void vmModel_DisplayProtocol_ItemsChanged()
    {
        UnitVmModel model = (UnitVmModel) getWindow();
        if (!model.getIsNew())
        {
            UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
            VM vm = (VM) selectedItem.getEntity();
            EntityModel<DisplayType> displayType = null;

            for (EntityModel<DisplayType> item : model.getDisplayProtocol().getItems())
            {
                DisplayType dt = item.getEntity();
                if (dt == vm.getDefaultDisplayType())
                {
                    displayType = item;
                    break;
                }
            }
            model.getDisplayProtocol().setSelectedItem(displayType);
        }
    }

    private Integer cachedMaxPriority;

    private void vmModel_Priority_ItemsChanged()
    {
        UnitVmModel model = (UnitVmModel) getWindow();
        if (!model.getIsNew())
        {
            if (cachedMaxPriority == null) {
                AsyncDataProvider.getInstance().getMaxVmPriority(new AsyncQuery(model,
                                                                                new INewAsyncCallback() {
                                                                                    @Override
                                                                                    public void onSuccess(Object target, Object returnValue) {
                                                                                        cachedMaxPriority = (Integer) returnValue;
                                                                                        updatePriority((UnitVmModel) target);
                                                                                    }
                                                                                }, model.getHash()));
            } else {
                updatePriority(model);
            }
        }
    }

    private void updatePriority(UnitVmModel model) {
        UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
        VM vm = (VM) selectedItem.getEntity();
        int roundPriority = AsyncDataProvider.getInstance().getRoundedPriority(vm.getPriority(), cachedMaxPriority);
        EntityModel<Integer> priority = null;

        for (EntityModel<Integer> a : model.getPriority().getItems())
        {
            int p = a.getEntity();
            if (p == roundPriority)
            {
                priority = a;
                break;
            }
        }
        ((UnitVmModel) model.getWindow()).getPriority().setSelectedItem(priority);
    }

    private void vmModel_TimeZone_ItemsChanged()
    {
        UnitVmModel model = (UnitVmModel) getWindow();
        if (!model.getIsNew())
        {
            UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
            VM vm = (VM) selectedItem.getEntity();

            if (!StringHelper.isNullOrEmpty(vm.getTimeZone()))
            {
                model.getTimeZone().setSelectedItem(Linq.firstOrDefault(model.getTimeZone().getItems(),
                        new Linq.TimeZonePredicate(vm.getTimeZone())));
            }
        }
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        UnitVmModel model = (UnitVmModel) getWindow();
        if (ev.matchesDefinition(itemsChangedEventDefinition) && sender == model.getDataCenterWithClustersList())
        {
            updateDataCenterWithCluster();
        }
        else if (ev.matchesDefinition(itemsChangedEventDefinition) && sender == model.getDefaultHost())
        {
            vmModel_DefaultHost_ItemsChanged();
        }
        else if (ev.matchesDefinition(itemsChangedEventDefinition) && sender == model.getDisplayProtocol())
        {
            vmModel_DisplayProtocol_ItemsChanged();
        }
        else if (ev.matchesDefinition(itemsChangedEventDefinition) && sender == model.getPriority())
        {
            vmModel_Priority_ItemsChanged();
        }
        else if (ev.matchesDefinition(itemsChangedEventDefinition) && sender == model.getTimeZone())
        {
            vmModel_TimeZone_ItemsChanged();
        }
    }

    @Override
    public void onVmAndPoolLoad() {
        if (getvms() != null && getpools() != null) {
            // Complete search.

            // Remove pools that has provided VMs.
            ArrayList<VmPool> filteredPools = new ArrayList<VmPool>();
            for (VmPool pool : getpools()) {
                // Add pool to map.

                int attachedVmsCount = 0;
                for (VM vm : getvms()) {
                    if (vm.getVmPoolId() != null && vm.getVmPoolId().equals(pool.getVmPoolId())) {
                        attachedVmsCount++;
                    }
                }

                if (attachedVmsCount < pool.getMaxAssignedVmsPerUser()) {
                    filteredPools.add(pool);
                }
            }

            // Merge VMs and Pools, and create item models.
            final List all = Linq.concat(getvms(), filteredPools);

            if (filteredPools.isEmpty()) {
                finishSearch(all);
            } else { // if we have pools we have to update their console cache and THEN finish search
                List<VdcQueryType> poolQueryList = new ArrayList<VdcQueryType>();
                List<VdcQueryParametersBase> poolParamList = new ArrayList<VdcQueryParametersBase>();

                for (VmPool p : filteredPools) {
                    poolQueryList.add(VdcQueryType.GetVmDataByPoolId);
                    poolParamList.add(new IdQueryParameters(p.getVmPoolId()));
                }

                Frontend.getInstance().runMultipleQueries(
                        poolQueryList, poolParamList,
                        new IFrontendMultipleQueryAsyncCallback() {
                            @Override
                            public void executed(FrontendMultipleQueryAsyncResult result) {
                                List<VM> poolRepresentants = new LinkedList<VM>();
                                List<VdcQueryReturnValue> poolRepresentantsRetval = result.getReturnValues();
                                for (VdcQueryReturnValue poolRepresentant : poolRepresentantsRetval) { // extract from return value
                                    poolRepresentants.add((VM) poolRepresentant.getReturnValue());
                                }
                                consoleModelsCache.updatePoolCache(poolRepresentants);
                                finishSearch(all);
                            }});
            }
        }
    }

    private void finishSearch(List vmsAndFilteredPools) {
        consoleModelsCache.updateVmCache(getvms());

        Collections.sort(vmsAndFilteredPools, new NameableComparator());

        ArrayList<Model> items = new ArrayList<Model>();
        for (Object item : vmsAndFilteredPools) {
            VmConsoles consoles = consoleModelsCache.getVmConsolesForEntity(item);
            UserPortalItemModel model = new UserPortalItemModel(item, consoles);
            model.setEntity(item);
            items.add(model);
        }

        setItems(items);

        setvms(null);
        setpools(null);

        getSearchCompletedEvent().raise(this, EventArgs.EMPTY);
    }

    @Override
    protected String getListName() {
        return "UserPortalListModel"; //$NON-NLS-1$
    }

    @Override
    protected ConsoleContext getConsoleContext() {
        return ConsoleContext.UP_EXTENDED;
    }
}
