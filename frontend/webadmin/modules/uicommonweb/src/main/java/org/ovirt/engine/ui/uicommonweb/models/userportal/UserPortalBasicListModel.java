package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister.ConsoleContext;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConsolesFactory;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.place.UserPortalApplicationPlaces;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.inject.Inject;

@SuppressWarnings("unused")
public class UserPortalBasicListModel extends AbstractUserPortalListModel {

    public static final EventDefinition searchCompletedEventDefinition;
    private Event<EventArgs> privateSearchCompletedEvent;

    @Override
    public Event<EventArgs> getSearchCompletedEvent() {
        return privateSearchCompletedEvent;
    }

    private void setSearchCompletedEvent(Event<EventArgs> value) {
        privateSearchCompletedEvent = value;
    }

    private final VmBasicDiskListModel vmBasicDiskListModel;

    public VmBasicDiskListModel getVmBasicDiskListModel() {
        return vmBasicDiskListModel;
    }

    private EntityModel<String> privateSelectedItemDefinedMemory;

    public EntityModel<String> getSelectedItemDefinedMemory() {
        return privateSelectedItemDefinedMemory;
    }

    private void setSelectedItemDefinedMemory(EntityModel<String> value) {
        privateSelectedItemDefinedMemory = value;
    }

    private EntityModel<String> privateSelectedItemNumOfCpuCores;

    public EntityModel<String> getSelectedItemNumOfCpuCores() {
        return privateSelectedItemNumOfCpuCores;
    }

    private void setSelectedItemNumOfCpuCores(EntityModel<String> value) {
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
        setSearchCompletedEvent(new Event<>(searchCompletedEventDefinition));

        setSelectedItemDefinedMemory(new EntityModel<String>());
        setSelectedItemNumOfCpuCores(new EntityModel<String>());

        consolesFactory = new ConsolesFactory(ConsoleContext.UP_BASIC, this);
    }

    private void setDetailList() {
        List<HasEntity<Object>> list = new ArrayList<>();
        list.add(getVmBasicDiskListModel());
        setDetailModels(list);
        setActiveDetailModel(getVmBasicDiskListModel());
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
                        UserPortalBasicListModel userPortalBasicListModel = (UserPortalBasicListModel) model;
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

                        userPortalBasicListModel.onVmAndPoolLoad(vms, pools);
                    }
                }));
    }

    @Override
    public void setItems(Collection value) {
        if (items != value) {
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
    protected void updateDetailsAvailability() {
        super.updateDetailsAvailability();
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();

        updateSelectedEntityDetails();
    }

    private void updateSelectedEntityDetails() {
        if (getSelectedItem() == null) {
            return;
        }

        Object entity = getSelectedItem().getEntity();
        if (entity instanceof VM) {
            VM vm = (VM) entity;
            updateDetails(vm);
        }
        else if (entity instanceof VmPool) {
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object result) {
                    UserPortalBasicListModel userPortalBasicListModel = (UserPortalBasicListModel) model;
                    if (result != null) {
                        VM vm = ((VdcQueryReturnValue) result).getReturnValue();
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

    public void updateDetails(VM vm) {
        getSelectedItemDefinedMemory().setEntity(sizeParser(vm.getVmMemSizeMb()));
        getSelectedItemNumOfCpuCores().setEntity(
                ConstantsManager.getInstance().getMessages().cpuInfoMessage(vm.getNumOfCpus(),
                        vm.getNumOfSockets(), vm.getCpuPerSocket(), vm.getThreadsPerCpu())
        );
    }

    // Temporarily converter
    // TODO: Use converters infrastructure in UICommon
    public String sizeParser(int sizeInMb) {
        return sizeInMb >= 1024 && sizeInMb % 1024 == 0 ? sizeInMb / 1024 + "GB" : sizeInMb + "MB"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);
    }

    @Override
    protected String getListName() {
        return "UserPortalBasicListModel"; //$NON-NLS-1$
    }

    // overridden only to allow the UIBinder to access this
    @Override
    public UserPortalItemModel getSelectedItem() {
        return super.getSelectedItem();
    }

    @Override
    protected boolean fetchLargeIcons() {
        return true;
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
