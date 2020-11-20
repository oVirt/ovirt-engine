package org.ovirt.engine.core.notifier;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.sql.DataSource;

import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.notifier.utils.NotificationProperties;
import org.ovirt.engine.core.notifier.utils.ShutdownHook;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.engine.core.utils.db.StandaloneDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class uses to monitor the oVirt Engineanager service by sampling its health servlet. Upon response other than code 200,
 * will report to <i>audit_log</i> table upon ENGINE error. <br>
 * If a server state was change from non responsive to responsive, will report the status change. <br>
 * The monitor service is detached from the notification service, being executed as a separated thread, with different
 * execution rate.
 */
public class EngineMonitorService implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(EngineMonitorService.class);
    private static final String ENGINE_NOT_RESPONDING_ERROR =
            "ovirt-engine-notifier is not able to connect to engine server, engine might be down or not responding.";
    private static final String ENGINE_RESPONDING_MESSAGE = "Engine server is up and running.";
    private static final String HEALTH_SERVLET_PATH = "/services/health";
    private DataSource ds;
    private NotificationProperties prop = null;
    private long serverMonitorTimeout;
    private URL serverUrl;
    private boolean isServerUp = true;
    private boolean repeatNonResponsiveNotification;
    private int serverMonitorRetries;
    private boolean isHttpsProtocol;
    private boolean sslIgnoreCertErrors;
    private SSLSocketFactory sslFactory = null;
    private boolean sslIgnoreHostVerification;
    private static final HostnameVerifier IgnoredHostnameVerifier = (hostname, session) -> true;

    /**
     * Creates {@code EngineMonitorService} by configuration element containing required properties.
     * @param notificationConf
     *            notification configuration contains service properties
     */
    public EngineMonitorService(NotificationProperties prop) throws NotificationServiceException {
        this.prop = prop;
        initConnectivity();
        initServerConnectivity();
        initServerMonitorInterval();
        serverMonitorRetries = prop.getInteger(NotificationProperties.ENGINE_MONITOR_RETRIES);
        repeatNonResponsiveNotification = this.prop.getBoolean(NotificationProperties.REPEAT_NON_RESPONSIVE_NOTIFICATION);
        if (log.isDebugEnabled()) {
            log.debug("Checking server status using {}, {}ignoring SSL errors.",
                    isHttpsProtocol ? "HTTPS" : "HTTP",
                    sslIgnoreCertErrors ? "" : "without ");
        }
    }

    /**
     * Reads period for timeout between retries of querying server status. <br>
     * If property isn't configured, uses default as set on {@code DEFAULT_SERVER_MONITOR_TIMEOUT}, if property is
     * misconfigured, throws exception.
     */
    private void initServerMonitorInterval() throws NotificationServiceException {
        long interval = prop.getLong(NotificationProperties.ENGINE_TIMEOUT_IN_SECONDS);
        if (interval < 0) {
            throw new NotificationServiceException(NotificationProperties.ENGINE_TIMEOUT_IN_SECONDS
                    + " value must be a positive integer number");
        }
        serverMonitorTimeout = TimeUnit.MILLISECONDS.convert(interval, TimeUnit.SECONDS);
    }

    /**
     * Initializes server connectivity settings:
     * <li> Resolves monitored server URL
     * <li> Sets protocol for connectivity (HTTP/HTTPS) and configures socket factories for SSL
     */
    private void initServerConnectivity() throws NotificationServiceException {
        isHttpsProtocol = prop.getBoolean(NotificationProperties.IS_HTTPS_PROTOCOL);
        sslIgnoreCertErrors = prop.getBoolean(NotificationProperties.SSL_IGNORE_CERTIFICATE_ERRORS);
        sslIgnoreHostVerification = prop.getBoolean(NotificationProperties.SSL_IGNORE_HOST_VERIFICATION);

        // Setting SSL_IGNORE_HOST_VERIFICATION in configuration file implies that SSL certification errors should be
        // ignored as well
        sslIgnoreCertErrors = sslIgnoreHostVerification || sslIgnoreCertErrors;

        if (isHttpsProtocol) {
            initHttpsSettings();
        } else if (sslIgnoreCertErrors || sslIgnoreHostVerification) {
            log.warn("Properties {} and {} are ignored, since property {} is not set.",
                    NotificationProperties.SSL_IGNORE_CERTIFICATE_ERRORS,
                    NotificationProperties.SSL_IGNORE_HOST_VERIFICATION,
                    NotificationProperties.IS_HTTPS_PROTOCOL);
        }
        initServerUrl();
    }

    /**
     * Initializes the SSL Socket Factory. Created SSL socket factory is determined by
     * {@code NotificationProperties.SSL_IGNORE_CERTIFICATE_ERRORS}. If set to true, creates dummy socket factory which
     * accept any request. If set to false or not set, creates SSL socket factory by trusted keystore defined on
     * vdc_options.
     */
    private void initHttpsSettings() throws NotificationServiceException {
        if (sslIgnoreCertErrors) {
            createDummySSLSocketFactory();
        } else {
            createConcreteSSLSocketFactory();
        }
    }

    /**
     * Creates SSL Socket factory which is configured by the associated keystore which is configured the database,
     * provided by {@code ConfigValues.keystoreUrl} for its location and {@code ConfigValues.keystorePass} for its
     * password.
     */
    private void createConcreteSSLSocketFactory() throws NotificationServiceException {
        try {
            String sslProtocol = prop.getProperty(NotificationProperties.SSL_PROTOCOL);
            SSLContext ctx = SSLContext.getInstance(sslProtocol);
            ctx.init(null, EngineEncryptionUtils.getTrustManagers(), null);
            sslFactory = ctx.getSocketFactory();
        } catch (Exception e) {
            throw new NotificationServiceException("Failed to create SSL factory when running with SSL mode.", e);
        }
    }

    /**
     * Creates dummy SSL Socket Factory factory which should be used by setting 'true' to
     * {@code NotificationProperties.SSL_IGNORE_CERTIFICATE_ERRORS}.
     */
    private void createDummySSLSocketFactory() throws NotificationServiceException {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] { new X509TrustManager() {

                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

            } }, null);
            sslFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new NotificationServiceException("Failed to create SSL factory with dummy truststore.", e);
        }
    }

    private void initServerUrl() throws NotificationServiceException {
        EngineLocalConfig config = EngineLocalConfig.getInstance();
        try {
            if (isHttpsProtocol) {
                serverUrl = config.getExternalHttpsUrl(HEALTH_SERVLET_PATH);
            } else {
                serverUrl = config.getExternalHttpUrl(HEALTH_SERVLET_PATH);
            }
            log.info("Engine health servlet URL is \"{}\".", serverUrl);
        } catch(MalformedURLException exception) {
            throw new NotificationServiceException("Can't get engine health servlet URL.", exception);
        }
    }

    @Override
    public void run() {
        ShutdownHook shutdownHook = ShutdownHook.getInstance();
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        shutdownHook.addScheduledExecutorService(exec);
        shutdownHook.addServiceHandler(
                exec.scheduleWithFixedDelay(
                        () -> mainLogic(),
                        1,
                        prop.getLong(NotificationProperties.ENGINE_INTERVAL_IN_SECONDS),
                        TimeUnit.SECONDS
                )
        );
    }

    /**
     * The service monitor the status of the JBoss server using its Health servlet
     */
    private void mainLogic() {
        try {
            monitorEngineServerStatus();
        } catch (Throwable e) {
            if (!Thread.interrupted()) {
                log.error("Error while trying to report engine server status", e);
            }
            // initialize server status if a dispatch failed to treat as new check for next iteration
            isServerUp = true;
        }
    }

    /**
     * Monitors the server status: attempts to query the server status for 3 times.<br>
     * Between attempts, waits for amount of seconds as defined on {@link #serverMonitorTimeout}.<br>
     * When 3 attempts exceed,
     */
    private void monitorEngineServerStatus() {
        boolean isResponsive = false;
        Set<String> errors = new HashSet<>();
        int retries = serverMonitorRetries;

        while (retries > 0) {
            retries--;
            try {
                isResponsive = checkServerStatus(errors);
                if (!isResponsive) {
                    if (retries > 0) {
                        Thread.sleep(serverMonitorTimeout);
                    }
                } else {
                    break; // server is up and health servlet returned HTTP_OK
                }
            } catch (InterruptedException e) {
                // ignore this error
            } catch (Exception e) {
                errors.add(e.getMessage());
            }
        }

        // errors should contain distinct list of errors while trying to obtain server status
        if (errors.size() > 0) {
            log.error("Failed to get server status with: {}", errors);
            errors.clear();
        }

        // analyzes server status and report if needed
        reportServerStatus(isResponsive);
    }

    /**
     * Analyzes server status and reports upon its status by configuration as needed:<br>
     * If compares the current server status to the latest one. <br>
     * if status was changed, adds an events to audit_log to represent the concrete event, else, <br>
     * if is a repetition of previous status, checks the {@link #repeatNonResponsiveNotification} flag to<br>
     * determine whether a user configured getting repeatable notifications or not.
     * @param isResponsive
     *            current server status
     */
    private void reportServerStatus(boolean isResponsive) {
        boolean statusChanged;
        boolean lastServerStatus = isServerUp;
        isServerUp = isResponsive;
        statusChanged = lastServerStatus ^ isResponsive;

        // reports for any server status change or in case of configure for repeatable notification
        if (statusChanged || repeatNonResponsiveNotification) {
            if (isResponsive) {
                // if server is up, report only if its status was changed from non responsive.
                if (statusChanged) {
                    insertEventIntoAuditLogSafe(AuditLogType.VDC_START,
                            AuditLogSeverity.NORMAL,
                            ENGINE_RESPONDING_MESSAGE,
                            "Failed auditing event down (for responsive server).");
                }
            } else {
                // reports an error for non responsive server
                EngineLocalConfig config = EngineLocalConfig.getInstance();
                if(config.getEngineUpMark().exists()) {
                    // assumed crash, since engine up file is still there
                    insertEventIntoAuditLogSafe(AuditLogType.VDC_STOP,
                            AuditLogSeverity.ERROR,
                            ENGINE_NOT_RESPONDING_ERROR,
                            "Failed auditing event up (for crashed non responsive server).");
                } else {
                    insertEventIntoAuditLogSafe(AuditLogType.VDC_STOP,
                            AuditLogSeverity.WARNING,
                            ENGINE_NOT_RESPONDING_ERROR,
                            "Failed auditing event up (for stopped non responsive server).");
                }
            }
        }
    }

    private void insertEventIntoAuditLogSafe(AuditLogType eventType, AuditLogSeverity severity, String message, String logMessage) {
        try {
            insertEventIntoAuditLog(eventType.name(),
                    eventType.getValue(),
                    severity.getValue(),
                    message);
        } catch (Exception e) {
            log.warn(message);
            log.error(logMessage, e);
        }

    }


    /**
     * Examines the status of the backend engine server
     *
     * @param serverUrl
     *            the engine server url of Health Servlet
     * @param errors
     *            collection which aggregates any error
     * @return true is engine server is responsive (response with code 200 - HTTP_OK), else false
     */
    private boolean checkServerStatus(Set<String> errors) {
        boolean isResponsive = true;
        HttpURLConnection engineConn = null;

        try {
            engineConn = (HttpURLConnection) serverUrl.openConnection();
            if (isHttpsProtocol) {
                ((HttpsURLConnection) engineConn).setSSLSocketFactory(sslFactory);
                if (sslIgnoreHostVerification) {
                    ((HttpsURLConnection) engineConn).setHostnameVerifier(IgnoredHostnameVerifier);
                }
            }
        } catch (IOException e) {
            errors.add(e.getMessage());
            isResponsive = false;
        }

        if (isResponsive) {
            try {
                int responseCode = engineConn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    isResponsive = false;
                    log.debug("Server is non responsive with response code: {}", responseCode);
                }
            } catch (Exception e) {
                errors.add(e.getMessage());
                isResponsive = false;
            } finally {
                if (engineConn != null) {
                    engineConn.disconnect();
                    engineConn = null;
                }
            }
        }
        log.debug("checkServerStatus return: {}", isResponsive);
        return isResponsive;
    }

    /**
     * Adds an event to audit_log table, representing server status
     * @param eventType
     *            {@code AuditLogType.VDC_START} or {@code AuditLogType.VDC_STOP} events
     * @param eventId
     *            id associated with {@code eventType} parameter
     * @param severity
     *            severity associated with eventType, values are taken from {@code AuditLogSeverity}
     * @param message
     *            a comprehensive message describing the event
     */
    private void insertEventIntoAuditLog(String eventType, int eventId, int severity, String message)
            throws SQLException {
        try (Connection connection = ds.getConnection();
             PreparedStatement ps = connection.prepareStatement
                     ("insert into audit_log(log_time, log_type_name , log_type, severity, message) values (?,?,?,?,?)")) {
            ps.setTimestamp(1, new Timestamp(new Date().getTime()));
            ps.setString(2, eventType);
            ps.setInt(3, eventId);
            ps.setInt(4, severity);
            ps.setString(5, message);
            ps.executeUpdate();
        }
    }

    private void initConnectivity() throws NotificationServiceException {
        try {
            ds = new StandaloneDataSource();
        } catch(SQLException exception) {
            throw new NotificationServiceException("Failed to obtain database connectivity", exception);
        }
    }

}
