package org.ovirt.engine.api.restapi.resource.validation;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.model.DetailedLink;
import org.ovirt.engine.api.model.RSDL;
import org.ovirt.engine.api.model.UsageMessage;
import org.ovirt.engine.api.restapi.resource.BackendApiResource;

public class UsageFinder {

    private static final String RESPONSE =
            "Request syntactically incorrect. See the description below for the correct usage:";
    private static final String UUID_REGEX =
            "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}";

    public UsageMessage getUsageMessage(Application application, UriInfo uriInfo, Request request)
            throws ClassNotFoundException, IOException {
        UsageMessage usage = new UsageMessage();
        usage.setMessage(RESPONSE);
        usage.setDetailedLink(findUsage(getRSDL(application), uriInfo, request.getMethod()));
        return usage;
    }

    private DetailedLink findUsage(RSDL rsdl, UriInfo uriInfo, String httpMethod) {
        DetailedLink link = null;
        for (DetailedLink currentLink : rsdl.getLinks().getLinks()) {
            if (isMatch(currentLink, uriInfo, httpMethod)) {
                link = currentLink;
                break;
            }
        }
        assert (link != null) : "Corresponding link not found (this should not happen)";
        return link;
    }

    private RSDL getRSDL(Application application) throws ClassNotFoundException, IOException {
        RSDL rsdl = null;
        for (Object obj : application.getSingletons()) {
            if (obj instanceof BackendApiResource) {
                BackendApiResource resource = (BackendApiResource) obj;
                rsdl = resource.getRSDL();
                break;
            }
        }
        assert (rsdl != null) : "Resource that generates RSDL, BackendApiResource, not found (this should never happen)";
        return rsdl;
    }

    private boolean isMatch(DetailedLink link, UriInfo uriInfo, String httpMethod) {
        int baseUriLength = uriInfo.getBaseUri().getPath().length();
        // e.g: [vms, {vm:id}, start]
        String[] linkPathSegments = link.getHref().substring(baseUriLength).split("/");
        // e.g: [vms, f26b0918-8e16-4915-b1c2-7f39e568de23, start]
        List<PathSegment> uriPathSegments = uriInfo.getPathSegments();
        return isMatchLength(linkPathSegments, uriPathSegments) &&
                isMatchPath(linkPathSegments, uriPathSegments) &&
                isMatchRel(link.getRel(), httpMethod);
    }

    private boolean isMatchLength(String[] linkPathSegments, List<PathSegment> uriPathSegments) {
        return linkPathSegments.length == uriPathSegments.size();
    }

    private boolean isMatchPath(String[] linkPathSegments, List<PathSegment> uriPathSegments) {
        for (int i = 0; i < linkPathSegments.length; i++) {
            String uriPathSegment = uriPathSegments.get(i).getPath();
            if (!isUUID(uriPathSegment) && !uriPathSegment.equals(linkPathSegments[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean isMatchRel(String rel, String httpMethod) {
        return ((rel.equals("get") && httpMethod.equals("GET")))
                || ((rel.equals("add") && httpMethod.equals("POST")))
                || ((rel.equals("update") && httpMethod.equals("PUT")))
                || httpMethod.equals("POST") ? true : false;
    }

    private boolean isUUID(String uriPathSegment) {
        return Pattern.matches(UUID_REGEX, uriPathSegment);
    }

}
