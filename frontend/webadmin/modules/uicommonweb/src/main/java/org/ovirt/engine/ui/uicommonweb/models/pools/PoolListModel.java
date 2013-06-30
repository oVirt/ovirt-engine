package org.ovirt.engine.ui.uicommonweb.models.pools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.searchbackend.SearchObjects;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ISupportSystemTreeContext;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ExistingPoolModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.NewPoolModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.TimeZoneModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmBasedWidgetSwitchModeCommand;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.ObservableCollection;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class PoolListModel extends ListWithDetailsModel implements ISupportSystemTreeContext
{

    private UICommand privateNewCommand;

    public UICommand getNewCommand()
    {
        return privateNewCommand;
    }

    private void setNewCommand(UICommand value)
    {
        privateNewCommand = value;
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

    private HashMap<Version, ArrayList<String>> privateCustomPropertiesKeysList;

    private HashMap<Version, ArrayList<String>> getCustomPropertiesKeysList() {
        return privateCustomPropertiesKeysList;
    }

    private void setCustomPropertiesKeysList(HashMap<Version, ArrayList<String>> value) {
        privateCustomPropertiesKeysList = value;
    }

    private SystemTreeItemModel systemTreeSelectedItem;

    @Override
    public SystemTreeItemModel getSystemTreeSelectedItem() {
        return systemTreeSelectedItem;
    }

    @Override
    public void setSystemTreeSelectedItem(SystemTreeItemModel value) {
        systemTreeSelectedItem = value;
    }

    protected Object[] getSelectedKeys()
    {
        // return SelectedItems == null ? new object[0] : SelectedItems.Cast<vm_pools>().Select(a =>
        // a.vm_pool_id).Cast<object>().ToArray(); }
        if (getSelectedItems() == null)
        {
            return new Object[0];
        }
        else
        {
            Object[] keys = new Object[getSelectedItems().size()];
            for (int i = 0; i < getSelectedItems().size(); i++)
            {
                keys[i] = ((VmPool) getSelectedItems().get(i)).getVmPoolId();
            }
            return keys;
        }
    }

    public PoolListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().poolsTitle());

        setDefaultSearchString("Pools:"); //$NON-NLS-1$
        setSearchString(getDefaultSearchString());
        setSearchObjects(new String[] { SearchObjects.VDC_POOL_OBJ_NAME, SearchObjects.VDC_POOL_PLU_OBJ_NAME });
        setAvailableInModes(ApplicationMode.VirtOnly);

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        updateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
        if (getCustomPropertiesKeysList() == null) {
            AsyncDataProvider.getCustomPropertiesList(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {
                            PoolListModel model = (PoolListModel) target;
                            if (returnValue != null) {
                                model.setCustomPropertiesKeysList((HashMap<Version, ArrayList<String>>) returnValue);
                            }
                        }
                    }));
        }
    }

    @Override
    protected void initDetailModels()
    {
        super.initDetailModels();

        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
        list.add(new PoolGeneralModel());
        list.add(new PoolVmListModel());
        list.add(new PermissionListModel());
        setDetailModels(list);
    }

    @Override
    public boolean isSearchStringMatch(String searchString)
    {
        return searchString.trim().toLowerCase().startsWith("pool"); //$NON-NLS-1$
    }

    @Override
    protected void syncSearch()
    {
        SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.VmPools);
        tempVar.setMaxCount(getSearchPageSize());
        super.syncSearch(VdcQueryType.Search, tempVar);
    }

    @Override
    public void search()
    {
        super.search();
    }

    public void newEntity()
    {
        if (getWindow() != null)
        {
            return;
        }

        PoolModel model = new PoolModel(new NewPoolModelBehavior());
        model.setIsNew(true);
        model.setCustomPropertiesKeysList(getCustomPropertiesKeysList());
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newPoolTitle());
        model.setHashName("new_pool"); //$NON-NLS-1$
        model.getVmType().setSelectedItem(VmType.Desktop);
        model.initialize(getSystemTreeSelectedItem());

        VmBasedWidgetSwitchModeCommand switchModeCommand = new VmBasedWidgetSwitchModeCommand();
        switchModeCommand.init(model);
        model.getCommands().add(switchModeCommand);

        UICommand command = new UICommand("OnSave", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        command.setIsDefault(true);
        model.getCommands().add(command);

        command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        command.setIsCancel(true);
        model.getCommands().add(command);
    }

    public void edit()
    {
        final VmPool pool = (VmPool) getSelectedItem();

        if (getWindow() != null)
        {
            return;
        }

        final PoolListModel poolListModel = this;

        Frontend.RunQuery(VdcQueryType.GetVmDataByPoolId,
                new IdQueryParameters(pool.getVmPoolId()),

                new AsyncQuery(this, new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object modell, Object result) {
                        final VM vm = (VM) ((VdcQueryReturnValue) result).getReturnValue();

                        final ExistingPoolModelBehavior behavior = new ExistingPoolModelBehavior(vm);
                        behavior.getPoolModelBehaviorInitializedEvent().addListener(new IEventListener() {
                            @Override
                            public void eventRaised(Event ev, Object sender, EventArgs args) {
                                final PoolModel model = behavior.getModel();

                                for (Object item : model.getPoolType().getItems())
                                {
                                    EntityModel a = (EntityModel) item;
                                    if (a.getEntity() == pool.getVmPoolType())
                                    {
                                        model.getPoolType().setSelectedItem(a);
                                        break;
                                    }
                                }
                                String cdImage = null;

                                if (vm != null) {
                                    model.getDataCenterWithClustersList().setSelectedItem(null);
                                    model.getDataCenterWithClustersList().setSelectedItem(Linq.firstOrDefault(model.getDataCenterWithClustersList()
                                            .getItems(),
                                            new Linq.DataCenterWithClusterPredicate(vm.getStoragePoolId(), vm.getVdsGroupId())));

                                    model.getTemplate().setIsChangable(false);
                                    cdImage = vm.getIsoPath();
                                    model.getVmType().setSelectedItem(vm.getVmType());
                                }
                                else
                                {
                                    model.getDataCenterWithClustersList()
                                            .setSelectedItem(Linq.firstOrDefault(Linq.<StoragePool> cast(model.getDataCenterWithClustersList()
                                                    .getItems())));
                                }

                                model.getDataCenterWithClustersList().setIsChangable(vm == null);

                                boolean hasCd = !StringHelper.isNullOrEmpty(cdImage);
                                model.getCdImage().setIsChangable(hasCd);
                                model.getCdAttached().setEntity(hasCd);
                                if (hasCd) {
                                    model.getCdImage().setSelectedItem(cdImage);
                                }

                                model.getProvisioning().setIsChangable(false);
                                model.getStorageDomain().setIsChangable(false);

                                VmBasedWidgetSwitchModeCommand switchModeCommand = new VmBasedWidgetSwitchModeCommand();
                                switchModeCommand.init(model);
                                model.getCommands().add(switchModeCommand);

                                UICommand command = new UICommand("OnSave", poolListModel); //$NON-NLS-1$
                                command.setTitle(ConstantsManager.getInstance().getConstants().ok());
                                command.setIsDefault(true);
                                model.getCommands().add(command);

                                command = new UICommand("Cancel", poolListModel); //$NON-NLS-1$
                                command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
                                command.setIsCancel(true);
                                model.getCommands().add(command);
                            }
                        });

                        PoolModel model = new PoolModel(behavior);
                        model.setCustomPropertiesKeysList(getCustomPropertiesKeysList());
                        model.startProgress("");
                        setWindow(model);
                        model.setTitle(ConstantsManager.getInstance().getConstants().editPoolTitle());
                        model.initialize(getSystemTreeSelectedItem());
                        model.getName().setEntity(pool.getName());
                        model.getDescription().setEntity(pool.getVmPoolDescription());
                        model.getAssignedVms().setEntity(pool.getAssignedVmsCount());
                        model.getPrestartedVms().setEntity(pool.getPrestartedVms());
                        model.setPrestartedVmsHint("0-" + pool.getAssignedVmsCount()); //$NON-NLS-1$
                        model.getMaxAssignedVmsPerUser().setEntity(pool.getMaxAssignedVmsPerUser());

                    }
                }));
    }

    private List<StoragePool> asList(Object returnValue) {
        if (returnValue instanceof ArrayList) {
            return (ArrayList<StoragePool>) returnValue;
        }

        if (returnValue instanceof StoragePool) {
            List<StoragePool> res = new ArrayList<StoragePool>();
            res.add((StoragePool) returnValue);
            return res;
        }

        throw new IllegalArgumentException("Expected ArrayList of storage_pools or a storage_pool. Given " + returnValue.getClass().getName()); //$NON-NLS-1$
    }

    public void remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removePoolsTitle());
        model.setHashName("remove_pool"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().poolsMsg());

        ArrayList<String> list = new ArrayList<String>();
        for (VmPool item : Linq.<VmPool> cast(getSelectedItems()))
        {
            list.add(item.getName());
        }
        model.setItems(list);

        UICommand tempVar = new UICommand("OnRemove", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void onRemove()
    {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        ArrayList<VdcActionParametersBase> list = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            VmPool pool = (VmPool) item;
            list.add(new VmPoolParametersBase(pool.getVmPoolId()));
        }

        model.startProgress(null);

        Frontend.RunMultipleAction(VdcActionType.RemoveVmPool, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.stopProgress();
                        cancel();

                    }
                }, model);
    }

    public void onSave()
    {
        final PoolModel model = (PoolModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.getIsNew() && getSelectedItem() == null)
        {
            cancel();
            return;
        }

        if (!model.validate())
        {
            return;
        }

        final VmPool pool = model.getIsNew() ? new VmPool() : (VmPool) Cloner.clone(getSelectedItem());

        final String name = (String) model.getName().getEntity();

        // Check name unicitate.
        AsyncDataProvider.isPoolNameUnique(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        Boolean isUnique = (Boolean) returnValue;

                        if ((model.getIsNew() && !isUnique)
                                || (!model.getIsNew() && !isUnique && name.compareToIgnoreCase(pool.getName()) != 0)) {
                            model.getName()
                                    .getInvalidityReasons()
                                    .add(ConstantsManager.getInstance().getConstants().nameMustBeUniqueInvalidReason());
                            model.getName().setIsValid(false);
                            model.setIsGeneralTabValid(false);
                            return;
                        }

                        // Save changes.
                        pool.setName((String) model.getName().getEntity());
                        pool.setVmPoolDescription((String) model.getDescription().getEntity());
                        pool.setVdsGroupId(model.getSelectedCluster().getId());
                        pool.setPrestartedVms(model.getPrestartedVms().asConvertible().integer());
                        pool.setMaxAssignedVmsPerUser(model.getMaxAssignedVmsPerUser().asConvertible().integer());

                        EntityModel poolTypeSelectedItem = (EntityModel) model.getPoolType().getSelectedItem();
                        pool.setVmPoolType((VmPoolType) poolTypeSelectedItem.getEntity());

                        Guid default_host;
                        VDS defaultHost = (VDS) model.getDefaultHost().getSelectedItem();
                        if ((Boolean) model.getIsAutoAssign().getEntity())
                        {
                            default_host = null;
                        }
                        else
                        {
                            default_host = defaultHost.getId();
                        }


                        VM vm = new VM();
                        vm.setVmtGuid(((VmTemplate) model.getTemplate().getSelectedItem()).getId());
                        vm.setName(name);
                        vm.setVmOs((Integer) model.getOSType().getSelectedItem());
                        vm.setDeleteProtected((Boolean) model.getIsDeleteProtected().getEntity());
                        vm.setSmartcardEnabled((Boolean) model.getIsSmartcardEnabled().getEntity());
                        vm.setNumOfMonitors((Integer) model.getNumOfMonitors().getSelectedItem());
                        vm.setVmDomain(model.getDomain().getIsAvailable() ? (String) model.getDomain()
                                .getSelectedItem() : ""); //$NON-NLS-1$
                        vm.setVmMemSizeMb((Integer) model.getMemSize().getEntity());
                        vm.setMinAllocatedMem((Integer) model.getMinAllocatedMemory().getEntity());
                        vm.setVdsGroupId(model.getSelectedCluster().getId());
                        vm.setTimeZone((model.getTimeZone().getIsAvailable() && model.getTimeZone()
                                .getSelectedItem() != null) ? ((TimeZoneModel) model.getTimeZone()
                                .getSelectedItem()).getTimeZoneKey()
                                : ""); //$NON-NLS-1$
                        vm.setNumOfSockets((Integer) model.getNumOfSockets().getSelectedItem());
                        vm.setCpuPerSocket(Integer.parseInt(model.getTotalCPUCores().getEntity().toString())
                                / (Integer) model.getNumOfSockets().getSelectedItem());
                        vm.setUsbPolicy((UsbPolicy) model.getUsbPolicy().getSelectedItem());
                        vm.setStateless(false);
                        vm.setDefaultBootSequence(model.getBootSequence());
                        vm.setIsoPath(model.getCdImage().getIsChangable() ? (String) model.getCdImage()
                                .getSelectedItem() : ""); //$NON-NLS-1$
                        vm.setDedicatedVmForVds(default_host);
                        vm.setKernelUrl((String) model.getKernel_path().getEntity());
                        vm.setKernelParams((String) model.getKernel_parameters().getEntity());
                        vm.setInitrdUrl((String) model.getInitrd_path().getEntity());
                        vm.setMigrationSupport((MigrationSupport) (model.getMigrationMode().getSelectedItem()));
                        vm.setVncKeyboardLayout((String) model.getVncKeyboardLayout().getSelectedItem());

                        EntityModel displayProtocolSelectedItem =
                                (EntityModel) model.getDisplayProtocol().getSelectedItem();
                        vm.setDefaultDisplayType((DisplayType) displayProtocolSelectedItem.getEntity());
                        vm.setCustomProperties(model.getCustomPropertySheet().getEntity());
                        vm.setVmType((VmType) model.getVmType().getSelectedItem());

                        AddVmPoolWithVmsParameters param =
                                new AddVmPoolWithVmsParameters(pool, vm, model.getNumOfDesktops()
                                        .asConvertible()
                                        .integer(), 0);

                        param.setStorageDomainId(Guid.Empty);
                        param.setDiskInfoDestinationMap(model.getDisksAllocationModel()
                                .getImageToDestinationDomainMap());

                        param.setSoundDeviceEnabled((Boolean) model.getIsSoundcardEnabled().getEntity());
                        if (model.getQuota().getSelectedItem() != null) {
                            vm.setQuotaId(((Quota) model.getQuota().getSelectedItem()).getId());
                        }

                        model.startProgress(null);

                        if (model.getIsNew())
                        {
                            Frontend.RunMultipleAction(VdcActionType.AddVmPoolWithVms,
                                    new ArrayList<VdcActionParametersBase>(Arrays.asList(new VdcActionParametersBase[] { param })),
                                    new IFrontendMultipleActionAsyncCallback() {
                                        @Override
                                        public void executed(FrontendMultipleActionAsyncResult result) {
                                            cancel();
                                            stopProgress();
                                        }
                                    },
                                    this);
                        }
                        else
                        {
                            Frontend.RunMultipleAction(VdcActionType.UpdateVmPoolWithVms,
                                    new ArrayList<VdcActionParametersBase>(Arrays.asList(new VdcActionParametersBase[] { param })),
                                    new IFrontendMultipleActionAsyncCallback() {
                                        @Override
                                        public void executed(FrontendMultipleActionAsyncResult result) {
                                            cancel();
                                            stopProgress();
                                        }
                                    },
                                    this);
                        }

                    }
                }),
                name);
    }

    public void cancel()
    {
        setWindow(null);
    }

    @Override
    protected void onSelectedItemChanged()
    {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged()
    {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.selectedItemPropertyChanged(sender, e);
        updateActionAvailability();
    }

    private void updateActionAvailability()
    {
        getEditCommand().setIsExecutionAllowed(getSelectedItem() != null && getSelectedItems() != null
                && getSelectedItems().size() == 1 && hasVms(getSelectedItem()));

        getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0
                && VdcActionUtils.CanExecute(getSelectedItems(), VmPool.class, VdcActionType.RemoveVmPool));
    }

    private boolean hasVms(Object selectedItem) {
        if (selectedItem instanceof VmPool) {
            return ((VmPool) selectedItem).getAssignedVmsCount() != 0;
        }
        return false;
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (command == getNewCommand())
        {
            newEntity();
        }
        if (command == getEditCommand())
        {
            edit();
        }
        if (command == getRemoveCommand())
        {
            remove();
        }
        if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            cancel();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnSave")) //$NON-NLS-1$
        {
            onSave();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnRemove")) //$NON-NLS-1$
        {
            onRemove();
        }
    }

    @Override
    protected String getListName() {
        return "PoolListModel"; //$NON-NLS-1$
    }

}
