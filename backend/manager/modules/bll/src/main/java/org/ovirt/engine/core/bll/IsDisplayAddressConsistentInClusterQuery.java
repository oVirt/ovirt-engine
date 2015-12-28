package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * Returns true if
 * <li>all hosts in cluster has overridden console addresses
 * <li>no hosts in cluster has overridden console addresses
 * <li>there are no hosts in cluster
 * <p>
 * Otherwise returns false
 */
public class IsDisplayAddressConsistentInClusterQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public IsDisplayAddressConsistentInClusterQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VDS> hosts =
                DbFacade.getInstance()
                        .getVdsDao()
                        .getAllForCluster(getParameters().getId(), getUserID(), getParameters().isFiltered());

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
