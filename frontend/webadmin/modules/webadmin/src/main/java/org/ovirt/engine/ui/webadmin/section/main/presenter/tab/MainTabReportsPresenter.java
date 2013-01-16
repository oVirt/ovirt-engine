package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.reports.ReportsListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.place.ApplicationPlaces;
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
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class MainTabReportsPresenter extends AbstractMainTabPresenter<Void, ReportsListModel, MainTabReportsPresenter.ViewDef, MainTabReportsPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.reportsMainTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<MainTabReportsPresenter> {
    }

    public interface ViewDef extends View {
        /**
         * POST the Frame Data
         */
        void updateReportsPanel(String url,
                Map<String, List<String>> params);
    }

    @TabInfo(container = MainTabPanelPresenter.class)
    static TabData getTabData(ClientGinjector ginjector) {
        return new ModelBoundTabData(ginjector.getApplicationConstants().reportsMainTabLabel(), 14,
                ginjector.getMainTabReportsModelProvider(), Align.RIGHT);
    }

    @Inject
    public MainTabReportsPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, MainModelProvider<Void, ReportsListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
        getModel().getReportModelRefreshEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                getView().updateReportsPanel(getModel().getUrl(), getModel().getParams());
            }
        });
    }

    @Override
    protected void onReset() {
        super.onReset();
        setSubTabPanelVisible(false);
        getModel().refreshReportModel();
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return new PlaceRequest(ApplicationPlaces.reportsMainTabPlace);
    }
}
