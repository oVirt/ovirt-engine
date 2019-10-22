/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.Path;

import org.ovirt.engine.api.model.ActionableResource;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.BaseResources;
import org.ovirt.engine.api.resource.BookmarksResource;
import org.ovirt.engine.api.resource.SystemResource;
import org.ovirt.engine.api.restapi.invocation.Current;
import org.ovirt.engine.api.restapi.invocation.CurrentManager;
import org.ovirt.engine.api.restapi.util.links.ApiLocationMetadata;
import org.ovirt.engine.api.restapi.util.links.EntityLocationMap;
import org.ovirt.engine.api.restapi.util.links.LocationByParentMap;
import org.ovirt.engine.api.restapi.util.links.PathKey;
import org.ovirt.engine.api.utils.EntityHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Contains a static addLinks() method which constructs any href attributes
 * and action links required by a representation.
 *
 * The information used to build links is obtained from the annotations on
 * the API definition interfaces.

 * For example, a link to a VM is the combination of the @Path attribute on
 * VmsResource and the VM id - i.e. '/restapi-definition/vms/{vm_id}'
 *
 * Resource collections which are a sub-resource of a parent collection
 * present a more difficult challenge. For example, the link to a VM tag
 * is the combination of the @Path attribute on VmsResource, the VM id,
 * the @Path attribute on VmResource.getTagsResource() and the tag id -
 * i.e. '/restapi-definition/vms/{vm_id}/tags/{tag_id}'
 * In most cases the parent type may be computed, but in exceptional
 * cases there are a number of equally valid candidates. Disambiguation
 * is achieved via an explicit suggestedParentType parameter.
 *
 * To be able to do this we need, for each collection, the collection type
 * (e.g. AssignedTagsResource), the resource type (e.g. AssignedTagResource)
 * and the parent model type (e.g. VM). The TYPES map below is populated
 * with this information for every resource type.
 */
public class LinkHelper {

    private static final Logger log = LoggerFactory.getLogger(LinkHelper.class);

    /**
     * A constant representing the pseudo-parent of a top-level collection
     */
    public static final Class<? extends BaseResource> NO_PARENT = BaseResource.class;

    /**
     * A map describing every possible collection
     */
    private static EntityLocationMap TYPES = EntityLocationMap.createLinksMap();

    /**
     * A map for caching relevant resource methods for each class
     */
    private static ConcurrentMap<Class<?>, List<Method>> methodCache = new ConcurrentHashMap<>();

    /**
     * A map for caching values of @Path annotations
     */
    private static ConcurrentMap<PathKey, String> pathCache = new ConcurrentHashMap<>();

    /**
     * Obtain the relative path to a top-level collection
     *
     * The path is the value of the {@link Path} annotation on resource locator method of the root resource that
     * returns a reference to this class of resource. For example, if the class is {@link BookmarksResource} then
     * returned value should be the value of the {@link Path} annotation on the
     * {@link SystemResource#getBookmarksResource()} method.
     *
     * @param service the collection resource type
     * @return the relative path to the collection
     */
    private static String getRelativePath(Class<?> service) {
        return getRelativePath(service, SystemResource.class);
    }

    /**
     * Obtain the relative path to a sub-collection.
     *
     * The path is obtained from the @Path annotation on the method on @parent
     * which returns an instance of @clz.
     *
     * @param service    the collection resource type (e.g. AssignedTagsResource)
     * @param parentService the parent resource type (e.g. VmResource)
     * @return       the relative path to the collection
     */
    private static String getRelativePath(Class<?> service, Class<?> parentService) {
        PathKey key = new PathKey(service, parentService);
        String path = pathCache.get(key);
        if (path!=null) {
            return path;
        } else {
            for (Method method : parentService.getMethods()) {
                if (method.getName().startsWith("get") && method.getReturnType() == service) {
                    Path pathAnnotation = method.getAnnotation(Path.class);
                    if (pathAnnotation != null) {
                        pathCache.put(key, pathAnnotation.value());
                        return pathAnnotation.value();
                    }
                }
            }
        }
        log.warn("Can't find relative path for class \"" + service.getName() + "\", will return null");
        return null;
    }

