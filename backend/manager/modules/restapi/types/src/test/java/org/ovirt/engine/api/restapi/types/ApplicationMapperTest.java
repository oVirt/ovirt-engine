package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.ovirt.engine.api.restapi.types.MappingTestHelper.populate;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.Application;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

public class ApplicationMapperTest extends AbstractInvertibleMappingTest<Application, VM, VM> {

    protected static final String[] NAMES = { "sedna", "eris", "orcus" };

    public ApplicationMapperTest() {
        super(Application.class, VM.class, VM.class);
    }

    @Test
    @Override
    public void testRoundtrip() throws Exception {
        Application model = Application.class.cast(populate(Application.class));
        model = postPopulate(model);
        model.setName(NAMES[0]);
        model.setId(new Guid(NAMES[0].getBytes()).toString());
        Mapper<String, Application> back = getMappingLocator().getMapper(String.class, Application.class);
        Application transform = back.map(NAMES[0], null);
        verify(model, transform);
    }

    @Override
    protected void verify(Application model, Application transform) {
        assertNotNull(transform);
        assertNotNull(transform.getId());
        assertNotNull(transform.getName());
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
    }
}
