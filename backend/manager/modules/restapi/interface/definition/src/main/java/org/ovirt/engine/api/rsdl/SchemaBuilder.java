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

import org.ovirt.engine.api.model.Schema;

public class SchemaBuilder {

    private Schema schema;

    public SchemaBuilder() {
        this.schema = new Schema();
    }

    public Schema build() {
       return this.schema;
    }

    public SchemaBuilder description(String description) {
        this.schema.setDescription(description);
        return this;
    }

    public SchemaBuilder href(String href) {
        this.schema.setHref(href);
        return this;
    }

    public SchemaBuilder name(String name) {
        this.schema.setName(name);
        return this;
    }

    public SchemaBuilder rel(String rel) {
        this.schema.setRel(rel);
        return this;
    }
}
