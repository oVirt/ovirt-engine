package org.ovirt.engine.core.common.businessentities;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.compat.Guid;

/**
 * The {@code BusinessEntityMap} class stores a nameable business entities for efficient access by their ID or name.<br>
 * The {@code BusinessEntityMap} ignores {@code null} IDs or {@code null} names to be search by.
 */
public class BusinessEntityMap<E extends BusinessEntity<Guid> & Nameable> {
    private Map<String, E> entitiesByName;
    private Map<Guid, E> entitiesById;

    // added for the sake of checkstyle rule
    @SuppressWarnings("unused")
    private BusinessEntityMap() {
    }

    public BusinessEntityMap(Collection<E> entities) {
        entitiesByName = new HashMap<>();
        entitiesById = new HashMap<>();

        if (entities != null) {
            int nullValuedNames = 0;
            int nullValuedIds = 0;
            int nonNullEntities = 0;

            for (E e : entities) {
                if (e == null) {
                    continue;
                }

                nonNullEntities++;
                if (e.getName() == null) {
                    nullValuedNames++;
                } else {
                    entitiesByName.put(e.getName(), e);
                }

                if (e.getId() == null) {
                    nullValuedIds++;
                } else {
                    entitiesById.put(e.getId(), e);
                }
            }

            if (entitiesByName.size() + nullValuedNames < nonNullEntities ||
                entitiesById.size() + nullValuedIds < nonNullEntities) {
                throw new IllegalArgumentException("duplicates in input.");
            }
        }
    }

    public E get(String name) {
        return entitiesByName.get(name);
    }

    public E get(Guid id) {
        return entitiesById.get(id);
    }

    public boolean containsKey(String name) {
        return entitiesByName.containsKey(name);
    }

    public boolean containsKey(Guid id) {
        return entitiesById.containsKey(id);
    }

    /**
     * Returns an entity from the map by the given ID if not {@code null}, else by the given name.
     *
     * @param id
     *            the entity's ID
     * @param name
     *            the entity's name
     */
    public E get(Guid id, String name) {
        return id == null ? get(name) : get(id);
    }


    public Map<Guid, E> unmodifiableEntitiesByIdMap() {
        return Collections.unmodifiableMap(entitiesById);
    }
}
