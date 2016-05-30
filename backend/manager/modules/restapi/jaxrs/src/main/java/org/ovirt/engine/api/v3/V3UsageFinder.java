/*
Copyright (c) 2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.v3;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.restapi.invocation.Current;
import org.ovirt.engine.api.restapi.invocation.CurrentManager;
import org.ovirt.engine.api.restapi.invocation.VersionSource;
import org.ovirt.engine.api.restapi.rsdl.RsdlLoader;
import org.ovirt.engine.api.v3.types.V3DetailedLink;
import org.ovirt.engine.api.v3.types.V3RSDL;
import org.ovirt.engine.api.v3.types.V3UsageMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class V3UsageFinder {
    private static final Logger log = LoggerFactory.getLogger(V3UsageFinder.class);

    private static final String RESPONSE =
            "Request syntactically incorrect. See the description below for the correct usage:";
    private static final String UUID_REGEX =
            "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}";

    private static V3RSDL rsdl;

    static {
        try {
            rsdl = RsdlLoader.loadRsdl(V3RSDL.class);
        }
        catch (IOException exception) {
            log.error("Can't load RSDL for version 3 of the API.", exception);
        }
    }

    public V3UsageMessage getUsageMessage(UriInfo uriInfo, Request request) throws ClassNotFoundException, IOException {
        V3UsageMessage usage = new V3UsageMessage();
        usage.setMessage(RESPONSE);
        usage.setDetailedLink(findUsage(uriInfo, request.getMethod()));
        return usage;
    }

    private V3DetailedLink findUsage(UriInfo uriInfo, String httpMethod) {
        V3DetailedLink link = null;
        for (V3DetailedLink currentLink : rsdl.getLinks().getLinks()) {
            if (isMatch(currentLink, uriInfo, httpMethod)) {
                link = currentLink;
                break;
            }
        }
        assert link != null : "Corresponding link not found (this should not happen)";
        return link;
    }

    private boolean isMatch(V3DetailedLink link, UriInfo uriInfo, String httpMethod) {
        int baseUriLength = uriInfo.getBaseUri().getPath().length();
        // e.g: [vms, {vm:id}, start]
        Current current = CurrentManager.get();
        int charsToTruncate = current.getVersionSource() == VersionSource.URL ? 0 : current.getVersion().length() + 2;
        String[] linkPathSegments = link.getHref().substring(baseUriLength-charsToTruncate).split("/");
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
        return (rel.equals("get") && httpMethod.equals("GET"))
                || (rel.equals("add") && httpMethod.equals("POST"))
                || (rel.equals("update") && httpMethod.equals("PUT"))
                || httpMethod.equals("POST") ? true : false;
    }

    private boolean isUUID(String uriPathSegment) {
        return Pattern.matches(UUID_REGEX, uriPathSegment);
    }
}
