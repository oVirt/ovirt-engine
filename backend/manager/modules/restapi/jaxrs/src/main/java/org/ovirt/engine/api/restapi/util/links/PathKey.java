package org.ovirt.engine.api.restapi.util.links;

import java.util.Objects;

/**
 * This class serves as a key to a map which stores values of 'Path' annotations
 * found in Service interfaces. A 'collection' service (e.g: VmsService) along
 * with a single-entity Service (e.g: VmService) identify a location on the API
 * tree, which may be associated with a value of a 'Path' annotation.
 */
public class PathKey {
    private Class<?> service;
    private Class<?> parentService;
    public PathKey(Class<?> service, Class<?> parentService) {
        super();
        this.service = service;
        this.parentService = parentService;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PathKey) {
            PathKey key = (PathKey)obj;
            return equals(service, key.service) && equals(parentService, key.parentService);
        } else {
            return false;
        }
    }
    private boolean equals(Class<?> class1, Class<?> class2) {
        return Objects.equals(class1, class2);
    }

    @Override
    public int hashCode() {
        if (service == null && parentService == null) {
            return 0;
        }
        if (service == null) {
            return parentService.hashCode();
        }
        if (parentService == null) {
            return service.hashCode();
        }
        return 997 * (service.hashCode()) ^ 991 * (parentService.hashCode()); //large primes!
    }
}
