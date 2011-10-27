package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationByAdElementIdParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class IsUserPowerUserOrAboveQuery<P extends MultilevelAdministrationByAdElementIdParameters>
        extends QueriesCommandBase<P> {
    public IsUserPowerUserOrAboveQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        final Guid superUserId = new Guid("00000000-0000-0000-0000-000000000001");
        final Guid powerUserId = new Guid("00000000-0000-0000-0001-000000000002");
        List<permissions> list = DbFacade.getInstance().getPermissionDAO()
                .getAllForAdElement(getParameters().getAdElementId());
        // LINQ 29456
        // QueryReturnValue.ReturnValue = (list.FirstOrDefault(p => (p.role_id
        // == superUserId || p.role_id == powerUserId)) != null);

        permissions permissions = LinqUtils.firstOrNull(list, new Predicate<permissions>() {
            @Override
            public boolean eval(permissions p) {
                return (p.getrole_id().equals(superUserId) || p.getrole_id().equals(powerUserId));
            }
        });
        getQueryReturnValue().setReturnValue(permissions != null);
    }
}
