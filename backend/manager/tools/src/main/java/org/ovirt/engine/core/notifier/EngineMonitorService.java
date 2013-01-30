package org.ovirt.engine.core.notifier;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.engineencryptutils.EncryptionUtils;
import org.ovirt.engine.core.notifier.utils.NotificationConfigurator;
import org.ovirt.engine.core.notifier.utils.NotificationProperties;
import org.ovirt.engine.core.tools.common.db.StandaloneDataSource;
import org.ovirt.engine.core.utils.LocalConfig;
import org.ovirt.engine.core.utils.db.DbUtils;

/**
 * Class uses to monitor the oVirt Engineanager service by sampling its health servlet. Upon response other than code 200,
 * will report to <i>audit_log</i> table upon ENGINE error. <br>
 * If a server state was change from non-responsive to responsive, will report the status change. <br>
 * The monitor service is detached from the notification service, being executed as a separated thread, with different
 * execution rate.
 */
public class EngineMonitorService implements Runnable {

    private static final Log log = LogFactory.getLog(EngineMonitorService.class);
    private static final String ENGINE_NOT_RESPONDING_ERROR = "Engine server is not responding.";
    private static final String ENGINE_RESPONDING_MESSAGE = "Engine server is up and running.";
    private static final String HEALTH_SERVLET_PATH = "/OvirtEngineWeb/HealthStatus";
    private static final String CERTIFICATION_TYPE = "PKCS12";
    private static final String DEFAULT_SSL_PROTOCOL = "TLS";
    private static final long DEFAULT_SERVER_MONITOR_TIMEOUT_IN_SECONDS = 30;
    private static final int DEFAULT_SERVER_MONITOR_RETRIES = 3;
    private DataSource ds;
    private Map<String, String> prop = null;
    private long serverMonitorTimeout;
    private URL serverUrl;
    private boolean isServerUp = true;
    private boolean repeatNonResponsiveNotification;
    private int serverMonitorRetries;
    private boolean isHttpsProtocol;
    private boolean sslIgnoreCertErrors;
    private SSLSocketFactory sslFactory = null;
    private boolean sslIgnoreHostVerification;
    private String pidFile;
    private static final HostnameVerifier IgnoredHostnameVerifier = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    /**
     * Creates {@code EngineMonitorService} by configuration element containing required properties.
     * @param notificationConf
     *            notification configuration contains service properties
     * @throws NotificationServiceException
     */
    public EngineMonitorService(NotificationConfigurator notificationConf) throws NotificationServiceException {
        this.prop = notificationConf.getProperties();
        initConnectivity();
        initServerConnectivity();
        initServerMonitorInterval();
        initServerMonitorRetries();
        initPidFile();
        // Boolean.valueOf always returns false unless gets a true expression.
        repeatNonResponsiveNotification =
                Boolean.valueOf(this.prop.get(NotificationProperties.REPEAT_NON_RESPONSIVE_NOTIFICATION));
        if (log.isDebugEnabled()) {
            log.debug(MessageFormat.format("Checking server status using {0}, {1}ignoring SSL errors.",
                    isHttpsProtocol ? "HTTPS" : "HTTP",
                    sslIgnoreCertErrors ? "" : "without "));
        }
    }

    private void initPidFile() {
        pidFile = prop.get(NotificationProperties.ENGINE_PID);
        if(pidFile == null) {
            pidFile = NotificationProperties.DEFAULT_ENGINE_PID;
        }
    }

    /**
     * Reads number of server monitoring retries for each iteration of the monitor service.<br>
     * If a property wasn't configured, uses the default from {@code SERVER_MONITOR_RETRIES}
     * @throws NotificationServiceException
     *             if a number is malformed
     */
    private void initServerMonitorRetries() throws NotificationServiceException {
        int retries;
        if (prop.containsKey(NotificationProperties.ENGINE_MONITOR_RETRIES)) {
            try {
                retries =
                        NotificationConfigurator.extractNumericProperty(this.prop.get(NotificationProperties.ENGINE_MONITOR_RETRIES));
            } catch (NumberFormatException e) {
                throw new NotificationServiceException(NotificationProperties.ENGINE_MONITOR_RETRIES
                        + " value must be a positive integer number");
            }
        } else {
            retries = DEFAULT_SERVER_MONITOR_RETRIES;
        }
        serverMonitorRetries = retries;
    }

