package org.ovirt.engine.api.utils;

import java.lang.reflect.Method;
import java.util.Collection;

import org.ovirt.engine.api.model.ActionableResource;
import org.ovirt.engine.api.model.BaseResources;

public class EntityHelper {

    /**
     * Returns true if this entity is a 'collection' type (Vms, Hosts, Disks...)
     * false otherwise (Vm, Host, Disk...) An entity is assumed to be a collection
     * type if it extends BaseResources class.
     */
    public static boolean isCollection(ActionableResource entity) {
        return entity instanceof BaseResources;
    }

    /**
     * For 'collection' type entities, this method returns the method in the
     * entity which returns the list or entities, for example: for a 'Vms' entity,
     * this method will return getVms() method.
     *
     * Note: This method relies on a strong and admittedly dangerous assumption
     * that collection type entities (Vms, Hosts, Disks...) only have one method
     * which returns a collection. The correct way to do this is using an explicit
     * annotation in collection types, which singles-out the method which returns
     * the list of entities ( //TODO).
     */
    public static Method getCollectionGetter(BaseResources entity) {
        for (Method method : entity.getClass().getMethods()) {
            Class<?> returnType = method.getReturnType();
            if (Collection.class.isAssignableFrom(returnType)) {
                return method;
            }
        }
        //should never happen
        throw new IllegalStateException("Collection-type entity does not contian a method which returns a list");
    }
}
