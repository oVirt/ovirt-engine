package org.ovirt.engine.core.vdsbroker.kubevirt;

import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.provider.ProviderDao;

import io.kubernetes.client.ApiException;

public class KubevirtAuditUtils {

    public static void auditAuthorizationIssues(ApiException e, AuditLogDirector auditLogDirector, Provider<?> provider) {
        if (e.getCode() == HttpStatus.SC_UNAUTHORIZED) {
            final AuditLogable event = new AuditLogableImpl();
            event.addCustomValue("ProviderName", provider.getName());
            Long flood = Long.valueOf(TimeUnit.SECONDS.convert(1L, TimeUnit.DAYS));
            event.setEventFloodInSec(flood.intValue());
            event.setCustomId(provider.getId().toString());

            auditLogDirector.log(event, AuditLogType.PROVIDER_AUTH_FAILED);
        }
    }

    public static void auditAuthorizationIssues(ApiException e, AuditLogDirector auditLogDirector, Guid clusterId,
            ProviderDao providerDao) {
        if (e.getCode() == HttpStatus.SC_UNAUTHORIZED) {
            auditAuthorizationIssues(e, auditLogDirector, providerDao.get(clusterId));
        }
    }
}
