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

package org.ovirt.engine.api.v3.helpers;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.restapi.invocation.Current;
import org.ovirt.engine.api.restapi.invocation.CurrentManager;
import org.ovirt.engine.api.restapi.invocation.VersionSource;
import org.ovirt.engine.api.v3.types.V3Link;
import org.ovirt.engine.api.v3.types.V3VM;

public class V3VmHelper {

    public static Response addDisksLinkToResponse(Response response) {
        if (response.getEntity() instanceof V3VM) {
            V3VM vm = (V3VM) response.getEntity();
            addDisksLink(vm);
        }
        return response;
    }

    public static V3VM addDisksLink(V3VM vm) {
        if (vm == null) {
            return null;
        }

        Current current = CurrentManager.get();
        StringBuilder buffer = new StringBuilder();
        buffer.append(current.getPrefix());
        if (current.getVersionSource() == VersionSource.URL) {
            buffer.append("/v");
            buffer.append(current.getVersion());
        }
        buffer.append(current.getPath());
        if (!current.getPath().contains(vm.getId())) {
            buffer.append(vm.getId());
        }
        buffer.append("/disks");
        String href = buffer.toString();

        // Make the link:
        V3Link link = new V3Link();
        link.setRel("disks");
        link.setHref(href);
        vm.getLinks().add(link);

        return vm;
    }
}
