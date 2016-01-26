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

package org.ovirt.engine.api.rsdl;

import org.ovirt.engine.api.model.GeneralMetadata;
import org.ovirt.engine.api.model.Header;
import org.ovirt.engine.api.model.Headers;
import org.ovirt.engine.api.model.Request;

public class GeneralMetadataBuilder {

    public static final String SESSION_TTL_HEADER_FIELD = "Session-TTL";
    private GeneralMetadata generalMetadata;

    public GeneralMetadataBuilder() {
        this.generalMetadata = new GeneralMetadata();
    }

    public GeneralMetadata build() {
        produceRequestHeaders();

        return this.generalMetadata;
    }

    private void produceRequestHeaders() {
        this.generalMetadata.setRequest(new Request());
        this.generalMetadata.getRequest().setHeaders(new Headers());

        injectSessionTtlHeader(this.generalMetadata.getRequest().getHeaders());
    }

    private void injectSessionTtlHeader(Headers headers) {
        String DESCRIPTION =
                "Idle session TTL. An interval value of zero\n" +
                        "or less indicates that the session should never timeout";

        if (headers != null) {
            Header header = new Header();
            header.setRequired(false);
            header.setName(SESSION_TTL_HEADER_FIELD);
            header.setValue("minutes");
            header.setDescription(DESCRIPTION);

            headers.getHeaders().add(header);
        }
    }

    public GeneralMetadataBuilder description(String description) {
        this.generalMetadata.setDescription(description);
        return this;
    }

    public GeneralMetadataBuilder href(String href) {
        this.generalMetadata.setHref(href);
        return this;
    }

    public GeneralMetadataBuilder name(String name) {
        this.generalMetadata.setName(name);
        return this;
    }

    public GeneralMetadataBuilder rel(String rel) {
        this.generalMetadata.setRel(rel);
        return this;
    }
}
