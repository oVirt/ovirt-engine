package org.ovirt.engine.ui.webadmin.widget.tab;

import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.dom.client.Style.HasCssName;

public enum AllModesSubMenu implements SecondaryMenuItem {

    //
    // NOTE TO DEVELOPERS: Please make sure if you add any menu items to put them in all variations of this
    // as well for instance gluster only or virt only. The presenters need that information to instantiate the
    // 'tabs' that make up the menu.
    //
    VIRTUAL_MACHINE(AssetProvider.getConstants().virtualMachineMainTabLabel(), 0, AllModesMenuLayout.COMPUTE,
            WebAdminApplicationPlaces.virtualMachineMainTabPlace),
    TEMPLATES(AssetProvider.getConstants().templateMainTabLabel(), 1, AllModesMenuLayout.COMPUTE,
            WebAdminApplicationPlaces.templateMainTabPlace),
    POOLS(AssetProvider.getConstants().poolMainTabLabel(), 2, AllModesMenuLayout.COMPUTE,
            WebAdminApplicationPlaces.poolMainTabPlace),
    HOSTS(AssetProvider.getConstants().hostMainTabLabel(), 3, AllModesMenuLayout.COMPUTE,
            WebAdminApplicationPlaces.hostMainTabPlace),
    DC(AssetProvider.getConstants().dataCenterMainTabLabel(), 4, AllModesMenuLayout.COMPUTE,
            WebAdminApplicationPlaces.dataCenterMainTabPlace),
    CLUSTERS(AssetProvider.getConstants().clusterMainTabLabel(), 5, AllModesMenuLayout.COMPUTE,
            WebAdminApplicationPlaces.clusterMainTabPlace),
    NETWORKS(AssetProvider.getConstants().networkMainTabLabel(), 0, AllModesMenuLayout.NETWORK,
            WebAdminApplicationPlaces.networkMainTabPlace),
    VNIC_PROFILE(AssetProvider.getConstants().vnicProfilesMainTabLabel(), 1, AllModesMenuLayout.NETWORK,
            WebAdminApplicationPlaces.vnicProfileMainTabPlace),
    STORAGE(AssetProvider.getConstants().storageDomainsMenuLabel(), 0, AllModesMenuLayout.STORAGE,
            WebAdminApplicationPlaces.storageMainTabPlace),
    DISKS(AssetProvider.getConstants().diskMainTabLabel(), 1, AllModesMenuLayout.STORAGE,
            WebAdminApplicationPlaces.diskMainTabPlace),
    VOLUMES(AssetProvider.getConstants().volumeMainTabLabel(), 2, AllModesMenuLayout.STORAGE,
            WebAdminApplicationPlaces.volumeMainTabPlace),
    PROVIDERS(AssetProvider.getConstants().providerMainTabLabel(), 0, AllModesMenuLayout.ADMIN,
            WebAdminApplicationPlaces.providerMainTabPlace),
    QUOTA(AssetProvider.getConstants().quotaMainTabLabel(), 1, AllModesMenuLayout.ADMIN,
            WebAdminApplicationPlaces.quotaMainTabPlace),
    SESSIONS(AssetProvider.getConstants().activeUserSessionMainTabLabel(), 2, AllModesMenuLayout.ADMIN,
            WebAdminApplicationPlaces.sessionMainTabPlace),
    USERS(AssetProvider.getConstants().userMainTabLabel(), 3, AllModesMenuLayout.ADMIN,
            WebAdminApplicationPlaces.userMainTabPlace),
    ERRATA(AssetProvider.getConstants().errataMainTabLabel(), 4, AllModesMenuLayout.ADMIN,
            WebAdminApplicationPlaces.errataMainTabPlace);

    private String title;
    private int priority;
    private PrimaryMenuItem primaryMenu;
    private String href;

    AllModesSubMenu(String title, int priority, AllModesMenuLayout primaryMenu, String href) {
        this.title = title;
        this.priority = priority;
        this.primaryMenu = primaryMenu;
        this.href = href;
    }

    public String getTitle() {
        return title;
    }

    public int getIndex() {
        return priority;
    }

    public PrimaryMenuItem getPrimaryMenu() {
        return primaryMenu;
    }

    public String getHref() {
        return href;
    }

    public HasCssName getIcon() {
        return primaryMenu.getIcon();
    }
}
