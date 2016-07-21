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
import org.ovirt.engine.api.restapi.invocation.VersionSource;
import org.ovirt.engine.api.v3.types.V3Link;

public class V3LinkHelper {
    /**
     * Creates a new string builder and populates it with the prefix that should be used to populate the "href"
     * attribute of links, taking into account the version of the API that has been requested and how it has been
     * requested (with the "Version" header or with the URL suffix).
     */
    public static StringBuilder linkBuffer() {
        Current current = CurrentManager.get();
        StringBuilder builder = new StringBuilder();
        builder.append(current.getPrefix());
        if (current.getVersionSource() == VersionSource.URL) {
            builder.append("/v");
            builder.append(current.getVersion());
        }
        return builder;
    }

    /**
     * Calculates an "href" using the given segments. For example, if the segments are {@code vms}, {@code 123} and
     * {@code disks} the result will be {@code <link rel="disks" href="/ovirt-engine/api/v3/vms/123/disks"/>}.
     */
    public static String linkHref(String... segments) {
        StringBuilder href = linkBuffer();
        for (String segment : segments) {
            href.append("/");
            href.append(segment);
        }
        return href.toString();
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
