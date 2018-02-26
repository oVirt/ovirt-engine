package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.DisksBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DiskActionPanelPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class MainDiskPresenter extends AbstractMainWithDetailsPresenter<Disk, DiskListModel, MainDiskPresenter.ViewDef, MainDiskPresenter.ProxyDef> {

    @GenEvent
    public class DiskSelectionChange {

        List<Disk> selectedItems;

    }

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.diskMainPlace)
    public interface ProxyDef extends ProxyPlace<MainDiskPresenter> {
    }

    public interface ViewDef extends AbstractMainWithDetailsPresenter.ViewDef<Disk> {

        IEventListener<EventArgs> getDiskTypeChangedEventListener();
        IEventListener<EventArgs> getDiskContentTypeChangedEventListener();

        void handleQuotaColumnVisibility();
        void ensureColumnsVisible(DiskStorageType diskType);
    }

    @Inject
    public MainDiskPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, MainModelProvider<Disk, DiskListModel> modelProvider,
            SearchPanelPresenterWidget<Disk, DiskListModel> searchPanelPresenterWidget,
            DisksBreadCrumbsPresenterWidget breadCrumbs, DiskActionPanelPresenterWidget actionPanel) {
        super(eventBus, view, proxy, placeManager, modelProvider, searchPanelPresenterWidget, breadCrumbs, actionPanel);
    }

    @Override
    protected void fireTableSelectionChangeEvent() {
        DiskSelectionChangeEvent.fire(this, getSelectedItems());
    }

    @Override
    protected PlaceRequest getMainViewRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.diskMainPlace);
    }

    @Override
    protected void onBind() {
        super.onBind();

        Event<EventArgs> entityChangedEvent = getModel().getDiskViewType().getEntityChangedEvent();
        entityChangedEvent.addListener(getView().getDiskTypeChangedEventListener());

        Event<EventArgs> diskContentTypeEntityChangedEvent = getModel().getDiskContentType().getEntityChangedEvent();
        diskContentTypeEntityChangedEvent.addListener(getView().getDiskContentTypeChangedEventListener());
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getView().handleQuotaColumnVisibility();
        // Make sure the menu has the right columns visible.
        getView().ensureColumnsVisible(getModel().getDiskViewType().getEntity());
    }

}
