package org.ovirt.engine.ui.uicommonweb.models.userportal;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.AddVmFromScratchParameters;
import org.ovirt.engine.core.common.action.AddVmFromTemplateParameters;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.action.ChangeVMClusterParameters;
import org.ovirt.engine.core.common.action.RemoveVmParameters;
import org.ovirt.engine.core.common.action.RunVmOnceParams;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.queries.GetAllVmPoolsAttachedToUserParameters;
import org.ovirt.engine.core.common.queries.GetUserVmsByUserIdAndGroupsParameters;
import org.ovirt.engine.core.common.queries.GetVmByVmIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.EventDefinition;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.BootSequenceModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.NewTemplateVmModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.RdpConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RunOnceModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UserPortalExistingVmModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.UserPortalNewVmModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmAppListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmMonitorModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncConsoleModel;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

@SuppressWarnings("unused")
public class UserPortalListModel extends IUserPortalListModel implements IVmPoolResolutionService
{

    public static EventDefinition SearchCompletedEventDefinition;
    private Event privateSearchCompletedEvent;

    public Event getSearchCompletedEvent()
    {
        return privateSearchCompletedEvent;
    }

    private void setSearchCompletedEvent(Event value)
    {
        privateSearchCompletedEvent = value;
    }

    private UICommand privateNewDesktopCommand;

    public UICommand getNewDesktopCommand()
    {
        return privateNewDesktopCommand;
    }

    private void setNewDesktopCommand(UICommand value)
    {
        privateNewDesktopCommand = value;
    }

    private UICommand privateNewServerCommand;

    public UICommand getNewServerCommand()
    {
        return privateNewServerCommand;
    }

    private void setNewServerCommand(UICommand value)
    {
        privateNewServerCommand = value;
    }

    private UICommand privateEditCommand;

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

    private UnitVmModel vmModel;

    public UnitVmModel getVmModel()
    {
        return vmModel;
    }

    public void setVmModel(UnitVmModel value)
    {
        if (vmModel != value)
        {
            vmModel = value;
            OnPropertyChanged(new PropertyChangedEventArgs("VmModel"));
        }
    }

    private ConfirmationModel confirmationModel;

    public ConfirmationModel getConfirmationModel()
    {
        return confirmationModel;
    }

    public void setConfirmationModel(ConfirmationModel value)
    {
        if (confirmationModel != value)
        {
            confirmationModel = value;
            OnPropertyChanged(new PropertyChangedEventArgs("ConfirmationModel"));
        }
    }

    private RunOnceModel runOnceModel;

    public RunOnceModel getRunOnceModel()
    {
        return runOnceModel;
    }

    public void setRunOnceModel(RunOnceModel value)
    {
        if (runOnceModel != value)
        {
            runOnceModel = value;
            OnPropertyChanged(new PropertyChangedEventArgs("RunOnceModel"));
        }
    }

    private AttachCdModel attachCdModel;

    public AttachCdModel getAttachCdModel()
    {
        return attachCdModel;
    }

    public void setAttachCdModel(AttachCdModel value)
    {
        if (attachCdModel != value)
        {
            attachCdModel = value;
            OnPropertyChanged(new PropertyChangedEventArgs("AttachCdModel"));
        }
    }

    private EntityModel vmGeneralModel;
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
    private java.util.ArrayList<VM> privatevms;

    public java.util.ArrayList<VM> getvms()
    {
        return privatevms;
    }

    public void setvms(java.util.ArrayList<VM> value)
    {
        privatevms = value;
    }

    private java.util.ArrayList<vm_pools> privatepools;

    public java.util.ArrayList<vm_pools> getpools()
    {
        return privatepools;
    }

    public void setpools(java.util.ArrayList<vm_pools> value)
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

    private storage_domains privatestorageDomain;

    public storage_domains getstorageDomain()
    {
        return privatestorageDomain;
    }

    public void setstorageDomain(storage_domains value)
    {
        privatestorageDomain = value;
    }

    private java.util.ArrayList<String> CustomPropertiesKeysList;
    private java.util.HashMap<Guid, java.util.ArrayList<ConsoleModel>> cachedConsoleModels;

    static
    {
        SearchCompletedEventDefinition = new EventDefinition("SearchCompleted", UserPortalListModel.class);
    }

