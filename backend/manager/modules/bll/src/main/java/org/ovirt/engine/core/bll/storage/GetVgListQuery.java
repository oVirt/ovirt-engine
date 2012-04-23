package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.queries.VdsIdParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class GetVgListQuery<P extends VdsIdParametersBase> extends QueriesCommandBase<P> {
    public GetVgListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        java.util.ArrayList<storage_domains> vgsFromVds = (java.util.ArrayList<storage_domains>) (Backend.getInstance()
                .getResourceManager().RunVdsCommand(VDSCommandType.GetVGList,
                                                    new VdsIdVDSCommandParametersBase(getParameters().getVdsId())))
                .getReturnValue();

        List<storage_domains> vgsFromDb = LinqUtils.filter(DbFacade.getInstance().getStorageDomainDAO().getAll(),
                new Predicate<storage_domains>() {
                    @Override
                    public boolean eval(storage_domains storageDomain) {
                        return storageDomain.getstorage_type() == StorageType.ISCSI
                                || storageDomain.getstorage_type() == StorageType.FCP;
                    }
                });

        java.util.HashSet<String> vgIdsFromDb = new java.util.HashSet<String>();

        for (storage_domains domain : vgsFromDb) {
            vgIdsFromDb.add(domain.getstorage());
        }

        java.util.ArrayList<storage_domains> returnValue = new java.util.ArrayList<storage_domains>();

        for (storage_domains domain : vgsFromVds) {
            if (domain.getId().equals(Guid.Empty) && !vgIdsFromDb.contains(domain.getstorage())) {
                returnValue.add(domain);
            }
        }
        getQueryReturnValue().setReturnValue(returnValue);
    }
}
