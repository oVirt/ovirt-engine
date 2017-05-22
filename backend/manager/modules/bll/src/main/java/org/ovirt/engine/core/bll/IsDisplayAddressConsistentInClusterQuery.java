package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VdsDao;

/**
 * Returns true if
 * <li>all hosts in cluster has overridden console addresses
 * <li>no hosts in cluster has overridden console addresses
 * <li>there are no hosts in cluster
 * <p>
 * Otherwise returns false
 */
public class IsDisplayAddressConsistentInClusterQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VdsDao vdsDao;

    public IsDisplayAddressConsistentInClusterQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<VDS> hosts = vdsDao.getAllForCluster(getParameters().getId(), getUserID(), getParameters().isFiltered());

        getQueryReturnValue().setReturnValue(!isDisplayAddressPartiallyOverridden(hosts));
    }

    boolean isDisplayAddressPartiallyOverridden(List<VDS> hosts) {

        if (hosts == null) {
            return false;
        }

        boolean foundOverridden = false;
        boolean foundDefault = false;

        for (VDS host : hosts) {
            if (host.getConsoleAddress() == null) {
                if (foundOverridden) {
                    // found both, it means that some are overridden and some not
                    return true;
                }

                foundDefault = true;
            } else {
                if (foundDefault) {
                    // found both, it means that some are overridden and some not
                    return true;
                }

                foundOverridden = true;
            }
        }

        // ok, no conflict found
        return false;

    }

}
