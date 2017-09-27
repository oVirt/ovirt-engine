package org.ovirt.engine.api.restapi.util.links;

import org.ovirt.engine.api.restapi.util.LinkHelper;

/**
 * A container of meta-data for a location in the API tree:
 * 1) the Service class which handles single entities in this location.
 * 2) the Service class which handles the collection of entities in this location.
 * 3) the parent-type of entities in this location (if any).
 * e.g: for VMs in root: VmResource, VmsResource, parentType=null.
 *      for VM-tags: AssignedTagResource, AssignedTagsResource, parentType=VM.
 */
public class ApiLocationMetadata {
    private final Class<?> entityServiceClass;
    private final Class<?> collectionServiceClass;
    private final Class<?> parentType;

    public ApiLocationMetadata(Class<?> entityServiceClass, Class<?> collectionServiceClass, Class<?> parentType) {
        this.entityServiceClass = entityServiceClass;
        this.collectionServiceClass = collectionServiceClass;
        this.parentType = parentType;
    }

    public Class<?> getEntityServiceClass() {
        return entityServiceClass;
    }

    public Class<?> getCollectionServiceClass() {
        return collectionServiceClass;
    }

    public Class<?> getParentType() {
        return parentType;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\t\tparent: ").append(parentType.equals(LinkHelper.NO_PARENT) ? "NO PARENT" : parentType.getSimpleName()).append("\n")
            .append("\t\tresource_single: ").append(entityServiceClass.getSimpleName()).append("\n")
            .append("\t\tresource_collction: ").append(collectionServiceClass.getSimpleName()).append("\n");
        return builder.toString();
    }
}
