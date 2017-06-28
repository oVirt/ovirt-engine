package org.ovirt.engine.ui.webadmin.widget.tab;

import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.dom.client.Style.HasCssName;

public enum GlusterModeSubMenu implements SecondaryMenuItem {
    HOSTS(AssetProvider.getConstants().hostMainTabLabel(), 0, GlusterModeMenuLayout.STORAGE,
            WebAdminApplicationPlaces.hostMainTabPlace),
    CLUSTERS(AssetProvider.getConstants().clusterMainTabLabel(), 1, GlusterModeMenuLayout.STORAGE,
            WebAdminApplicationPlaces.clusterMainTabPlace),
    VOLUMES(AssetProvider.getConstants().volumeMainTabLabel(), 2, GlusterModeMenuLayout.STORAGE,
            WebAdminApplicationPlaces.volumeMainTabPlace),
    SESSIONS(AssetProvider.getConstants().activeUserSessionMainTabLabel(), 0, GlusterModeMenuLayout.ADMIN,
            WebAdminApplicationPlaces.sessionMainTabPlace),
    USERS(AssetProvider.getConstants().userMainTabLabel(), 1, GlusterModeMenuLayout.ADMIN,
            WebAdminApplicationPlaces.userMainTabPlace),
    ERRATA(AssetProvider.getConstants().errataMainTabLabel(), 2, GlusterModeMenuLayout.ADMIN,
            WebAdminApplicationPlaces.errataMainTabPlace),
    // These are here so they can be hidden. We still create the presenters which will query for their
    // information, but then get hidden (as well as being blocked at the places level).
    // It doesn't really matter which main tab these get put into, they will be hidden.
    // TODO: With ModelBoundTab removal patch, this might not be needed anymore.
    DC(AssetProvider.getConstants().dataCenterMainTabLabel(), 3, GlusterModeMenuLayout.STORAGE,
            WebAdminApplicationPlaces.dataCenterMainTabPlace),
    DISKS(AssetProvider.getConstants().diskMainTabLabel(), 4, GlusterModeMenuLayout.STORAGE,
            WebAdminApplicationPlaces.diskMainTabPlace),
    POOLS(AssetProvider.getConstants().poolMainTabLabel(), 5, GlusterModeMenuLayout.STORAGE,
            WebAdminApplicationPlaces.poolMainTabPlace),
    PROVIDERS(AssetProvider.getConstants().providerMainTabLabel(), 3, GlusterModeMenuLayout.ADMIN,
            WebAdminApplicationPlaces.providerMainTabPlace),
    QUOTA(AssetProvider.getConstants().quotaMainTabLabel(), 4, GlusterModeMenuLayout.ADMIN,
            WebAdminApplicationPlaces.quotaMainTabPlace),
    STORAGE(AssetProvider.getConstants().storageMainTabLabel(), 6, GlusterModeMenuLayout.STORAGE,
            WebAdminApplicationPlaces.storageMainTabPlace),
    TEMPLATES(AssetProvider.getConstants().templateMainTabLabel(), 7, GlusterModeMenuLayout.STORAGE,
            WebAdminApplicationPlaces.templateMainTabPlace),
    VIRTUAL_MACHINE(AssetProvider.getConstants().virtualMachineMainTabLabel(), 8, GlusterModeMenuLayout.STORAGE,
            WebAdminApplicationPlaces.virtualMachineMainTabPlace),
    VNIC_PROFILE(AssetProvider.getConstants().vnicProfilesMainTabLabel(), 9, GlusterModeMenuLayout.STORAGE,
            WebAdminApplicationPlaces.vnicProfileMainTabPlace);

    private String title;
    private int priority;
    private PrimaryMenuItem primaryMenu;
    private String href;

    GlusterModeSubMenu(String title, int priority, PrimaryMenuItem primaryMenu, String href) {
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
