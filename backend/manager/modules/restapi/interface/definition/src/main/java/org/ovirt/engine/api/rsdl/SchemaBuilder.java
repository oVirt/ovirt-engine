/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
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
