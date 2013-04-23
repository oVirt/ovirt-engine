package org.ovirt.engine.ui.uicommonweb.models.pools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.AddVmPoolWithVmsParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.GetVmdataByPoolIdParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
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

        UpdateActionAvailability();

        getSearchNextPageCommand().setIsAvailable(true);
        getSearchPreviousPageCommand().setIsAvailable(true);
        if (getCustomPropertiesKeysList() == null) {
            AsyncDataProvider.GetCustomPropertiesList(new AsyncQuery(this,
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
    protected void InitDetailModels()
    {
        super.InitDetailModels();

        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
        list.add(new PoolGeneralModel());
        list.add(new PoolVmListModel());
        list.add(new PermissionListModel());
        setDetailModels(list);
    }

    @Override
    public boolean IsSearchStringMatch(String searchString)
    {
        return searchString.trim().toLowerCase().startsWith("pool"); //$NON-NLS-1$
    }

    @Override
    protected void SyncSearch()
    {
        SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.VmPools);
        tempVar.setMaxCount(getSearchPageSize());
        super.SyncSearch(VdcQueryType.Search, tempVar);
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        setAsyncResult(Frontend.RegisterSearch(getSearchString(), SearchType.VmPools, getSearchPageSize()));
        setItems(getAsyncResult().getData());
    }

    @Override
    public void Search()
    {
        super.Search();
    }

    public void New()
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
        model.setVmType(VmType.Desktop);
        model.Initialize(getSystemTreeSelectedItem());

        UICommand command = new UICommand("OnSave", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().ok());
        command.setIsDefault(true);
        model.getCommands().add(command);

        command = new UICommand("Cancel", this); //$NON-NLS-1$
        command.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        command.setIsCancel(true);
        model.getCommands().add(command);
    }

    public void Edit()
    {
        final VmPool pool = (VmPool) getSelectedItem();

        if (getWindow() != null)
        {
            return;
        }

        final PoolListModel poolListModel = this;

        Frontend.RunQuery(VdcQueryType.GetVmDataByPoolId,
                new GetVmdataByPoolIdParameters(pool.getVmPoolId()),

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
                                    model.getDataCenter().setSelectedItem(null);
                                    model.getDataCenter().setSelectedItem(Linq.FirstOrDefault(model.getDataCenter()
                                            .getItems(),
                                            new Linq.DataCenterPredicate(vm.getStoragePoolId())));
                                    model.getDataCenter().setIsChangable(false);
                                    model.getTemplate().setIsChangable(false);

                                    cdImage = vm.getIsoPath();

                                }
                                else
                                {
                                    model.getDataCenter()
                                            .setSelectedItem(Linq.FirstOrDefault(Linq.<storage_pool> Cast(model.getDataCenter()
                                                    .getItems())));
                                }

                                model.getCluster().setIsChangable(vm == null);

                                boolean hasCd = !StringHelper.isNullOrEmpty(cdImage);
                                model.getCdImage().setIsChangable(hasCd);
                                model.getCdAttached().setEntity(hasCd);
                                if (hasCd) {
                                    model.getCdImage().setSelectedItem(cdImage);
                                }

                                model.getProvisioning().setIsChangable(false);
                                model.getStorageDomain().setIsChangable(false);

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
                        model.StartProgress("");
                        setWindow(model);
                        model.setTitle(ConstantsManager.getInstance().getConstants().editPoolTitle());
                        model.setHashName("edit_pool"); //$NON-NLS-1$
                        model.setVmType(VmType.Desktop);
                        model.Initialize(getSystemTreeSelectedItem());
                        model.getName().setEntity(pool.getName());
                        model.getDescription().setEntity(pool.getVmPoolDescription());
                        model.getAssignedVms().setEntity(pool.getAssignedVmsCount());
                        model.getPrestartedVms().setEntity(pool.getPrestartedVms());
                        model.setPrestartedVmsHint("0-" + pool.getAssignedVmsCount()); //$NON-NLS-1$
                    }
                }));
    }

    private List<storage_pool> asList(Object returnValue) {
        if (returnValue instanceof ArrayList) {
            return (ArrayList<storage_pool>) returnValue;
        }

        if (returnValue instanceof storage_pool) {
            List<storage_pool> res = new ArrayList<storage_pool>();
            res.add((storage_pool) returnValue);
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
        for (VmPool item : Linq.<VmPool> Cast(getSelectedItems()))
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

    public void OnRemove()
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

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.RemoveVmPool, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.StopProgress();
                        Cancel();

                    }
                }, model);
    }

    public void OnSave()
    {
        final PoolModel model = (PoolModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.getIsNew() && getSelectedItem() == null)
        {
            Cancel();
            return;
        }

        if (!model.Validate())
        {
            return;
        }

        final VmPool pool = model.getIsNew() ? new VmPool() : (VmPool) Cloner.clone(getSelectedItem());

        final String name = (String) model.getName().getEntity();

        // Check name unicitate.
        AsyncDataProvider.IsPoolNameUnique(new AsyncQuery(this,
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
                        pool.setVdsGroupId(((VDSGroup) model.getCluster().getSelectedItem()).getId());
                        pool.setPrestartedVms(model.getPrestartedVms().asConvertible().Integer());

                        EntityModel poolTypeSelectedItem = (EntityModel) model.getPoolType().getSelectedItem();
                        pool.setVmPoolType((VmPoolType) poolTypeSelectedItem.getEntity());

                        NGuid default_host;
                        VDS defaultHost = (VDS) model.getDefaultHost().getSelectedItem();
                        if ((Boolean) model.getIsAutoAssign().getEntity())
                        {
                            default_host = null;
                        }
                        else
                        {
                            default_host = defaultHost.getId();
                        }


                        VM desktop = new VM();
                        desktop.setVmtGuid(((VmTemplate) model.getTemplate().getSelectedItem()).getId());
                        desktop.setName(name);
                        desktop.setVmOs((VmOsType) model.getOSType().getSelectedItem());
                        desktop.setDeleteProtected((Boolean) model.getIsDeleteProtected().getEntity());
                        desktop.setSmartcardEnabled((Boolean) model.getIsSmartcardEnabled().getEntity());
                        desktop.setNumOfMonitors((Integer) model.getNumOfMonitors().getSelectedItem());
                        desktop.setVmDomain(model.getDomain().getIsAvailable() ? (String) model.getDomain()
                                .getSelectedItem() : ""); //$NON-NLS-1$
                        desktop.setVmMemSizeMb((Integer) model.getMemSize().getEntity());
                        desktop.setMinAllocatedMem((Integer) model.getMinAllocatedMemory().getEntity());
                        desktop.setVdsGroupId(((VDSGroup) model.getCluster().getSelectedItem()).getId());
                        desktop.setTimeZone((model.getTimeZone().getIsAvailable() && model.getTimeZone()
                                .getSelectedItem() != null) ? ((Map.Entry<String, String>) model.getTimeZone()
                                .getSelectedItem()).getKey()
                                : ""); //$NON-NLS-1$
                        desktop.setNumOfSockets((Integer) model.getNumOfSockets().getSelectedItem());
                        desktop.setCpuPerSocket(Integer.parseInt(model.getTotalCPUCores().getEntity().toString())
                                / (Integer) model.getNumOfSockets().getSelectedItem());
                        desktop.setUsbPolicy((UsbPolicy) model.getUsbPolicy().getSelectedItem());
                        desktop.setAutoSuspend(false);
                        desktop.setStateless(false);
                        desktop.setDefaultBootSequence(model.getBootSequence());
                        desktop.setIsoPath(model.getCdImage().getIsChangable() ? (String) model.getCdImage()
                                .getSelectedItem() : ""); //$NON-NLS-1$
                        desktop.setDedicatedVmForVds(default_host);
                        desktop.setKernelUrl((String) model.getKernel_path().getEntity());
                        desktop.setKernelParams((String) model.getKernel_parameters().getEntity());
                        desktop.setInitrdUrl((String) model.getInitrd_path().getEntity());
                        desktop.setMigrationSupport((MigrationSupport) (model.getMigrationMode().getSelectedItem()));
                        desktop.setVncKeyboardLayout((String) model.getVncKeyboardLayout().getSelectedItem());

                        EntityModel displayProtocolSelectedItem =
                                (EntityModel) model.getDisplayProtocol().getSelectedItem();
                        desktop.setDefaultDisplayType((DisplayType) displayProtocolSelectedItem.getEntity());
                        desktop.setCustomProperties(model.getCustomPropertySheet().getEntity());

                        AddVmPoolWithVmsParameters param =
                                new AddVmPoolWithVmsParameters(pool, desktop, model.getNumOfDesktops()
                                        .asConvertible()
                                        .Integer(), 0);

                        param.setStorageDomainId(Guid.Empty);
                        param.setDiskInfoDestinationMap(model.getDisksAllocationModel()
                                .getImageToDestinationDomainMap());

                        if (model.getQuota().getSelectedItem() != null) {
                            desktop.setQuotaId(((Quota) model.getQuota().getSelectedItem()).getId());
                        }

                        model.StartProgress(null);

                        if (model.getIsNew())
                        {
                            Frontend.RunMultipleAction(VdcActionType.AddVmPoolWithVms,
                                    new ArrayList<VdcActionParametersBase>(Arrays.asList(new VdcActionParametersBase[] { param })),
                                    new IFrontendMultipleActionAsyncCallback() {
                                        @Override
                                        public void Executed(FrontendMultipleActionAsyncResult result) {
                                            Cancel();
                                            StopProgress();
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
                                        public void Executed(FrontendMultipleActionAsyncResult result) {
                                            Cancel();
                                            StopProgress();
                                        }
                                    },
                                    this);
                        }

                    }
                }),
                name);
    }

    public void Cancel()
    {
        setWindow(null);
    }

    @Override
    protected void OnSelectedItemChanged()
    {
        super.OnSelectedItemChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void SelectedItemsChanged()
    {
        super.SelectedItemsChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void SelectedItemPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.SelectedItemPropertyChanged(sender, e);
        UpdateActionAvailability();
    }

    private void UpdateActionAvailability()
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
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getNewCommand())
        {
            New();
        }
        if (command == getEditCommand())
        {
            Edit();
        }
        if (command == getRemoveCommand())
        {
            remove();
        }
        if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnSave")) //$NON-NLS-1$
        {
            OnSave();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnRemove")) //$NON-NLS-1$
        {
            OnRemove();
        }
    }

    @Override
    protected String getListName() {
        return "PoolListModel"; //$NON-NLS-1$
    }

}
