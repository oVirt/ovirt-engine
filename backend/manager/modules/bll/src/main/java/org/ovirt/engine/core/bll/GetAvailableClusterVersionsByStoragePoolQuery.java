package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StoragePoolDao;

public class GetAvailableClusterVersionsByStoragePoolQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    @Inject
    private StoragePoolDao storagePoolDao;

    public GetAvailableClusterVersionsByStoragePoolQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        if (getParameters().getId() != null) {
            ArrayList<Version> result = new ArrayList<>();
            StoragePool storagePool = storagePoolDao.get(getParameters().getId());
            if (storagePool != null) {
                // return all versions that >= to the storage pool version
                for (Version supportedVer : Config.<Set<Version>> getValue(ConfigValues.SupportedClusterLevels)) {
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
                    new ArrayList<>(Config.<Set<Version>> getValue(ConfigValues.SupportedClusterLevels)));
        }
    }
}