    /**
     * Obtain a set of inline BaseResource objects from @obj
     *
     * i.e. return the value of any properties on @obj which are a
     * sub-type of BaseResource
     *
     * @param obj the object to check
     * @return    a list of any inline BaseResource objects
     */
    @SuppressWarnings("unchecked")
    private static List<BaseResource> getInlineResources(Object obj) {
        ArrayList<BaseResource> ret = new ArrayList<>();

        for (Method method : getRelevantMethods(obj.getClass())) {
            // We need to recursively scan everything that is in the model package, as there may be references
            // to resources deeply nested:
            Object inline = null;
            try {
                inline = method.invoke(obj);
            } catch (Exception e) {
                // invocation target exception should not occur on simple getter
            }
            if (inline != null) {
                if (inline instanceof BaseResource) {
                    ret.add((BaseResource) inline);
                } else if (inline instanceof BaseResources) {
                    BaseResources entities = (BaseResources)inline;
                    Method getter = EntityHelper.getCollectionGetter(entities);
                    try {
                        List<BaseResource> entitiesList = (List<BaseResource>) getter.invoke(entities);
                        for (BaseResource entity : entitiesList) {
                            ret.add(entity);
                        }
                    } catch (Exception e) {
                        log.error(
                            "Error invoking method '{}' on class '{}'.",
                            method.getName(),
                            entities.getClass().getSimpleName()
                        );
                        log.error("Exception", e);
                    }
                } else {
                    ret.addAll(getInlineResources(inline));
                }
            }
        }
        return ret;
    }

    /**
     * Gets all the relevant possible inline resources methods of a class. Data is cached for future use.
     * @param clz
     *            The class to examine
     * @return The list of relevant methods.
     */
    private static List<Method> getRelevantMethods(Class<?> clz) {
        List<Method> methods = methodCache.get(clz);
        if (methods == null) {
            methods = new ArrayList<>();
            for (Method method : clz.getMethods()) {
                if (method.getName().startsWith("get")) {
                    if (method.getReturnType().getPackage() == BaseResource.class.getPackage()) {
                        methods.add(method);
                    }
                }
            }
            methodCache.put(clz, methods);
        }

        return methods;
    }
    /**
     * Unset the property on @model of type @type
     *
     * @param model the object with the property to unset
     * @param type  the type of the property
     */
    private static void unsetInlineResource(BaseResource model, Class<?> type) {
        for (Method method : model.getClass().getMethods()) {
            if (method.getName().startsWith("set")) {
                try {
                    if (type.isAssignableFrom(method.getParameterTypes()[0])) {
                        method.invoke(model, new Object[]{null});
                        return;
                    }
                } catch (Exception e) {
                    // invocation target exception should not occur on simple setter
                }
            }
        }
    }

    /**
     * Return any parent object set on @model
     *
     * i.e. return the value of any bean property whose type matches @parentType
     *
     * @param model      object to check
     * @param parentType the type of the parent
     * @return           the parent object, or null if not set
     */
    private static <R extends BaseResource> BaseResource getParent(R model, Class<?> parentType) {
        for (Method method : getRelevantMethods(model.getClass())) {
            try {
                Object potentialParent = method.invoke(model);
                if (potentialParent != null && parentType.isAssignableFrom(potentialParent.getClass())) {
                    return (BaseResource)potentialParent;
                }
            } catch (Exception e) {
                log.error("Error invoking method when adding links to an API entity", e);
                continue;
            }
        }
        return null;
    }

    /**
     * Lookup the #Collection instance which represents this object
     *
     * i.e. for a VM tag (i.e. a Tag object which its VM property set)
     * return the #Collection instance which encapsulates AssignedTagResource,
     * AssignedTagsResource and VM.
     *
     * @param model the object to query for
     * @return      the #Collection instance representing the object's collection
     */
    private static ApiLocationMetadata getCollection(BaseResource model) {
        return getLocationMetadata(model, null);
    }

    /**
     * Lookup the #Collection instance which represents this object
     *
     * i.e. for a VM tag (i.e. a Tag object which its VM property set)
     * return the #Collection instance which encapsulates AssignedTagResource,
     * AssignedTagsResource and VM.
     *
     * @param model                the object to query for
     * @param suggestedParentType  the suggested parent type
     * @return                     the #Collection instance representing the object's collection
     */
    private static ApiLocationMetadata getLocationMetadata(BaseResource model, Class<? extends BaseResource> suggestedParentType) {
        LocationByParentMap locationByParentMap = TYPES.get(model.getClass());

        if (locationByParentMap == null) {
            return null;
        }

        if (suggestedParentType != null && locationByParentMap.containsKey(suggestedParentType)) {
            return locationByParentMap.get(suggestedParentType);
        }

        for (Entry<Class<? extends BaseResource>, ApiLocationMetadata> entry : locationByParentMap.entrySet()) {
            if (!entry.getKey().equals(NO_PARENT) &&
                getParent(model, entry.getKey()) != null) {
                return entry.getValue();
            }
        }

        return locationByParentMap.get(NO_PARENT);
    }

