package org.ovirt.engine.ui.uicommonweb;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import org.ovirt.engine.ui.frontend.communication.SSOTokenChangeEvent;
import org.ovirt.engine.ui.frontend.communication.SSOTokenChangeEvent.SSOTokenChangeHandler;
import org.ovirt.engine.ui.frontend.utils.BaseContextPathData;
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
    private static final int MAX_RETRY_COUNTS = 20;
    private static final int RETRY_INTERVAL = 30000;
    public static final String REDIRECT_SERVICE = "services/reports-dashboard-redirect"; //$NON-NLS-1$
    public static final String RIGHT_CLICK_REDIRECT_SERVICE = "services/reports-rightclick-redirect"; //$NON-NLS-1$
    public static final String STATUS_SERVICE = "services/reports-interface-proxy?command=status"; //$NON-NLS-1$
    public static final String XML_SERVICE = "services/reports-interface-proxy?command=webadmin-ui-xml"; //$NON-NLS-1$
    private int retryCount;
    private boolean reportsWebappDeployed;
    private boolean scheduledStatusCheckInProgress;
    private boolean reportsEnabled;
    private boolean xmlInitialized;
    private boolean initEventRaised;
    private Event reportsInitEvent;
    private String reportBaseUrl;
    private String reportRightClickUrl;
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
        setReportBaseUrl(buildUrl(REDIRECT_SERVICE));
        setReportRightClickUrl(buildUrl(RIGHT_CLICK_REDIRECT_SERVICE));
        parseReportsXML();
    }

    private void initState() {
        reportsEnabled = false;
        xmlInitialized = false;
        reportsWebappDeployed = false;
        scheduledStatusCheckInProgress = false;
        initEventRaised = false;
        reportBaseUrl = ""; //$NON-NLS-1$
        reportRightClickUrl = ""; //$NON-NLS-1$
        isCommunityEdition = false;
        resourceMap = new HashMap<String, Resource>();
        dashboardMap = new HashMap<String, Dashboard>();
        reportsInitEvent = new Event("ReportsInitialize", ReportInit.class); //$NON-NLS-1$
        retryCount = 0;
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

    public static RequestBuilder constructServiceRequestBuilder(String service) {
        return new RequestBuilder(RequestBuilder.GET, buildUrl(service));
    }

    private static String buildUrl(String service) {
        return "/" //$NON-NLS-1$
                + BaseContextPathData.getInstance().getRelativePath()
                + service;
    }

    private void scheduleCheckStatus() {
        if (scheduledStatusCheckInProgress || retryCount > MAX_RETRY_COUNTS || reportsWebappDeployed) {
            return;
        }
        scheduledStatusCheckInProgress = true;
        Scheduler.get().scheduleFixedDelay(
                new Scheduler.RepeatingCommand() {
                    @Override
                    public boolean execute() {
                        if (retryCount > MAX_RETRY_COUNTS || reportsWebappDeployed) {
                            scheduledStatusCheckInProgress = false;
                            return false;
                        }
                        retryCount++;
                        checkReportsWebAppStatus();
                        return true;
                    }
                },
                RETRY_INTERVAL);
    }

    private void checkReportsWebAppStatus() {
        try {
            constructServiceRequestBuilder(STATUS_SERVICE).sendRequest(null, new RequestCallback() {
                @Override
                public void onError(Request request, Throwable exception) {
                    // ignore error
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK) {
                        reportsWebappDeployed = true;
                        parseReportsXML();
                    }
                }
            });
        } catch (RequestException e) {
            // ignore error
        }
    }

    private void parseReportsXML() {
        try {
            constructServiceRequestBuilder(XML_SERVICE).sendRequest(null, new RequestCallback() {
                @Override
                public void onError(Request request, Throwable exception) {
                    scheduleCheckStatus();
                    setXmlInitialized();
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK) {
                        try {
                            if (ReportParser.getInstance().parseReport(response.getText())) {
                                resourceMap = ReportParser.getInstance().getResourceMap();
                                dashboardMap = ReportParser.getInstance().getDashboardMap();
                                isCommunityEdition = ReportParser.getInstance().isCommunityEdition();
                            }
                        } catch (DOMParseException e) {
                        } finally {
                            setXmlInitialized();
                        }
                    } else {
                        scheduleCheckStatus();
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
        if (reportBaseUrl != null) {
            this.reportBaseUrl = reportBaseUrl;
        }
        checkIfInitFinished();
    }

    public String getReportBaseUrl() {
        return reportBaseUrl;
    }

    public void setReportRightClickUrl(String reportRightClickUrl) {
        this.reportRightClickUrl = reportRightClickUrl;
    }

    public String getReportRightClickUrl() {
        return reportRightClickUrl;
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
        if (xmlInitialized && ssoToken != null) {

            // Check if the reports should be enabled in this system
            if (!"".equals(reportBaseUrl) && !resourceMap.isEmpty() && !"".equals(ssoToken)) { //$NON-NLS-1$ $NON-NLS-2$
                setReportsEnabled(true);
            } else {
                setReportsEnabled(false);
            }

            if (isReportsEnabled() && !initEventRaised) {
                reportsInitEvent.raise(this, null);
                initEventRaised = true;
            }
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
