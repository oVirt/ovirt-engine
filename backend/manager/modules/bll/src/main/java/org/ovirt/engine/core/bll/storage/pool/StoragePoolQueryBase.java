package org.ovirt.engine.core.bll.storage.pool;

import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.StoragePoolDao;

/**
 * Class which encapsulates logic of figuring out macPoolId for data center based on pools used by data centers
 * clusters.
 */
public abstract class StoragePoolQueryBase<P extends VdcQueryParametersBase>  extends QueriesCommandBase<P> {

    @Inject
    protected StoragePoolDao storagePoolDao;

    @Inject
    ClusterDao clusterDao;

    public StoragePoolQueryBase(P parameters) {
        super(parameters);
    }

    @Override
    protected final void executeQueryCommand() {
        Object dataCenterQueryResult = queryDataCenter();
        setMacPoolReference(dataCenterQueryResult);

        getQueryReturnValue().setReturnValue(dataCenterQueryResult);
    }

    private void setMacPoolReference(Object dataCenterQueryResult) {
        if (dataCenterQueryResult == null) {
            return;
        }

        if (dataCenterQueryResult instanceof StoragePool) {
            dataCenterQueryResult((StoragePool)dataCenterQueryResult);
            return;
        }

        if (dataCenterQueryResult instanceof Iterable) {
            //noinspection unchecked
            for (StoragePool storagePool : (Iterable<StoragePool>) dataCenterQueryResult) {
                dataCenterQueryResult(storagePool);
            }

            return;
        }

        throw new IllegalArgumentException("Unsupported query result");
    }

    private void dataCenterQueryResult(StoragePool storagePool) {
        final List<Cluster> clusters = clusterDao.getAllForStoragePool(storagePool.getId());
        final Set<Guid> macPoolIds = clusters.stream().map(Cluster::getMacPoolId).collect(toSet());

        //each cluster, due to db constraint, must have not null reference to pool, thus collection cannot be empty.
        storagePool.setMacPoolId(macPoolIds.size() != 1 ? null : macPoolIds.iterator().next());
    }

    /**
     * @return either StoragePool or collection of StoragePool instances.
     */
    protected abstract Object queryDataCenter();
}
