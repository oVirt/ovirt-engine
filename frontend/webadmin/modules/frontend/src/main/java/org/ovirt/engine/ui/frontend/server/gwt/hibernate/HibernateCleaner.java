package org.ovirt.engine.ui.frontend.server.gwt.hibernate;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.collection.internal.AbstractPersistentCollection;
import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.collection.internal.PersistentList;
import org.hibernate.collection.internal.PersistentMap;
import org.hibernate.collection.internal.PersistentSet;
import org.hibernate.collection.internal.PersistentSortedSet;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

public class HibernateCleaner {
    public static Object doClean(Object dirty) {
        return doClean(dirty, null);
    }

    private static Object doClean(Object dirty, Map<Object, Object> processed) {
        if (dirty == null) {
            return null;
        }

        //Create object tree cache, so we don't process the same object twice.
        if (processed == null) {
            processed = new HashMap<>();
        }

        //Don't process primitive types.
        if (isPrimitive(dirty)) {
            return dirty;
        }

        //We already processed the object, return the result of the previous processing.
        if (processed.get(dirty) != null) {
            return processed.get(dirty);
        }

        if (!processCollections(dirty, processed)) {
            //The object is not a collection, find all the getters and process the objects associated with those.
            for (Method getter : ReflectionUtils.getGetters(dirty.getClass())) {
                Object object = ReflectionUtils.get(dirty, getter);
                if (!dirty.equals(object)) {
                    if (object instanceof AbstractPersistentCollection) {
                        // Hibernate persistent class, replace the implementation.
                        ReflectionUtils.setIfPossible(dirty, getter, doHibernateClean(object, processed));
                    } else {
                        processed.put(object, doClean(object, processed));
                    }
                }
            }
        }
        return dirty;
    }

    /**
     * Process the object if it is a collection. If it is not a collection then don't do anything and return false.
     * @param dirty The object to process.
     * @param processed A map of already processed objects so we can avoid processing the same object twice.
     * @return true if the passed in object is a collection, false otherwise.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static boolean processCollections(Object dirty, Map<Object, Object> processed) {
        if (dirty instanceof List) {
            for (Object value : (List) dirty) {
                doClean(value, processed);
            }
            return true;
        } else if (dirty instanceof Map) {
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) dirty).entrySet()) {
                doClean(entry.getKey(), processed);
                doClean(entry.getValue(), processed);
            }
            return true;
        } else if (dirty instanceof Set) {
            for (Object value : (Set<Object>) dirty) {
                doClean(value, processed);
            }
            return true;
        } else if (dirty instanceof HibernateProxy) {
            HibernateProxy proxy = (HibernateProxy) dirty;
            LazyInitializer lazyInitializer = proxy.getHibernateLazyInitializer();
            if (lazyInitializer.isUninitialized()) {
                return true;
            } else {
                dirty = lazyInitializer.getImplementation();
                doClean(dirty, processed);
            }
            return true;
        }
        return false;
    }

    private static Object doHibernateClean(Object dirty, Map<Object, Object> processed) {
        if (dirty instanceof PersistentList) {
            PersistentList dirtyList = (PersistentList) dirty;
            List<Object> cleanList = new ArrayList<Object>();
            processed.put(dirtyList, cleanList);
            if (dirtyList.wasInitialized()) {
                for (Object value : dirtyList) {
                    cleanList.add(doClean(value, processed));
                }
            }
            return cleanList;
        }

        if (dirty instanceof PersistentBag) {
            PersistentBag dirtyList = (PersistentBag) dirty;
            List<Object> cleanList = new ArrayList<Object>();
            processed.put(dirtyList, cleanList);
            if (dirtyList.wasInitialized()) {
                for (Object value : dirtyList) {
                    cleanList.add(doClean(value, processed));
                }
            }
            return cleanList;
        }

        if (dirty instanceof PersistentSortedSet) {
            PersistentSortedSet dirtySet = (PersistentSortedSet) dirty;
            Set<Object> cleanSet = new TreeSet<Object>();
            processed.put(dirtySet, cleanSet);
            if (dirtySet.wasInitialized()) {
                for (Object value : dirtySet) {
                    cleanSet.add(doClean(value, processed));
                }
            }
            return cleanSet;
        }

        if (dirty instanceof PersistentSet) {
            PersistentSet dirtySet = (PersistentSet) dirty;
            Set<Object> cleanSet = new HashSet<Object>();
            processed.put(dirtySet, cleanSet);
            if (dirtySet.wasInitialized()) {
                for (Object value : dirtySet) {
                    cleanSet.add(doClean(value, processed));
                }
            }
            return cleanSet;
        }

        if (dirty instanceof PersistentMap) {
            PersistentMap dirtyMap = (PersistentMap) dirty;
            Map<Object, Object> cleanMap = new LinkedHashMap<Object, Object>();
            processed.put(dirtyMap, cleanMap);
            if (dirtyMap.wasInitialized()) {
                for (Object entryObject : dirtyMap.entrySet()) {
                    //Cast the entry object to the right type since the hibernate library doesn't know how to do
                    //generics properly.
                    @SuppressWarnings("unchecked")
                    Entry<Object, Object> entry = (Entry<Object, Object>) entryObject;
                    cleanMap.put(doClean(entry.getKey(), processed), doClean(entry.getValue(), processed));
                }
            }
            return cleanMap;
        }
        return null;
    }

    /**
     * Determine if an object is a 'primitive' in that it doesn't need to be processed by the cleaner.
     * An object is considered primitive if it is one of the following
     * <ul>
     *   <li>A java primitive</li>
     *   <li>A java.util.Date</li>
     *   <li>An Enum</li>
     *   <li>A String</li>
     * </ul>
     * @param object The object to check
     * @return true if considered primitive, false otherwise.
     */
    private static boolean isPrimitive(Object object) {
        if (object instanceof String) {
            return true;
        }

        if (object instanceof Date) {
            return true;
        }

        if (object instanceof Enum) {
            return true;
        }

        Class<? extends Object> clazz = object.getClass();
        if (clazz.isPrimitive()) {
            return true;
        }
        return false;
    }
}