    /**
     * Reads period for timeout between retries of querying server status. <br>
     * If property isn't configured, uses default as set on {@code DEFAULT_SERVER_MONITOR_TIMEOUT}, if property is
     * misconfigured, throws exception.
     */
    private void initServerMonitorInterval() throws NotificationServiceException {
        long interval;
        if (prop.containsKey(NotificationProperties.ENGINE_TIMEOUT_IN_SECONDS)) {
            String timeout = prop.get(NotificationProperties.ENGINE_TIMEOUT_IN_SECONDS);
            try {
                interval = Long.valueOf(timeout);
                if (interval < 0) {
                    throw new NotificationServiceException(NotificationProperties.ENGINE_TIMEOUT_IN_SECONDS
                            + " value must be a positive integer number");
                }
            } catch (NumberFormatException e) {
                throw new NotificationServiceException(String.format("Invalid format of property [%s]",
                        NotificationProperties.ENGINE_TIMEOUT_IN_SECONDS), e);
            }
        } else {
            interval = DEFAULT_SERVER_MONITOR_TIMEOUT_IN_SECONDS;
        }
        serverMonitorTimeout = TimeUnit.SECONDS.convert(interval, TimeUnit.MILLISECONDS);
    }

    /**
     * Initializes server connectivity settings:
     * <li> Resolves monitored server URL
     * <li> Sets protocol for connectivity (HTTP/HTTPS) and configures socket factories for SSL
     * @throws NotificationServiceException
     */
    private void initServerConnectivity() throws NotificationServiceException {
        isHttpsProtocol = Boolean.valueOf(prop.get(NotificationProperties.IS_HTTPS_PROTOCOL));
        sslIgnoreCertErrors = Boolean.valueOf(prop.get(NotificationProperties.SSL_IGNORE_CERTIFICATE_ERRORS));
        sslIgnoreHostVerification = Boolean.valueOf(prop.get(NotificationProperties.SSL_IGNORE_HOST_VERIFICATION));

        // Setting SSL_IGNORE_HOST_VERIFICATION in configuration file implies that SSL certification errors should be
        // ignored as well
        sslIgnoreCertErrors = sslIgnoreHostVerification || sslIgnoreCertErrors;

        if (isHttpsProtocol) {
            initHttpsSettings();
        } else if (sslIgnoreCertErrors || sslIgnoreHostVerification) {
            log.warn("Properties " + NotificationProperties.SSL_IGNORE_CERTIFICATE_ERRORS
                    + " and " + NotificationProperties.SSL_IGNORE_HOST_VERIFICATION + " are ignored, since property "
                    + NotificationProperties.IS_HTTPS_PROTOCOL + " is not set.");
        }
        initServerUrl();
    }

    /**
     * Initializes the SSL Socket Factory. Created SSL socket factory is determined by
     * {@code NotificationProperties.SSL_IGNORE_CERTIFICATE_ERRORS}. If set to true, creates dummy socket factory which
     * accept any request. If set to false or not set, creates SSL socket factory by trusted keystore defined on
     * vdc_options.
     * @throws NotificationServiceException
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
     * @throws NotificationServiceException
     */
    private void createConcreteSSLSocketFactory() throws NotificationServiceException {
        String keystorePass =
                getConfigurationProperty(ConfigValues.keystorePass.name(),
                        prop.get(NotificationProperties.keystorePassVersion));
        String keystoreUrl =
                getConfigurationProperty(ConfigValues.keystoreUrl.name(),
                        prop.get(NotificationProperties.keystoreUrlVersion));

        validateConfigurationProperty(keystorePass);
        validateConfigurationProperty(keystoreUrl);

        try {
            String sslProtocol = prop.get(NotificationProperties.SSL_PROTOCOL);
            if (StringUtils.isEmpty(sslProtocol)) {
                sslProtocol = DEFAULT_SSL_PROTOCOL;
            }
            KeyStore keyStore = EncryptionUtils.getKeyStore(keystoreUrl, keystorePass, CERTIFICATION_TYPE);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            SSLContext ctx = SSLContext.getInstance(sslProtocol);
            ctx.init(null, tmf.getTrustManagers(), null);
            sslFactory = ctx.getSocketFactory();
        } catch (Exception e) {
            throw new NotificationServiceException("Failed to create SSL factory when running with SSL mode.", e);
        }
    }

