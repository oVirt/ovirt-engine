package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.comparators.ImagesComparerByName;
import org.ovirt.engine.core.common.businessentities.comparators.VmsComparerByDiskSize;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.StorageDomainQueryTopSizeVmsParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetTopSizeVmsFromStorageDomainQuery<P extends StorageDomainQueryTopSizeVmsParameters>
extends QueriesCommandBase<P> {
    public GetTopSizeVmsFromStorageDomainQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VM> vms = DbFacade.getInstance().getVmDao().getAllForStorageDomain(
                getParameters().getStorageDomainId());
        for (VM vm : vms) {
            VmHandler.updateDisksFromDb(vm);
            Collections.sort(vm.getDiskList(), new ImagesComparerByName());
            ImagesHandler.fillImagesBySnapshots(vm);
        }
        Collections.sort(vms, Collections.reverseOrder(new VmsComparerByDiskSize()));

        /*
         * BZ#700327 requires that we return a maximum entries according to the following logic:
         * According to given parameter we are asked to
         * (-1): means return all available entries.
         * 0: means use whatever we have defined in the DB (vdc_options)
         * otherwise: use the limitation we got in the parameter.
         */

        int maxEntriesToReturn = getParameters().getMaxVmsToReturn();
        switch (maxEntriesToReturn) {
        case -1:
            maxEntriesToReturn = vms.size();
            break;
        case 0:
            maxEntriesToReturn = Math.min(vms.size(), Config.<Integer> GetValue(ConfigValues.NumberOfVmsForTopSizeVms));
            break;
        default:
            maxEntriesToReturn = Math.min(vms.size(), maxEntriesToReturn);
            break;
        }

        getQueryReturnValue().setReturnValue(vms.subList(0, maxEntriesToReturn));
    }
}
