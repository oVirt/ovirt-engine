package org.ovirt.engine.core.bll.storage.domain;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.vdscommands.PostDeleteAction;
import org.ovirt.engine.core.common.vdscommands.StoragePoolDomainAndGroupIdBaseVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.StorageDomainDao;

@Singleton
public class PostDeleteActionHandler {

    @Inject
    private StorageDomainDao storageDomainStaticDao;

    @Inject
    private DiskVmElementDao diskVmElementDao;

    @Inject
    private AuditLogDirector auditLogDirector;

    /**
     * Fixes the fields 'postZero' and 'discard' for parameters
     * with post delete action, before sending them to vdsm.
     * @param parameters the parameters of the command that should be executed.
     * @param <T> the parameters type.
     * @return the fixed parameters.
     */
    public <T extends StoragePoolDomainAndGroupIdBaseVDSCommandParameters & PostDeleteAction> T fixParameters(
            T parameters) {
        StorageDomain storageDomain = storageDomainStaticDao.get(parameters.getStorageDomainId());
        T parametersWithFixedPostZero = fixPostZeroField(parameters, storageDomain.getStorageType().isFileDomain());
        return fixDiscardField(parametersWithFixedPostZero, storageDomain);
    }

    /**
     * Since the file system is responsible for handling block allocation, there is no need
     * for posting zeros on file domains. This method gets the parameters of a command that may
     * post zeros on the storage and fixes its postZero value if required.
     * @param parameters the parameters of the command that should be executed.
     * @param isFileDomain is the storage domain a file domain.
     * @param <T> the parameters type.
     * @return the fixed parameters.
     */
    protected <T extends StoragePoolDomainAndGroupIdBaseVDSCommandParameters & PostDeleteAction> T fixPostZeroField(
            T parameters, boolean isFileDomain) {
        if (isFileDomain) {
            parameters.setPostZero(false);
        }
        return parameters;
    }

    /**
     * Since a storage domain's discard support may be changed since the discard after delete value was chosen by the
     * user, we should check its support before sending it to vdsm. If it doesn't support discard any more, that means
     * that discarding the disk will not work, and we can let the user know about it and send discard=false to vdsm.
     * @param parameters the parameters of the command that should be executed.
     * @param storageDomain the storage domain that the disk or snapshot belongs to.
     * @param <T> the parameters type.
     * @return the fixed parameters.
     */
    protected <T extends StoragePoolDomainAndGroupIdBaseVDSCommandParameters & PostDeleteAction> T fixDiscardField(
            T parameters, StorageDomain storageDomain) {
        if (storageDomain.getDiscardAfterDelete()) {
            if (!Boolean.TRUE.equals(storageDomain.getSupportsDiscard())) {
                parameters.setDiscard(false);
                AuditLogable auditLog = new AuditLogableImpl();
                auditLog.setStorageDomainId(storageDomain.getId());
                auditLogDirector.log(auditLog, AuditLogType.ILLEGAL_STORAGE_DOMAIN_DISCARD_AFTER_DELETE);
            }
        } else if (diskVmElementWithPassDiscardExists(parameters.getImageGroupId()) &&
                Boolean.TRUE.equals(storageDomain.getSupportsDiscard())) {
            // At least one vm has this disk with pass discard enabled.
            // Thus, although the relevant storage domain's discard after
            // delete value is false, we send discard = true to vdsm.
            parameters.setDiscard(true);
        }
        return parameters;
    }

    protected boolean diskVmElementWithPassDiscardExists(Guid diskId) {
        return diskVmElementDao.getAllDiskVmElementsByDiskId(diskId).stream().anyMatch(DiskVmElement::isPassDiscard);
    }
}
