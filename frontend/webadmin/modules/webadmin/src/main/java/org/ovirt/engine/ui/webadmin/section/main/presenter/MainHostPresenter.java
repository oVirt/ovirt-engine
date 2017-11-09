package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.OvirtBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.HostActionPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.model.TagActivationChangeEvent;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class MainHostPresenter extends AbstractMainWithDetailsPresenter<VDS, HostListModel<Void>,
    MainHostPresenter.ViewDef, MainHostPresenter.ProxyDef>
        implements TagActivationChangeEvent.TagActivationChangeHandler{

    @GenEvent
    public class HostSelectionChange {

        List<VDS> selectedItems;

    }

    private final TagEventCollector tagEventCollector;

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.hostMainPlace)
    public interface ProxyDef extends ProxyPlace<MainHostPresenter> {
    }

    public interface ViewDef extends AbstractMainWithDetailsPresenter.ViewDef<VDS> {
        void setActiveTags(List<TagModel> tags);
    }

    @Inject
    public MainHostPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, MainModelProvider<VDS, HostListModel<Void>> modelProvider,
            SearchPanelPresenterWidget<VDS, HostListModel<Void>> searchPanelPresenterWidget,
            OvirtBreadCrumbsPresenterWidget<VDS, HostListModel<Void>> breadCrumbs,
            TagEventCollector tagEventCollector,
            HostActionPanelPresenterWidget actionPanel) {
        super(eventBus, view, proxy, placeManager, modelProvider, searchPanelPresenterWidget, breadCrumbs, actionPanel);
        this.tagEventCollector = tagEventCollector;
    }

    @Override
    protected void fireTableSelectionChangeEvent() {
        HostSelectionChangeEvent.fire(this, getSelectedItems());
    }

    @Override
    protected PlaceRequest getMainViewRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.hostMainPlace);
    }

    @Override
    protected void onBind() {
        super.onBind();
        registerHandler(getEventBus().addHandler(TagActivationChangeEvent.getType(), this));
        tagEventCollector.getActivationEvents().forEach(e -> onTagActivationChange(e));
        tagEventCollector.activateHosts();
    }

    @Override
    public void onTagActivationChange(TagActivationChangeEvent event) {
        getView().setActiveTags(event.getActiveTags());
        setTags(event.getActiveTags());
    }

}
