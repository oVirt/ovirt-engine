package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterVolumeSnapshotListModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
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

public class SubTabGlusterVolumeSnapshotPresenter
    extends AbstractSubTabGlusterPresenter<GlusterVolumeSnapshotListModel, SubTabGlusterVolumeSnapshotPresenter.ViewDef,
        SubTabGlusterVolumeSnapshotPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.volumeSnapshotSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabGlusterVolumeSnapshotPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<GlusterVolumeEntity> {
    }

    @TabInfo(container = VolumeSubTabPanelPresenter.class)
    static TabData getTabData(
            SearchableDetailModelProvider<GlusterVolumeSnapshotEntity, VolumeListModel, GlusterVolumeSnapshotListModel> modelProvider) {
        return new ModelBoundTabData(constants.volumeSnapshotSubTabLabel(), 5, modelProvider);
    }

    @Inject
    public SubTabGlusterVolumeSnapshotPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, VolumeMainTabSelectedItems selectedItems,
            SearchableDetailModelProvider<GlusterVolumeSnapshotEntity, VolumeListModel, GlusterVolumeSnapshotListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems,
                VolumeSubTabPanelPresenter.TYPE_SetTabContent);
    }
}
