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

import java.util.List;

import org.ovirt.engine.api.restapi.invocation.Current;
import org.ovirt.engine.api.restapi.invocation.CurrentManager;
import org.ovirt.engine.api.v3.types.V3Link;

public class V3LinkHelper {
    /**
     * Calculates an "href" using the given segments. For example, if the segments are {@code vms}, {@code 123} and
     * {@code disks} the result will be {@code <link rel="disks" href="/ovirt-engine/api/v3/vms/123/disks"/>}.
     */
    public static String linkHref(String... segments) {
        Current current = CurrentManager.get();
        return current.getAbsolutePath(segments);
    }

    /**
     * Adds to the given list a link composed from the given {@code rel} and an {@code href} calculated using the given
     * segments. For example, if the rel is {@code import} and the segments are {@code vms}, {@code 123} and
     * {@code disks} the link added will be {@code <link rel="disks" href="/ovirt-engine/api/v3/vms/123/disks"/>}.
     */
    public static void addLink(List<V3Link> links, String rel, String... segments) {
        V3Link link = new V3Link();
        link.setHref(linkHref(segments));
        link.setRel(rel);
        links.add(link);
    }
}
