package org.ovirt.engine.api.rsdl;

import java.util.Iterator;
import java.util.List;

/**
 * This class is an iterator which 'crawls' along the API ServiceTree
 * according to a 'path', which is provided as a list of strings, and
 * at each step returns the appropriate 'Resource' class.
 *
 * For example, for: [vms, 58cfb470-03b9-01d0-03b9-0000000001e7, nics]
 *
 * 1) next() returns BackendVmsResouce
 * 2) next() returns BackendVmResource (initialized with
 *           58cfb470-03b9-01d0-03b9-0000000001e7 as the Vm ID)
 * 3) next() returns BackendVmNicsResource (initialized with
 *           58cfb470-03b9-01d0-03b9-0000000001e7 as the Vm ID)
 */
public class ServiceTreeCrawler implements Iterator<ServiceTreeNode>{

    private Iterator<String> pathIterator;
    private ServiceTreeNode node;
    private String currentPathSegment;

    public ServiceTreeCrawler(List<String> pathSegments) {
        super();
        node = ServiceTree.getTree();
        pathIterator = pathSegments.iterator();
    }

    @Override
    public boolean hasNext() {
        return pathIterator.hasNext();
    }

    public String getCurrentPathSegment() {
        return currentPathSegment;
    }

    @Override
    public ServiceTreeNode next() {
        currentPathSegment = pathIterator.next();
        if (isID(currentPathSegment, node)) {
            node = node.getSubService("{id}");
        } else if (node.containsSubService(currentPathSegment)) {
            node = node.getSubService(currentPathSegment);
        } else {
            throw new IllegalArgumentException("Segment '" + currentPathSegment + "' of path does not exist");
        }
        return node;
    }

    private boolean isID(String segment, ServiceTreeNode node) {
        //the provided string is assumed to be an ID if the node has no action or sub-service by this name.
        return !actionExists(node, segment) && !node.containsSubService(segment);
    }

    private boolean actionExists(ServiceTreeNode node, String actionName) {
        return node.getActions().stream().anyMatch(action -> action.toLowerCase().equals(actionName));
    }

}
