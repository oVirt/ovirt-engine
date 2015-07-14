package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashSet;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetAvailableClusterVersionsByStoragePoolQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    public GetAvailableClusterVersionsByStoragePoolQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        if (getParameters().getId() != null) {
            ArrayList<Version> result = new ArrayList<>();
            StoragePool storagePool = DbFacade.getInstance().getStoragePoolDao().get(
                    getParameters().getId());
            if (storagePool != null) {
                // return all versions that >= to the storage pool version
                for (Version supportedVer : Config
                        .<HashSet<Version>> getValue(ConfigValues.SupportedClusterLevels)) {
                    // if version lower than current skip because cannot
                    // decrease version
                    if (supportedVer.compareTo(storagePool.getCompatibilityVersion()) < 0) {
                        continue;
                    }
                    result.add(supportedVer);
                }
            }
            getQueryReturnValue().setReturnValue(result);
        } else {
            getQueryReturnValue().setReturnValue(
                    new ArrayList<>(Config.<HashSet<Version>> getValue(ConfigValues.SupportedClusterLevels)));
        }
    }
}
