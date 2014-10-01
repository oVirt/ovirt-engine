package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.vds_spm_id_map;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;

@InternalCommandAttribute
public class AddVdsSpmIdCommand<T extends VdsActionParameters> extends VdsCommand<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    public AddVdsSpmIdCommand(Guid commandId) {
        super(commandId);
    }

    public AddVdsSpmIdCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWait(true);
    }

    @Override
    protected boolean canDoAction() {
        return true;
    }

    @Override
    protected void executeCommand() {
        List<vds_spm_id_map> vds_spm_id_mapList = DbFacade.getInstance().getVdsSpmIdMapDao().getAll(
                getVds().getStoragePoolId());
        if (vds_spm_id_mapList.size() >= Config.<Integer> getValue(ConfigValues.MaxNumberOfHostsInStoragePool)) {
            buildFaultResult();
            return;
        }
        insertSpmIdToDb(vds_spm_id_mapList);

        setSucceeded(true);
    }

    private void insertSpmIdToDb(List<vds_spm_id_map> vds_spm_id_mapList) {
        // according to shaharf the first id is 1
        int selectedId = 1;
        List<Integer> list = LinqUtils.transformToList(vds_spm_id_mapList, new Function<vds_spm_id_map, Integer>() {
            @Override
            public Integer eval(vds_spm_id_map vds_spm_id_map) {
                return vds_spm_id_map.getvds_spm_id();
            }
        });
        Collections.sort(list);
        for (int id : list) {
            if (selectedId == id) {
                selectedId++;
            } else {
                break;
            }
        }
        // get the dc id from cluster if DC was removed and cluster is attached to a new DC
        Guid dcId = (getVds().getStoragePoolId().equals(Guid.Empty) ? getVdsGroup().getStoragePoolId() : getVds().getStoragePoolId());
        vds_spm_id_map newMap = new vds_spm_id_map(dcId, getVdsId(), selectedId);
        DbFacade.getInstance().getVdsSpmIdMapDao().save(newMap);
        if (getParameters().isCompensationEnabled()) {
            getCompensationContext().snapshotNewEntity(newMap);
            getCompensationContext().stateChanged();
        }
    }

    private void buildFaultResult() {
        VdcFault fault = new VdcFault();
        fault.setError(VdcBllErrors.ReachedMaxNumberOfHostsInDC);
        fault.setMessage(Backend.getInstance()
                .getVdsErrorsTranslator()
                .TranslateErrorTextSingle(fault.getError().toString()));
        getReturnValue().setFault(fault);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVds().getStoragePoolId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.REGISTER_VDS, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }
}
