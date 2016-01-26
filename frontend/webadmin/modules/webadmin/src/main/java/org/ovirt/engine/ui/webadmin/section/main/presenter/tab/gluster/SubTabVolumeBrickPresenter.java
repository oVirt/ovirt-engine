package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeBrickListModel;
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

public class SubTabVolumeBrickPresenter
    extends AbstractSubTabGlusterPresenter<VolumeBrickListModel, SubTabVolumeBrickPresenter.ViewDef,
        SubTabVolumeBrickPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.volumeBrickSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabVolumeBrickPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<GlusterVolumeEntity> {
    }

    @TabInfo(container = VolumeSubTabPanelPresenter.class)
    static TabData getTabData(
            SearchableDetailModelProvider<GlusterBrickEntity, VolumeListModel, VolumeBrickListModel> modelProvider) {
        return new ModelBoundTabData(constants.volumeBrickSubTabLabel(), 1, modelProvider);
    }

    @Inject
    public SubTabVolumeBrickPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, VolumeMainTabSelectedItems selectedItems,
            SearchableDetailModelProvider<GlusterBrickEntity, VolumeListModel, VolumeBrickListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems,
                VolumeSubTabPanelPresenter.TYPE_SetTabContent);
    }
}
