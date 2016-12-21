package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;

/**
 * Query to fetch gluster geo-replication sessions that are associated with
 * storage domain's gluster volume.
 */
public class GetGeoRepSessionsForStorageDomainQuery<P extends IdQueryParameters> extends GlusterQueriesCommandBase<P> {

    @Inject
    StorageDomainStaticDao storageDomainDao;

    @Inject
    StorageServerConnectionDao storageServerConnectionDao;

    @Inject
    VdsDao vdsDao;

    @Inject
    InterfaceDao interfaceDao;

    public GetGeoRepSessionsForStorageDomainQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        StorageDomainStatic domain = storageDomainDao.get(getParameters().getId());
        StorageServerConnections connection = storageServerConnectionDao.get(domain.getStorage());
        getQueryReturnValue().setReturnValue(new ArrayList<GlusterGeoRepSession>());
        if (connection.getStorageType() != StorageType.GLUSTERFS) {
            //return empty
            getQueryReturnValue().setSucceeded(false);
            return;
        }
        Guid glusterVolumeId = connection.getGlusterVolumeId();
        if (glusterVolumeId == null) {
            //retrieve the gluster volume associated with path
            String path = connection.getConnection();
            String[] pathElements = path.split(StorageConstants.GLUSTER_VOL_SEPARATOR);
            String volumeName = pathElements[1];
            String hostName = pathElements[0];
            List<VDS> vdsList = vdsDao.getAll();
            VDS vds = vdsList.stream()
                    .filter(v -> v.getName().equals(hostName)
                            || interfaceDao.getAllInterfacesForVds(v.getId())
                                    .stream()
                                    .anyMatch(iface -> iface.getIpv4Address().equals(hostName)))
                    .findFirst()
                    .orElse(null);
            if (vds == null) {
                // return empty
                getQueryReturnValue().setSucceeded(false);
                return;
            }
            GlusterVolumeEntity vol = glusterVolumeDao.getByName(vds.getClusterId(), volumeName);
            if (vol == null) {
                getQueryReturnValue().setSucceeded(false);
                return;
            }
            glusterVolumeId = vol.getId();
        }
        getQueryReturnValue().setReturnValue(glusterGeoRepDao.getGeoRepSessions(glusterVolumeId));
        getQueryReturnValue().setSucceeded(true);
    }

}
