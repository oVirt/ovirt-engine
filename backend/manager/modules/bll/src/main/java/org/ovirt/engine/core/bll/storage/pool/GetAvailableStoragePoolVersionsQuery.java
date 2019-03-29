package org.ovirt.engine.core.bll.storage.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StoragePoolDao;

public class GetAvailableStoragePoolVersionsQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    @Inject
    private StoragePoolDao storagePoolDao;

    @Inject
    private ClusterDao clusterDao;

    public GetAvailableStoragePoolVersionsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        if (getParameters().getId() != null) {
            ArrayList<Version> result = new ArrayList<>();
            StoragePool storagePool = storagePoolDao.get(getParameters().getId());
            if (storagePool != null) {
                List<Cluster> clusters =
                        clusterDao.getAllForStoragePool(storagePool.getId(), getUserID(), getParameters().isFiltered());

                for (Version supportedVer : Config.<Set<Version>> getValue(ConfigValues.SupportedClusterLevels)) {
                    // if version lower than current skip because cannot
                    // decrease version
                    if (supportedVer.compareTo(storagePool.getCompatibilityVersion()) < 0) {
                        continue;
                    }

                    boolean versionOk = true;
                    // check all clusters are not grater than this ver
                    for (Cluster cluster : clusters) {
                        if (supportedVer.compareTo(cluster.getCompatibilityVersion()) > 0) {
                            versionOk = false;
                            break;
                        }
                    }
                    if (versionOk) {
                        result.add(supportedVer);
                    }
                }
            }
            getQueryReturnValue().setReturnValue(result);
        } else {
            getQueryReturnValue().setReturnValue(
                    new ArrayList<>(Config.<Set<Version>> getValue(ConfigValues.SupportedClusterLevels)));
        }
    }
}
