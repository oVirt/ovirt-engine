package org.ovirt.engine.core.bll;

import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CertificationValidityChecker implements BackendService {

    private static Logger log = LoggerFactory.getLogger(CertificationValidityChecker.class);

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private VdsDao hostDao;

    @Inject
    private ResourceManager resourceManager;

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService executor;

    @PostConstruct
    public void scheduleJob() {
        double interval = Config.<Double>getValue(ConfigValues.CertificationValidityCheckTimeInHours);
        final int HOURS_TO_MINUTES = 60;
        long intervalInMinutes = Math.round(interval * HOURS_TO_MINUTES);

        executor.scheduleWithFixedDelay(this::checkCertificationValidity,
                10,
                intervalInMinutes,
                TimeUnit.MINUTES);
    }

    private void checkCertificationValidity() {
        try {
            if (!checkCertificate(EngineEncryptionUtils.getCertificate(EngineLocalConfig.getInstance().getPKICACert()),
                    AuditLogType.ENGINE_CA_CERTIFICATION_HAS_EXPIRED,
                    AuditLogType.ENGINE_CA_CERTIFICATION_IS_ABOUT_TO_EXPIRE_ALERT,
                    AuditLogType.ENGINE_CA_CERTIFICATION_IS_ABOUT_TO_EXPIRE,
                    null)
                    ^ !checkCertificate((X509Certificate) EngineEncryptionUtils.getCertificate(),
                    AuditLogType.ENGINE_CERTIFICATION_HAS_EXPIRED,
                    AuditLogType.ENGINE_CERTIFICATION_IS_ABOUT_TO_EXPIRE_ALERT,
                    AuditLogType.ENGINE_CERTIFICATION_IS_ABOUT_TO_EXPIRE,
                    null)) {
                return;
            }

            if (!Config.<Boolean>getValue(ConfigValues.EncryptHostCommunication)) {
                return;
            }

            hostDao.getAll()
                    .stream()
                    .filter(host -> host.getStatus() == VDSStatus.Up || host.getStatus() == VDSStatus.NonOperational)
                    .filter(VDS::isManaged)
                    .forEach(this::checkHostCertificateValidity);
        } catch (Throwable t) {
            log.error("Failed to check certification validity: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
        }
    }

    private void checkHostCertificateValidity(VDS host) {
        VdsManager hostManager = resourceManager.getVdsManager(host.getId());
        List<Certificate> peerCertificates = hostManager.getVdsProxy().getPeerCertificates();

        if (peerCertificates == null || peerCertificates.isEmpty()) {
            log.error("Failed to retrieve peer certifications for host '{}'", host.getName());
        } else {
            X509Certificate hostCertificate = (X509Certificate) peerCertificates.get(0);
            checkCertificate(hostCertificate,
                    AuditLogType.HOST_CERTIFICATION_HAS_EXPIRED,
                    AuditLogType.HOST_CERTIFICATION_IS_ABOUT_TO_EXPIRE_ALERT,
                    AuditLogType.HOST_CERTIFICATION_IS_ABOUT_TO_EXPIRE,
                    host);
            checkCertificateSan(hostCertificate, host.getHostName());
        }
    }

    private boolean checkCertificate(X509Certificate cert,
            AuditLogType alertExpirationEventType,
            AuditLogType alertAboutToExpireEventType,
            AuditLogType warnAboutToExpireEventType,
            VDS host) {
        Date expirationDate = cert.getNotAfter();
        Date certWarnTime = getExpirationDate(expirationDate, ConfigValues.CertExpirationWarnPeriodInDays);
        Date certAlertTime = getExpirationDate(expirationDate, ConfigValues.CertExpirationAlertPeriodInDays);
        Date now = new Date();

        AuditLogType eventType = null;

        if (now.compareTo(expirationDate) > 0) {
            eventType = alertExpirationEventType;
        } else if (now.compareTo(certAlertTime) > 0) {
            eventType = alertAboutToExpireEventType;
        } else if (now.compareTo(certWarnTime) > 0) {
            eventType = warnAboutToExpireEventType;
        }

        if (eventType != null) {
            AuditLogable event = new AuditLogableImpl();
            event.addCustomValue("ExpirationDate", new SimpleDateFormat("yyyy-MM-dd").format(expirationDate));
            if (host != null) {
                event.setVdsName(host.getName());
                event.setVdsId(host.getId());
            }
            auditLogDirector.log(event, eventType);
            return false;
        }

        return true;
    }

    private boolean checkCertificateSan(X509Certificate cert, String hostName) {
        boolean valid = false;
        Collection<List<?>> sanRecords;
        try {
            sanRecords = cert.getSubjectAlternativeNames();
        } catch (CertificateParsingException ex) {
            log.error("Error parsing certificate of host '{}': {}", hostName, ex.getMessage());
            log.debug("Exception", ex);
            sanRecords = null;
        }
        if (sanRecords != null) {
            valid = sanRecords.stream()
                    // check only records of type DNS (2) or IP address (7)
                    .filter(record -> ((Integer) record.get(0)) == 2 || ((Integer) record.get(0)) == 7)
                    .anyMatch(record -> hostName.equals((String) record.get(1)));
        }
        if (!valid) {
            AuditLogable event = new AuditLogableImpl();
            event.setVdsName(hostName);
            auditLogDirector.log(event, AuditLogType.HOST_CERTIFICATE_HAS_INVALID_SAN);
        }
        return valid;
    }

    private Date getExpirationDate(Date expirationDate, ConfigValues daysBeforeExpiration) {
        Calendar expirationTime = Calendar.getInstance();
        expirationTime.setTime(expirationDate);
        expirationTime.add(Calendar.DAY_OF_MONTH, -1 * Config.<Integer> getValue(daysBeforeExpiration));
        return expirationTime.getTime();
    }

    public static Calendar computeFutureExpirationDate(int days) {
        Calendar expirationTime = Calendar.getInstance();
        expirationTime.add(Calendar.DAY_OF_MONTH, days);
        return expirationTime;
    }
}
