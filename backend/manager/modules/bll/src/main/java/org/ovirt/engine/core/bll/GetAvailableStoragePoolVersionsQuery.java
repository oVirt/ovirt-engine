package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetAvailableStoragePoolVersionsParameters;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetAvailableStoragePoolVersionsQuery<P extends GetAvailableStoragePoolVersionsParameters>
        extends QueriesCommandBase<P> {
    public GetAvailableStoragePoolVersionsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        if (getParameters().getStoragePoolId() != null) {
            java.util.ArrayList<Version> result = new java.util.ArrayList<Version>();
            StoragePool storagePool = DbFacade.getInstance().getStoragePoolDao().get(
                    getParameters().getStoragePoolId().getValue());
            if (storagePool != null) {
                List<VDSGroup> clusters = DbFacade.getInstance().getVdsGroupDao().getAllForStoragePool(
                        storagePool.getId(), getUserID(), getParameters().isFiltered());

                for (Version supportedVer : Config
                        .<java.util.HashSet<Version>> GetValue(ConfigValues.SupportedClusterLevels)) {
                    // if version lower than current skip because cannot
                    // decrease version
                    if (supportedVer.compareTo(storagePool.getcompatibility_version()) < 0) {
                        continue;
                    }

                    boolean versionOk = true;
                    // check all clusters are not grater than this ver
                    for (VDSGroup cluster : clusters) {
                        if (supportedVer.compareTo(cluster.getcompatibility_version()) > 0) {
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
                    new java.util.ArrayList<Version>(Config
                            .<java.util.HashSet<Version>> GetValue(ConfigValues.SupportedClusterLevels)));
        }
    }
}
