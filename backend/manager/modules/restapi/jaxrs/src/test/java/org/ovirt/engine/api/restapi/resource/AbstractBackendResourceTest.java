package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.ovirt.engine.api.model.BaseResource;

public abstract class AbstractBackendResourceTest<R extends BaseResource, Q /* extends Queryable */>
        extends AbstractBackendBaseTest {

    protected void initResource(AbstractBackendResource<R, Q> resource) {
        resource.setMappingLocator(mapperLocator);
        initBackendResource(resource);
    }

    protected Q getEntity(int index) {
        throw new UnsupportedOperationException();
    }

    protected void verifyModel(R model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getName());
        assertEquals(DESCRIPTIONS[index], model.getDescription());
        verifyLinks(model);
    }
}
