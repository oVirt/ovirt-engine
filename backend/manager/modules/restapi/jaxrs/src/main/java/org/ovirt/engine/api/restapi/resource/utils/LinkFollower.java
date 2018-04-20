package org.ovirt.engine.api.restapi.resource.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.GET;

import org.ovirt.engine.api.model.ActionableResource;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.BaseResources;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.restapi.resource.BaseBackendResource;
import org.ovirt.engine.api.restapi.resource.ResourceLocator;
import org.ovirt.engine.api.utils.EntityHelper;
import org.ovirt.engine.api.utils.ReflectionHelper;

public class LinkFollower {

    private ResourceLocator resourceLocator;

    public LinkFollower() {
        this.resourceLocator =  ResourceLocator.getInstance();
    }

    public LinkFollower(ResourceLocator resourceLocator) {
        this.resourceLocator =  resourceLocator;
    }

    /**
     * Follow all links in the links-tree. Each node in the tree (except ROOT) is a link, and
     * links should be followed in a pre-order DFS (Depth-First-Search) manner. For example,
     * in the tree:
     *
     * ROOT
     *   nics
     *       x
     *       y
     *   disk_attachments
     *
     * The order of following will be: nics, x, y, disk_attachments. The order is important
     * because in the above example, we can't fetch 'x's of nics before we have the nics themselves.
     */
    public void followLinks(ActionableResource entity, LinksTreeNode linksTree) {
        //This method is the entry-point of the link follower, so it is assumed that
        //the tree-node provided is the root of the links tree.
        assert linksTree.isRoot();
        for (LinksTreeNode branch : linksTree.getChildren()) {
            //follow all links in this branch of the tree.
            followLinks(Arrays.asList(entity), branch);
        }
    }

    /**
     * Links are received in a flat structure, arrange them in a tree structure.
     * e.g: GET .../api/vms?follow="disk_attachments,nics.x,nics.y" becomes the tree:
     *
     * ROOT
     *   disk_attachments
     *   nics
     *       x
     *       y
     *
     */
    public LinksTreeNode createLinksTree(Class<? extends ActionableResource> root, String follow) {
        LinksTreeNode linksTree = new LinksTreeNode(root.getSimpleName().toLowerCase());
        String[] links = follow.split(",");
        for (String link : links) {
            addLink(linksTree, link);
        }
        return linksTree;
    }

    /**
     * Add a link to the tree
     */
    private void addLink(LinksTreeNode linksTree, String link) {
        //link segments are separated by '.' (e.g: (disk_attachements.template)
        String[] linkSegments = link.split("\\.");
        for (String segment : linkSegments) {
            linksTree = updateTree(linksTree, segment);
        }
    }


    /**
     * If the tree has a child-node with the provided, return it.
     * Otherwise create such a node, add it to the tree and return it.
     */
    private LinksTreeNode updateTree(LinksTreeNode linksTree, String segment) {
        Optional<LinksTreeNode> childNode = linksTree.getChild(segment);
        if (!childNode.isPresent()) {
            linksTree.addChild(segment);
        }
        return linksTree.getChild(segment).get();
    }

    /**
     * For all the provided business-entities (whether single (e.g: Nic) or collection (e.g: Nics) types)
     * follow all links in the provided links-tree, recursively.
     *
     * For example, for a list of three 'Nics' entities:
     *
     *   [
     *    nics1: [nic11, nic12]
     *    nics2: [nic21, nic22]
     *    nics3: [nic31, nic32]
     *   ]
     *
     * and the tree:
     *
     *     vnicprofiles
     *         networkfilter
     *         qos
     *
     * This method will fetch the vnic-profiles of nic11, nic12, nic13, nic21, nic22, nic23,
     * set them in the respected Nic objects, and store them in a list.
     *
     * Then the method will recursively run on the list of vnic-profiles with the sub-tree
     *
     *    networkfilter
     *
     * and again with the sub-tree
     *
     *    qos
     */
    private void followLinks(List<ActionableResource> entities, LinksTreeNode node) {
        for (ActionableResource entity : entities) {
            followLink(entity, node);
        }
        node.setFollowed(true);
    }

    /**
     * For the provided business entity (whether single (e.g: Nic) or collection (e.g: Nics) type,
     * follow all links in the provided links-tree, recursively.
     */
    private void followLink(ActionableResource entity, LinksTreeNode node) {
        List<ActionableResource> nextStepEntities = new LinkedList<>();
        if (EntityHelper.isCollection(entity)) {
            nextStepEntities.addAll(fetchData((BaseResources)entity, node));
        } else {
            nextStepEntities.add(fetchData((BaseResource)entity, node));
        }
        for (LinksTreeNode child : node.getChildren()) {
            followLinks(nextStepEntities, child);
        }
    }

