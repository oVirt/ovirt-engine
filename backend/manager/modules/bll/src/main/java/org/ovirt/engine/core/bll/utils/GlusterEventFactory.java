package org.ovirt.engine.core.bll.utils;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;

public class GlusterEventFactory {
    public static AuditLogable createEvent(VDS vds, VDSReturnValue returnValue) {
        AuditLogable logable = new AuditLogableImpl();
        logable.setVdsId(vds.getId());
        logable.setVdsName(vds.getName());
        logable.setClusterId(vds.getClusterId());
        logable.setClusterName(vds.getClusterName());
        logable.addCustomValue("ErrorMessage", returnValue.getVdsError().getMessage());
        logable.updateCallStackFromThrowable(returnValue.getExceptionObject());
        return logable;
    }
}
