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

package org.ovirt.engine.api.resource.aaa;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import org.ovirt.engine.api.model.Domain;
import org.jboss.resteasy.annotations.providers.jaxb.Formatted;
import org.ovirt.engine.api.resource.ApiMediaType;
import javax.ws.rs.Path;

@Produces( { ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML })
public interface DomainResource {

    @GET
    @Formatted
    public Domain get();

    @Path("users")
    public DomainUsersResource getDomainUsersResource();

    @Path("groups")
    public DomainGroupsResource getDomainGroupsResource();
}
