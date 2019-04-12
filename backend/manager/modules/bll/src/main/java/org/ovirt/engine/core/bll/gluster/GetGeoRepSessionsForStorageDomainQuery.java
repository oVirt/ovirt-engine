package org.ovirt.engine.core.bll.gluster;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
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

    public GetGeoRepSessionsForStorageDomainQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
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
            if (pathElements.length !=2 ) {
               // return empty as volume name could not be determined
                log.info("Volume name could not be determined from storage connection '{}' ", path);
                getQueryReturnValue().setSucceeded(false);
                return;
            }
            String volumeName = pathElements[1];
            String hostName = pathElements[0];
            String hostAddress;

            hostAddress = resolveHostName(hostName);
            if (hostAddress == null) {
                getQueryReturnValue().setSucceeded(false);
                return;
            }
            List<VDS> vdsList = vdsDao.getAll();
            VDS vds = vdsList.stream()
                    .filter(v -> hostName.equals(v.getName())
                            || interfaceDao.getAllInterfacesForVds(v.getId())
                                    .stream()
                                    .anyMatch(iface -> hostAddress.equals(iface.getIpv4Address())
                                              || hostAddress.equals(iface.getIpv6Address())))
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

    //extracted to enable testing
    protected String resolveHostName(String hostName) {
        try {
            return InetAddress.getByName(hostName).getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }

}
