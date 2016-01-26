package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.CommonModel;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractMainTabWithDetailsPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTabPanelPresenter;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class MainTabDiskPresenter extends AbstractMainTabWithDetailsPresenter<Disk, DiskListModel, MainTabDiskPresenter.ViewDef, MainTabDiskPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

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

    final IEventListener<EventArgs> systemTreeListener = new IEventListener<EventArgs>() {
        @Override
        public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
            // Should be invoked only when Disks tab is selected
            if (commonModelProvider.get().getSelectedItem() instanceof DiskListModel) {
                getView().handleQuotaColumnVisibility();
            }
        }
    };

    @TabInfo(container = MainTabPanelPresenter.class)
    static TabData getTabData(
            MainModelProvider<Disk, DiskListModel> modelProvider) {
        return new ModelBoundTabData(constants.diskMainTabLabel(), 5, modelProvider);
    }

    @Inject
    private Provider<CommonModel> commonModelProvider;

    @Inject
    public MainTabDiskPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, MainModelProvider<Disk, DiskListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
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

        Event<EventArgs> systemTreeSelectedItemChangedEvent =
                commonModelProvider.get().getSystemTree().getSelectedItemChangedEvent();
        systemTreeSelectedItemChangedEvent.addListener(systemTreeListener);

        super.onReveal();
        getView().handleQuotaColumnVisibility();
    }

    @Override
    protected void onHide() {
        super.onHide();
        Event<EventArgs> systemTreeSelectedItemChangedEvent =
                commonModelProvider.get().getSystemTree().getSelectedItemChangedEvent();
        systemTreeSelectedItemChangedEvent.removeListener(systemTreeListener);
    }

}
