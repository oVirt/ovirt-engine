package org.ovirt.engine.core.bll.storage.pool;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;

/**
 * Class which encapsulates logic of figuring out macPoolId for data center based on pools used by data centers
 * clusters.
 */
public abstract class StoragePoolQueryBase<P extends QueryParametersBase>  extends QueriesCommandBase<P> {

    @Inject
    protected StoragePoolDao storagePoolDao;

    @Inject
    private DcSingleMacPoolFinder dcSingleMacPoolFinder;

    public StoragePoolQueryBase(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
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
        final Guid macPoolId = dcSingleMacPoolFinder.find(storagePool.getId());
        storagePool.setMacPoolId(macPoolId);
    }

    /**
     * @return either StoragePool or collection of StoragePool instances.
     */
    protected abstract Object queryDataCenter();
}
