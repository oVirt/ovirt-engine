package org.ovirt.engine.core.bll;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.MacPoolDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.utils.ReplacementUtils;

public class MacPoolValidator {

    private final MacPool macPool;

    public MacPoolValidator(MacPool macPool) {
        this.macPool = macPool;
    }

    public ValidationResult notRemovingDefaultPool() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_CANNOT_REMOVE_DEFAULT_MAC_POOL).
                when(macPool.isDefaultPool());
    }

    public ValidationResult notRemovingUsedPool() {
        final StoragePoolDao storagePoolDao = getDbFacade().getStoragePoolDao();
        final List<StoragePool> dataCenters = storagePoolDao.getAllDataCentersByMacPoolId(macPool.getId());

        final Collection<String> replacements = ReplacementUtils.replaceWithNameable("DATACENTERS_USING_MAC_POOL", dataCenters);
        replacements.add(EngineMessage.VAR__ENTITIES__DATA_CENTERS.name());
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_CANNOT_REMOVE_STILL_USED_MAC_POOL,
                replacements.toArray(new String[0])).when(dataCenters.size() != 0);
    }

    protected DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    public ValidationResult macPoolExists() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_MAC_POOL_DOES_NOT_EXIST).
                when(macPool == null);
    }

    public ValidationResult defaultPoolFlagIsNotSet() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_SETTING_DEFAULT_MAC_POOL_IS_NOT_SUPPORTED).
                when(macPool.isDefaultPool());
    }

    public ValidationResult hasUniqueName() {
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED).
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
        return getDbFacade().getMacPoolDao();
    }

}
