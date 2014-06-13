package org.ovirt.engine.core.bll;

import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.MacPoolDao;

public class MacPoolValidator {

    private final MacPool macPool;

    public MacPoolValidator(MacPool macPool) {
        this.macPool = macPool;
    }

    public ValidationResult notRemovingDefaultPool() {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_REMOVE_DEFAULT_MAC_POOL).
                when(macPool.isDefaultPool());
    }

    public ValidationResult notRemovingUsedPool() {
        final int dcUsageCount = getMacPoolDao().getDcUsageCount(macPool.getId());
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_REMOVE_STILL_USED_MAC_POOL).
                when(dcUsageCount != 0);
    }

    public ValidationResult macPoolExists() {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_MAC_POOL_DOES_NOT_EXIST).
                when(macPool == null);
    }

    public ValidationResult defaultPoolFlagIsNotSet() {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_SETTING_DEFAULT_MAC_POOL_IS_NOT_SUPPORTED).
                when(macPool.isDefaultPool());
    }

    public ValidationResult hasUniqueName() {
        return ValidationResult.failWith(VdcBllMessages.ACTION_TYPE_FAILED_NAME_ALREADY_USED).
                        when(!macPoolNameUnique());
    }

    private boolean macPoolNameUnique() {
        final List<MacPool> macPools = getMacPoolDao().getAll();
        for (MacPool pool : macPools) {
            if (!Objects.equals(pool.getId(), macPool.getId()) &&
                    pool.getName().equals(macPool.getName())) {
                return false;
            }
        }

        return true;
    }

    private MacPoolDao getMacPoolDao() {
        return DbFacade.getInstance().getMacPoolDao();
    }

}
