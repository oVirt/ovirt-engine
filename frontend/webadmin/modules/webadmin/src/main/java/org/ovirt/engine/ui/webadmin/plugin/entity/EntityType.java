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

    DataCenter(WebAdminApplicationPlaces.dataCenterMainTabPlace,
            DataCenterSubTabPanelPresenter.TYPE_RequestTabs,
            DataCenterSubTabPanelPresenter.TYPE_ChangeTab,
            DataCenterSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<EntityType, String> map) {
            map.put(Event, WebAdminApplicationPlaces.dataCenterEventSubTabPlace);
        }
    },
    Cluster(WebAdminApplicationPlaces.clusterMainTabPlace,
            ClusterSubTabPanelPresenter.TYPE_RequestTabs,
            ClusterSubTabPanelPresenter.TYPE_ChangeTab,
            ClusterSubTabPanelPresenter.TYPE_SetTabContent) {
    },
    Host(WebAdminApplicationPlaces.hostMainTabPlace,
            HostSubTabPanelPresenter.TYPE_RequestTabs,
            HostSubTabPanelPresenter.TYPE_ChangeTab,
            HostSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<EntityType, String> map) {
            map.put(Event, WebAdminApplicationPlaces.hostEventSubTabPlace);
        }
    },
    Network(WebAdminApplicationPlaces.networkMainTabPlace,
            NetworkSubTabPanelPresenter.TYPE_RequestTabs,
            NetworkSubTabPanelPresenter.TYPE_ChangeTab,
            NetworkSubTabPanelPresenter.TYPE_SetTabContent) {
    },
    VnicProfile(WebAdminApplicationPlaces.vnicProfileMainTabPlace,
            VnicProfileSubTabPanelPresenter.TYPE_RequestTabs,
            VnicProfileSubTabPanelPresenter.TYPE_ChangeTab,
            VnicProfileSubTabPanelPresenter.TYPE_SetTabContent) {
    },
    Storage(WebAdminApplicationPlaces.storageMainTabPlace,
            StorageSubTabPanelPresenter.TYPE_RequestTabs,
            StorageSubTabPanelPresenter.TYPE_ChangeTab,
            StorageSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<EntityType, String> map) {
            map.put(Event, WebAdminApplicationPlaces.storageEventSubTabPlace);
        }
    },
    Disk(WebAdminApplicationPlaces.diskMainTabPlace,
            DiskSubTabPanelPresenter.TYPE_RequestTabs,
            DiskSubTabPanelPresenter.TYPE_ChangeTab,
            DiskSubTabPanelPresenter.TYPE_SetTabContent) {
    },
    VirtualMachine(WebAdminApplicationPlaces.virtualMachineMainTabPlace,
            VirtualMachineSubTabPanelPresenter.TYPE_RequestTabs,
            VirtualMachineSubTabPanelPresenter.TYPE_ChangeTab,
            VirtualMachineSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<EntityType, String> map) {
            map.put(Event, WebAdminApplicationPlaces.virtualMachineEventSubTabPlace);
        }
    },
    Pool(WebAdminApplicationPlaces.poolMainTabPlace,
            PoolSubTabPanelPresenter.TYPE_RequestTabs,
            PoolSubTabPanelPresenter.TYPE_ChangeTab,
            PoolSubTabPanelPresenter.TYPE_SetTabContent) {
    },
    Template(WebAdminApplicationPlaces.templateMainTabPlace,
            TemplateSubTabPanelPresenter.TYPE_RequestTabs,
            TemplateSubTabPanelPresenter.TYPE_ChangeTab,
            TemplateSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<EntityType, String> map) {
            map.put(Event, WebAdminApplicationPlaces.templateEventSubTabPlace);
        }
    },
    GlusterVolume(WebAdminApplicationPlaces.volumeMainTabPlace,
            VolumeSubTabPanelPresenter.TYPE_RequestTabs,
            VolumeSubTabPanelPresenter.TYPE_ChangeTab,
            VolumeSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<EntityType, String> map) {
            map.put(Event, WebAdminApplicationPlaces.volumeEventSubTabPlace);
        }
    },
    Provider(WebAdminApplicationPlaces.providerMainTabPlace,
            ProviderSubTabPanelPresenter.TYPE_RequestTabs,
            ProviderSubTabPanelPresenter.TYPE_ChangeTab,
            ProviderSubTabPanelPresenter.TYPE_SetTabContent) {
    },
    User(WebAdminApplicationPlaces.userMainTabPlace,
            UserSubTabPanelPresenter.TYPE_RequestTabs,
            UserSubTabPanelPresenter.TYPE_ChangeTab,
            UserSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<EntityType, String> map) {
            map.put(Event, WebAdminApplicationPlaces.userEventSubTabPlace);
        }
    },
    Quota(WebAdminApplicationPlaces.quotaMainTabPlace,
            QuotaSubTabPanelPresenter.TYPE_RequestTabs,
            QuotaSubTabPanelPresenter.TYPE_ChangeTab,
            QuotaSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<EntityType, String> map) {
            map.put(Event, WebAdminApplicationPlaces.quotaEventSubTabPlace);
        }
    },
    Event(WebAdminApplicationPlaces.eventMainTabPlace),

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

    private final String mainTabHistoryToken;

    private final Type<RequestTabsHandler> subTabPanelRequestTabs;
    private final Type<ChangeTabHandler> subTabPanelChangeTab;
    private final Type<RevealContentHandler<?>> subTabPanelContentSlot;

    private Map<EntityType, String> subTabHistoryTokenMap;

    EntityType(String mainTabHistoryToken,
            Type<RequestTabsHandler> subTabPanelRequestTabs,
            Type<ChangeTabHandler> subTabPanelChangeTab,
            Type<RevealContentHandler<?>> subTabPanelContentSlot) {
        this.mainTabHistoryToken = mainTabHistoryToken;
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
    public String getMainTabHistoryToken() {
        return mainTabHistoryToken;
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
    public String getSubTabHistoryToken(EntityType subTabEntityType) {
        if (subTabHistoryTokenMap == null) {
            subTabHistoryTokenMap = new HashMap<>();
            initSubTabHistoryTokens(subTabHistoryTokenMap);
        }
        return subTabHistoryTokenMap.get(subTabEntityType);
    }

    protected void initSubTabHistoryTokens(Map<EntityType, String> map) {
        // No-op, override as necessary
    }

}
