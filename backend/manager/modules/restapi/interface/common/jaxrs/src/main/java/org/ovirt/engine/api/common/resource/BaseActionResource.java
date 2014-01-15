/*
* Copyright (c) 2010 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*           http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.common.resource;

import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.utils.LinkHelper;


public class BaseActionResource<R extends BaseResource> implements ActionResource {

    private Action action;
    private R parent;

    public BaseActionResource(UriInfo uriInfo, Action action, R parent) {
        this.action = action;
        this.parent = parent;
        action.setId(UUID.randomUUID().toString());
        addLinks(uriInfo);
    }

    @Override
    public Response get() {
        return Response.ok(action).build();
    }

    @Override
    public Action getAction() {
        return action;
    }

    private String getPath(UriInfo uriInfo) {
        return combine(uriInfo.getBaseUri().getPath(), uriInfo.getPath());
    }

    private void addLink(String rel, String href) {
        Link link = new Link();
        link.setRel(rel);
        link.setHref(href);
        action.getLinks().add(link);
    }

    private void addLinks(UriInfo uriInfo) {
        action.setHref(UriBuilder.fromPath(getPath(uriInfo)).path(action.getId()).build().toString());

        String parentHref = LinkHelper.addLinks(uriInfo, parent).getHref();
        if (parentHref != null) {
            addLink("parent", parentHref);
        }
        addLink("replay", getPath(uriInfo));
    }

    private String combine(String head, String tail) {
        if (head.endsWith("/")) {
            head = head.substring(0, head.length() - 1);
        }
        if (tail.startsWith("/")) {
            tail = tail.substring(1);
        }
        return head + "/" + tail;
    }
}
