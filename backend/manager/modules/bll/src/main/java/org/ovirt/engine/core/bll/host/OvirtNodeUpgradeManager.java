package org.ovirt.engine.core.bll.host;

import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.RpmVersionUtils;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

@Singleton
public class OvirtNodeUpgradeManager implements UpdateAvailable {

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private BackendInternal backendInternal;

    @Override
    public boolean isUpdateAvailable(VDS host) {
        VdcQueryReturnValue returnValue =
                backendInternal.runInternalQuery(VdcQueryType.GetoVirtISOs, new IdQueryParameters(host.getId()));
        List<RpmVersion> isos = returnValue.getReturnValue();
        boolean updateAvailable = RpmVersionUtils.isUpdateAvailable(isos, host.getHostOs());
        if (updateAvailable) {
            AuditLogableBase auditLog = new AuditLogableBase();
            auditLog.setVds(host);
            auditLogDirector.log(auditLog, AuditLogType.OVIRT_NODE_UPDATES_ARE_AVAILABLE);
        }

        return updateAvailable;
    }

    @Override
    public EnumSet<VDSType> getHostTypes() {
        return EnumSet.of(VDSType.oVirtVintageNode);
    }
}
