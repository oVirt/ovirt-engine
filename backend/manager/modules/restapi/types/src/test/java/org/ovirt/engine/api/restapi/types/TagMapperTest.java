package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.core.common.businessentities.tags;

public class TagMapperTest extends AbstractInvertibleMappingTest<Tag, tags, tags> {

    protected TagMapperTest() {
        super(Tag.class, tags.class, tags.class);
    }

    @Override
    protected void verify(Tag model, Tag transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getDescription(), transform.getDescription());
    }
}
