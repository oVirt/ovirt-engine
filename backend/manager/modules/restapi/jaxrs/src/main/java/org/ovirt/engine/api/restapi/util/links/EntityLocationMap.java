package org.ovirt.engine.api.restapi.util.links;

import java.util.HashMap;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.resource.SystemResource;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.api.rsdl.ServiceTree;
import org.ovirt.engine.api.rsdl.ServiceTreeNode;

/**
 * A #Map sub-class which holds location meta-data by API entity.
 * For efficient access each entity contains its metadata objects in a map,
 * with parent-type as key. For example, the following is an entry in
 * EntityLocationMap for the entity 'Group':
 *
 * -------------------------------------------------
 * Group:
 *   NO_PARENT:
 *      parent: NO_PARENT
 *      resource_single    : GroupResource
 *      resource_collection: GroupsResource
 *   Domain:
 *      parent: Domain
 *      resource_single    : DomainGroupResource
 *      resource_collection: DomainGroupsResource
 * -------------------------------------------------
 *
 * Out of which the following are entries in LocationByParentMap:
 *
 *--------------------------------------------------
 * NO_PARENT:
 *    parent: NO_PARENT
 *    resource_single    : GroupResource
 *    resource_collection: GroupsResource
 *--------------------------------------------------
 *--------------------------------------------------
 * Domain:
 *    parent: Domain
 *    resource_single    : DomainGroupResource
 *    resource_collection: DomainGroupsResource
 *--------------------------------------------------
 *
 * Out of which the following are instances of ApiLocationMetadata:
 *
 *--------------------------------------------------
 * parent: NO_PARENT
 * resource_single    : GroupResource
 * resource_collection: GroupsResource
 *--------------------------------------------------
 *--------------------------------------------------
 * parent: Domain
 * resource_single    : DomainGroupResource
 * resource_collection: DomainGroupsResource
 *--------------------------------------------------
 */
public class EntityLocationMap extends HashMap<Class<?>, LocationByParentMap> {
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Entry<Class<?>, LocationByParentMap> entry : this.entrySet()) {
            builder.append(entry.getKey().getSimpleName()).append("\n")
            .append(entry.getValue().toString()).append("\n");
        }
        return builder.toString();
    }

    /**
     * Recursively traverses the API tree and creates the
     * entity-location-map from the information in it.
     */
    public static EntityLocationMap createLinksMap() {
        EntityLocationMap map = new EntityLocationMap();
        for (ServiceTreeNode node : ServiceTree.getTree().getSubServices()) {
            handleNode(node, map);
        }
        return map;
    }

    private static void handleNode(ServiceTreeNode node, EntityLocationMap map) {
        if (!node.isCollection()) { // single entity node, e.g: VmResource
            Class<?> type = node.getType();
            if (type != null) { // type is null in a few exceptional cases
                if (!map.containsKey(type)) {
                    map.put(type, new LocationByParentMap());
                }
                LocationByParentMap innerMap = map.get(type);
                Class<?> singleEntityResource = node.getResourceClass();
                Class<?> collectionResource = node.getParent().getResourceClass();
                @SuppressWarnings("unchecked")
                Class<? extends BaseResource> parentType = node.getParent().getParent().getResourceClass().equals(SystemResource.class) ? LinkHelper.NO_PARENT
                        : (Class<? extends BaseResource>)(node.getParent().getParent().getType());
                innerMap.add(singleEntityResource, collectionResource, parentType);
            }
        }
        for (ServiceTreeNode subServiceNode : node.getSubServices()) {
            handleNode(subServiceNode, map);
        }
     }
}
