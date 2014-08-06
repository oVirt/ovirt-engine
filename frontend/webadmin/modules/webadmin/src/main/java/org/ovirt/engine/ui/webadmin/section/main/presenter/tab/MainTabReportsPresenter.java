package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import java.util.List;
import java.util.Map;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.common.widget.table.ActionTable;
import org.ovirt.engine.ui.uicommonweb.ErrorPopupManager;
import org.ovirt.engine.ui.uicommonweb.models.reports.ReportsListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
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

    private static final String REPORTS_WEBAPP_DEPLOYED_MSG = "Reports Webapp Deployed"; //$NON-NLS-1$
    private static boolean reportsWebappDeployed = false;
    private final ErrorPopupManager errorPopupManager;
    private final ApplicationConstants applicationConstants;

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.reportsMainTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<MainTabReportsPresenter> {
    }

    public interface ViewDef extends View {

        void updateReportsPanel(String url, Map<String, List<String>> params);

    }

    @TabInfo(container = MainTabPanelPresenter.class)
    static TabData getTabData(ApplicationConstants applicationConstants,
            MainModelProvider<Void, ReportsListModel> modelProvider) {
        return new ModelBoundTabData(applicationConstants.reportsMainTabLabel(), 15, modelProvider, Align.RIGHT);
    }

    @Inject
    public MainTabReportsPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            ErrorPopupManager errorPopupManager, ApplicationConstants applicationConstants,
            PlaceManager placeManager, MainModelProvider<Void, ReportsListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
        this.errorPopupManager = errorPopupManager;
        this.applicationConstants = applicationConstants;
        getModel().getReportModelRefreshEvent().addListener(new ReportsModelRefreshEvent());
    }

    private void checkUpdateReportsPanel(final String url,
                                         final Map<String, List<String>> params) {
        RequestBuilder requestBuilder =
                new RequestBuilder(RequestBuilder.GET, buildStatusUrl(url)); //$NON-NLS-1$
        try {
            requestBuilder.setTimeoutMillis(1000);
            requestBuilder.sendRequest(null, new RequestCallback() {
                @Override
                public void onError(Request request, Throwable exception) {
                    errorPopupManager.show(applicationConstants.reportsWebAppNotDeployedMsg());
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK
                            && response.getText().trim().equals(REPORTS_WEBAPP_DEPLOYED_MSG)) {
                        reportsWebappDeployed = true;
                        getView().updateReportsPanel(url, params);
                    } else {
                        errorPopupManager.show(applicationConstants.reportsWebAppNotDeployedMsg());
                    }
                }
            });
        } catch (RequestException e) {
            errorPopupManager.show(applicationConstants.reportsWebAppErrorMsg());
        }
    }

    private String buildStatusUrl(String url) {
        int index = url.lastIndexOf("/"); //$NON-NLS-1$
        return url.substring(0, index+1) + "Status"; //$NON-NLS-1$
    }

    @Override
    protected ActionTable<?> getTable() {
        // Reports main tab view has no table widget associated
        return null;
    }

    @Override
    protected void onReset() {
        super.onReset();
        if (!hasReportsModelRefreshEvent()) {
            reportsWebappDeployed = false;
            getModel().getReportModelRefreshEvent().addListener(new ReportsModelRefreshEvent());
        }
        setSubTabPanelVisible(false);
        getModel().refreshReportModel();
    }

    private boolean hasReportsModelRefreshEvent() {
        for (IEventListener<EventArgs> listnr : getModel().getReportModelRefreshEvent().getListeners()) {
            if (listnr instanceof ReportsModelRefreshEvent) {
               return true;
            }
        }
        return false;
    }

    class ReportsModelRefreshEvent implements IEventListener<EventArgs> {
        @Override
        public void eventRaised(Event<EventArgs> ev, Object sender, EventArgs args) {
            if (reportsWebappDeployed) {
                getView().updateReportsPanel(getModel().getUrl(), getModel().getParams());
            } else {
                checkUpdateReportsPanel(getModel().getUrl(), getModel().getParams());
            }
        }
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(ApplicationPlaces.reportsMainTabPlace);
    }

}
