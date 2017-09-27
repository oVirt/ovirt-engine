package org.ovirt.engine.api.restapi.util.links;

import java.util.LinkedHashMap;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.restapi.util.LinkHelper;

/**
 * A map which holds entity location meta-data according to parent-type.
 * This is a utility map, which exists only for performance reasons, and is always
 * used in a broader context. An instance of this map represents location
 * metadata for a specific entity, but the entity-type is not saved within the map itself,
 * meaning that looking at an instance of LocationByParentMap without the context in which
 * it was created, one could not tell which entity the map describes.
 */
public class LocationByParentMap extends LinkedHashMap<Class<? extends BaseResource>, ApiLocationMetadata> {

    public void add(Class<?> resourceType,
                    Class<?> collectionType,
                    Class<? extends BaseResource> parentType) {
        put(parentType, new ApiLocationMetadata(resourceType, collectionType, parentType));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (java.util.Map.Entry<Class<? extends BaseResource>, ApiLocationMetadata> entry : entrySet()) {
            builder.append("\t").append(entry.getKey().equals(LinkHelper.NO_PARENT) ? "NO PARENT" : entry.getKey().getSimpleName()).append("\n")
            .append(entry.getValue().toString());
        }
        return builder.toString();
    }
}
