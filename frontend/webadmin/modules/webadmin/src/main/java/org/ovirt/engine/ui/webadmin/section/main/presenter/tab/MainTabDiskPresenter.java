package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.OvirtBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.GroupedTabData;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractMainTabWithDetailsPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.SearchPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.tab.MenuLayoutMenuDetails;
import org.ovirt.engine.ui.webadmin.widget.tab.WebadminMenuLayout;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class MainTabDiskPresenter extends AbstractMainTabWithDetailsPresenter<Disk, DiskListModel, MainTabDiskPresenter.ViewDef, MainTabDiskPresenter.ProxyDef> {

    @GenEvent
    public class DiskSelectionChange {

        List<Disk> selectedItems;

    }

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.diskMainTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<MainTabDiskPresenter> {
    }

    public interface ViewDef extends AbstractMainTabWithDetailsPresenter.ViewDef<Disk> {

        IEventListener<EventArgs> getDiskTypeChangedEventListener();

        void handleQuotaColumnVisibility();

    }

    @TabInfo(container = MainTabPanelPresenter.class)
    static TabData getTabData(WebadminMenuLayout menuLayout) {
        MenuLayoutMenuDetails menuDetails = menuLayout.getDetails(
                WebAdminApplicationPlaces.diskMainTabPlace);
        return new GroupedTabData(menuDetails);
    }

    @Inject
    public MainTabDiskPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, MainModelProvider<Disk, DiskListModel> modelProvider,
            SearchPanelPresenterWidget<Disk, DiskListModel> searchPanelPresenterWidget,
            OvirtBreadCrumbsPresenterWidget<Disk, DiskListModel> breadCrumbs,
            DiskActionPanelPresenterWidget actionPanel) {
        super(eventBus, view, proxy, placeManager, modelProvider, searchPanelPresenterWidget, breadCrumbs, actionPanel);
    }

    @Override
    protected void fireTableSelectionChangeEvent() {
        DiskSelectionChangeEvent.fire(this, getSelectedItems());
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.diskMainTabPlace);
    }

    @Override
    protected void onReveal() {
        Event<EventArgs> entityChangedEvent = getModel().getDiskViewType().getEntityChangedEvent();
        if (!entityChangedEvent.getListeners().contains(getView().getDiskTypeChangedEventListener())) {
            entityChangedEvent.addListener(getView().getDiskTypeChangedEventListener());
        }

        super.onReveal();
        getView().handleQuotaColumnVisibility();
    }

}
