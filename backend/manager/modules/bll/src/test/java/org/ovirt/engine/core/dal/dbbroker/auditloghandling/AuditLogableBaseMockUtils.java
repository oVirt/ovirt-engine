package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import static org.mockito.Mockito.doReturn;

import org.ovirt.engine.core.dao.VmDAO;

/**
 * Utility class to help mock out behavior inherited from {@link AuditLogableBase}, since it may have been inherited
 * from another package, and it's protected methods can't be accessed.
 */
public class AuditLogableBaseMockUtils {

    /**
     * Mock that the given {@link AuditLogableBase} uses the given {@link VmDAO}.
     *
     * @param auditLogableBase
     *            The base class instance.
     * @param vmDao
     *            The DAO to use.
     */
    public static void mockVmDao(AuditLogableBase auditLogableBase, VmDAO vmDao) {
        doReturn(vmDao).when(auditLogableBase).getVmDAO();
    }
}
