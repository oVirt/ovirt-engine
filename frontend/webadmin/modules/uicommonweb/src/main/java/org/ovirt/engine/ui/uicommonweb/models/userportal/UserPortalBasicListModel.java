package org.ovirt.engine.ui.uicommonweb.models.userportal;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.common.queries.GetAllVmPoolsAttachedToUserParameters;
import org.ovirt.engine.core.common.queries.GetUserVmsByUserIdAndGroupsParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.EventDefinition;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RdpConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncConsoleModel;

@SuppressWarnings("unused")
public class UserPortalBasicListModel extends IUserPortalListModel implements IVmPoolResolutionService
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

    private ListModel privatevmBasicDiskListModel;

    public ListModel getvmBasicDiskListModel()
    {
        return privatevmBasicDiskListModel;
    }

    private void setvmBasicDiskListModel(ListModel value)
    {
        privatevmBasicDiskListModel = value;
    }

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

    private EntityModel privateSelectedItemDefinedMemory;

    public EntityModel getSelectedItemDefinedMemory()
    {
        return privateSelectedItemDefinedMemory;
    }

    private void setSelectedItemDefinedMemory(EntityModel value)
    {
        privateSelectedItemDefinedMemory = value;
    }

    private EntityModel privateSelectedItemNumOfCpuCores;

    public EntityModel getSelectedItemNumOfCpuCores()
    {
        return privateSelectedItemNumOfCpuCores;
    }

    private void setSelectedItemNumOfCpuCores(EntityModel value)
    {
        privateSelectedItemNumOfCpuCores = value;
    }

    private java.util.HashMap<Guid, java.util.ArrayList<ConsoleModel>> cachedConsoleModels;

    static
    {
        SearchCompletedEventDefinition = new EventDefinition("SearchCompleted", UserPortalBasicListModel.class);
    }

    public UserPortalBasicListModel()
    {
        setSearchCompletedEvent(new Event(SearchCompletedEventDefinition));

        setSelectedItemDefinedMemory(new EntityModel());
        setSelectedItemNumOfCpuCores(new EntityModel());

        cachedConsoleModels = new java.util.HashMap<Guid, java.util.ArrayList<ConsoleModel>>();
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
                UserPortalBasicListModel userPortalBasicListModel = (UserPortalBasicListModel) model;
                userPortalBasicListModel.setvms((java.util.ArrayList<VM>) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
                userPortalBasicListModel.OnVmAndPoolLoad();
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
                if (ReturnValue != null)
                {
                    UserPortalBasicListModel userPortalBasicListModel = (UserPortalBasicListModel) model;
                    userPortalBasicListModel.setpools((java.util.ArrayList<vm_pools>) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
                    userPortalBasicListModel.OnVmAndPoolLoad();
                }
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

        setvmBasicDiskListModel(new VmBasicDiskListModel());

        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
        list.add(getvmBasicDiskListModel());

        setDetailModels(list);
        setActiveDetailModel(getvmBasicDiskListModel());
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
    }

    @Override
    protected void OnSelectedItemChanged()
    {
        super.OnSelectedItemChanged();

        UpdateSelectedEntityDetails();
    }

    private void UpdateSelectedEntityDetails()
    {
        if (getSelectedItem() == null)
        {
            return;
        }

        Object entity = ((EntityModel) getSelectedItem()).getEntity();
        if (entity instanceof VM)
        {
            VM vm = (VM) entity;
            UpdateDetails(vm);
        }
        else if (entity instanceof vm_pools)
        {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object result)
                {
                    UserPortalBasicListModel userPortalBasicListModel = (UserPortalBasicListModel) model;
                    VM vm = (VM) result;
                    if (vm != null)
                    {
                        userPortalBasicListModel.UpdateDetails(vm);
                    }
                }
            };

            vm_pools pool = (vm_pools) entity;
            AsyncDataProvider.GetAnyVm(_asyncQuery, pool.getvm_pool_name());
        }
    }

    public void UpdateDetails(VM vm)
    {
        getSelectedItemDefinedMemory().setEntity(SizeParser(vm.getvm_mem_size_mb()));
        getSelectedItemNumOfCpuCores().setEntity(vm.getnum_of_cpus() + " " + "(" + vm.getnum_of_sockets()
                + " Socket(s), " + vm.getcpu_per_socket() + " Core(s) per Socket)");
    }

    // Temporarily converter
    // TODO: Use converters infrastructure in UICommon
    public String SizeParser(int sizeInMb)
    {
        return ((sizeInMb >= 1024 && sizeInMb % 1024 == 0) ? (sizeInMb / 1024 + "GB") : (sizeInMb + "MB"));
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);
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

            // In userportal 'Basic View': Set 'CanConnectAutomatically' to true if there's one and only one VM in
            // status 'UP' and the other VMs aren't up.
            setCanConnectAutomatically(GetStatusUpVms(items).size() == 1 && GetUpVms(items).size() == 1
                    && GetStatusUpVms(items).get(0).getDefaultConsole().getConnectCommand().getIsExecutionAllowed());

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
                cachedModel.setEntity(vm);
            }

            // Set default console by vm's display type
            item.setDefaultConsole(vm.getdisplay_type() == DisplayType.vnc ? cachedModels.get(1) : cachedModels.get(0));

            // Adjust item's default console for userportal 'Basic View'
            item.getDefaultConsole().setForceVmStatusUp(true);

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
        return "UserPortalBasicListModel";
    }
}
