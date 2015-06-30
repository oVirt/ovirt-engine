package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class GetVgListQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVgListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        ArrayList<StorageDomain> vgsFromVds = (ArrayList<StorageDomain>) (Backend.getInstance()
                .getResourceManager().RunVdsCommand(VDSCommandType.GetVGList,
                                                    new VdsIdVDSCommandParametersBase(getParameters().getId())))
                .getReturnValue();

        List<StorageDomain> vgsFromDb = LinqUtils.filter(DbFacade.getInstance().getStorageDomainDao().getAll(),
                new Predicate<StorageDomain>() {
                    @Override
                    public boolean eval(StorageDomain storageDomain) {
                        return storageDomain.getStorageType().isBlockDomain();
                    }
                });

        HashSet<String> vgIdsFromDb = new HashSet<>();

        for (StorageDomain domain : vgsFromDb) {
            vgIdsFromDb.add(domain.getStorage());
        }

        ArrayList<StorageDomain> returnValue = new ArrayList<>();

        for (StorageDomain domain : vgsFromVds) {
            if (domain.getId().equals(Guid.Empty) && !vgIdsFromDb.contains(domain.getStorage())) {
                returnValue.add(domain);
            }
        }
        getQueryReturnValue().setReturnValue(returnValue);
    }
}
