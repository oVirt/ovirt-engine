package org.ovirt.engine.core.bll.host;

import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.HostUpgradeManagerResult;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.RpmVersionUtils;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;

@Singleton
public class OvirtNodeUpgradeManager implements UpdateAvailable {

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private BackendInternal backendInternal;

    @Override
    public HostUpgradeManagerResult checkForUpdates(VDS host) {
        QueryReturnValue returnValue =
                backendInternal.runInternalQuery(QueryType.GetoVirtISOs, new IdQueryParameters(host.getId()));
        List<RpmVersion> isos = returnValue.getReturnValue();
        boolean updateAvailable = RpmVersionUtils.isUpdateAvailable(isos, host.getHostOs());
        HostUpgradeManagerResult hostUpgradeManagerResult = new HostUpgradeManagerResult();
        hostUpgradeManagerResult.setUpdatesAvailable(updateAvailable);
        if (updateAvailable) {
            AuditLogable auditLog = new AuditLogableImpl();
            auditLog.setVdsName(host.getName());
            auditLog.setVdsId(host.getId());
            auditLog.setClusterName(host.getClusterName());
            auditLog.setClusterId(host.getClusterId());
            auditLogDirector.log(auditLog, AuditLogType.HOST_UPDATES_ARE_AVAILABLE);
        }

        return hostUpgradeManagerResult;
    }

    @Override
    public EnumSet<VDSType> getHostTypes() {
        return EnumSet.of(VDSType.oVirtVintageNode);
    }
}
