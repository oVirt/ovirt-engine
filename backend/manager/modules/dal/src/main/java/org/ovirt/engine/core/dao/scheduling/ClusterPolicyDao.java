package org.ovirt.engine.core.dao.scheduling;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ModificationDao;

public interface ClusterPolicyDao extends ModificationDao<ClusterPolicy, Guid> {
    /**
     * Retrieves the entity with the given id.
     *
     * @param id
     *            The id to look by (can't be <code>null</code>).
     * @param internalUnitTypes
     *            Mapping between internal cluster policy IDs and their types
     * @return The entity instance, or <code>null</code> if not found.
     */
    ClusterPolicy get(Guid id,
            @NotNull Map<Guid, PolicyUnitType> internalUnitTypes);

    /**
     * Retrieves all the entities of type {@link ClusterPolicy}.
     *
     * @param internalUnitTypes
     *            Mapping between internal cluster policy IDs and their types
     *
     * @return A list of all the entities, or an empty list if none is found.
     */
    List<ClusterPolicy> getAll(@NotNull Map<Guid, PolicyUnitType> internalUnitTypes);
}
