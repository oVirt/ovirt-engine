package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DetailTabDataIndex;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabVolumePermissionPresenter
    extends AbstractSubTabGlusterPresenter<PermissionListModel<GlusterVolumeEntity>,
        SubTabVolumePermissionPresenter.ViewDef, SubTabVolumePermissionPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.volumePermissionSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabVolumePermissionPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<GlusterVolumeEntity> {
    }

    @TabInfo(container = VolumeSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.GLUSTER_PERMISSIONS;
    }

    @Inject
    public SubTabVolumePermissionPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, VolumeMainSelectedItems selectedItems,
            SearchableDetailModelProvider<Permission, VolumeListModel, PermissionListModel<GlusterVolumeEntity>> modelProvider) {
        // View uses PermissionWithInheritedPermissionListModelTable to get action panel elsewhere passing null.
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, null,
                VolumeSubTabPanelPresenter.TYPE_SetTabContent);
    }

}
