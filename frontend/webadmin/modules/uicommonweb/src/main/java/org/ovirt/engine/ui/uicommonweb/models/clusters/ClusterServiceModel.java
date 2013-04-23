package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeAdvancedDetails;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceInfo;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class ClusterServiceModel extends EntityModel {

    private ListModel hostList;

    public ListModel getHostList() {
        return hostList;
    }

    public void setHostList(ListModel hostList) {
        this.hostList = hostList;
    }

    private ListModel serviceTypeList;

    public ListModel getServiceTypeList() {
        return serviceTypeList;
    }

    public void setServiceTypeList(ListModel serviceTypeList) {
        this.serviceTypeList = serviceTypeList;
    }

    private ListModel serviceList;

    public ListModel getServiceList() {
        return serviceList;
    }

    public void setServiceList(ListModel serviceList) {
        this.serviceList = serviceList;
    }

    private List<ServiceInfo> actualServiceList;

    public List<ServiceInfo> getActualServiceList() {
        return actualServiceList;
    }

    public void setActualServiceList(List<ServiceInfo> actualServiceList) {
        this.actualServiceList = actualServiceList;
    }

    private UICommand filterServicesCommand;

    public UICommand getFilterServicesCommand() {
        return filterServicesCommand;
    }

    private void setFilterServicesCommand(UICommand value) {
        filterServicesCommand = value;
    }

    private UICommand clearFilterServicesCommand;

    public UICommand getClearFilterServicesCommand() {
        return clearFilterServicesCommand;
    }

    private void setClearFilterServicesCommand(UICommand value) {
        clearFilterServicesCommand = value;
    }

    @Override
    public VDSGroup getEntity() {
        return (VDSGroup) ((super.getEntity() instanceof VDSGroup) ? super.getEntity() : null);
    }

    public ClusterServiceModel() {
        setTitle(ConstantsManager.getInstance().getConstants().servicesTitle());
        setHashName("services"); //$NON-NLS-1$

        setActualServiceList(new ArrayList<ServiceInfo>());
        setServiceList(new ListModel());
        setHostList(new ListModel());
        setServiceTypeList(new ListModel());
        updateServiceTypeList();
        setFilterServicesCommand(new UICommand("FilterServices", this)); //$NON-NLS-1$
        setClearFilterServicesCommand(new UICommand("ClearFilterServices", this)); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        if (getEntity() != null) {
            updateServiceList();
            updateHostList();
        }
    }

    private void updateHostList() {
        boolean refreshNeeded = false;
        List<VDS> hostList = (List<VDS>) getHostList().getItems();
        if (hostList != null && hostList.size() > 1) {
            for(VDS vds : hostList) {
                if (vds != null && !vds.getVdsGroupName().equals(getEntity().getname())) {
                    refreshNeeded = true;
                    break;
                }
            }
        }
        else {
            refreshNeeded = true;
        }

        if (!refreshNeeded) {
            return;
        }

        updateServiceTypeList();

        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.setModel(this);
        asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                List<VDS> hostList = (List<VDS>) result;
                hostList.add(0, null);
                getHostList().setItems(hostList);
            }
        };
        AsyncDataProvider.GetHostListByCluster(asyncQuery, getEntity().getname());
    }

    private void updateServiceTypeList() {
        ArrayList<ServiceType> serviceTypes = new ArrayList<ServiceType>();
        serviceTypes.add(null);
        serviceTypes.addAll(Arrays.asList(ServiceType.values()));
        getServiceTypeList().setItems(serviceTypes);
    }

    private void updateServiceList() {
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.setModel(this);
        asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                GlusterVolumeAdvancedDetails details = (GlusterVolumeAdvancedDetails) result;
                if (details.getServiceInfo() != null) {
                    setActualServiceList(details.getServiceInfo());
                }
                else {
                    setActualServiceList(new ArrayList<ServiceInfo>());
                }
                filterServices();
            }
        };
        AsyncDataProvider.GetClusterGlusterServices(asyncQuery, getEntity().getId());
    }

    private void filterServices() {
        VDS selectedVds = (VDS) hostList.getSelectedItem();
        ServiceType serviceType = (ServiceType) serviceTypeList.getSelectedItem();
        ArrayList<EntityModel> list = new ArrayList<EntityModel>();
        for (ServiceInfo service : getActualServiceList()) {
            if ((selectedVds == null || service.getHostName().equals(selectedVds.getHostName()))
                    && (serviceType == null || service.getServiceType() == serviceType)) {
                list.add(new EntityModel(service));
            }
        }
        getServiceList().setItems(list);
    }

    private void clearFilters() {
        getHostList().setSelectedItem(null);
        getServiceTypeList().setSelectedItem(null);
        filterServices();
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getFilterServicesCommand())
        {
            filterServices();
        }
        else if (command == getClearFilterServicesCommand())
        {
            clearFilters();
        }
    }
}
