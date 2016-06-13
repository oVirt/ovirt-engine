package org.ovirt.engine.api.rsdl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.ovirt.engine.api.resource.SystemResource;

public class ServiceTree {

    private static boolean SUB_RESOURCES = true;
    private static boolean ACTIONS = false;

    private static ServiceTreeNode tree = buildTree();

    private static ServiceTreeNode buildTree() {
        return buildNode(SystemResource.class, "");
    }

    public static ServiceTreeNode getTree() {
        return tree;
    }

    /**
     * Build the API tree
     */
    private static ServiceTreeNode buildNode(Class<?> resource, String path) {
        return new ServiceTreeNode.Builder()
                .name(resource.getSimpleName())
                .path(path)
                .subCollections(getSubServices(resource))
                .actions(getActions(resource))
                .build();
    }

    /**
     * Get all methods in the provided 'Resource', which lead to a sub-resource.
     * For example, for VmResource this method should return getDisksResource(),
     * getCdRomsResource(), etc.
     */
    public static List<ServiceTreeNode> getSubServices(Class<?> resource) {
        List<ServiceTreeNode> resources = new ArrayList<>();
        for (Method method : getMethods(resource, SUB_RESOURCES)) {
            Path path = method.getAnnotation(Path.class);
            resources.add(buildNode(method.getReturnType(), path.value()));
        }
        return resources;
    }

    private static List<String> getActions(Class<?> resource) {
        List<String> resourceMethods = new ArrayList<>();
        for (Method method : getMethods(resource, ACTIONS)) {
            resourceMethods.add(method.getName());
        }
        return resourceMethods;
    }

    public static List<Method> getMethods(Class<?> clazz, boolean subService) {
        List<Method> methods = new ArrayList<>();
        while (clazz != null) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (isBlacklist(method)) {
                    continue;
                }
                //XOR achieves the required behavior
                if (isSubService(method) ^ !subService) {
                    methods.add(method);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return methods;
    }

    /**
     * Returns true if the provided method should be ignored
     * in the process of building the API tree.
     */
    private static boolean isBlacklist(Method method) {
        return method.getName().equals("getActionService");
    }

    /**
     * This method returns 'true' if the provided method's return-
     * value is a 'sub-resource'. For example, true should be
     * VmResource.getCdRomsResource(), but false should be returned
     * for VmResource.start().
     */
    private static boolean isSubService(Method method) {
        /*
         * Methods which return sub-resources are always expected to
         * be annotated with @Path, but never with any of the HTTP
         * annotations: @POST, @PUT, @DELETE, @GET.
         */
        return hasPathAnnotation(method) && !hasHttpAnnotation(method);
    }

    private static boolean hasPathAnnotation(Method method) {
        return method.isAnnotationPresent(Path.class);
    }

    private static boolean hasHttpAnnotation(Method method) {
        return method.isAnnotationPresent(POST.class)
                || method.isAnnotationPresent(PUT.class)
                || method.isAnnotationPresent(DELETE.class);
    }
}
