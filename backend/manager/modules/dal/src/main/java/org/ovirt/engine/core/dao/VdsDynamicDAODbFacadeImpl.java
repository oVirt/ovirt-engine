package org.ovirt.engine.core.dao;

import java.util.Collection;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.Interceptors;

import org.ovirt.engine.core.common.businessentities.ExternalStatus;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.jpa.AbstractJpaDao;
import org.ovirt.engine.core.utils.transaction.TransactionalInterceptor;
import org.springframework.stereotype.Component;

/**
 * <code>VdsDAODbFacadeImpl</code> provides an implementation of {@link VdsDAO} that uses previously written code from
 * {@link org.ovirt.engine.core.dal.dbbroker.DbFacade}.
 *
 *
 */
@Interceptors({ TransactionalInterceptor.class })
@ApplicationScoped
@Component
public class VdsDynamicDAODbFacadeImpl extends AbstractJpaDao<VdsDynamic, Guid> implements VdsDynamicDAO {

    public VdsDynamicDAODbFacadeImpl() {
        super(VdsDynamic.class);
    }

    @Override
    public void updateStatus(Guid id, VDSStatus status) {
        VdsDynamic entity = get(id);
        entity.setStatus(status);
        update(entity);
    }

    @Override
    public void updateNetConfigDirty(Guid id, Boolean netConfigDirty) {
        VdsDynamic entity = get(id);
        entity.setNetConfigDirty(netConfigDirty);
        update(entity);
    }

    @Override
    public void updateVdsDynamicPowerManagementPolicyFlag(Guid id,
                                                          boolean controlledByPmPolicy) {
        VdsDynamic entity = get(id);
        entity.setPowerManagementControlledByPolicy(controlledByPmPolicy);
        update(entity);
    }

    @Override
    public void updateIfNeeded(VdsDynamic vdsDynamic) {
        update(vdsDynamic);
    }

    @Override
    public void updateStatusAndReasons(VdsDynamic host) {
        update(host);
    }

    public void updateCpuFlags(Guid id, String cpuFlags) {
        VdsDynamic entity = get(id);
        entity.setCpuFlags(cpuFlags);
        update(entity);
    }

    public void updateExternalStatus(Guid id, ExternalStatus status) {
        VdsDynamic entity = get(id);
        entity.setExternalStatus(status);
        update(entity);
    }

    public void updateAll(Collection<VdsDynamic> entities) {
        for (VdsDynamic entity : entities) {
            update(entity);
        }
    }

    @Override
    public void removeAll(Collection<Guid> ids) {
        for (Guid id : ids) {
            remove(id);
        }
    }

    @Override
    public void removeAllInBatch(Collection<VdsDynamic> entities) {
        for (VdsDynamic entity : entities) {
            remove(entity);
        }
    }

    @Override
    public void updateAllInBatch(Collection<VdsDynamic> entities) {
        updateAll(entities);
    }

    @Override
    public void saveAll(Collection<VdsDynamic> entities) {
        updateAll(entities);
    }

    @Override
    public void saveAllInBatch(Collection<VdsDynamic> entities) {
        updateAll(entities);
    }

    @Override
    public void updateUpdateAvailable(Guid id, boolean updateAvailable) {
        VdsDynamic entity = get(id);
        entity.setUpdateAvailable(updateAvailable);
        update(entity);
    }

    public List<Guid> getIdsOfHostsWithStatus(VDSStatus status) {
        return multipleResults(entityManager.createNamedQuery("VdsDynamic.idByStatus").setParameter("status",
                status.getValue()));
    }

    @Override
    public void updateAll(String procedureName, Collection<VdsDynamic> entities) {
        updateAll(entities);
    }
}
