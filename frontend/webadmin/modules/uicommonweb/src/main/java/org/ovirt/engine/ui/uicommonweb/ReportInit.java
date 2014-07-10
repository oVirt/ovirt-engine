package org.ovirt.engine.ui.uicommonweb;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.frontend.communication.SSOTokenChangeEvent;
import org.ovirt.engine.ui.frontend.communication.SSOTokenChangeEvent.SSOTokenChangeHandler;
import org.ovirt.engine.ui.frontend.utils.BaseContextPathData;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.ReportParser;
import org.ovirt.engine.ui.uicompat.ReportParser.Dashboard;
import org.ovirt.engine.ui.uicompat.ReportParser.Resource;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.xml.client.impl.DOMParseException;

public class ReportInit {

    private static final ReportInit INSTANCE = new ReportInit();
    private boolean reportsEnabled;
    private boolean xmlInitialized;
    private boolean urlInitialized;
    private Event reportsInitEvent;
    private String reportBaseUrl;
    private String ssoToken;
    private boolean isCommunityEdition;

    private Map<String, Resource> resourceMap;
    private Map<String, Dashboard> dashboardMap;

    private HandlerRegistration ssoTokenHandlerRegistration;

    public static ReportInit getInstance() {
        return INSTANCE;
    }

    public boolean isCommunityEdition() {
        return isCommunityEdition;
    }

    public void init() {
        // the re-init can happen after logout/login.
        // As this class has it's state, it needs to be inited again
        initState();

        AsyncDataProvider.getInstance().getRedirectServletReportsPage(new AsyncQuery(this,
                                                                                     new INewAsyncCallback() {
                                                                                         @Override
                                                                                         public void onSuccess(Object target, Object returnValue) {
                                                                                             setReportBaseUrl((String) returnValue);
                                                                                         }
                                                                                     }));

        parseReportsXML();
    }

    private void initState() {
        reportsEnabled = false;
        xmlInitialized = false;
        urlInitialized = false;
        reportBaseUrl = ""; //$NON-NLS-1$
        isCommunityEdition = false;
        resourceMap = new HashMap<String, Resource>();
        dashboardMap = new HashMap<String, Dashboard>();
        reportsInitEvent = new Event("ReportsInitialize", ReportInit.class); //$NON-NLS-1$
    }

    private ReportInit() {
    }

    public Event getReportsInitEvent() {
        return reportsInitEvent;
    }

    public Resource getResource(String type) {
        return resourceMap.get(type);
    }

    public Dashboard getDashboard(String type) {
        return dashboardMap.get(type);
    }

    private void parseReportsXML() {

        RequestBuilder requestBuilder =
                new RequestBuilder(RequestBuilder.GET,
                    "/" //$NON-NLS-1$
                    + BaseContextPathData.getInstance().getRelativePath()
                    + "services/reports-ui"); //$NON-NLS-1$
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                @Override
                public void onError(Request request, Throwable exception) {
                    setXmlInitialized();
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                    try {
                        if (response.getStatusCode() == Response.SC_OK
                                && ReportParser.getInstance().parseReport(response.getText())) {
                            resourceMap = ReportParser.getInstance().getResourceMap();
                            dashboardMap = ReportParser.getInstance().getDashboardMap();
                            isCommunityEdition = ReportParser.getInstance().isCommunityEdition();
                        }
                    } catch (DOMParseException e) {
                    } finally {
                        setXmlInitialized();
                    }

                }
            });
        } catch (RequestException e) {
            setXmlInitialized();
        }
    }

    public boolean isReportsEnabled() {
        return reportsEnabled;
    }

    private void setReportsEnabled(boolean reportsEnabled) {
        this.reportsEnabled = reportsEnabled;
    }

    public void setReportBaseUrl(String reportBaseUrl) {
        this.reportBaseUrl = reportBaseUrl;
        this.urlInitialized = true;
        checkIfInitFinished();
    }

    public String getReportBaseUrl() {
        return reportBaseUrl;
    }

    public void setSsoToken(final String token) {
        this.ssoToken = token;
        checkIfInitFinished();
    }

    public String getSsoToken() {
        return this.ssoToken;
    }

    private void setXmlInitialized() {
        this.xmlInitialized = true;
        checkIfInitFinished();
    }

    private void checkIfInitFinished() {
        if (xmlInitialized && urlInitialized && ssoToken != null) {

            // Check if the reports should be enabled in this system
            if (!"".equals(reportBaseUrl) && !resourceMap.isEmpty() && !"".equals(ssoToken)) { //$NON-NLS-1$ $NON-NLS-2$
                setReportsEnabled(true);
            } else {
                setReportsEnabled(false);
            }

            // The initialization process blocks on this event after the login
            reportsInitEvent.raise(this, null);
        }
    }

    public void initHandlers(EventBus eventBus) {
        if (ssoTokenHandlerRegistration != null) {
            ssoTokenHandlerRegistration.removeHandler();
        }
        // Register to listen for session id acquired events.
        ssoTokenHandlerRegistration = eventBus.addHandler(SSOTokenChangeEvent.getType(),
            new SSOTokenChangeHandler() {

                @Override
                public void onSSOTokenChange(SSOTokenChangeEvent event) {
                    ReportInit.this.ssoToken = event.getToken();
                    if (ReportInit.this.ssoToken == null) { //This should not happen
                        //This will make the login continue, just the reports will be broken.
                        ReportInit.this.ssoToken = "";
                    }
                    checkIfInitFinished();
                }
            }
        );
    }

}
