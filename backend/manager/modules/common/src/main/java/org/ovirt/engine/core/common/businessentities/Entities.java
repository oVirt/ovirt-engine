package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

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

    public static <E extends StorageServerConnections> Map<String, E> connectionsByIQN(List<E> entityList) {
        if (entityList != null) {
            Map<String, E> map = new HashMap<String, E>();
            for (E e : entityList) {
                map.put(e.getiqn(), e);
            }
            return map;
        } else {
            return Collections.emptyMap();
        }
    }

    public static <E extends VmNetworkInterface> Map<String, E> vmInterfacesByNetworkName(List<E> entityList) {
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

    public static <E extends VmNetworkInterface> Map<Guid, List<E>> vmInterfacesByVmId(List<E> vnics) {
        if (vnics == null || vnics.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Guid, List<E>> map = new HashMap<Guid, List<E>>();
        for (E vnic : vnics) {
            if (!map.containsKey(vnic.getVmId())) {
                map.put(vnic.getVmId(), new ArrayList<E>());
            }

            map.get(vnic.getVmId()).add(vnic);
        }

        return map;
    }

    public static <E extends VdsNetworkInterface> Map<String, E> hostInterfacesByNetworkName(Collection<E> entityList) {
        if (entityList != null) {
            Map<String, E> map = new HashMap<String, E>();
            for (E e : entityList) {
                if (e.getNetworkName() != null) {
                    map.put(e.getNetworkName(), e);
                }
            }
            return map;
        } else {
            return Collections.emptyMap();
        }
    }

    public static <E extends Nameable> Set<String> objectNames(Collection<E> entities) {
        if (entities != null && !entities.isEmpty()) {
            Set<String> names = new HashSet<String>();
            for (E e : entities) {
                if (e != null) {
                    names.add(e.getName());
                }
            }
            return names;
        } else {
            return Collections.emptySet();
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

    /**
     * Convert the given collections to a {@link String}. Empty or <code>null</code> collections will be converted to
     * "[]", otherwise first element will appear after the opening bracket, and the consecutive elements (if any) will
     * appear on a new line, prefixed by the given prefix.
     *
     * @param objects
     *            The collection to convert (can be <code>null</code>).
     * @param prefix
     *            The prefix to print on new line of a collection element (not on 1st element).
     * @return A {@link String} representation of the given collection.
     */
    public static String collectionToString(Collection<?> objects, String prefix) {
        StringBuilder sb = new StringBuilder("[");
        if (objects != null) {
            boolean first = true;
            for (Iterator<?> iterator = objects.iterator(); iterator.hasNext();) {
                Object object = iterator.next();

                if (first) {
                    first = false;
                } else {
                    sb.append(prefix);
                }

                sb.append(object.toString());

                if (iterator.hasNext()) {
                    sb.append(",\n");
                }
            }
        }

        sb.append("]");
        return sb.toString();
    }

    public static <B, G extends B> List<B> upcast(List<G> entities) {
        List<B> baseEntities = new ArrayList<B>(entities.size());
        for (G entity : entities) {
            baseEntities.add(entity);
        }

        return baseEntities;
    }

    public static <E extends BusinessEntity<I>, I extends Serializable> List<I> getIds(List<E> entities) {
        List<I> ids = new ArrayList<I>(entities.size());
        for (E entity : entities) {
            ids.add(entity.getId());
        }
        return ids;
    }
}