    private static ApiLocationMetadata getLocationMetadata(BaseResource model) {
        return getLocationMetadata(model, null);
    }
    /**
     * Computes the path for the given object. For example, for a tag of a virtual machine returns the path
     * {@code /ovirt-engine/api/vms/{vm:id}/tags/{tag:id}}.
     *
     * @param object the object
     * @return the path for the object, or {@code null} if the path can't be determined
     */
    public static String getPath(BaseResource object) {
        return getPath(object, null);
    }

    /**
     * Computes the path for the given object, using the given type to find out what is the type of the parent.
     *
     * @param entity the object
     * @param suggestedParentType the suggested parent type
     * @return the path for the object, or {@code null} if the path can't be determined
     */
    public static String getPath(BaseResource entity, Class<? extends BaseResource> suggestedParentType) {
        ApiLocationMetadata locationMetadata = getLocationMetadata(entity, suggestedParentType);
        if (locationMetadata != null) {
            if (locationMetadata.getParentType() != NO_PARENT) {
                return getPathConsideringParent(entity, locationMetadata);
            } else {
                return getPathWithoutParent(entity, locationMetadata);
            }
        } else {
            return null;
        }
    }

    private static String getPathWithoutParent(BaseResource entity, ApiLocationMetadata locationMetadata) {
        Current current = CurrentManager.get();
        return current.getAbsolutePath(
            getRelativePath(locationMetadata.getCollectionServiceClass()),
            entity.getId()
        );
    }

    private static String getPathConsideringParent(BaseResource entity, ApiLocationMetadata locationMetadata) {
        BaseResource parent = getParent(entity, locationMetadata.getParentType());
        if (parent == null) {
            return null;
        }
        ApiLocationMetadata parentLocationMetadata = getLocationMetadata(parent);
        if (parentLocationMetadata == null) {
            return null;
        }
        String parentPath = getPath(parent);
        if (parentPath == null) {
            return null;
        }
        String relativePath = getRelativePath(locationMetadata.getCollectionServiceClass(), parentLocationMetadata.getEntityServiceClass());
        return String.join("/", parentPath, relativePath, entity.getId());
    }

    /**
     * Construct the set of action links for an object
     *
     * @param model   the object
     * @param suggestedParentType  the suggested parent type
     */
    private static void setActions(BaseResource model, String path) {
        ApiLocationMetadata collection = getCollection(model);
        if (collection != null) {
            ActionsBuilder actionsBuilder = new ActionsBuilder(path, collection.getEntityServiceClass());
            model.setActions(actionsBuilder.build());
        }
    }

    /**
     * Adds the set of action links for an object
     *
     * @param model the object to add actions to
     * @param collection the object to get implemented methods from
     */
    public static <R extends ActionableResource> void addActions(R model, Object collection) {
        Current current = CurrentManager.get();
        String base = current.getPrefix() + current.getPath();
        if (base != null) {
            ActionsBuilder actionsBuilder = new ActionsBuilder(base, model.getClass(), collection.getClass());
            model.setActions(actionsBuilder.build());
        }
    }

    /**
     * Set the href attribute on the object (and its inline objects)
     * and construct its set of action links
     *
     * @param model the object
     * @return the object, with href attributes and action links
     */
    public static <R extends BaseResource> R addLinks(R model) {
        return addLinks(model, null);
    }

    public static <R extends BaseResource> R addLinks(R model, Class<? extends BaseResource> suggestedParentType) {
        return addLinks(model, suggestedParentType, true);
    }

    public static <R extends BaseResource> R addLinks(R model, Class<? extends BaseResource> suggestedParentType, boolean addActions) {
        String path = getPath(model, suggestedParentType);
        if (path != null) {
            model.setHref(path);
            if (addActions) {
                setActions(model, path);
            }
        }
        for (BaseResource inline : getInlineResources(model)) {
            if (inline.getId() != null) {
                path = getPath(inline, null);
                if (path!=null) {
                    inline.setHref(path);
                }
            }
            for (BaseResource grandParent : getInlineResources(inline)) {
                unsetInlineResource(inline, grandParent.getClass());
            }
        }
        return model;
    }

}
