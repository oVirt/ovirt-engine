package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeGeoRepListModel;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.VolumeSelectionChangeEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabVolumeGeoRepPresenter extends AbstractSubTabPresenter<GlusterVolumeEntity, VolumeListModel, VolumeGeoRepListModel, SubTabVolumeGeoRepPresenter.ViewDef, SubTabVolumeGeoRepPresenter.ProxyDef> {
    @TabInfo(container = VolumeSubTabPanelPresenter.class)
    static TabData getTabData(ApplicationConstants applicationConstants, SearchableDetailModelProvider<GlusterGeoRepSession, VolumeListModel, VolumeGeoRepListModel> modelProvider) {
        return new ModelBoundTabData(applicationConstants.volumeGeoRepSubTabLabel(), 4, modelProvider);
    }

    @Inject
    public SubTabVolumeGeoRepPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy, PlaceManager placeManager, SearchableDetailModelProvider<GlusterGeoRepSession, VolumeListModel, VolumeGeoRepListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, VolumeSubTabPanelPresenter.TYPE_SetTabContent);
    }

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.volumeGeoRepSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabVolumeGeoRepPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<GlusterVolumeEntity> {
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.volumeMainTabPlace);
    }

    @ProxyEvent
    public void onVolumeSelectionChange(VolumeSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }
}
