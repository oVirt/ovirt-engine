package org.ovirt.engine.ui.webadmin.widget.tab;

import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.dom.client.Style.HasCssName;

public enum DefaultSubMenu {

    VIRTUAL_MACHINE(AssetProvider.getConstants().virtualMachineMainTabLabel(), 0, DefaultMenuLayout.COMPUTE,
            WebAdminApplicationPlaces.virtualMachineMainTabPlace, null),
    TEMPLATES(AssetProvider.getConstants().templateMainTabLabel(), 1, DefaultMenuLayout.COMPUTE,
            WebAdminApplicationPlaces.templateMainTabPlace, null),
    POOLS(AssetProvider.getConstants().poolMainTabLabel(), 2, DefaultMenuLayout.COMPUTE,
            WebAdminApplicationPlaces.poolMainTabPlace, null),
    HOSTS(AssetProvider.getConstants().hostMainTabLabel(), 3, DefaultMenuLayout.COMPUTE,
            WebAdminApplicationPlaces.hostMainTabPlace, null),
    DC(AssetProvider.getConstants().dataCenterMainTabLabel(), 4, DefaultMenuLayout.COMPUTE,
            WebAdminApplicationPlaces.dataCenterMainTabPlace, null),
    CLUSTERS(AssetProvider.getConstants().clusterMainTabLabel(), 5, DefaultMenuLayout.COMPUTE,
            WebAdminApplicationPlaces.clusterMainTabPlace, null),
    NETWORKS(AssetProvider.getConstants().networkMainTabLabel(), 0, DefaultMenuLayout.NETWORK,
            WebAdminApplicationPlaces.networkMainTabPlace, null),
    VNIC_PROFILE(AssetProvider.getConstants().vnicProfilesMainTabLabel(), 1, DefaultMenuLayout.NETWORK,
            WebAdminApplicationPlaces.vnicProfileMainTabPlace, null),
    STORAGE(AssetProvider.getConstants().storageMainTabLabel(), 0, DefaultMenuLayout.STORAGE,
            WebAdminApplicationPlaces.storageMainTabPlace, null),
    DISKS(AssetProvider.getConstants().diskMainTabLabel(), 1, DefaultMenuLayout.STORAGE,
            WebAdminApplicationPlaces.diskMainTabPlace, null),
    VOLUMES(AssetProvider.getConstants().volumeMainTabLabel(), 2, DefaultMenuLayout.STORAGE,
            WebAdminApplicationPlaces.volumeMainTabPlace, null),
    PROVIDERS(AssetProvider.getConstants().providerMainTabLabel(), 0, DefaultMenuLayout.ADMIN,
            WebAdminApplicationPlaces.providerMainTabPlace, null),
    QUOTA(AssetProvider.getConstants().quotaMainTabLabel(), 1, DefaultMenuLayout.ADMIN,
            WebAdminApplicationPlaces.quotaMainTabPlace, null),
    SESSIONS(AssetProvider.getConstants().activeUserSessionMainTabLabel(), 2, DefaultMenuLayout.ADMIN,
            WebAdminApplicationPlaces.sessionMainTabPlace, null),
    USERS(AssetProvider.getConstants().userMainTabLabel(), 3, DefaultMenuLayout.ADMIN,
            WebAdminApplicationPlaces.userMainTabPlace, null),
    ERRATA(AssetProvider.getConstants().errataMainTabLabel(), 4, DefaultMenuLayout.ADMIN,
            WebAdminApplicationPlaces.errataMainTabPlace, null);

    private String title;
    private int priority;
    private DefaultMenuLayout primaryMenu;
    private String href;
    private HasCssName icon;

    DefaultSubMenu(String title, int priority, DefaultMenuLayout primaryMenu, String href, HasCssName icon) {
        this.title = title;
        this.priority = priority;
        this.primaryMenu = primaryMenu;
        this.href = href;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public int getPriority() {
        return priority;
    }

    public DefaultMenuLayout getPrimaryMenu() {
        return primaryMenu;
    }

    public String getHref() {
        return href;
    }

    public HasCssName getIcon() {
        return primaryMenu.getIcon();
    }
}
