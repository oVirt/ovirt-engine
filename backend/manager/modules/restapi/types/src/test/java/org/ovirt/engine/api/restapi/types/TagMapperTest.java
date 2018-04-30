package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.core.common.businessentities.Tags;

public class TagMapperTest extends AbstractInvertibleMappingTest<Tag, Tags, Tags> {

    public TagMapperTest() {
        super(Tag.class, Tags.class, Tags.class);
    }

    @Override
    protected void verify(Tag model, Tag transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getDescription(), transform.getDescription());
    }
}