    /**
     * For the provided collection-type entity (e.g: Nics), follow the link represented by the
     * provided node. Do not follow child-links of this node.
     *
     * For example, for a Nics object:
     *
     *   nics [nic1, nic2]
     *
     * and the tree:
     *
     *   vnicprofiles
     *        networkfilter
     *        qos
     *
     * This method fetches all vnicprofiles of nic1, nic2, and sets them in these Nic objects.
     * The method will return the fetched vnic-profile objects. The child links networkfilter, qos
     * are purposely ignored.
     */
    @SuppressWarnings("unchecked")
    private List<ActionableResource> fetchData(BaseResources collectionEntity, LinksTreeNode node) {
        List<ActionableResource> results = new LinkedList<>();
        Method collectionGetter = EntityHelper.getCollectionGetter(collectionEntity);
        try {
            //get the actual list of entities in the collection-type, e.g for Nics get List<Nic>
            //(by invoking nics.getNics() using reflection)
            List<BaseResource> entities = (List<BaseResource>)collectionGetter.invoke(collectionEntity);
            //for each entity in the list, fetch link data.
            for (BaseResource entity : entities) {
                results.add(fetchData(entity, node));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Problem following '" + node.getElement() + "' link in " + collectionEntity.getClass().getSimpleName() + " entity.", e);
        }
        return results;
    }

    /**
     * For the provided single-entity type (e.g: Nic), follow the link represented by the
     * provided node. Do not follow child-links of this node.
     *
     * For example, for a Nic object and the tree:
     *
     *   vnicprofiles
     *        networkfilter
     *        qos
     *
     * This method fetches all vnicprofiles of this nic object and sets them in it. The method
     * then returns the fetched vnic-profiles. The child links networkfilter, qos are purposely ignored.
     */
    private ActionableResource fetchData(BaseResource entity, LinksTreeNode link) {
        try {
            String element = underscoreToCamelCase(link.getElement());
            if (link.isFollowed()) {
                Method getter = ReflectionHelper.getGetter(entity, element);
                return (ActionableResource)getter.invoke(entity);
            } else {
                String href = getHref((BaseResource)entity, link.getElement());
                ActionableResource result = fetch(href);
                Method setter = ReflectionHelper.getSetter(entity, element);
                setter.invoke(entity, result);
                return result;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Problem fetching '" + link.getElement() +
                    "' from " + entity.getClass().getSimpleName(), e);
        }
    }

    private String underscoreToCamelCase(String element) {
        StringBuilder builder = new StringBuilder();
        for (String s : element.split("_")) {
            builder.append(Character.toUpperCase(s.charAt(0)));
            if (s.length() > 1) {
                builder.append(s.substring(1, s.length()).toLowerCase());
            }
        }
        return builder.toString();
    }

    /**
     * Returns the 'href' string - the actual link that should be followed.
     * There are two possible locations for the href string:
     * 1) For sub-collections, (e.g: disk_attachments of a VM) the href will
     *    appear under the list of links (vm.getLinks()...)
     * 2) For referenced single entities (e.g Template of a VM) the href will appear
     *    in the member itself (vm.getTemplate().getHref()).
     */
    private String getHref(BaseResource entity, String link) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Optional<Link> optional = entity.getLinks().stream().filter(x -> x.getRel().equals(toRelFormat(link))).findFirst();
        if (optional.isPresent()) {
            return optional.get().getHref();
        } else {//assume this is not a sub-collection, since it wasn't found among links.
            Method getter = ReflectionHelper.getGetter(entity, underscoreToCamelCase(link));
            BaseResource member = (BaseResource)getter.invoke(entity);
            return member.getHref();
        }
    }

    /**
     * This scope of this method is 'protected' for testing purposes.
     */
    protected ActionableResource fetch(String href) {
        try {
            BaseBackendResource resource = resourceLocator.locateResource(href);
            //need to invoke the method in the resource annotated with @GET
            //(it could be get() or list())
            for (Method method : resource.getClass().getMethods()) {
                if (method.isAnnotationPresent(GET.class)) {
                    return (ActionableResource) method.invoke(resource);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Problem following link: " + href, e);
        }
        return null;
    }

    /**
     * The value of 'rel' inside the Link object is a string of lowercase letters with no spaces,
     * e.g: diskattachments. This method converts a string into this format.
     */
    private String toRelFormat(String link) {
        return link.toLowerCase().replaceAll("_", "");
    }

}
