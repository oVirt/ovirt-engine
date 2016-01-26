package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabDiskPermissionPresenter
    extends AbstractSubTabDiskPresenter<PermissionListModel<Disk>, SubTabDiskPermissionPresenter.ViewDef,
        SubTabDiskPermissionPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.diskPermissionSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabDiskPermissionPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<Disk> {
    }

    @TabInfo(container = DiskSubTabPanelPresenter.class)
    static TabData getTabData(
            SearchableDetailModelProvider<Permission, DiskListModel,
            PermissionListModel<Disk>> modelProvider) {
        return new ModelBoundTabData(constants.diskPermissionSubTabLabel(), 4, modelProvider);
    }

    @Inject
    public SubTabDiskPermissionPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, DiskMainTabSelectedItems selectedItems,
            SearchableDetailModelProvider<Permission, DiskListModel,
            PermissionListModel<Disk>> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems,
                DiskSubTabPanelPresenter.TYPE_SetTabContent);
    }
}
