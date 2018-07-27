package org.ovirt.engine.ui.webadmin.plugin.entity;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.ClusterSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.DataCenterSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk.DiskSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.VolumeSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.HostSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.NetworkSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool.PoolSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.profile.VnicProfileSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.provider.ProviderSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.QuotaSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.StorageSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.TemplateSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.UserSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.VirtualMachineSubTabPanelPresenter;

import com.google.gwt.event.shared.GwtEvent.Type;
import com.gwtplatform.mvp.client.ChangeTabHandler;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

/**
 * Enumerates possible entity types used within the plugin API.
 * <p>
 * Contains useful meta-data associated with business entity types.
 */
public enum EntityType {

    DataCenter(WebAdminApplicationPlaces.dataCenterMainPlace,
            DataCenterSubTabPanelPresenter.TYPE_RequestTabs,
            DataCenterSubTabPanelPresenter.TYPE_ChangeTab,
            DataCenterSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<String, String> map) {
            map.put(Storage.name(), WebAdminApplicationPlaces.dataCenterStorageSubTabPlace);
            map.put("IscsiBond", WebAdminApplicationPlaces.dataCenterIscsiBondSubTabPlace); //$NON-NLS-1$
            map.put(Network.name(), WebAdminApplicationPlaces.dataCenterNetworkSubTabPlace);
            map.put(Cluster.name(), WebAdminApplicationPlaces.dataCenterClusterSubTabPlace);
            map.put(Quota.name(), WebAdminApplicationPlaces.dataCenterQuotaSubTabPlace);
            map.put("Permission", WebAdminApplicationPlaces.dataCenterPermissionSubTabPlace); //$NON-NLS-1$
            map.put(Event.name(), WebAdminApplicationPlaces.dataCenterEventSubTabPlace);
        }
    },
    Cluster(WebAdminApplicationPlaces.clusterMainPlace,
            ClusterSubTabPanelPresenter.TYPE_RequestTabs,
            ClusterSubTabPanelPresenter.TYPE_ChangeTab,
            ClusterSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<String, String> map) {
            map.put(Host.name(), WebAdminApplicationPlaces.clusterHostSubTabPlace);
            map.put(VirtualMachine.name(), WebAdminApplicationPlaces.clusterVmSubTabPlace);
            map.put(Network.name(), WebAdminApplicationPlaces.clusterNetworkSubTabPlace);
            map.put("GlusterHook", WebAdminApplicationPlaces.clusterGlusterHookSubTabPlace); //$NON-NLS-1$
            map.put("AffinityGroup", WebAdminApplicationPlaces.clusterAffinityGroupsSubTabPlace); //$NON-NLS-1$
            map.put("AffinityLabel", WebAdminApplicationPlaces.clusterAffinityLabelsSubTabPlace); //$NON-NLS-1$
            map.put("CpuProfile", WebAdminApplicationPlaces.clusterCpuProfileSubTabPlace); //$NON-NLS-1$
            map.put("Permission", WebAdminApplicationPlaces.clusterPermissionSubTabPlace); //$NON-NLS-1$
        }
    },
    Host(WebAdminApplicationPlaces.hostMainPlace,
            HostSubTabPanelPresenter.TYPE_RequestTabs,
            HostSubTabPanelPresenter.TYPE_ChangeTab,
            HostSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<String, String> map) {
            map.put(VirtualMachine.name(), WebAdminApplicationPlaces.hostVmSubTabPlace);
            map.put("NetworkInterface", WebAdminApplicationPlaces.hostInterfaceSubTabPlace); //$NON-NLS-1$
            map.put("HostDevice", WebAdminApplicationPlaces.hostDeviceSubTabPlace); //$NON-NLS-1$
            map.put("HostHook", WebAdminApplicationPlaces.hostHookSubTabPlace); //$NON-NLS-1$
            map.put("GlusterSwift", WebAdminApplicationPlaces.hostGlusterSwiftSubTabPlace); //$NON-NLS-1$
            map.put("GlusterBrick", WebAdminApplicationPlaces.hostBricksSubTabPlace); //$NON-NLS-1$
            map.put("GlusterStorageDevice", WebAdminApplicationPlaces.hostGlusterStorageDevicesSubTabPlace); //$NON-NLS-1$
            map.put("Permission", WebAdminApplicationPlaces.hostPermissionSubTabPlace); //$NON-NLS-1$
            map.put(Event.name(), WebAdminApplicationPlaces.hostEventSubTabPlace);
            map.put("AffinityLabel", WebAdminApplicationPlaces.hostAffinityLabelsSubTabPlace); //$NON-NLS-1$
        }
    },
    Network(WebAdminApplicationPlaces.networkMainPlace,
            NetworkSubTabPanelPresenter.TYPE_RequestTabs,
            NetworkSubTabPanelPresenter.TYPE_ChangeTab,
            NetworkSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<String, String> map) {
            map.put("NetworkProfile", WebAdminApplicationPlaces.networkProfileSubTabPlace); //$NON-NLS-1$
            map.put("ExternalSubnet", WebAdminApplicationPlaces.networkExternalSubnetSubTabPlace); //$NON-NLS-1$
            map.put(Cluster.name(), WebAdminApplicationPlaces.networkClusterSubTabPlace);
            map.put(Host.name(), WebAdminApplicationPlaces.networkHostSubTabPlace);
            map.put(VirtualMachine.name(), WebAdminApplicationPlaces.networkVmSubTabPlace);
            map.put(Template.name(), WebAdminApplicationPlaces.networkTemplateSubTabPlace);
            map.put("Permission", WebAdminApplicationPlaces.networkPermissionSubTabPlace); //$NON-NLS-1$
        }
    },
    VnicProfile(WebAdminApplicationPlaces.vnicProfileMainPlace,
            VnicProfileSubTabPanelPresenter.TYPE_RequestTabs,
            VnicProfileSubTabPanelPresenter.TYPE_ChangeTab,
            VnicProfileSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<String, String> map) {
            map.put("Permission", WebAdminApplicationPlaces.vnicProfilePermissionSubTabPlace); //$NON-NLS-1$
            map.put(VirtualMachine.name(), WebAdminApplicationPlaces.vnicProfileVmSubTabPlace);
            map.put(Template.name(), WebAdminApplicationPlaces.vnicProfileTemplateSubTabPlace);
        }
    },
    Storage(WebAdminApplicationPlaces.storageMainPlace,
            StorageSubTabPanelPresenter.TYPE_RequestTabs,
            StorageSubTabPanelPresenter.TYPE_ChangeTab,
            StorageSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<String, String> map) {
            map.put(DataCenter.name(), WebAdminApplicationPlaces.storageDataCenterSubTabPlace);
            map.put("VirtualMachineImport", WebAdminApplicationPlaces.storageVmBackupSubTabPlace); //$NON-NLS-1$
            map.put("TemplateImport", WebAdminApplicationPlaces.storageTemplateBackupSubTabPlace); //$NON-NLS-1$
            map.put("VirtualMachineRegister", WebAdminApplicationPlaces.storageVmRegisterSubTabPlace); //$NON-NLS-1$
            map.put("TemplateRegister", WebAdminApplicationPlaces.storageTemplateRegisterSubTabPlace); //$NON-NLS-1$
            map.put("DiskImageRegister", WebAdminApplicationPlaces.storageDisksImageRegisterSubTabPlace); //$NON-NLS-1$
            map.put("Iso", WebAdminApplicationPlaces.storageIsoSubTabPlace); //$NON-NLS-1$
            map.put(Disk.name(), WebAdminApplicationPlaces.storageDiskSubTabPlace);
            map.put("DiskRegister", WebAdminApplicationPlaces.storageRegisterDiskSubTabPlace); //$NON-NLS-1$
            map.put("StorageSnapshot", WebAdminApplicationPlaces.storageSnapshotSubTabPlace); //$NON-NLS-1$
            map.put("DiskProfile", WebAdminApplicationPlaces.storageDiskProfileSubTabPlace); //$NON-NLS-1$
            map.put("StorageDR", WebAdminApplicationPlaces.storageDRSubTabPlace); //$NON-NLS-1$
            map.put("StorageLease", WebAdminApplicationPlaces.storageLeaseSubTabPlace); //$NON-NLS-1$
            map.put("Permission", WebAdminApplicationPlaces.storagePermissionSubTabPlace); //$NON-NLS-1$
            map.put(Event.name(), WebAdminApplicationPlaces.storageEventSubTabPlace);
        }
    },
    Disk(WebAdminApplicationPlaces.diskMainPlace,
            DiskSubTabPanelPresenter.TYPE_RequestTabs,
            DiskSubTabPanelPresenter.TYPE_ChangeTab,
            DiskSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<String, String> map) {
            map.put(VirtualMachine.name(), WebAdminApplicationPlaces.diskVmSubTabPlace);
            map.put(Template.name(), WebAdminApplicationPlaces.diskTemplateSubTabPlace);
            map.put(Storage.name(), WebAdminApplicationPlaces.diskStorageSubTabPlace);
            map.put("Permission", WebAdminApplicationPlaces.diskPermissionSubTabPlace); //$NON-NLS-1$
        }
    },
    VirtualMachine(WebAdminApplicationPlaces.virtualMachineMainPlace,
            VirtualMachineSubTabPanelPresenter.TYPE_RequestTabs,
            VirtualMachineSubTabPanelPresenter.TYPE_ChangeTab,
            VirtualMachineSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<String, String> map) {
            map.put("NetworkInterface", WebAdminApplicationPlaces.virtualMachineNetworkInterfaceSubTabPlace); //$NON-NLS-1$
            map.put(Disk.name(), WebAdminApplicationPlaces.virtualMachineVirtualDiskSubTabPlace);
            map.put("HostDevice", WebAdminApplicationPlaces.virtualMachineHostDeviceSubTabPlace); //$NON-NLS-1$
            map.put("VirtualMachineSnapshot", WebAdminApplicationPlaces.virtualMachineSnapshotSubTabPlace); //$NON-NLS-1$
            map.put("VirtualMachineApplication", WebAdminApplicationPlaces.virtualMachineApplicationSubTabPlace); //$NON-NLS-1$
            map.put("VirtualMachineContainer", WebAdminApplicationPlaces.virtualMachineContainerSubTabPlace); //$NON-NLS-1$
            map.put("VirtualMachineDevice", WebAdminApplicationPlaces.virtualMachineVmDeviceSubTabPlace); //$NON-NLS-1$
            map.put("AffinityGroup", WebAdminApplicationPlaces.virtualMachineAffinityGroupsSubTabPlace); //$NON-NLS-1$
            map.put("AffinityLabel", WebAdminApplicationPlaces.virtualMachineAffinityLabelsSubTabPlace); //$NON-NLS-1$
            map.put("Permission", WebAdminApplicationPlaces.virtualMachinePermissionSubTabPlace); //$NON-NLS-1$
            map.put(Event.name(), WebAdminApplicationPlaces.virtualMachineEventSubTabPlace);
        }
    },
    Pool(WebAdminApplicationPlaces.poolMainPlace,
            PoolSubTabPanelPresenter.TYPE_RequestTabs,
            PoolSubTabPanelPresenter.TYPE_ChangeTab,
            PoolSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<String, String> map) {
            map.put(VirtualMachine.name(), WebAdminApplicationPlaces.poolVmSubTabPlace);
            map.put("Permission", WebAdminApplicationPlaces.poolPermissionSubTabPlace); //$NON-NLS-1$
        }
    },
    Template(WebAdminApplicationPlaces.templateMainPlace,
            TemplateSubTabPanelPresenter.TYPE_RequestTabs,
            TemplateSubTabPanelPresenter.TYPE_ChangeTab,
            TemplateSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<String, String> map) {
            map.put(VirtualMachine.name(), WebAdminApplicationPlaces.templateVmSubTabPlace);
            map.put("NetworkInterface", WebAdminApplicationPlaces.templateInterfaceSubTabPlace); //$NON-NLS-1$
            map.put("Permission", WebAdminApplicationPlaces.templatePermissionSubTabPlace); //$NON-NLS-1$
            map.put(Event.name(), WebAdminApplicationPlaces.templateEventSubTabPlace);
        }
    },
    GlusterVolume(WebAdminApplicationPlaces.volumeMainPlace,
            VolumeSubTabPanelPresenter.TYPE_RequestTabs,
            VolumeSubTabPanelPresenter.TYPE_ChangeTab,
            VolumeSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<String, String> map) {
            map.put("GlusterVolumeParameter", WebAdminApplicationPlaces.volumeParameterSubTabPlace); //$NON-NLS-1$
            map.put("GlusterBrick", WebAdminApplicationPlaces.volumeBrickSubTabPlace); //$NON-NLS-1$
            map.put("Permission", WebAdminApplicationPlaces.volumePermissionSubTabPlace); //$NON-NLS-1$
            map.put(Event.name(), WebAdminApplicationPlaces.volumeEventSubTabPlace);
            map.put("GlusterGeoRep", WebAdminApplicationPlaces.volumeGeoRepSubTabPlace); //$NON-NLS-1$
            map.put("GlusterVolumeSnapshot", WebAdminApplicationPlaces.volumeSnapshotSubTabPlace); //$NON-NLS-1$
        }
    },
    Provider(WebAdminApplicationPlaces.providerMainPlace,
            ProviderSubTabPanelPresenter.TYPE_RequestTabs,
            ProviderSubTabPanelPresenter.TYPE_ChangeTab,
            ProviderSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<String, String> map) {
            map.put(Network.name(), WebAdminApplicationPlaces.providerNetworkSubTabPlace);
            map.put("ProviderSecret", WebAdminApplicationPlaces.providerSecretSubTabPlace); //$NON-NLS-1$
        }
    },
    User(WebAdminApplicationPlaces.userMainPlace,
            UserSubTabPanelPresenter.TYPE_RequestTabs,
            UserSubTabPanelPresenter.TYPE_ChangeTab,
            UserSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<String, String> map) {
            map.put(Quota.name(), WebAdminApplicationPlaces.userQuotaSubTabPlace);
            map.put("UserGroup", WebAdminApplicationPlaces.userGroupSubTabPlace); //$NON-NLS-1$
            map.put("EventNotifier", WebAdminApplicationPlaces.userEventNotifierSubTabPlace); //$NON-NLS-1$
            map.put("Permission", WebAdminApplicationPlaces.userPermissionSubTabPlace); //$NON-NLS-1$
            map.put(Event.name(), WebAdminApplicationPlaces.userEventSubTabPlace);
        }
    },
    Quota(WebAdminApplicationPlaces.quotaMainPlace,
            QuotaSubTabPanelPresenter.TYPE_RequestTabs,
            QuotaSubTabPanelPresenter.TYPE_ChangeTab,
            QuotaSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<String, String> map) {
            map.put(Cluster.name(), WebAdminApplicationPlaces.quotaClusterSubTabPlace);
            map.put(Storage.name(), WebAdminApplicationPlaces.quotaStorageSubTabPlace);
            map.put(User.name(), WebAdminApplicationPlaces.quotaUserSubTabPlace);
            map.put("Permission", WebAdminApplicationPlaces.quotaPermissionSubTabPlace); //$NON-NLS-1$
            map.put(Event.name(), WebAdminApplicationPlaces.quotaEventSubTabPlace);
            map.put(VirtualMachine.name(), WebAdminApplicationPlaces.quotaVmSubTabPlace);
            map.put(Template.name(), WebAdminApplicationPlaces.quotaTemplateSubTabPlace);
        }
    },
    Errata(WebAdminApplicationPlaces.errataMainPlace),
    Event(WebAdminApplicationPlaces.eventMainPlace),

    Undefined; // Null object

    public static EntityType from(String name) {
        EntityType result = EntityType.Undefined;

        try {
            result = EntityType.valueOf(name);
        } catch (IllegalArgumentException e) {
            // Do nothing
        }

        return result;
    }

    private final String mainHistoryToken;

    private final Type<RequestTabsHandler> subTabPanelRequestTabs;
    private final Type<ChangeTabHandler> subTabPanelChangeTab;
    private final Type<RevealContentHandler<?>> subTabPanelContentSlot;

    private Map<String, String> subTabHistoryTokens;

    EntityType(String mainTabHistoryToken,
            Type<RequestTabsHandler> subTabPanelRequestTabs,
            Type<ChangeTabHandler> subTabPanelChangeTab,
            Type<RevealContentHandler<?>> subTabPanelContentSlot) {
        this.mainHistoryToken = mainTabHistoryToken;
        this.subTabPanelRequestTabs = subTabPanelRequestTabs;
        this.subTabPanelChangeTab = subTabPanelChangeTab;
        this.subTabPanelContentSlot = subTabPanelContentSlot;
    }

    EntityType(String mainTabHistoryToken) {
        this(mainTabHistoryToken, null, null, null);
    }

    EntityType() {
        this(null);
    }

    /**
     * Returns the history token used to access main tab presenter.
     */
    public String getMainHistoryToken() {
        return mainHistoryToken;
    }

    /**
     * Returns GWTP {@code RequestTabs} event type for sub tab panel presenter.
     */
    public Type<RequestTabsHandler> getSubTabPanelRequestTabs() {
        return subTabPanelRequestTabs;
    }

    /**
     * Returns GWTP {@code ChangeTab} event type for sub tab panel presenter.
     */
    public Type<ChangeTabHandler> getSubTabPanelChangeTab() {
        return subTabPanelChangeTab;
    }

    /**
     * Returns GWTP {@code ContentSlot} event type for sub tab panel presenter.
     */
    public Type<RevealContentHandler<?>> getSubTabPanelContentSlot() {
        return subTabPanelContentSlot;
    }

    /**
     * Returns the history token used to access sub tab presenter of the given type.
     */
    public String getSubTabHistoryToken(String detailPlaceId) {
        if (subTabHistoryTokens == null) {
            subTabHistoryTokens = new HashMap<>();
            initSubTabHistoryTokens(subTabHistoryTokens);
        }
        return subTabHistoryTokens.get(detailPlaceId);
    }

    protected void initSubTabHistoryTokens(Map<String, String> map) {
        // No-op, override as necessary
    }

}
