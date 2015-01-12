package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetAvailableStoragePoolVersionsQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    public GetAvailableStoragePoolVersionsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        if (getParameters().getId() != null) {
            ArrayList<Version> result = new ArrayList<Version>();
            StoragePool storagePool = DbFacade.getInstance().getStoragePoolDao().get(
                    getParameters().getId());
            if (storagePool != null) {
                List<VDSGroup> clusters = DbFacade.getInstance().getVdsGroupDao().getAllForStoragePool(
                        storagePool.getId(), getUserID(), getParameters().isFiltered());

                for (Version supportedVer : Config
                        .<HashSet<Version>> getValue(ConfigValues.SupportedClusterLevels)) {
                    // if version lower than current skip because cannot
                    // decrease version
                    if (supportedVer.compareTo(storagePool.getCompatibilityVersion()) < 0) {
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
                    new ArrayList<Version>(Config
                            .<HashSet<Version>> getValue(ConfigValues.SupportedClusterLevels)));
        }
    }
}