    /**
     * Creates dummy SSL Socket Factory factory which should be used by setting 'true' to
     * {@code NotificationProperties.SSL_IGNORE_CERTIFICATE_ERRORS}.
     * @throws NotificationServiceException
     */
    private void createDummySSLSocketFactory() throws NotificationServiceException {
        try {
            SSLContext sslContext = SSLContext.getInstance(DEFAULT_SSL_PROTOCOL);
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
        LocalConfig config = LocalConfig.getInstance();
        try {
            if (isHttpsProtocol) {
                serverUrl = config.getExternalHttpsUrl(HEALTH_SERVLET_PATH);
            }
            else {
                serverUrl = config.getExternalHttpUrl(HEALTH_SERVLET_PATH);
            }
            log.info("Engine health servlet URL is \"" + serverUrl + "\".");
        }
        catch (MalformedURLException exception) {
            throw new NotificationServiceException("Can't get engine health servlet URL.", exception);
        }
    }

    private void validateConfigurationProperty(String propertyValue) throws NotificationServiceException {
        final String MISSING_PROPERTY_ERROR = "Empty or missing property '%s' from vdc_options table";
        if (StringUtils.isEmpty(propertyValue)) {
            String errorMessage = String.format(MISSING_PROPERTY_ERROR, ConfigValues.keystorePass.name());
            log.error(errorMessage);
            throw new NotificationServiceException(errorMessage);
        }
    }

    /**
     * The service monitor the status of the JBoss server using its Health servlet
     */
    @Override
    public void run() {
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
        Set<String> errors = new HashSet<String>();
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
            log.error("Failed to get server status with:" + errors);
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
        if (statusChanged || repeatNonResponsiveNotification)
        {
            if (isResponsive) {
                // if server is up, report only if its status was changed from non-responsive.
                if (statusChanged) {
                    insertEventIntoAuditLogSafe(AuditLogType.VDC_START,
                            AuditLogSeverity.NORMAL,
                            ENGINE_RESPONDING_MESSAGE,
                            "Failed auditing event down (for responsive server).");
                }
            } else {
                // reports an error for non-responsive server
                if(new File(pidFile).exists()) {
                    // assumed crash, since the pid file is still there
                    insertEventIntoAuditLogSafe(AuditLogType.VDC_STOP,
                            AuditLogSeverity.ERROR,
                            ENGINE_NOT_RESPONDING_ERROR,
                            "Failed auditing event up (for crashed non-responsive server).");
                } else {
                    insertEventIntoAuditLogSafe(AuditLogType.VDC_STOP,
                            AuditLogSeverity.WARNING,
                            ENGINE_NOT_RESPONDING_ERROR,
                            "Failed auditing event up (for stopped non-responsive server).");
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
                    log.debug(MessageFormat.format("Server is non responsive with response code: {0}", responseCode));
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
        log.debug("checkServerStatus return: " + isResponsive);
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
     * @throws SQLException
     * @throws NaiveConnectionHelperException
     */
    private void insertEventIntoAuditLog(String eventType, int eventId, int severity, String message)
            throws SQLException {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = ds.getConnection();
            ps = connection.prepareStatement("insert into audit_log(log_time, log_type_name , log_type, severity, message) values (?,?,?,?,?)");
            ps.setTimestamp(1,(new Timestamp(new Date().getTime())));
            ps.setString(2, eventType);
            ps.setInt(3, eventId);
            ps.setInt(4, severity);
            ps.setString(5, message);
            ps.executeUpdate();
        } finally {
            DbUtils.closeQuietly(ps,connection);
        }
    }

    private void initConnectivity() throws NotificationServiceException {
        try {
            ds = new StandaloneDataSource();
        }
        catch (SQLException exception) {
            throw new NotificationServiceException("Failed to obtain database connectivity", exception);
        }
    }

    /**
     * Retrieves property from vdc_option table by its name
     * @param propertyName
     *            property name to retrieve
     * @param propertyVersion
     *            the property version
     * @return the property value or null if doesn't exists or failed to retrieve
     */
    private String getConfigurationProperty(String propertyName, String propertyVersion) {
        final String GET_CONFIGURATION_PROPERTY_SQL =
                "select option_value from vdc_options where option_name = ? and version = ?";
        Connection connection = null;
        PreparedStatement pStmt = null;
        String propertyValue = null;
        ResultSet rs = null;

        if (StringUtils.isEmpty(propertyVersion)) {
            propertyVersion = ConfigCommon.defaultConfigurationVersion;
        }

        try {
            connection = ds.getConnection();
            pStmt = connection.prepareStatement(GET_CONFIGURATION_PROPERTY_SQL);
            pStmt.setString(1, propertyName);
            pStmt.setString(2, propertyVersion);
            rs = pStmt.executeQuery();
            if (rs.next()) {
                propertyValue = rs.getString(1);
            }
            if (propertyValue == null && !ConfigCommon.defaultConfigurationVersion.equals(propertyVersion)) {
                rs.close();
                pStmt.setString(1, propertyName);
                pStmt.setString(2, ConfigCommon.defaultConfigurationVersion);
                rs = pStmt.executeQuery();
                if (rs.next()) {
                    propertyValue = rs.getString(1);
                }
                log.warn(MessageFormat.format("Property {0} does not exists on vdc_option with version {1}. Trying to obtain it with default version.",
                        propertyName,
                        propertyVersion));
            }
        } catch (Exception e) {
            log.error(MessageFormat.format("Failed to retrieve property {0} from the database", propertyName), e);
        } finally {
            DbUtils.closeQuietly(rs,pStmt,connection);
        }
        return propertyValue;
    }

}
