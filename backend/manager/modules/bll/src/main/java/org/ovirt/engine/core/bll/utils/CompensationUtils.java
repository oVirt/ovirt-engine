package org.ovirt.engine.core.bll.utils;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.dao.GenericDao;
import org.ovirt.engine.core.dao.MassOperationsDao;
import org.ovirt.engine.core.dao.ModificationDao;

public final class CompensationUtils {

    public static <ID extends Serializable, T extends BusinessEntity<ID>>
    void saveEntity(T entity, ModificationDao<T, ID> dao, CompensationContext compensationContext) {
        dao.save(entity);
        if (compensationContext != null) {
            compensationContext.snapshotNewEntity(entity);
        }
    }

    public static <ID extends Serializable, T extends BusinessEntity<ID>>
    void updateEntity(T entity, GenericDao<T, ID> dao, CompensationContext compensationContext) {
        if (compensationContext == null) {
            dao.update(entity);
            return;
        }

        updateEntity(entity, dao.get(entity.getId()), dao, compensationContext);
    }

    public static <ID extends Serializable, T extends BusinessEntity<ID>>
    void updateEntity(T newEntity, T oldEntity, ModificationDao<T, ID> dao, CompensationContext compensationContext) {
        if (compensationContext != null) {
            compensationContext.snapshotEntityUpdated(oldEntity);
        }

        dao.update(newEntity);
    }

    public static <ID extends Serializable, T extends BusinessEntity<ID>>
    void updateEntity(T oldEntity, Consumer<T> modificationFunction, ModificationDao<T, ID> dao, CompensationContext compensationContext) {
        if (compensationContext != null) {
            compensationContext.snapshotEntityUpdated(oldEntity);
        }

        modificationFunction.accept(oldEntity);
        dao.update(oldEntity);
    }

    public static <ID extends Serializable, T extends BusinessEntity<ID>>
    void removeEntity(ID entityId, GenericDao<T, ID> dao, CompensationContext compensationContext) {
        if (compensationContext == null) {
            dao.remove(entityId);
            return;
        }

        T oldEntity = dao.get(entityId);
        removeEntity(oldEntity, dao, compensationContext);
    }

    public static <ID extends Serializable, T extends BusinessEntity<ID>>
    void removeEntity(T entity, ModificationDao<T, ID> dao, CompensationContext compensationContext) {
        if (entity == null) {
            return;
        }

        if (compensationContext != null) {
            compensationContext.snapshotEntity(entity);
        }

        dao.remove(entity.getId());
    }

    public static <ID extends Serializable, T extends BusinessEntity<ID>>
    void removeEntities(Collection<T> entities, MassOperationsDao<T, ID> dao, CompensationContext compensationContext) {
        if (compensationContext != null) {
            compensationContext.snapshotEntities(entities);
        }
        dao.removeAll(entities.stream()
                .map(BusinessEntity::getId)
                .collect(Collectors.toList()));
    }
}
