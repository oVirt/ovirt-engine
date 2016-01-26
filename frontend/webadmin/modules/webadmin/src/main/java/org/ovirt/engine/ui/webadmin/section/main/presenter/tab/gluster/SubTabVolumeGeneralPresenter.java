package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeGeneralModel;
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

public class SubTabVolumeGeneralPresenter
    extends AbstractSubTabGlusterPresenter<VolumeGeneralModel, SubTabVolumeGeneralPresenter.ViewDef,
        SubTabVolumeGeneralPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.volumeGeneralSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabVolumeGeneralPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<GlusterVolumeEntity> {
    }

    @TabInfo(container = VolumeSubTabPanelPresenter.class)
    static TabData getTabData(
            DetailModelProvider<VolumeListModel, VolumeGeneralModel> modelProvider) {
        return new ModelBoundTabData(constants.volumeGeneralSubTabLabel(), 0, modelProvider);
    }

    @Inject
    public SubTabVolumeGeneralPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, VolumeMainTabSelectedItems selectedItems,
            DetailModelProvider<VolumeListModel, VolumeGeneralModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems,
                VolumeSubTabPanelPresenter.TYPE_SetTabContent);
    }
}
