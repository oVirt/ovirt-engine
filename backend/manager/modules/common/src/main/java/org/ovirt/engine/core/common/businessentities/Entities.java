package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * General utility class for common entities functions
 */
public class Entities {
    /**
     * Map entity's name to entity object. A lot of logic code does filtering of data from 2 collections by quad
     * iterating over them. Common scenario: entity Parent.name is represent in Child.parentName. Given
     * List&lt;Parent&gt; and List&lt;Child&gt;, find each child that has parents in List&lt;Parent&gt;
     * <code>
     * List&lt;Parent&gt; parents = ...
     * List&lt;Child&gt; childs = ...
     * Map&lt;String, Parent&gt; parentsByName = Entities.byName(parents)
     * for (Child c : childs) {
     * if(parents.contatinsKey(c.getParentName())) {
     * doThis();
     * }
     * }
     * }
     * </code>
     */
    public static <E extends Nameable> Map<String, E> entitiesByName(Collection<E> entities) {
        if (entities != null) {
            Map<String, E> map = new HashMap<>();
            for (E e : entities) {
                map.put(e.getName(), e);
            }
            return map;
        } else {
            return Collections.emptyMap();
        }
    }

    public static <F extends Serializable, B extends BusinessEntity<F>> Map<F, B> businessEntitiesById(Collection<B> entities) {
        if (entities != null) {
            Map<F, B> map = new HashMap<>();
            for (B b : entities) {
                map.put(b.getId(), b);
            }
            return map;
        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * @param requestedIds ids to find.
     * @param existingEntities all entities to search among
     *
     * @return all entities from <code>existingEntities</code> collection, which has complying id.
     */
    public static <E extends BusinessEntity<I>, I extends Serializable> List<E> filterEntitiesByRequiredIds(Collection<I> requestedIds,
        Collection<E> existingEntities) {

        List<E> resultCollection = new ArrayList<>(requestedIds.size());

        Map<I, E> existingEntitiesMap = businessEntitiesById(existingEntities);
        for (I requestedId : requestedIds) {
            if (existingEntitiesMap.containsKey(requestedId)) {
                resultCollection.add(existingEntitiesMap.get(requestedId));
            }
        }

        return resultCollection;
    }

    public static <E extends BusinessEntity<I>, I extends Serializable> List<I> idsNotReferencingExistingRecords(
            Collection<I> ids,
            Collection<E> existingEntities) {

        return idsNotReferencingExistingRecords(ids, businessEntitiesById(existingEntities));
    }

    public static <E extends BusinessEntity<I>, I extends Serializable> List<I> idsNotReferencingExistingRecords(
            Collection<I> ids, Map<I, E> entitiesById) {
        List<I> idsNotReferencingExistingRecords = new ArrayList<>();
        for (I id : ids) {
            if (!entitiesById.containsKey(id)) {
                idsNotReferencingExistingRecords.add(id);
            }
        }

        return idsNotReferencingExistingRecords;
    }
}
