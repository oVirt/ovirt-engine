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

package org.ovirt.engine.api.restapi.rsdl;

import org.ovirt.engine.api.common.security.auth.SessionUtils;
import org.ovirt.engine.api.model.EntryPoint;
import org.ovirt.engine.api.model.Header;
import org.ovirt.engine.api.model.Headers;
import org.ovirt.engine.api.model.Request;

public class EntryPointBuilder {

    private EntryPoint entryPoint;

    public EntryPointBuilder() {
        this.entryPoint = new EntryPoint();
    }

    public EntryPoint build() {
        produceRequestHeaders();

        return this.entryPoint;
    }

    private void produceRequestHeaders() {
        this.entryPoint.setRequest(new Request());
        this.entryPoint.getRequest().setHeaders(new Headers());

        injectSessionTtlHeader(this.entryPoint.getRequest().getHeaders());
    }

    private void injectSessionTtlHeader(Headers headers) {
        String DESCRIPTION =
                "Idle session TTL. An interval value of zero\n" +
                        "or less indicates that the session should never timeout";

        if (headers != null) {
            Header header = new Header();
            header.setRequired(false);
            header.setName(SessionUtils.SESSION_TTL_HEADER_FIELD);
            header.setValue("minutes");
            header.setDescription(DESCRIPTION);

            headers.getHeaders().add(header);
        }
    }

    public EntryPointBuilder description(String description) {
        this.entryPoint.setDescription(description);
        return this;
    }

    public EntryPointBuilder href(String href) {
        this.entryPoint.setHref(href);
        return this;
    }

    public EntryPointBuilder name(String name) {
        this.entryPoint.setName(name);
        return this;
    }

    public EntryPointBuilder rel(String rel) {
        this.entryPoint.setRel(rel);
        return this;
    }
}
