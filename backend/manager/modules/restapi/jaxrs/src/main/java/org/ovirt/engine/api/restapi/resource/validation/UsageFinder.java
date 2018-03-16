package org.ovirt.engine.api.restapi.resource.validation;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.restapi.invocation.Current;
import org.ovirt.engine.api.restapi.invocation.CurrentManager;
import org.ovirt.engine.api.rsdl.ServiceTree;
import org.ovirt.engine.api.rsdl.ServiceTreeNode;

public class UsageFinder {

    private static final String RESPONSE =
            "Request syntactically incorrect.";
    private static final String CAMEL_CASE_REGEX =
            "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])";

    public Fault getUsageMessage(UriInfo uriInfo, Request request)
            throws ClassNotFoundException, IOException {
        Fault fault = new Fault();
        fault.setReason(RESPONSE);
        fault.setDetail("For correct usage, see: " + getUsageLink(uriInfo, request.getMethod()));
        return fault;
    }

    private String getUsageLink(UriInfo uriInfo, String httpMethod) {
        List<PathSegment> pathSegments = uriInfo.getPathSegments();
        ServiceTreeNode node = ServiceTree.getTree();
        //step into the Service tree according to the URL.
        for (PathSegment pathSegment : pathSegments) {
            node = step(node, pathSegment);
        }
        //check whether the last step in the URL represent an 'action'.
        PathSegment lastPathSegment = pathSegments.get(pathSegments.size()-1);
        //Get the prefix of the link, with or without 's' appended to the
        //entity name, according to whether this action is on a single entity
        //or on the collection context, e.g:
        // .../apidoc#services/vm/methods/start    //action on *vm*
        // .../apidoc#services/vms/methods/add     //action on *vms*
        // .../apidoc#services/vm/methods/update   //action on *vm*
        // .../apidoc#services/vm/methods/remove   //action on *vm*
        String link = getLinkPrefix(node);
        if (isAction(node, lastPathSegment.getPath())) {
            link += camelCaseToUnderscore(getAction(node, lastPathSegment.getPath()));
        } else {
            link += getMethodName(httpMethod);
        }
        return link;
    }

    private String camelCaseToUnderscore(String path) {
        return path.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
    }

    private boolean isAction(ServiceTreeNode node, String path) {
        return node.containsAction(path);
    }

    /**
     * Gets all-lowercase action name, and if this action exists
     * in the node, returns it properly CamelCased, e.g:
     * For ClusterService node:
     * provide: "resetemulatedmachine"
     * receive: "resetEmulatedMachine"
     */
    private String getAction(ServiceTreeNode node, String path) {
        return node.getActions().stream()
                .filter(action -> action.toLowerCase().equals(path))
                .findFirst()
                .orElse(null);
    }

    private String getLinkPrefix(ServiceTreeNode node) {
        Current current = CurrentManager.get();
        StringBuilder buffer = new StringBuilder();
        buffer.append(current.getRoot());
        buffer.append("/ovirt-engine/apidoc#services/");
        buffer.append(processNodeName(node));
        buffer.append("/methods/");
        return buffer.toString();
    }

    private String processNodeName(ServiceTreeNode node) {
        String[] parts = node.getName().replaceAll("Resource$", "").split(CAMEL_CASE_REGEX);
        StringBuilder builder = new StringBuilder("");
        for (String part : parts) {
            builder.append(part.toLowerCase()).append("_");
        }
        String name = builder.toString();
        return name.substring(0, name.length() -1);
    }

    private String getMethodName(String httpMethod) {
        switch (httpMethod) {
        case "POST":
            return "add";
        case "PUT":
            return "update";
        case "GET":
            return "get";
        case "DELETE":
            return "remove";
        default:
            return ""; //shouldn't reach here.
        }
    }

    private ServiceTreeNode step(ServiceTreeNode node, PathSegment pathSegment) {
        if (isID(pathSegment.getPath(), node)) {
            return node.getSubService("{id}");
        } else {
            if (node.containsSubService(pathSegment.getPath())) {
                return node.getSubService(pathSegment.getPath());
            } else {
                return node;
            }
        }
    }

    private boolean isID(String segment, ServiceTreeNode node) {
        //the provided string is assumed to be an ID if the
        //node has no action or sub-service by this name.
        return getAction(node, segment) == null && !node.containsSubService(segment);
    }
}
