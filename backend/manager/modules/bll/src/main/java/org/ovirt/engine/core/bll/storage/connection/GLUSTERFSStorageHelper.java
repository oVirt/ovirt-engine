package org.ovirt.engine.core.bll.storage.connection;

import java.util.List;
import org.apache.commons.lang.StringUtils;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ConnectHostToStoragePoolServersParameters;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Storage helper for Posix FS connections
 */
public class GLUSTERFSStorageHelper extends BaseFsStorageHelper {

    private static final Logger log = LoggerFactory.getLogger(GLUSTERFSStorageHelper.class);

    @Override
    protected StorageType getType() {
        return StorageType.GLUSTERFS;
    }

    @Override
    protected Pair<Boolean, EngineFault> runConnectionStorageToDomain(StorageDomain storageDomain, Guid vdsId, int type) {
        VDS vds = getVdsDao().get(vdsId);
        if (!canVDSConnectToGlusterfs(vds)) {
            log.error("Couldn't find glusterfs-cli package on vds {} (needed for connecting storage domain {}).",
                    vds.getName(), storageDomain.getName());
            StorageHelperBase.addMessageToAuditLog(AuditLogType.VDS_CANNOT_CONNECT_TO_GLUSTERFS, null, vds.getName());
            return new Pair<>(false, null);
        }

        return super.runConnectionStorageToDomain(storageDomain, vdsId, type);
    }

    @Override
    public boolean prepareConnectHostToStoragePoolServers(CommandContext cmdContext,
            ConnectHostToStoragePoolServersParameters parameters,
            List<StorageServerConnections> connections) {
        if (isActiveGlusterfsDomainAvailable(parameters.getStoragePoolId())) {
            // Validate glusterfs-cli package availability
            if (!canVDSConnectToGlusterfs(parameters.getVds())) {
                log.error("Couldn't find glusterfs-cli package on vds {} (needed for connecting storage domains).",
                        parameters.getVds().getName());
                setNonOperational(cmdContext,
                        parameters.getVds().getId(),
                        NonOperationalReason.VDS_CANNOT_CONNECT_TO_GLUSTERFS);
                return false;
            }
        }
        return true;
    }

    public static boolean canVDSConnectToGlusterfs(VDS vds) {
        RpmVersion glusterfsCliVer = vds.getGlusterfsCliVersion();
        return glusterfsCliVer != null && StringUtils.isNotEmpty(glusterfsCliVer.getRpmRelease());
    }

    private boolean isActiveGlusterfsDomainAvailable(Guid poolId) {
        return isActiveStorageDomainAvailable(StorageType.GLUSTERFS, poolId);
    }

    private static VdsDao getVdsDao() {
        return getDbFacade().getVdsDao();
    }

    private static DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

}
