package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.ovirt.engine.api.model.Bookmark;

public class BookmarkMapperTest extends AbstractInvertibleMappingTest<Bookmark,
    org.ovirt.engine.core.common.businessentities.Bookmark, org.ovirt.engine.core.common.businessentities.Bookmark> {

    public BookmarkMapperTest() {
        super(Bookmark.class, org.ovirt.engine.core.common.businessentities.Bookmark.class,
                org.ovirt.engine.core.common.businessentities.Bookmark.class);
    }

    @Override
    protected void verify(Bookmark model, Bookmark transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getValue(), transform.getValue());
    }
}
