package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister.ConsoleContext;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConsoleModelsCache;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.VmConsoles;
import org.ovirt.engine.ui.uicommonweb.place.UserPortalApplicationPlaces;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.inject.Inject;

@SuppressWarnings("unused")
public class UserPortalBasicListModel extends AbstractUserPortalListModel {

    public static final EventDefinition searchCompletedEventDefinition;
    private Event<EventArgs> privateSearchCompletedEvent;

    public Event<EventArgs> getSearchCompletedEvent()
    {
        return privateSearchCompletedEvent;
    }

    private void setSearchCompletedEvent(Event<EventArgs> value)
    {
        privateSearchCompletedEvent = value;
    }

    private final VmBasicDiskListModel vmBasicDiskListModel;

    public VmBasicDiskListModel getVmBasicDiskListModel() {
        return vmBasicDiskListModel;
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

    private EntityModel<String> privateSelectedItemDefinedMemory;

    public EntityModel<String> getSelectedItemDefinedMemory()
    {
        return privateSelectedItemDefinedMemory;
    }

    private void setSelectedItemDefinedMemory(EntityModel<String> value)
    {
        privateSelectedItemDefinedMemory = value;
    }

    private EntityModel<String> privateSelectedItemNumOfCpuCores;

    public EntityModel<String> getSelectedItemNumOfCpuCores()
    {
        return privateSelectedItemNumOfCpuCores;
    }

    private void setSelectedItemNumOfCpuCores(EntityModel<String> value)
    {
        privateSelectedItemNumOfCpuCores = value;
    }

    static {
        searchCompletedEventDefinition = new EventDefinition("SearchCompleted", UserPortalBasicListModel.class); //$NON-NLS-1$
    }

    @Inject
    public UserPortalBasicListModel(final VmBasicDiskListModel vmBasicDiskListModel) {
        this.vmBasicDiskListModel = vmBasicDiskListModel;
        setDetailList();
        setApplicationPlace(UserPortalApplicationPlaces.basicMainTabPlace);
        setSearchCompletedEvent(new Event<EventArgs>(searchCompletedEventDefinition));

        setSelectedItemDefinedMemory(new EntityModel<String>());
        setSelectedItemNumOfCpuCores(new EntityModel<String>());

        consoleModelsCache = new ConsoleModelsCache(ConsoleContext.UP_BASIC, this);
    }

    private void setDetailList() {
        List<EntityModel> list = new ArrayList<EntityModel>();
        list.add(getVmBasicDiskListModel());
        setDetailModels(list);
        setActiveDetailModel(getVmBasicDiskListModel());
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
                        userPortalBasicListModel.onVmAndPoolLoad();
                    }
                }));
    }

    @Override
    public void setItems(Collection value) {
        if (items != value)
        {
            itemsChanging(value, items);
            items = value;
            getItemsChangedEvent().raise(this, EventArgs.EMPTY);
            onPropertyChanged(new PropertyChangedEventArgs("Items")); //$NON-NLS-1$
        }
    }

    @Override
    public void forceRefresh() {
        super.forceRefresh();
        getVmBasicDiskListModel().forceRefresh();
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

        updateSelectedEntityDetails();
    }

    private void updateSelectedEntityDetails()
    {
        if (getSelectedItem() == null)
        {
            return;
        }

        Object entity = getSelectedItem().getEntity();
        if (entity instanceof VM)
        {
            VM vm = (VM) entity;
            updateDetails(vm);
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
                            userPortalBasicListModel.updateDetails(vm);
                        }
                    }
                }
            };

            VmPool pool = (VmPool) entity;
            Frontend.getInstance().runQuery(VdcQueryType.GetVmDataByPoolId,
                    new IdQueryParameters(pool.getVmPoolId()),
                    _asyncQuery);
        }
    }

    public void updateDetails(VM vm)
    {
        getSelectedItemDefinedMemory().setEntity(sizeParser(vm.getVmMemSizeMb()));
        getSelectedItemNumOfCpuCores().setEntity(
                ConstantsManager.getInstance().getMessages().cpuInfoMessage(vm.getNumOfCpus(), vm.getNumOfSockets(), vm.getCpuPerSocket())
        );
    }

    // Temporarily converter
    // TODO: Use converters infrastructure in UICommon
    public String sizeParser(int sizeInMb)
    {
        return ((sizeInMb >= 1024 && sizeInMb % 1024 == 0) ? (sizeInMb / 1024 + "GB") : (sizeInMb + "MB")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);
    }

    @Override
    public void onVmAndPoolLoad() {
        if (getvms() != null && getpools() != null) {
            // Complete search.
            // Remove pools that has provided VMs.
            ArrayList<VmPool> filteredPools = new ArrayList<VmPool>();

            for (VmPool pool : getpools()) {
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
