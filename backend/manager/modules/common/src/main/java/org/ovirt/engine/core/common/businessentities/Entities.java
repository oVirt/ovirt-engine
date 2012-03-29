package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * General utility class for common entities functions
 */
public class Entities {

    /**
     * Map entity's name -> entity object. A lot of logic code does filtering of data from 2 collections by quad
     * iterating over them. Common scenario: entity Parent.name is represent in Child.parentName. given list<Parent> and
     * List<Child> find each Child that have parents in List<Parent> <code>
     * <code>
     * List<Parent> parents = ...
     * List<Child> childs = ...
     * Map<String, Parent> parentsByName = Entities.byName(parents)
     *      for (Child c : childs) {
     *          if(parents.contatinsKey(c.getParentName())) {
     *              doThis();
     *          }
     *      }
     * }
     * </code>
     * @param entityList
     * @return
     */
    public static <E extends Nameable> Map<String, E> entitiesByName(List<E> entityList) {
        if (entityList != null) {
            Map<String, E> map = new HashMap<String, E>();
            for (E e : entityList) {
                map.put(e.getName(), e);
            }
            return map;
        } else {
            return Collections.emptyMap();
        }
    }

    public static <E extends NetworkInterface<?>> Map<String, E> interfacesByNetworkName(List<E> entityList) {
        if (entityList != null) {
            Map<String, E> map = new HashMap<String, E>();
            for (E e : entityList) {
                map.put(e.getNetworkName(), e);
            }
            return map;
        } else {
            return Collections.emptyMap();
        }
    }

    public static <F extends Serializable, B extends BusinessEntity<F>> Map<F, B> businessEntitiesById(List<B> entityList) {
        if (entityList != null) {
            Map<F, B> map = new HashMap<F, B>();
            for (B b : entityList) {
                map.put(b.getId(), b);
            }
            return map;
        } else {
            return Collections.emptyMap();
        }
    }

}
