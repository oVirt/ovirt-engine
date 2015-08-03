package org.ovirt.engine.core.bll.storage;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
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

    public static boolean canVDSConnectToGlusterfs(VDS vds) {
        if (FeatureSupported.glusterVolumeInfoSupported(getStoragePool(vds.getId()).getCompatibilityVersion())) {
            RpmVersion glusterfsCliVer = vds.getGlusterfsCliVersion();
            return glusterfsCliVer != null && StringUtils.isNotEmpty(glusterfsCliVer.getRpmRelease());
        }

        return true;
    }

    private static VdsDao getVdsDao() {
        return getDbFacade().getVdsDao();
    }

    private static StoragePool getStoragePool(Guid vdsId) {
        return getDbFacade().getStoragePoolDao().getForVds(vdsId);
    }

    private static DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

}
