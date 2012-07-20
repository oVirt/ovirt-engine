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
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.GetVmdataByPoolIdParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.searchbackend.SearchObjects;
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
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ExistingPoolModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.NewPoolModelBehavior;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public class PoolListModel extends ListWithDetailsModel
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
                keys[i] = ((vm_pools) getSelectedItems().get(i)).getvm_pool_id();
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
                        public void OnSuccess(Object target, Object returnValue) {

                            PoolListModel model = (PoolListModel) target;
                            if (returnValue != null)
                            {
                                model.setCustomPropertiesKeysList(new HashMap<Version, ArrayList<String>>());
                                HashMap<Version, String> dictionary = (HashMap<Version, String>) returnValue;
                                for (Map.Entry<Version, String> keyValuePair : dictionary.entrySet())
                                {
                                    model.getCustomPropertiesKeysList().put(keyValuePair.getKey(),
                                            new ArrayList<String>());
                                    for (String s : keyValuePair.getValue().split("[;]", -1)) //$NON-NLS-1$
                                    {
                                        model.getCustomPropertiesKeysList().get(keyValuePair.getKey()).add(s);
                                    }
                                }
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
        model.Initialize(null);

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
        final vm_pools pool = (vm_pools) getSelectedItem();

        if (getWindow() != null)
        {
            return;
        }

        final PoolListModel poolListModel = this;

        Frontend.RunQuery(VdcQueryType.GetVmDataByPoolId,
                new GetVmdataByPoolIdParameters(pool.getvm_pool_id()),

                new AsyncQuery(this, new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object modell, Object result) {
                        final VM vm = (VM) ((VdcQueryReturnValue) result).getReturnValue();

                        final ExistingPoolModelBehavior behavior = new ExistingPoolModelBehavior(vm);
                        behavior.getPoolModelBehaviorInitializedEvent().addListener(new IEventListener() {
                            @Override
                            public void eventRaised(Event ev, Object sender, EventArgs args) {
                                final PoolModel model = behavior.getModel();

                                for (Object item : model.getPoolType().getItems())
                                {
                                    EntityModel a = (EntityModel) item;
                                    if (a.getEntity() == pool.getvm_pool_type())
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
                                            new Linq.DataCenterPredicate(vm.getstorage_pool_id())));
                                    model.getDataCenter().setIsChangable(false);
                                    model.getTemplate().setIsChangable(false);

                                    if (model.getDataCenter().getSelectedItem() == null)
                                    {

                                        AsyncDataProvider.GetDataCenterById(new AsyncQuery(this,
                                                new INewAsyncCallback() {
                                                    @Override
                                                    public void OnSuccess(Object target, Object returnValue) {
                                                        List<storage_pool> list =
                                                                asList(returnValue);
                                                        model.getDataCenter().setItems(list);
                                                        model.getDataCenter().setSelectedItem(list.get(0));

                                                        ArrayList<VmTemplate> templates = new ArrayList<VmTemplate>();
                                                        // TODO: need to be async
                                                        VmTemplate basedOnTemplate =
                                                                DataProvider.GetTemplateByID(vm.getvmt_guid());
                                                        if (basedOnTemplate != null)
                                                        {
                                                            templates.add(basedOnTemplate);
                                                        }

                                                        model.getTemplate().setItems(templates);
                                                        model.getTemplate().setSelectedItem(basedOnTemplate);

                                                        model.getDefaultHost().setSelectedItem(null);
                                                        VDS host = null;
                                                        if (model.getDefaultHost().getItems() != null) {
                                                            for (Object item : model.getDefaultHost().getItems())
                                                            {
                                                                VDS a = (VDS) item;
                                                                if (a.getId()
                                                                        .equals(((vm.getdedicated_vm_for_vds()) != null) ? vm.getdedicated_vm_for_vds()
                                                                                : Guid.Empty))
                                                                {
                                                                    host = a;
                                                                    break;
                                                                }
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

                                                        if (vm.getMigrationSupport() == MigrationSupport.PINNED_TO_HOST)
                                                        {
                                                            model.getRunVMOnSpecificHost().setEntity(true);
                                                        }
                                                        else
                                                        {
                                                            if (vm.getMigrationSupport() == MigrationSupport.IMPLICITLY_NON_MIGRATABLE)
                                                            {
                                                                model.getDontMigrateVM().setEntity(true);
                                                            }
                                                        }

                                                        model.getMemSize().setEntity(vm.getvm_mem_size_mb());
                                                        model.getMinAllocatedMemory()
                                                                .setEntity(vm.getMinAllocatedMem());
                                                        model.getOSType().setSelectedItem(vm.getvm_os());
                                                        model.getDomain().setSelectedItem(vm.getvm_domain());

                                                        if (!StringHelper.isNullOrEmpty(vm.gettime_zone()))
                                                        {
                                                            model.getTimeZone()
                                                                    .setSelectedItem(Linq.FirstOrDefault(model.getTimeZone()
                                                                            .getItems(),
                                                                            new Linq.TimeZonePredicate(vm.gettime_zone())));
                                                        }

                                                        EntityModel displayType = null;
                                                        for (Object item : model.getDisplayProtocol().getItems())
                                                        {
                                                            EntityModel a = (EntityModel) item;
                                                            DisplayType dt = (DisplayType) a.getEntity();
                                                            if (dt == vm.getdefault_display_type())
                                                            {
                                                                displayType = a;
                                                                break;
                                                            }
                                                        }
                                                        model.getDisplayProtocol().setSelectedItem(displayType);

                                                        model.getUsbPolicy().setSelectedItem(vm.getusb_policy());
                                                        model.getNumOfMonitors()
                                                                .setSelectedItem(vm.getnum_of_monitors());
                                                        model.getNumOfSockets().setEntity(vm.getnum_of_sockets());
                                                        model.getTotalCPUCores().setEntity(vm.getnum_of_cpus());
                                                        model.setBootSequence(vm.getdefault_boot_sequence());

                                                        model.getKernel_path().setEntity(vm.getkernel_url());
                                                        model.getKernel_parameters().setEntity(vm.getkernel_params());
                                                        model.getInitrd_path().setEntity(vm.getinitrd_url());

                                                        AsyncDataProvider.GetVmDiskList(new AsyncQuery(this,
                                                                new INewAsyncCallback() {
                                                                    @Override
                                                                    public void OnSuccess(Object modell,
                                                                            Object returnValue) {

                                                                        List<DiskImage> disks =
                                                                                (List<DiskImage>) returnValue;
                                                                        // feature for filling storage domain in case of
                                                                        // data center list empty
                                                                        if (disks.size() > 0)
                                                                        {
                                                                            ArrayList<Guid> storage_ids =
                                                                                    disks.get(0).getstorage_ids();
                                                                            Guid storageId =
                                                                                    storage_ids != null
                                                                                            && storage_ids.size() > 0 ? storage_ids.get(0)
                                                                                            : null;

                                                                            if (storageId != null) {
                                                                                AsyncDataProvider.GetStorageDomainById(new AsyncQuery(this,
                                                                                        new INewAsyncCallback() {
                                                                                            @Override
                                                                                            public void OnSuccess(Object target,
                                                                                                    Object returnValue) {
                                                                                                storage_domains storageDomain =
                                                                                                        (storage_domains) returnValue;
                                                                                                model.getStorageDomain()
                                                                                                        .setItems(new ArrayList<storage_domains>(Arrays.asList(new storage_domains[] { storageDomain })));
                                                                                                model.getStorageDomain()
                                                                                                        .setSelectedItem(storageDomain);
                                                                                            }
                                                                                        }),
                                                                                        storageId);
                                                                            }
                                                                        }
                                                                        model.getStorageDomain().setIsChangable(false);

                                                                    }
                                                                }),
                                                                vm.getId(),
                                                                false);
                                                    }

                                                }),
                                                vm.getstorage_pool_id());
                                    }

                                    cdImage = vm.getiso_path();
                                }
                                else
                                {
                                    model.getDataCenter()
                                            .setSelectedItem(Linq.FirstOrDefault(Linq.<storage_pool> Cast(model.getDataCenter()
                                                    .getItems())));
                                }

                                // make sure that Clusters list won't be null:
                                ArrayList<VDSGroup> clusters = new ArrayList<VDSGroup>();
                                if (model.getCluster().getItems() == null)
                                {
                                    VDSGroup poolCluster = DataProvider.GetClusterById(pool.getvds_group_id());
                                    if (poolCluster != null)
                                    {
                                        clusters.add(poolCluster);
                                    }

                                    model.getCluster().setItems(clusters);
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
                        model.Initialize(null);
                        model.getName().setEntity(pool.getvm_pool_name());
                        model.getDescription().setEntity(pool.getvm_pool_description());
                        model.getAssignedVms().setEntity(pool.getvm_assigned_count());
                        model.getPrestartedVms().setEntity(pool.getPrestartedVms());
                        model.setPrestartedVmsHint("0-" + pool.getvm_assigned_count()); //$NON-NLS-1$
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
        for (vm_pools item : Linq.<vm_pools> Cast(getSelectedItems()))
        {
            list.add(item.getvm_pool_name());
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
            vm_pools pool = (vm_pools) item;
            list.add(new VmPoolParametersBase(pool.getvm_pool_id()));
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

        final vm_pools pool = model.getIsNew() ? new vm_pools() : (vm_pools) Cloner.clone(getSelectedItem());

        final String name = (String) model.getName().getEntity();

        // Check name unicitate.
        AsyncDataProvider.IsPoolNameUnique(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        Boolean isUnique = (Boolean) returnValue;

                        if ((model.getIsNew() && !isUnique)
                                || (!model.getIsNew() && !isUnique && name.compareToIgnoreCase(pool.getvm_pool_name()) != 0)) {
                            model.getName()
                                    .getInvalidityReasons()
                                    .add(ConstantsManager.getInstance().getConstants().nameMustBeUniqueInvalidReason());
                            model.getName().setIsValid(false);
                            model.setIsGeneralTabValid(false);
                            return;
                        }

                        // Save changes.
                        pool.setvm_pool_name((String) model.getName().getEntity());
                        pool.setvm_pool_description((String) model.getDescription().getEntity());
                        pool.setvds_group_id(((VDSGroup) model.getCluster().getSelectedItem()).getId());
                        pool.setPrestartedVms(model.getPrestartedVms().AsConvertible().Integer());

                        EntityModel poolTypeSelectedItem = (EntityModel) model.getPoolType().getSelectedItem();
                        pool.setvm_pool_type((VmPoolType) poolTypeSelectedItem.getEntity());

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

                        MigrationSupport migrationSupport = MigrationSupport.MIGRATABLE;
                        if ((Boolean) model.getRunVMOnSpecificHost().getEntity())
                        {
                            migrationSupport = MigrationSupport.PINNED_TO_HOST;
                        }
                        else if ((Boolean) model.getDontMigrateVM().getEntity())
                        {
                            migrationSupport = MigrationSupport.IMPLICITLY_NON_MIGRATABLE;
                        }

                        VM desktop = new VM();
                        desktop.setvmt_guid(((VmTemplate) model.getTemplate().getSelectedItem()).getId());
                        desktop.setvm_name(name);
                        desktop.setvm_os((VmOsType) model.getOSType().getSelectedItem());
                        desktop.setnum_of_monitors((Integer) model.getNumOfMonitors().getSelectedItem());
                        desktop.setvm_domain(model.getDomain().getIsAvailable() ? (String) model.getDomain()
                                .getSelectedItem() : ""); //$NON-NLS-1$
                        desktop.setvm_mem_size_mb((Integer) model.getMemSize().getEntity());
                        desktop.setMinAllocatedMem((Integer) model.getMinAllocatedMemory().getEntity());
                        desktop.setvds_group_id(((VDSGroup) model.getCluster().getSelectedItem()).getId());
                        desktop.settime_zone((model.getTimeZone().getIsAvailable() && model.getTimeZone()
                                .getSelectedItem() != null) ? ((Map.Entry<String, String>) model.getTimeZone()
                                .getSelectedItem()).getKey()
                                : ""); //$NON-NLS-1$
                        desktop.setnum_of_sockets((Integer) model.getNumOfSockets().getEntity());
                        desktop.setcpu_per_socket((Integer) model.getTotalCPUCores().getEntity()
                                / (Integer) model.getNumOfSockets().getEntity());
                        desktop.setusb_policy((UsbPolicy) model.getUsbPolicy().getSelectedItem());
                        desktop.setis_auto_suspend(false);
                        desktop.setis_stateless(false);
                        desktop.setdefault_boot_sequence(model.getBootSequence());
                        desktop.setiso_path(model.getCdImage().getIsChangable() ? (String) model.getCdImage()
                                .getSelectedItem() : ""); //$NON-NLS-1$
                        desktop.setdedicated_vm_for_vds(default_host);
                        desktop.setkernel_url((String) model.getKernel_path().getEntity());
                        desktop.setkernel_params((String) model.getKernel_parameters().getEntity());
                        desktop.setinitrd_url((String) model.getInitrd_path().getEntity());
                        desktop.setMigrationSupport(migrationSupport);

                        EntityModel displayProtocolSelectedItem =
                                (EntityModel) model.getDisplayProtocol().getSelectedItem();
                        desktop.setdefault_display_type((DisplayType) displayProtocolSelectedItem.getEntity());
                        desktop.setCustomProperties(model.getCustomPropertySheet().getEntity());

                        AddVmPoolWithVmsParameters param =
                                new AddVmPoolWithVmsParameters(pool, desktop, model.getNumOfDesktops()
                                        .AsConvertible()
                                        .Integer(), 0);

                        param.setStorageDomainId(Guid.Empty);
                        param.setDiskInfoDestinationMap(
                                model.getDisksAllocationModel()
                                        .getImageToDestinationDomainMap((Boolean) model.getDisksAllocationModel()
                                                .getIsSingleStorageDomain()
                                                .getEntity()));

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
                && getSelectedItems().size() == 1);

        getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && getSelectedItems().size() > 0
                && VdcActionUtils.CanExecute(getSelectedItems(), vm_pools.class, VdcActionType.RemoveVmPool));
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