    public UserPortalListModel()
    {
        setSearchCompletedEvent(new Event(SearchCompletedEventDefinition));

        cachedConsoleModels = new java.util.HashMap<Guid, java.util.ArrayList<ConsoleModel>>();

        setNewDesktopCommand(new UICommand("NewDesktop", this));
        setNewServerCommand(new UICommand("NewServer", this));
        setEditCommand(new UICommand("Edit", this));
        setRemoveCommand(new UICommand("Remove", this));
        setSaveCommand(new UICommand("Save", this));
        setRunOnceCommand(new UICommand("RunOnce", this));
        setChangeCdCommand(new UICommand("ChangeCD", this));
        setNewTemplateCommand(new UICommand("NewTemplate", this));

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                UserPortalListModel userPortalListModel = (UserPortalListModel) model;
                if (result != null)
                {
                    userPortalListModel.CustomPropertiesKeysList = new java.util.ArrayList<String>();
                    for (String s : ((String) result).split("[;]", -1))
                    {
                        userPortalListModel.CustomPropertiesKeysList.add(s);
                    }
                }

            }
        };
        AsyncDataProvider.GetCustomPropertiesList(_asyncQuery);
    }

    @Override
    protected void SyncSearch()
    {
        super.SyncSearch();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                UserPortalListModel userPortalListModel = (UserPortalListModel) model;
                userPortalListModel.setvms((java.util.ArrayList<VM>) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
                userPortalListModel.OnVmAndPoolLoad();
            }
        };

        Frontend.RunQuery(VdcQueryType.GetUserVmsByUserIdAndGroups,
                new GetUserVmsByUserIdAndGroupsParameters(Frontend.getLoggedInUser().getUserId()),
                _asyncQuery);

        AsyncQuery _asyncQuery1 = new AsyncQuery();
        _asyncQuery1.setModel(this);
        _asyncQuery1.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                UserPortalListModel userPortalListModel = (UserPortalListModel) model;
                userPortalListModel.setpools((java.util.ArrayList<vm_pools>) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
                userPortalListModel.OnVmAndPoolLoad();
            }
        };

        Frontend.RunQuery(VdcQueryType.GetAllVmPoolsAttachedToUser,
                new GetAllVmPoolsAttachedToUserParameters(Frontend.getLoggedInUser().getUserId()),
                _asyncQuery1);

    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();
        SyncSearch();
    }

    @Override
    protected void InitDetailModels()
    {
        super.InitDetailModels();

        vmGeneralModel = new VmGeneralModel();
        vmGeneralModel.setIsAvailable(false);

        vmSnapshotListModel = new VmSnapshotListModel();
        vmSnapshotListModel.setIsAvailable(false);

        vmMonitorModel = new VmMonitorModel();
        vmMonitorModel.setIsAvailable(false);

        vmDiskListModel = new VmDiskListModel();
        vmDiskListModel.setIsAvailable(false);

        vmInterfaceListModel = new VmInterfaceListModel();
        vmInterfaceListModel.setIsAvailable(false);

        permissionListModel = new PermissionListModel();
        permissionListModel.setIsAvailable(false);

        vmEventListModel = new VmEventListModel();
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

        setDetailModels(list);

        permissionListModel.setIsAvailable(true);
        vmEventListModel.setIsAvailable(true);
        vmAppListModel.setIsAvailable(true);
    }

    @Override
    protected Object ProvideDetailModelEntity(Object selectedItem)
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
    protected void UpdateDetailsAvailability()
    {
        super.UpdateDetailsAvailability();

        UserPortalItemModel item = (UserPortalItemModel) getSelectedItem();

        vmGeneralModel.setIsAvailable(item != null && !item.getIsPool());
        vmSnapshotListModel.setIsAvailable(item != null && !item.getIsPool());
        vmMonitorModel.setIsAvailable(item != null && !item.getIsPool());
        vmDiskListModel.setIsAvailable(item != null && !item.getIsPool());
        vmInterfaceListModel.setIsAvailable(item != null && !item.getIsPool());
        vmEventListModel.setIsAvailable(item != null && !item.getIsPool());

        poolGeneralModel.setIsAvailable(item != null && item.getIsPool());
        poolDiskListModel.setIsAvailable(item != null && item.getIsPool());
        poolInterfaceListModel.setIsAvailable(item != null && item.getIsPool());
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getNewDesktopCommand())
        {
            NewDesktop();
        }
        if (command == getNewServerCommand())
        {
            NewServer();
        }
        else if (command == getEditCommand())
        {
            Edit();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }
        else if (command == getSaveCommand())
        {
            OnSave();
        }
        else if (command == getRunOnceCommand())
        {
            RunOnce();
        }
        else if (command == getChangeCdCommand())
        {
            ChangeCD();
        }
        else if (command == getNewTemplateCommand())
        {
            NewTemplate();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel"))
        {
            Cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove"))
        {
            OnRemove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRunOnce"))
        {
            OnRunOnce();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnChangeCD"))
        {
            OnChangeCD();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnNewTemplate"))
        {
            OnNewTemplate();
        }
    }

    private void NewTemplate()
    {
        UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
        if (selectedItem == null)
        {
            return;
        }

        VM vm = (VM) selectedItem.getEntity();
        setVmModel(new UnitVmModel(new NewTemplateVmModelBehavior(vm)));
        getVmModel().setTitle("New Template");
        getVmModel().setHashName("new_template");
        getVmModel().setIsNew(true);
        getVmModel().setVmType(vm.getvm_type());
        getVmModel().Initialize(null);

        UICommand tempVar = new UICommand("OnNewTemplate", this);
        tempVar.setTitle("OK");
        tempVar.setIsDefault(true);
        getVmModel().getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this);
        tempVar2.setTitle("Cancel");
        tempVar2.setIsCancel(true);
        getVmModel().getCommands().add(tempVar2);
    }

    private void OnNewTemplate()
    {
        UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
        if (selectedItem == null)
        {
            Cancel();
            return;
        }

        UnitVmModel model = vmModel;

        if (!model.Validate())
        {
            model.setIsValid(false);
        }
        else
        {
            String name = (String) model.getName().getEntity();

            // Check name unicitate.
            AsyncDataProvider.IsTemplateNameUnique(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {

                            UserPortalListModel userPortalListModel = (UserPortalListModel) target;
                            boolean isNameUnique = (Boolean) returnValue;
                            if (!isNameUnique)
                            {
                                userPortalListModel.getVmModel().getName().getInvalidityReasons().clear();
                                userPortalListModel.getVmModel()
                                        .getName()
                                        .getInvalidityReasons()
                                        .add("Name must be unique.");
                                userPortalListModel.getVmModel().getName().setIsValid(false);
                                userPortalListModel.getVmModel().setIsValid(false);
                            }
                            else
                            {
                                userPortalListModel.PostNameUniqueCheck(userPortalListModel);
                                Cancel();
                            }

                        }
                    }),
                    name);
        }
    }

    public void PostNameUniqueCheck(UserPortalListModel userPortalListModel)
    {
        UnitVmModel model = userPortalListModel.getVmModel();
        UserPortalItemModel selectedItem = (UserPortalItemModel) userPortalListModel.getSelectedItem();
        VM vm = (VM) selectedItem.getEntity();

        VM tempVar = new VM();
        tempVar.setvm_guid(vm.getvm_guid());
        tempVar.setvm_type(model.getVmType());
        tempVar.setvm_os((VmOsType) model.getOSType().getSelectedItem());
        tempVar.setnum_of_monitors((Integer) model.getNumOfMonitors().getSelectedItem());
        tempVar.setvm_domain(model.getDomain().getIsAvailable() ? (String) model.getDomain().getSelectedItem() : "");
        tempVar.setvm_mem_size_mb((Integer) model.getMemSize().getEntity());
        tempVar.setMinAllocatedMem((Integer) model.getMinAllocatedMemory().getEntity());
        tempVar.setvds_group_id(((VDSGroup) model.getCluster().getSelectedItem()).getID());
        tempVar.settime_zone(model.getTimeZone().getIsAvailable() && model.getTimeZone().getSelectedItem() != null ? ((java.util.Map.Entry<String, String>) model.getTimeZone()
                .getSelectedItem()).getKey()
                : "");
        tempVar.setnum_of_sockets((Integer) model.getNumOfSockets().getEntity());
        tempVar.setcpu_per_socket((Integer) model.getTotalCPUCores().getEntity()
                / (Integer) model.getNumOfSockets().getEntity());
        tempVar.setusb_policy((UsbPolicy) model.getUsbPolicy().getSelectedItem());
        tempVar.setis_auto_suspend(false);
        tempVar.setis_stateless((Boolean) model.getIsStateless().getEntity());
        tempVar.setdefault_boot_sequence(model.getBootSequence());
        tempVar.setauto_startup((Boolean) model.getIsHighlyAvailable().getEntity());
        tempVar.setiso_path(model.getCdImage().getIsChangable() ? (String) model.getCdImage().getSelectedItem() : "");
        tempVar.setinitrd_url(vm.getinitrd_url());
        tempVar.setkernel_url(vm.getkernel_url());
        tempVar.setkernel_params(vm.getkernel_params());
        VM newvm = tempVar;

        EntityModel displayProtocolSelectedItem = (EntityModel) model.getDisplayProtocol().getSelectedItem();
        newvm.setdefault_display_type((DisplayType) displayProtocolSelectedItem.getEntity());

        EntityModel prioritySelectedItem = (EntityModel) model.getPriority().getSelectedItem();
        newvm.setpriority((Integer) prioritySelectedItem.getEntity());

        AddVmTemplateParameters addVmTemplateParameters =
                new AddVmTemplateParameters(newvm,
                        (String) model.getName().getEntity(),
                        (String) model.getDescription().getEntity());
        addVmTemplateParameters.setDestinationStorageDomainId(((storage_domains) model.getStorageDomain()
                .getSelectedItem()).getid());
        addVmTemplateParameters.setPublicUse((Boolean) model.getIsTemplatePublic().getEntity());

        Frontend.RunAction(VdcActionType.AddVmTemplate, addVmTemplateParameters,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                    }
                }, this);
    }

    @Override
    protected void OnSelectedItemChanged()
    {
        super.OnSelectedItemChanged();
        UpdateActionAvailability();
    }

    private void RunOnce()
    {
        UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
        if (selectedItem == null || selectedItem.getEntity() == null)
        {
            return;
        }

        VM vm = (VM) selectedItem.getEntity();

        RunOnceModel model = new RunOnceModel();
        setRunOnceModel(model);
        model.setTitle("Run Virtual Machine(s)");
        model.setHashName("run_virtual_machine");
        model.getAttachIso().setEntity(false);
        model.getAttachFloppy().setEntity(false);
        model.getRunAsStateless().setEntity(vm.getis_stateless());
        model.getRunAndPause().setEntity(false);
        model.setHwAcceleration(true);

        AsyncQuery _asyncQuery0 = new AsyncQuery();
        _asyncQuery0.setModel(this);

        _asyncQuery0.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model0, Object result0)
            {
                if (result0 != null)
                {
                    storage_domains isoDomain = (storage_domains) result0;
                    UserPortalListModel thisUserPortalListModel = (UserPortalListModel) model0;

                    AsyncQuery _asyncQuery01 = new AsyncQuery();
                    _asyncQuery01.setModel(thisUserPortalListModel);

                    _asyncQuery01.asyncCallback = new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object model1, Object result)
                        {
                            UserPortalListModel userPortalListModel = (UserPortalListModel) model1;
                            RunOnceModel runOnceModel = userPortalListModel.getRunOnceModel();
                            java.util.ArrayList<String> images = (java.util.ArrayList<String>) result;
                            runOnceModel.getIsoImage().setItems(images);

                            if (runOnceModel.getIsoImage().getIsChangable()
                                    && runOnceModel.getIsoImage().getSelectedItem() == null)
                            {
                                runOnceModel.getIsoImage().setSelectedItem(Linq.FirstOrDefault(images));
                            }
                        }
                    };
                    AsyncDataProvider.GetIrsImageList(_asyncQuery01, isoDomain.getid(), false);

                    AsyncQuery _asyncQuery02 = new AsyncQuery();
                    _asyncQuery02.setModel(thisUserPortalListModel);

                    _asyncQuery02.asyncCallback = new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object model2, Object result)
                        {
                            UserPortalListModel userPortalListModel = (UserPortalListModel) model2;
                            UserPortalItemModel userPortalItemModel =
                                    (UserPortalItemModel) userPortalListModel.getSelectedItem();
                            RunOnceModel runOnceModel = userPortalListModel.getRunOnceModel();
                            VM selectedVM = (VM) userPortalItemModel.getEntity();
                            java.util.ArrayList<String> images = (java.util.ArrayList<String>) result;

                            if (DataProvider.IsWindowsOsType(selectedVM.getvm_os()))
                            {
                                // Add a pseudo floppy disk image used for Windows' sysprep.
                                if (!selectedVM.getis_initialized())
                                {
                                    images.add(0, "[sysprep]");
                                    runOnceModel.getAttachFloppy().setEntity(true);
                                }
                                else
                                {
                                    images.add("[sysprep]");
                                }
                            }
                            runOnceModel.getFloppyImage().setItems(images);

                            if (runOnceModel.getFloppyImage().getIsChangable()
                                    && runOnceModel.getFloppyImage().getSelectedItem() == null)
                            {
                                runOnceModel.getFloppyImage().setSelectedItem(Linq.FirstOrDefault(images));
                            }
                        }
                    };
                    AsyncDataProvider.GetFloppyImageList(_asyncQuery02, isoDomain.getid(), false);
                }

            }
        };
        AsyncDataProvider.GetIsoDomainByDataCenterId(_asyncQuery0, vm.getstorage_pool_id());

        // passing Kernel parameters
        model.getKernel_parameters().setEntity(vm.getkernel_params());
        model.getKernel_path().setEntity(vm.getkernel_url());
        model.getInitrd_path().setEntity(vm.getinitrd_url());

        model.getCustomProperties().setEntity(vm.getCustomProperties());

        model.setIsLinux_Unassign_UnknownOS(DataProvider.IsLinuxOsType(vm.getvm_os())
                || vm.getvm_os() == VmOsType.Unassigned || vm.getvm_os() == VmOsType.Other);

        model.setIsWindowsOS(DataProvider.IsWindowsOsType(vm.getvm_os()));
        model.getIsVmFirstRun().setEntity(!vm.getis_initialized());
        model.getSysPrepDomainName().setSelectedItem(vm.getvm_domain());

        // Update Domain list
        AsyncDataProvider.GetDomainList(new AsyncQuery(model,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue1) {

                        RunOnceModel runOnceModel = (RunOnceModel) target;
                        java.util.List<String> domains = (java.util.List<String>) returnValue1;
                        String oldDomain = (String) runOnceModel.getSysPrepDomainName().getSelectedItem();
                        if (oldDomain != null && !oldDomain.equals("") && !domains.contains(oldDomain))
                        {
                            domains.add(0, oldDomain);
                        }
                        runOnceModel.getSysPrepDomainName().setItems(domains);
                        runOnceModel.getSysPrepDomainName().setSelectedItem((oldDomain != null) ? oldDomain
                                : Linq.FirstOrDefault(domains));

                    }
                }),
                true);

        // Display protocols.
        EntityModel tempVar = new EntityModel();
        tempVar.setTitle("VNC");
        tempVar.setEntity(DisplayType.vnc);
        EntityModel vncProtocol = tempVar;

        EntityModel tempVar2 = new EntityModel();
        tempVar2.setTitle("Spice");
        tempVar2.setEntity(DisplayType.qxl);
        EntityModel qxlProtocol = tempVar2;

        java.util.ArrayList<EntityModel> items = new java.util.ArrayList<EntityModel>();
        items.add(vncProtocol);
        items.add(qxlProtocol);
        model.getDisplayProtocol().setItems(items);
        model.getDisplayProtocol().setSelectedItem(vm.getdefault_display_type() == DisplayType.vnc ? vncProtocol
                : qxlProtocol);

        model.setCustomPropertiesKeysList(this.CustomPropertiesKeysList);

        // Boot sequence.
        AsyncQuery _asyncQuery2 = new AsyncQuery();
        _asyncQuery2.setModel(this);

        _asyncQuery2.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model3, Object ReturnValue)
            {
                UserPortalListModel userPortalListModel = (UserPortalListModel) model3;
                boolean hasNics =
                        ((java.util.ArrayList<VmNetworkInterface>) ((VdcQueryReturnValue) ReturnValue).getReturnValue()).size() > 0;

                if (!hasNics)
                {
                    BootSequenceModel bootSequenceModel = userPortalListModel.getRunOnceModel().getBootSequence();
                    bootSequenceModel.getNetworkOption().setIsChangable(false);
                    bootSequenceModel.getNetworkOption()
                            .getChangeProhibitionReasons()
                            .add("Virtual Machine must have at least one network interface defined to boot from network.");
                }
            }
        };

        Frontend.RunQuery(VdcQueryType.GetVmInterfacesByVmId, new GetVmByVmIdParameters(vm.getvm_guid()), _asyncQuery2);

        UICommand tempVar3 = new UICommand("OnRunOnce", this);
        tempVar3.setTitle("OK");
        tempVar3.setIsDefault(true);
        model.getCommands().add(tempVar3);
        UICommand tempVar4 = new UICommand("Cancel", this);
        tempVar4.setTitle("Cancel");
        tempVar4.setIsCancel(true);
        model.getCommands().add(tempVar4);
    }

    private void OnRunOnce()
    {
        UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
        if (selectedItem == null || selectedItem.getEntity() == null)
        {
            Cancel();
            return;
        }

        VM vm = (VM) selectedItem.getEntity();

        RunOnceModel model = runOnceModel;

        if (!model.Validate())
        {
            return;
        }

        BootSequenceModel bootSequenceModel = model.getBootSequence();

        RunVmOnceParams tempVar = new RunVmOnceParams();
        tempVar.setVmId(vm.getvm_guid());
        tempVar.setBootSequence(bootSequenceModel.getSequence());
        tempVar.setDiskPath((Boolean) model.getAttachIso().getEntity() ? (String) model.getIsoImage().getSelectedItem()
                : "");
        tempVar.setFloppyPath(model.getFloppyImagePath());
        tempVar.setKvmEnable(model.getHwAcceleration());
        tempVar.setRunAndPause((Boolean) model.getRunAndPause().getEntity());
        tempVar.setAcpiEnable(true);
        tempVar.setRunAsStateless((Boolean) model.getRunAsStateless().getEntity());
        tempVar.setReinitialize(model.getReinitialize());
        tempVar.setCustomProperties((String) model.getCustomProperties().getEntity());
        RunVmOnceParams param = tempVar;

        // kernel params
        if (model.getKernel_path().getEntity() != null)
        {
            param.setkernel_url((String) model.getKernel_path().getEntity());
        }
        if (model.getKernel_parameters().getEntity() != null)
        {
            param.setkernel_params((String) model.getKernel_parameters().getEntity());
        }
        if (model.getInitrd_path().getEntity() != null)
        {
            param.setinitrd_url((String) model.getInitrd_path().getEntity());
        }

        // Sysprep params
        if (model.getSysPrepDomainName().getSelectedItem() != null)
        {
            param.setSysPrepDomainName((String) model.getSysPrepDomainName().getSelectedItem());
        }
        if (model.getSysPrepUserName().getEntity() != null)
        {
            param.setSysPrepUserName((String) model.getSysPrepUserName().getEntity());
        }
        if (model.getSysPrepPassword().getEntity() != null)
        {
            param.setSysPrepPassword((String) model.getSysPrepPassword().getEntity());
        }

        EntityModel displayProtocolSelectedItem = (EntityModel) model.getDisplayProtocol().getSelectedItem();
        param.setUseVnc((DisplayType) displayProtocolSelectedItem.getEntity() == DisplayType.vnc);

        Frontend.RunAction(VdcActionType.RunVmOnce, param,
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                    }
                }, this);

        Cancel();
    }

    private void UpdateActionAvailability()
    {
        UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();

        getEditCommand().setIsExecutionAllowed(selectedItem != null && !selectedItem.getIsPool());

        getRemoveCommand().setIsExecutionAllowed(selectedItem != null
                && !selectedItem.getIsPool()
                && VdcActionUtils.CanExecute(new java.util.ArrayList<VM>(java.util.Arrays.asList(new VM[] { (VM) selectedItem.getEntity() })),
                        VM.class,
                        VdcActionType.RemoveVm));

        getRunOnceCommand().setIsExecutionAllowed(selectedItem != null
                && !selectedItem.getIsPool()
                && VdcActionUtils.CanExecute(new java.util.ArrayList<VM>(java.util.Arrays.asList(new VM[] { (VM) selectedItem.getEntity() })),
                        VM.class,
                        VdcActionType.RunVmOnce));

        getChangeCdCommand().setIsExecutionAllowed(selectedItem != null
                && !selectedItem.getIsPool()
                && VdcActionUtils.CanExecute(new java.util.ArrayList<VM>(java.util.Arrays.asList(new VM[] { (VM) selectedItem.getEntity() })),
                        VM.class,
                        VdcActionType.ChangeDisk));

        getNewTemplateCommand().setIsExecutionAllowed(selectedItem != null
                && !selectedItem.getIsPool()
                && VdcActionUtils.CanExecute(new java.util.ArrayList<VM>(java.util.Arrays.asList(new VM[] { (VM) selectedItem.getEntity() })),
                        VM.class,
                        VdcActionType.AddVmTemplate));
    }

    private void NewDesktop()
    {
        NewInternal(VmType.Desktop);
    }

    private void NewServer()
    {
        NewInternal(VmType.Server);
    }

    private void NewInternal(VmType vmType)
    {
        setVmModel(new UnitVmModel(new UserPortalNewVmModelBehavior()));
        getVmModel().setTitle("New " + (vmType == VmType.Server ? "Server" : "Desktop") + " Virtual Machine");
        vmModel.setHashName(getVmModel().getTitle().toLowerCase().replace(' ', '_'));
        getVmModel().setIsNew(true);
        getVmModel().setVmType(vmType);
        getVmModel().setCustomPropertiesKeysList(CustomPropertiesKeysList);

        getVmModel().Initialize(null);

        // Ensures that the default provisioning is "Clone" for a new server and "Thin" for a new desktop.
        EntityModel selectedItem = null;
        boolean selectValue = getVmModel().getVmType() == VmType.Server;

        for (Object item : getVmModel().getProvisioning().getItems())
        {
            EntityModel a = (EntityModel) item;
            if ((Boolean) a.getEntity() == selectValue)
            {
                selectedItem = a;
                break;
            }
        }
        getVmModel().getProvisioning().setSelectedItem(selectedItem);
    }

    private void Edit()
    {
        UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
        if (selectedItem == null)
        {
            return;
        }

        VM vm = (VM) selectedItem.getEntity();

        setVmModel(new UnitVmModel(new UserPortalExistingVmModelBehavior(vm)));
        getVmModel().setTitle("Edit " + (vm.getvm_type() == VmType.Server ? "Server" : "Desktop") + " Virtual Machine");
        vmModel.setHashName(getVmModel().getTitle().toLowerCase().replace(' ', '_'));
        getVmModel().setVmType(vm.getvm_type());
        getVmModel().setCustomPropertiesKeysList(CustomPropertiesKeysList);

        getVmModel().Initialize(null);
    }

    private void remove()
    {
        UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
        VM vm = (VM) selectedItem.getEntity();

        setConfirmationModel(new ConfirmationModel());
        getConfirmationModel().setTitle("Remove Virtual Machine");
        getConfirmationModel().setHashName("remove_virtual_machine");
        getConfirmationModel().setMessage("Virtual Machine");

        java.util.ArrayList<String> list = new java.util.ArrayList<String>();
        list.add(vm.getvm_name());
        getConfirmationModel().setItems(list);

        UICommand tempVar = new UICommand("OnRemove", this);
        tempVar.setTitle("OK");
        tempVar.setIsDefault(true);
        getConfirmationModel().getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this);
        tempVar2.setTitle("Cancel");
        tempVar2.setIsCancel(true);
        getConfirmationModel().getCommands().add(tempVar2);
    }

    private void OnRemove()
    {
        UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
        VM vm = (VM) selectedItem.getEntity();

        Frontend.RunAction(VdcActionType.RemoveVm, new RemoveVmParameters(vm.getvm_guid(), false),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                    }
                }, this);

        Cancel();
    }

    private void ChangeCD()
    {
        UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
        if (selectedItem == null || selectedItem.getEntity() == null)
        {
            return;
        }

        VM vm = (VM) selectedItem.getEntity();

        AttachCdModel model = new AttachCdModel();
        setAttachCdModel(model);
        model.setTitle("Change CD");
        model.setHashName("change_cd");

        AsyncQuery _asyncQuery0 = new AsyncQuery();
        _asyncQuery0.setModel(this);

        _asyncQuery0.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model0, Object result0)
            {
                UserPortalListModel userPortalListModel0 = (UserPortalListModel) model0;
                java.util.ArrayList<String> images0 =
                        new java.util.ArrayList<String>(java.util.Arrays.asList(new String[] { "No CDs" }));
                userPortalListModel0.getAttachCdModel().getIsoImage().setItems(images0);
                userPortalListModel0.getAttachCdModel().getIsoImage().setSelectedItem(Linq.FirstOrDefault(images0));

                if (result0 != null)
                {
                    storage_domains isoDomain = (storage_domains) result0;

                    AsyncQuery _asyncQuery = new AsyncQuery();
                    _asyncQuery.setModel(userPortalListModel0);

                    _asyncQuery.asyncCallback = new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object model1, Object result)
                        {
                            UserPortalListModel userPortalListModel = (UserPortalListModel) model1;
                            AttachCdModel _attachCdModel = userPortalListModel.getAttachCdModel();
                            java.util.ArrayList<String> images = (java.util.ArrayList<String>) result;
                            if (images.size() > 0)
                            {
                                images.add(0, ConsoleModel.EjectLabel);
                                _attachCdModel.getIsoImage().setItems(images);
                            }
                            if (_attachCdModel.getIsoImage().getIsChangable())
                            {
                                _attachCdModel.getIsoImage().setSelectedItem(Linq.FirstOrDefault(images));
                            }
                        }
                    };
                    AsyncDataProvider.GetIrsImageList(_asyncQuery, isoDomain.getid(), false);
                }
            }
        };

        AsyncDataProvider.GetIsoDomainByDataCenterId(_asyncQuery0, vm.getstorage_pool_id());

        UICommand tempVar = new UICommand("OnChangeCD", this);
        tempVar.setTitle("OK");
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this);
        tempVar2.setTitle("Cancel");
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private void OnChangeCD()
    {
        UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
        if (selectedItem == null || selectedItem.getEntity() == null)
        {
            Cancel();
            return;
        }

        VM vm = (VM) selectedItem.getEntity();
        AttachCdModel model = getAttachCdModel();
        String isoName =
                (StringHelper.stringsEqual(model.getIsoImage().getSelectedItem().toString(), ConsoleModel.EjectLabel)) ? ""
                        : model.getIsoImage().getSelectedItem().toString();

        Frontend.RunAction(VdcActionType.ChangeDisk, new ChangeDiskCommandParameters(vm.getvm_guid(), isoName),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                    }
                }, this);
    }

    private void OnSave()
    {
        UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
        if (!getVmModel().getIsNew() && selectedItem.getEntity() == null)
        {
            Cancel();
            return;
        }

        settempVm(getVmModel().getIsNew() ? new VM() : (VM) Cloner.clone(selectedItem.getEntity()));

        if (!getVmModel().Validate())
        {
            return;
        }

        // Check name uniqueness.
        AsyncDataProvider.IsVmNameUnique(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        UserPortalListModel userPortalListModel = (UserPortalListModel) target;
                        boolean isNameUnique = (Boolean) returnValue;
                        String newName = (String) getVmModel().getName().getEntity();
                        String currentName = userPortalListModel.gettempVm().getvm_name();
                        if (!isNameUnique && newName.compareToIgnoreCase(currentName) != 0)
                        {
                            userPortalListModel.getVmModel().getName().getInvalidityReasons().clear();
                            userPortalListModel.getVmModel()
                                    .getName()
                                    .getInvalidityReasons()
                                    .add("Name must be unique.");
                            userPortalListModel.getVmModel().getName().setIsValid(false);
                            userPortalListModel.getVmModel().setIsValid(false);
                            userPortalListModel.getVmModel().setIsGeneralTabValid(false);
                        }
                        else
                        {
                            userPortalListModel.PostVmNameUniqueCheck(userPortalListModel);
                        }

                    }
                }),
                (String) getVmModel().getName().getEntity());
    }

    public void PostVmNameUniqueCheck(UserPortalListModel userPortalListModel)
    {
        UserPortalItemModel selectedItem = (UserPortalItemModel) userPortalListModel.getSelectedItem();
        String name = (String) getVmModel().getName().getEntity();

        // Save changes.
        VmTemplate template = (VmTemplate) getVmModel().getTemplate().getSelectedItem();

        gettempVm().setvm_type(getVmModel().getVmType());
        gettempVm().setvmt_guid(template.getId());
        gettempVm().setvm_name(name);
        gettempVm().setvm_os((VmOsType) getVmModel().getOSType().getSelectedItem());
        gettempVm().setnum_of_monitors((Integer) getVmModel().getNumOfMonitors().getSelectedItem());
        gettempVm().setvm_description((String) getVmModel().getDescription().getEntity());
        gettempVm().setvm_domain(getVmModel().getDomain().getIsAvailable() ? (String) getVmModel().getDomain()
                .getSelectedItem() : "");
        gettempVm().setvm_mem_size_mb((Integer) getVmModel().getMemSize().getEntity());
        gettempVm().setMinAllocatedMem((Integer) getVmModel().getMinAllocatedMemory().getEntity());
        Guid newClusterID = ((VDSGroup) getVmModel().getCluster().getSelectedItem()).getID();
        gettempVm().setvds_group_id(newClusterID);
        gettempVm().settime_zone((getVmModel().getTimeZone().getIsAvailable() && getVmModel().getTimeZone()
                .getSelectedItem() != null) ? ((java.util.Map.Entry<String, String>) getVmModel().getTimeZone()
                .getSelectedItem()).getKey() : "");
        gettempVm().setnum_of_sockets((Integer) getVmModel().getNumOfSockets().getEntity());
        gettempVm().setcpu_per_socket((Integer) getVmModel().getTotalCPUCores().getEntity()
                / (Integer) getVmModel().getNumOfSockets().getEntity());
        gettempVm().setusb_policy((UsbPolicy) getVmModel().getUsbPolicy().getSelectedItem());
        gettempVm().setis_auto_suspend(false);
        gettempVm().setis_stateless((Boolean) getVmModel().getIsStateless().getEntity());
        gettempVm().setdefault_boot_sequence(getVmModel().getBootSequence());
        gettempVm().setiso_path(getVmModel().getCdImage().getIsChangable() ? (String) getVmModel().getCdImage()
                .getSelectedItem() : "");
        gettempVm().setauto_startup((Boolean) getVmModel().getIsHighlyAvailable().getEntity());

        gettempVm().setinitrd_url((String) getVmModel().getInitrd_path().getEntity());
        gettempVm().setkernel_url((String) getVmModel().getKernel_path().getEntity());
        gettempVm().setkernel_params((String) getVmModel().getKernel_parameters().getEntity());

        gettempVm().setCustomProperties((String) getVmModel().getCustomProperties().getEntity());

        EntityModel displayProtocolSelectedItem = (EntityModel) getVmModel().getDisplayProtocol().getSelectedItem();
        gettempVm().setdefault_display_type((DisplayType) displayProtocolSelectedItem.getEntity());

        EntityModel prioritySelectedItem = (EntityModel) getVmModel().getPriority().getSelectedItem();
        gettempVm().setpriority((Integer) prioritySelectedItem.getEntity());

        VDS defaultHost = (VDS) getVmModel().getDefaultHost().getSelectedItem();
        if ((Boolean) getVmModel().getIsAutoAssign().getEntity())
        {
            gettempVm().setdedicated_vm_for_vds(null);
        }
        else
        {
            gettempVm().setdedicated_vm_for_vds(defaultHost.getvds_id());
        }

        gettempVm().setMigrationSupport(MigrationSupport.MIGRATABLE);
        if ((Boolean) getVmModel().getRunVMOnSpecificHost().getEntity())
        {
            gettempVm().setMigrationSupport(MigrationSupport.PINNED_TO_HOST);
        }
        else if ((Boolean) getVmModel().getDontMigrateVM().getEntity())
        {
            gettempVm().setMigrationSupport(MigrationSupport.IMPLICITLY_NON_MIGRATABLE);
        }

        boolean cancel = true;
        if (getVmModel().getIsNew())
        {
            if (gettempVm().getvmt_guid().equals(NGuid.Empty))
            {
                AddVmFromScratchParameters parameters =
                        new AddVmFromScratchParameters(gettempVm(),
                                new java.util.ArrayList<DiskImageBase>(),
                                NGuid.Empty);
                parameters.setMakeCreatorExplicitOwner(true);

                Frontend.RunAction(VdcActionType.AddVmFromScratch, parameters,
                        new IFrontendActionAsyncCallback() {
                            @Override
                            public void Executed(FrontendActionAsyncResult result) {

                            }
                        }, this);
            }
            else
            {
                setstorageDomain((storage_domains) getVmModel().getStorageDomain().getSelectedItem());

                if ((Boolean) ((EntityModel) getVmModel().getProvisioning().getSelectedItem()).getEntity())
                {
                    AsyncQuery _asyncQuery = new AsyncQuery();
                    _asyncQuery.setModel(this);
                    _asyncQuery.asyncCallback = new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object model, Object result)
                        {
                            UserPortalListModel userPortalListModel1 = (UserPortalListModel) model;
                            java.util.ArrayList<DiskImage> templateDisks = (java.util.ArrayList<DiskImage>) result;
                            for (DiskImage templateDisk : templateDisks)
                            {
                                DiskModel disk = null;
                                for (DiskModel a : userPortalListModel1.getVmModel().getDisks())
                                {
                                    if (StringHelper.stringsEqual(a.getName(), templateDisk.getinternal_drive_mapping()))
                                    {
                                        disk = a;
                                        break;
                                    }
                                }

                                templateDisk.setvolume_type((VolumeType) disk.getVolumeType().getSelectedItem());
                                templateDisk.setvolume_format(DataProvider.GetDiskVolumeFormat((VolumeType) disk.getVolumeType()
                                        .getSelectedItem(),
                                        getstorageDomain().getstorage_type()));
                            }

                            java.util.HashMap<String, DiskImageBase> dict =
                                    new java.util.HashMap<String, DiskImageBase>();
                            for (DiskImage a : templateDisks)
                            {
                                dict.put(a.getinternal_drive_mapping(), a);
                            }

                            AddVmFromTemplateParameters parameters =
                                    new AddVmFromTemplateParameters(gettempVm(), dict, getstorageDomain().getid());
                            parameters.setMakeCreatorExplicitOwner(true);

                            Frontend.RunAction(VdcActionType.AddVmFromTemplate, parameters,
                                    new IFrontendActionAsyncCallback() {
                                        @Override
                                        public void Executed(FrontendActionAsyncResult a) {

                                        }
                                    }, this);

                            userPortalListModel1.Cancel();

                        }
                    };
                    AsyncDataProvider.GetTemplateDiskList(_asyncQuery, template.getId());
                    cancel = false;
                }
                else
                {
                    VmManagementParametersBase parameters = new VmManagementParametersBase(gettempVm());
                    parameters.setStorageDomainId(getstorageDomain().getid());
                    parameters.setMakeCreatorExplicitOwner(true);

                    Frontend.RunAction(VdcActionType.AddVm, parameters,
                            new IFrontendActionAsyncCallback() {
                                @Override
                                public void Executed(FrontendActionAsyncResult result) {

                                }
                            }, this);
                }
            }
        }
        else
        {
            Guid oldClusterID = ((VM) selectedItem.getEntity()).getvds_group_id();
            if (oldClusterID.equals(newClusterID) == false)
            {
                Frontend.RunAction(VdcActionType.ChangeVMCluster, new ChangeVMClusterParameters(newClusterID,
                        gettempVm().getvm_guid()),
                        new IFrontendActionAsyncCallback() {
                            @Override
                            public void Executed(FrontendActionAsyncResult result) {

                                Frontend.RunAction(VdcActionType.UpdateVm, new VmManagementParametersBase(gettempVm()),
                                        new IFrontendActionAsyncCallback() {
                                            @Override
                                            public void Executed(FrontendActionAsyncResult a) {

                                            }
                                        }, this);

                            }
                        }, this);
            }
            else
            {
                Frontend.RunAction(VdcActionType.UpdateVm, new VmManagementParametersBase(gettempVm()),
                        new IFrontendActionAsyncCallback() {
                            @Override
                            public void Executed(FrontendActionAsyncResult a) {

                            }
                        }, this);
            }
        }

        if (cancel)
        {
            Cancel();
        }
    }

    private void Cancel()
    {
        Frontend.Unsubscribe();

        setConfirmationModel(null);
        setVmModel(null);
    }

    private void VmModel_DataCenter_ItemsChanged()
    {
        storage_pool dataCenter = null;
        for (Object item : getVmModel().getDataCenter().getItems())
        {
            storage_pool a = (storage_pool) item;

            if (getVmModel().getIsNew())
            {
                dataCenter = a;
                break;
            }
            else
            {
                UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
                VM vm = (VM) selectedItem.getEntity();

                if (a.getId().equals(vm.getstorage_pool_id()))
                {
                    dataCenter = a;
                    break;
                }
            }
        }

        if (!getVmModel().getIsNew() && dataCenter == null)
        {
            UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
            VM vm = (VM) selectedItem.getEntity();
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object result)
                {
                    UserPortalListModel userPortalListModel = (UserPortalListModel) model;
                    java.util.ArrayList<storage_pool> list =
                            new java.util.ArrayList<storage_pool>(java.util.Arrays.asList(new storage_pool[] { (storage_pool) result }));
                    userPortalListModel.getVmModel().getDataCenter().setItems(list);
                    userPortalListModel.getVmModel().getDataCenter().setSelectedItem(Linq.FirstOrDefault(list));

                }
            };
            AsyncDataProvider.GetDataCenterById(_asyncQuery, vm.getstorage_pool_id());
        }
        else
        {
            getVmModel().getDataCenter().setSelectedItem(dataCenter);
        }
    }

    private void VmModel_Cluster_ItemsChanged()
    {
        if (!getVmModel().getIsNew())
        {
            UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
            VM vm = (VM) selectedItem.getEntity();
            VDSGroup cluster = null;

            for (Object item : getVmModel().getCluster().getItems())
            {
                VDSGroup a = (VDSGroup) item;
                if (a.getID().equals(vm.getvds_group_id()))
                {
                    cluster = a;
                    break;
                }
            }
            getVmModel().getCluster().setSelectedItem(cluster);

            getVmModel().getCluster().setIsChangable(vm.getstatus() == VMStatus.Down);
        }
    }

    private void VmModel_DefaultHost_ItemsChanged()
    {
        if (!getVmModel().getIsNew())
        {
            UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
            VM vm = (VM) selectedItem.getEntity();
            VDS host = null;

            for (Object item : getVmModel().getDefaultHost().getItems())
            {
                VDS a = (VDS) item;
                if (a.getvds_id().equals(((vm.getdedicated_vm_for_vds() != null) ? vm.getdedicated_vm_for_vds()
                        : NGuid.Empty)))
                {
                    host = a;
                    break;
                }
            }
            if (host == null)
            {
                getVmModel().getIsAutoAssign().setEntity(true);
            }
            else
            {
                getVmModel().getDefaultHost().setSelectedItem(host);
                getVmModel().getIsAutoAssign().setEntity(false);
            }
        }
    }

    private void VmModel_DisplayProtocol_ItemsChanged()
    {
        if (!getVmModel().getIsNew())
        {
            UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
            VM vm = (VM) selectedItem.getEntity();
            EntityModel displayType = null;

            for (Object item : getVmModel().getDisplayProtocol().getItems())
            {
                EntityModel a = (EntityModel) item;
                DisplayType dt = (DisplayType) a.getEntity();
                if (dt == vm.getdefault_display_type())
                {
                    displayType = a;
                    break;
                }
            }
            getVmModel().getDisplayProtocol().setSelectedItem(displayType);
        }
    }

    private void VmModel_Priority_ItemsChanged()
    {
        if (!getVmModel().getIsNew())
        {
            UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
            VM vm = (VM) selectedItem.getEntity();
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object result)
                {
                    UserPortalListModel userPortalListModel = (UserPortalListModel) model;
                    int roundPriority = (Integer) result;
                    EntityModel priority = null;

                    for (Object item : userPortalListModel.getVmModel().getPriority().getItems())
                    {
                        EntityModel a = (EntityModel) item;
                        int p = (Integer) a.getEntity();
                        if (p == roundPriority)
                        {
                            priority = a;
                            break;
                        }
                    }
                    userPortalListModel.getVmModel().getPriority().setSelectedItem(priority);

                }
            };
            AsyncDataProvider.GetRoundedPriority(_asyncQuery, vm.getpriority());
        }
    }

    private void VmModel_TimeZone_ItemsChanged()
    {
        if (!getVmModel().getIsNew())
        {
            UserPortalItemModel selectedItem = (UserPortalItemModel) getSelectedItem();
            VM vm = (VM) selectedItem.getEntity();

            if (!StringHelper.isNullOrEmpty(vm.gettime_zone()))
            {
                getVmModel().getTimeZone().setSelectedItem(Linq.FirstOrDefault(getVmModel().getTimeZone().getItems(),
                        new Linq.TimeZonePredicate(vm.gettime_zone())));
            }
        }
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.equals(ItemsChangedEventDefinition) && sender == getVmModel().getDataCenter())
        {
            VmModel_DataCenter_ItemsChanged();
        }
        else if (ev.equals(ItemsChangedEventDefinition) && sender == getVmModel().getCluster())
        {
            VmModel_Cluster_ItemsChanged();
        }
        else if (ev.equals(ItemsChangedEventDefinition) && sender == getVmModel().getDefaultHost())
        {
            VmModel_DefaultHost_ItemsChanged();
        }
        else if (ev.equals(ItemsChangedEventDefinition) && sender == getVmModel().getDisplayProtocol())
        {
            VmModel_DisplayProtocol_ItemsChanged();
        }
        else if (ev.equals(ItemsChangedEventDefinition) && sender == getVmModel().getPriority())
        {
            VmModel_Priority_ItemsChanged();
        }
        else if (ev.equals(ItemsChangedEventDefinition) && sender == getVmModel().getTimeZone())
        {
            VmModel_TimeZone_ItemsChanged();
        }
    }

    @Override
    public void OnVmAndPoolLoad()
    {
        if (getvms() != null && getpools() != null)
        {
            // Complete search.

            // Remove pools that has provided VMs.
            java.util.ArrayList<vm_pools> filteredPools = new java.util.ArrayList<vm_pools>();
            poolMap = new java.util.HashMap<Guid, vm_pools>();

            for (vm_pools pool : getpools())
            {
                // Add pool to map.
                poolMap.put(pool.getvm_pool_id(), pool);

                boolean found = false;
                for (VM vm : getvms())
                {
                    if (vm.getVmPoolId() != null && vm.getVmPoolId().equals(pool.getvm_pool_id()))
                    {
                        found = true;
                        break;
                    }
                }

                if (!found)
                {
                    filteredPools.add(pool);
                }
            }

            // Merge VMs and Pools, and create item models.
            java.util.List all = Linq.Concat(getvms(), filteredPools);
            Linq.Sort(all, new Linq.VmAndPoolByNameComparer());

            java.util.ArrayList<Model> items = new java.util.ArrayList<Model>();
            for (Object item : all)
            {
                UserPortalItemModel model = new UserPortalItemModel(this);
                model.setEntity(item);
                items.add(model);

                UpdateConsoleModel(model);
            }

            // In userportal 'Extended View': Set 'CanConnectAutomatically' to true if there's one and only one up VM.
            setCanConnectAutomatically(GetUpVms(items).size() == 1
                    && GetUpVms(items).get(0).getDefaultConsole().getConnectCommand().getIsExecutionAllowed());

            setItems(items);

            setvms(null);
            setpools(null);

            getSearchCompletedEvent().raise(this, EventArgs.Empty);
        }
    }

    private void UpdateConsoleModel(UserPortalItemModel item)
    {
        if (item.getEntity() != null)
        {
            Object tempVar = item.getEntity();
            VM vm = (VM) ((tempVar instanceof VM) ? tempVar : null);
            if (vm == null)
            {
                return;
            }

            // Caching console model if needed
            if (!cachedConsoleModels.containsKey(vm.getvm_guid()))
            {
                SpiceConsoleModel spiceConsoleModel = new SpiceConsoleModel();
                spiceConsoleModel.getErrorEvent().addListener(this);
                VncConsoleModel vncConsoleModel = new VncConsoleModel();
                RdpConsoleModel rdpConsoleModel = new RdpConsoleModel();

                cachedConsoleModels.put(vm.getvm_guid(),
                        new java.util.ArrayList<ConsoleModel>(java.util.Arrays.asList(new ConsoleModel[] {
                                spiceConsoleModel, vncConsoleModel, rdpConsoleModel })));
            }

            // Getting cached console model
            java.util.ArrayList<ConsoleModel> cachedModels = cachedConsoleModels.get(vm.getvm_guid());
            for (ConsoleModel cachedModel : cachedModels)
            {
                cachedModel.setEntity(null);
                cachedModel.setEntity(vm);
            }

            // Set default console by vm's display type
            item.setDefaultConsole(vm.getdisplay_type() == DisplayType.vnc ? cachedModels.get(1) : cachedModels.get(0));

            // Adjust item's default console for userportal 'Extended View'
            item.getDefaultConsole().setForceVmStatusUp(false);

            // Update additional console
            if (DataProvider.IsWindowsOsType(vm.getvm_os()))
            {
                item.setAdditionalConsole(cachedModels.get(2));
                item.setHasAdditionalConsole(true);
            }
            else
            {
                item.setAdditionalConsole(null);
                item.setHasAdditionalConsole(false);
            }
        }
    }

    @Override
    protected String getListName() {
        return "UserPortalListModel";
    }
}
