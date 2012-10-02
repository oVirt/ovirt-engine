package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.vds_spm_id_map;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;

@InternalCommandAttribute
@LockIdNameAttribute(isWait=true)
public class AddVdsSpmIdCommand<T extends VdsActionParameters> extends VdsCommand<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    public AddVdsSpmIdCommand(Guid commandId) {
        super(commandId);
    }

    public AddVdsSpmIdCommand(T parametars) {
        super(parametars);
    }

    @Override
    protected boolean canDoAction() {
        return !Guid.Empty.equals(getVds().getstorage_pool_id())
                && DbFacade.getInstance().getVdsSpmIdMapDao().get(getVdsId()) == null;
    }

    @Override
    protected void executeCommand() {
        // according to shaharf the first id is 1
        int selectedId = 1;
        List<vds_spm_id_map> list = DbFacade.getInstance().getVdsSpmIdMapDao().getAll(
                getVds().getstorage_pool_id());
        List<Integer> map = LinqUtils.foreach(list, new Function<vds_spm_id_map, Integer>() {
            @Override
            public Integer eval(vds_spm_id_map vds_spm_id_map) {
                return vds_spm_id_map.getvds_spm_id();
            }
        });
        Collections.sort(map);
        for (int id : map) {
            if (selectedId == id) {
                selectedId++;
            } else {
                break;
            }
        }
        vds_spm_id_map newMap = new vds_spm_id_map(getVds().getstorage_pool_id(), getVdsId(), selectedId);
        DbFacade.getInstance().getVdsSpmIdMapDao().save(newMap);
        if (getParameters().isCompensationEnabled()) {
            getCompensationContext().snapshotNewEntity(newMap);
            getCompensationContext().stateChanged();
        }

        setSucceeded(true);
    }

    @Override
    protected Map<String, String> getExclusiveLocks() {
        return Collections.singletonMap(getVds().getstorage_pool_id().toString(), LockingGroup.REGISTER_VDS.name());
    }
}
