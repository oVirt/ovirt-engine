package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.common.widget.table.ActionTable;
import org.ovirt.engine.ui.uicommonweb.ErrorPopupManager;
import org.ovirt.engine.ui.uicommonweb.models.reports.ReportsListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractMainTabPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTabPanelPresenter;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class MainTabReportsPresenter extends AbstractMainTabPresenter<Void, ReportsListModel, MainTabReportsPresenter.ViewDef, MainTabReportsPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private static boolean reportsWebappDeployed = false;

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.reportsMainTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<MainTabReportsPresenter> {
    }

    public interface ViewDef extends View {

        void updateReportsPanel(String url, Map<String, List<String>> params);

    }

    @TabInfo(container = MainTabPanelPresenter.class)
    static TabData getTabData(
            MainModelProvider<Void, ReportsListModel> modelProvider) {
        return new ModelBoundTabData(constants.reportsMainTabLabel(), 15, modelProvider, Align.RIGHT);
    }

    @Inject
    public MainTabReportsPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            ErrorPopupManager errorPopupManager,
            PlaceManager placeManager, MainModelProvider<Void, ReportsListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
        getModel().getReportsAvailabilityEvent().addListener(new ReportsModelRefreshEvent());
    }

    @Override
    protected ActionTable<?> getTable() {
        // Reports main tab view has no table widget associated
        return null;
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getModel().setReportsTabSelected(true);
        reportsWebappDeployed = true;
        getModel().updateReportsAvailability();
    }

    @Override
    protected void onHide() {
        super.onHide();
        getModel().setReportsTabSelected(false);
    }

    @Override
    protected void onReset() {
        super.onReset();
        if (!hasReportsModelRefreshEvent()) {
            getModel().getReportsAvailabilityEvent().addListener(new ReportsModelRefreshEvent());
        }
        setSubTabPanelVisible(false);
        getModel().refreshReportModel();
    }

    private boolean hasReportsModelRefreshEvent() {
        for (IEventListener<? extends EventArgs> listnr : (List<IEventListener<? extends EventArgs>>) getModel().getReportsAvailabilityEvent().getListeners()) {
            if (listnr instanceof ReportsModelRefreshEvent) {
               return true;
            }
        }
        return false;
    }

    class ReportsModelRefreshEvent implements IEventListener<EventArgs> {
        @Override
        public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
            if (reportsWebappDeployed && getModel().getUri() != null) {
                getView().updateReportsPanel(getModel().getUrl(), getModel().getParams());
            }
        }
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.reportsMainTabPlace);
    }

}
