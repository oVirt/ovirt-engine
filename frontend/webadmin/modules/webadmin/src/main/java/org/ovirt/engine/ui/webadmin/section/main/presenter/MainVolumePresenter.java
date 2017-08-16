package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.OvirtBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.volumes.VolumeListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.VolumeActionPanelPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class MainVolumePresenter extends AbstractMainWithDetailsPresenter<GlusterVolumeEntity, VolumeListModel, MainVolumePresenter.ViewDef, MainVolumePresenter.ProxyDef> {

    @GenEvent
    public class VolumeSelectionChange {

        List<GlusterVolumeEntity> selectedItems;

    }

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.volumeMainPlace)
    public interface ProxyDef extends ProxyPlace<MainVolumePresenter> {
    }

    public interface ViewDef extends AbstractMainWithDetailsPresenter.ViewDef<GlusterVolumeEntity> {
    }

    @Inject
    public MainVolumePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, MainModelProvider<GlusterVolumeEntity, VolumeListModel> modelProvider,
            SearchPanelPresenterWidget<GlusterVolumeEntity, VolumeListModel> searchPanelPresenterWidget,
            OvirtBreadCrumbsPresenterWidget<GlusterVolumeEntity, VolumeListModel> breadCrumbs,
            VolumeActionPanelPresenterWidget actionPanel) {
        super(eventBus, view, proxy, placeManager, modelProvider, searchPanelPresenterWidget, breadCrumbs, actionPanel);
    }

    @Override
    protected void fireTableSelectionChangeEvent() {
        VolumeSelectionChangeEvent.fire(this, getSelectedItems());
    }

    @Override
    protected PlaceRequest getMainViewRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.volumeMainPlace);
    }

}
