package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.queries.GetVmdataByPoolIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister.ConsoleContext;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.ObservableCollection;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

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

    private final HashMap<Guid, ArrayList<ConsoleModel>> cachedConsoleModels;

    static
    {
        SearchCompletedEventDefinition = new EventDefinition("SearchCompleted", UserPortalBasicListModel.class); //$NON-NLS-1$
    }

    public UserPortalBasicListModel()
    {
        setSearchCompletedEvent(new Event(SearchCompletedEventDefinition));

        setSelectedItemDefinedMemory(new EntityModel());
        setSelectedItemNumOfCpuCores(new EntityModel());

        cachedConsoleModels = new HashMap<Guid, ArrayList<ConsoleModel>>();
    }

    @Override
    protected void syncSearch()
    {
        super.syncSearch();
        Frontend.RunQuery(VdcQueryType.GetAllVmsAndVmPools,
                new VdcQueryParametersBase(),
                new AsyncQuery(this, new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        UserPortalBasicListModel userPortalBasicListModel = (UserPortalBasicListModel) model;
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

                        userPortalBasicListModel.setvms(vms);
                        userPortalBasicListModel.setpools(pools);
                        userPortalBasicListModel.OnVmAndPoolLoad();
                    }
                }));
    }

    @Override
    protected void asyncSearch()
    {
        super.asyncSearch();
        syncSearch();
    }

    @Override
    public void setItems(Iterable value) {
        if (items != value)
        {
            itemsChanging(value, items);
            items = value;
            getItemsChangedEvent().raise(this, EventArgs.Empty);
            onPropertyChanged(new PropertyChangedEventArgs("Items")); //$NON-NLS-1$
        }
    }

    @Override
    protected void initDetailModels()
    {
        super.initDetailModels();

        setvmBasicDiskListModel(new VmBasicDiskListModel());

        ObservableCollection<EntityModel> list = new ObservableCollection<EntityModel>();
        list.add(getvmBasicDiskListModel());

        setDetailModels(list);
        setActiveDetailModel(getvmBasicDiskListModel());
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
    }

    @Override
    protected void onSelectedItemChanged()
    {
        super.onSelectedItemChanged();

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
        else if (entity instanceof VmPool)
        {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object result)
                {
                    UserPortalBasicListModel userPortalBasicListModel = (UserPortalBasicListModel) model;
                    if (result != null)
                    {
                        VM vm = (VM) ((VdcQueryReturnValue) result).getReturnValue();
                        if (vm != null) {
                            userPortalBasicListModel.UpdateDetails(vm);
                        }
                    }
                }
            };

            VmPool pool = (VmPool) entity;
            Frontend.RunQuery(VdcQueryType.GetVmDataByPoolId,
                    new GetVmdataByPoolIdParameters(pool.getVmPoolId()),
                    _asyncQuery);
        }
    }

    public void UpdateDetails(VM vm)
    {
        getSelectedItemDefinedMemory().setEntity(SizeParser(vm.getVmMemSizeMb()));
        getSelectedItemNumOfCpuCores().setEntity(vm.getNumOfCpus() + " " + "(" + vm.getNumOfSockets() //$NON-NLS-1$ //$NON-NLS-2$
                + " Socket(s), " + vm.getCpuPerSocket() + " Core(s) per Socket)"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    // Temporarily converter
    // TODO: Use converters infrastructure in UICommon
    public String SizeParser(int sizeInMb)
    {
        return ((sizeInMb >= 1024 && sizeInMb % 1024 == 0) ? (sizeInMb / 1024 + "GB") : (sizeInMb + "MB")); //$NON-NLS-1$ //$NON-NLS-2$
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
            ArrayList<VmPool> filteredPools = new ArrayList<VmPool>();
            poolMap = new HashMap<Guid, VmPool>();

            for (VmPool pool : getpools())
            {
                // Add pool to map.
                poolMap.put(pool.getVmPoolId(), pool);

                int attachedVmsCount = 0;
                for (VM vm : getvms())
                {
                    if (vm.getVmPoolId() != null && vm.getVmPoolId().equals(pool.getVmPoolId()))
                    {
                        attachedVmsCount++;
                    }
                }

                if (attachedVmsCount < pool.getMaxAssignedVmsPerUser())
                {
                    filteredPools.add(pool);
                }
            }

            // Merge VMs and Pools, and create item models.
            List all = Linq.concat(getvms(), filteredPools);
            Linq.sort(all, new Linq.VmAndPoolByNameComparer());

            ArrayList<Model> items = new ArrayList<Model>();
            for (Object item : all)
            {
                UserPortalItemModel model = new UserPortalItemModel(this, this, ConsoleContext.UP_BASIC);
                model.setEntity(item);
                items.add(model);

                updateConsoleModel(model);
            }

            // In userportal 'Basic View': Set 'CanConnectAutomatically' to true if there's one and only one VM in
            // status 'UP' and the other VMs aren't up.
            setCanConnectAutomatically(GetStatusUpVms(items).size() == 1 && GetUpVms(items).size() == 1);

            setItems(items);

            setvms(null);
            setpools(null);

            getSearchCompletedEvent().raise(this, EventArgs.Empty);
        }
    }

    @Override
    protected void updateConsoleModel(UserPortalItemModel item) {
        super.updateConsoleModel(item);
        if (item.getEntity() != null && item.getDefaultConsoleModel() != null) {
            // Adjust item's default console for userportal 'Basic View'
            item.getDefaultConsoleModel().setForceVmStatusUp(true);
        }
    }

    @Override
    protected String getListName() {
        return "UserPortalBasicListModel"; //$NON-NLS-1$
    }

    // overridden only to allow the UIBinder to access this
    @Override
    public UserPortalItemModel getSelectedItem()
    {
        return (UserPortalItemModel) super.getSelectedItem();
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command.getName().equals("closeVncInfo")) { //$NON-NLS-1$
            setWindow(null);
        }
    }

    @Override
    protected ConsoleContext getConsoleContext() {
        return ConsoleContext.UP_BASIC;
    }

}
