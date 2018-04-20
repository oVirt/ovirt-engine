package org.ovirt.engine.ui.uicommonweb.models.clusters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class ClusterServiceModel extends EntityModel<Cluster> {

    private ListModel<VDS> hostList;

    public ListModel<VDS> getHostList() {
        return hostList;
    }

    public void setHostList(ListModel<VDS> hostList) {
        this.hostList = hostList;
    }

    private ListModel<ServiceType> serviceTypeList;

    public ListModel<ServiceType> getServiceTypeList() {
        return serviceTypeList;
    }

    public void setServiceTypeList(ListModel<ServiceType> serviceTypeList) {
        this.serviceTypeList = serviceTypeList;
    }

    private ListModel<EntityModel<GlusterServerService>> serviceList;

    public ListModel<EntityModel<GlusterServerService>> getServiceList() {
        return serviceList;
    }

    public void setServiceList(ListModel<EntityModel<GlusterServerService>> serviceList) {
        this.serviceList = serviceList;
    }

    private List<GlusterServerService> actualServiceList;

    public List<GlusterServerService> getActualServiceList() {
        return actualServiceList;
    }

    public void setActualServiceList(List<GlusterServerService> actualServiceList) {
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

    public ClusterServiceModel() {
        setTitle(ConstantsManager.getInstance().getConstants().servicesTitle());
        setHelpTag(HelpTag.services);
        setHashName("services"); //$NON-NLS-1$

        setActualServiceList(new ArrayList<GlusterServerService>());
        setServiceList(new ListModel<EntityModel<GlusterServerService>>());
        setHostList(new ListModel<VDS>());
        setServiceTypeList(new ListModel<ServiceType>());
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
                if (vds != null && !vds.getClusterName().equals(getEntity().getName())) {
                    refreshNeeded = true;
                    break;
                }
            }
        } else {
            refreshNeeded = true;
        }

        if (!refreshNeeded) {
            return;
        }

        updateServiceTypeList();

        AsyncDataProvider.getInstance().getHostListByCluster(new AsyncQuery<>(hosts -> {
            hosts.add(0, null);
            getHostList().setItems(hosts);
        }), getEntity().getName());
    }

    private void updateServiceTypeList() {
        ArrayList<ServiceType> serviceTypes = new ArrayList<>();
        serviceTypes.add(null);
        serviceTypes.add(ServiceType.NFS);
        serviceTypes.add(ServiceType.SHD);
        getServiceTypeList().setItems(serviceTypes);
    }

    private void updateServiceList() {
        AsyncDataProvider.getInstance().getClusterGlusterServices(new AsyncQuery<>(details -> {
            if (details.getServiceInfo() != null) {
                setActualServiceList(details.getServiceInfo());
            } else {
                setActualServiceList(new ArrayList<GlusterServerService>());
            }
            filterServices();
        }), getEntity().getId());
    }

    private void filterServices() {
        VDS selectedVds = hostList.getSelectedItem();
        ServiceType serviceType = serviceTypeList.getSelectedItem();
        ArrayList<EntityModel<GlusterServerService>> list = new ArrayList<>();
        List<GlusterServerService> serviceList = new ArrayList<>(getActualServiceList());
        Collections.sort(serviceList,
                Comparator.comparing(GlusterServerService::getHostName)
                        .thenComparing(g -> g.getServiceType().toString()));
        for (GlusterServerService service : serviceList) {
            if ((selectedVds == null || service.getHostName().equals(selectedVds.getHostName()))
                    && (serviceType == null || service.getServiceType() == serviceType)) {
                list.add(new EntityModel<>(service));
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

        if (command == getFilterServicesCommand()) {
            filterServices();
        } else if (command == getClearFilterServicesCommand()) {
            clearFilters();
        }
    }
}
