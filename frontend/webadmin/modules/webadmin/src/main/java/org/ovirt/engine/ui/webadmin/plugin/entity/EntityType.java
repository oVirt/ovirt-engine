package org.ovirt.engine.ui.webadmin.plugin.entity;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.ui.webadmin.place.ApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.ClusterSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.DataCenterSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk.DiskSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster.VolumeSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.HostSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.NetworkSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool.PoolSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.provider.ProviderSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.QuotaSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.StorageSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.TemplateSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.UserSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.VirtualMachineSubTabPanelPresenter;

import com.google.gwt.event.shared.GwtEvent.Type;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

/**
 * Enumerates possible entity types used within the plugin API.
 * <p>
 * Contains useful meta-data associated with business entity types.
 */
public enum EntityType {

    DataCenter(ApplicationPlaces.dataCenterMainTabPlace,
            DataCenterSubTabPanelPresenter.TYPE_RequestTabs,
            DataCenterSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<EntityType, String> map) {
            map.put(Event, ApplicationPlaces.dataCenterEventSubTabPlace);
        }
    },
    Cluster(ApplicationPlaces.clusterMainTabPlace,
            ClusterSubTabPanelPresenter.TYPE_RequestTabs,
            ClusterSubTabPanelPresenter.TYPE_SetTabContent) {
    },
    Host(ApplicationPlaces.hostMainTabPlace,
            HostSubTabPanelPresenter.TYPE_RequestTabs,
            HostSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<EntityType, String> map) {
            map.put(Event, ApplicationPlaces.hostEventSubTabPlace);
        }
    },
    Network(ApplicationPlaces.networkMainTabPlace,
            NetworkSubTabPanelPresenter.TYPE_RequestTabs,
            NetworkSubTabPanelPresenter.TYPE_SetTabContent) {
    },
    Storage(ApplicationPlaces.storageMainTabPlace,
            StorageSubTabPanelPresenter.TYPE_RequestTabs,
            StorageSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<EntityType, String> map) {
            map.put(Event, ApplicationPlaces.storageEventSubTabPlace);
        }
    },
    Disk(ApplicationPlaces.diskMainTabPlace,
            DiskSubTabPanelPresenter.TYPE_RequestTabs,
            DiskSubTabPanelPresenter.TYPE_SetTabContent) {
    },
    VirtualMachine(ApplicationPlaces.virtualMachineMainTabPlace,
            VirtualMachineSubTabPanelPresenter.TYPE_RequestTabs,
            VirtualMachineSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<EntityType, String> map) {
            map.put(Event, ApplicationPlaces.virtualMachineEventSubTabPlace);
        }
    },
    Pool(ApplicationPlaces.poolMainTabPlace,
            PoolSubTabPanelPresenter.TYPE_RequestTabs,
            PoolSubTabPanelPresenter.TYPE_SetTabContent) {
    },
    Template(ApplicationPlaces.templateMainTabPlace,
            TemplateSubTabPanelPresenter.TYPE_RequestTabs,
            TemplateSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<EntityType, String> map) {
            map.put(Event, ApplicationPlaces.templateEventSubTabPlace);
        }
    },
    GlusterVolume(ApplicationPlaces.volumeMainTabPlace,
            VolumeSubTabPanelPresenter.TYPE_RequestTabs,
            VolumeSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<EntityType, String> map) {
            map.put(Event, ApplicationPlaces.volumeEventSubTabPlace);
        }
    },
    Provider(ApplicationPlaces.providerMainTabPlace,
            ProviderSubTabPanelPresenter.TYPE_RequestTabs,
            ProviderSubTabPanelPresenter.TYPE_SetTabContent) {
    },
    User(ApplicationPlaces.userMainTabPlace,
            UserSubTabPanelPresenter.TYPE_RequestTabs,
            UserSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<EntityType, String> map) {
            map.put(Event, ApplicationPlaces.userEventSubTabPlace);
        }
    },
    Quota(ApplicationPlaces.quotaMainTabPlace,
            QuotaSubTabPanelPresenter.TYPE_RequestTabs,
            QuotaSubTabPanelPresenter.TYPE_SetTabContent) {
        @Override
        protected void initSubTabHistoryTokens(Map<EntityType, String> map) {
            map.put(Event, ApplicationPlaces.quotaEventSubTabPlace);
        }
    },
    Event(ApplicationPlaces.eventMainTabPlace),

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
    private final Type<RevealContentHandler<?>> subTabPanelContentSlot;

    private Map<EntityType, String> subTabHistoryTokenMap;

    EntityType(String mainTabHistoryToken,
            Type<RequestTabsHandler> subTabPanelRequestTabs,
            Type<RevealContentHandler<?>> subTabPanelContentSlot) {
        this.mainTabHistoryToken = mainTabHistoryToken;
        this.subTabPanelRequestTabs = subTabPanelRequestTabs;
        this.subTabPanelContentSlot = subTabPanelContentSlot;
    }

    EntityType(String mainTabHistoryToken) {
        this(mainTabHistoryToken, null, null);
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
     * Returns GWTP RequestTabs event type for sub tab panel presenter.
     */
    public Type<RequestTabsHandler> getSubTabPanelRequestTabs() {
        return subTabPanelRequestTabs;
    }

    /**
     * Returns GWTP ContentSlot event type for sub tab panel presenter.
     */
    public Type<RevealContentHandler<?>> getSubTabPanelContentSlot() {
        return subTabPanelContentSlot;
    }

    /**
     * Returns the history token used to access sub tab presenter of the given type.
     */
    public String getSubTabHistoryToken(EntityType subTabEntityType) {
        if (subTabHistoryTokenMap == null) {
            subTabHistoryTokenMap = new HashMap<EntityType, String>();
            initSubTabHistoryTokens(subTabHistoryTokenMap);
        }
        return subTabHistoryTokenMap.get(subTabEntityType);
    }

    protected void initSubTabHistoryTokens(Map<EntityType, String> map) {
        // No-op, override as necessary
    }

}
