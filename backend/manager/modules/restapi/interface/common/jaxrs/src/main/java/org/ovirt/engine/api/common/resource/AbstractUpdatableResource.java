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


import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.ObjectFactory;
import org.ovirt.engine.api.utils.ReflectionHelper;

import org.ovirt.engine.api.common.util.MutabilityAssertor;


public abstract class AbstractUpdatableResource<R extends BaseResource> {

    private static final String[] STRICTLY_IMMUTABLE = {"id"};

    protected final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    protected final R model = newModel();

    /**
     * Create a new instance of the resource
     *
     * @return a newly constructed instance
     */
    protected R newModel() {
        return ReflectionHelper.newModel(this);
    }

    public AbstractUpdatableResource(String id) {
        setId(id);
    }

    /**
     * Validate update from an immutability point of view.
     *
     * @param incoming  the incoming resource representation
     * @param existing  the existing resource representation
     * @throws WebApplicationException wrapping an appropriate response
     * iff an immutability constraint has been broken
     */
    protected void validateUpdate(R incoming) {
        refresh();
        MutabilityAssertor.validateUpdate(getStrictlyImmutable(), incoming, model);
    }

    public void setId(String id) {
        model.setId(id);
    }

    public String getId() {
        return model.getId();
    }

    /**
     * Override this method if any additional resource-specific fields are
     * strictly immutable
     *
     * @return array of strict immutable field names
     */
    protected String[] getStrictlyImmutable() {
        return STRICTLY_IMMUTABLE;
    }

    protected String[] addStrictlyImmutable(String... fields) {
        String[] immutable = new String[STRICTLY_IMMUTABLE.length + fields.length];
        System.arraycopy(STRICTLY_IMMUTABLE, 0, immutable, 0, STRICTLY_IMMUTABLE.length);
        System.arraycopy(fields, 0, immutable, STRICTLY_IMMUTABLE.length, fields.length);
        return immutable;
    }

    /**
     * Refresh the current model state for update validity checking.
     *
     * Override this method if any additional resource-specific fields
     * are strictly immutable by the client but may change in the backend.
     */
    protected void refresh() {}
}
