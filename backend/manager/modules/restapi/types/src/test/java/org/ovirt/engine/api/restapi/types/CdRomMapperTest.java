package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.ovirt.engine.api.model.Cdrom;
import org.ovirt.engine.core.common.businessentities.VM;

public class CdRomMapperTest extends AbstractInvertibleMappingTest<Cdrom, VM, VM> {

    public CdRomMapperTest() {
        super(Cdrom.class, VM.class, VM.class);
    }

    @Override
    protected void verify(Cdrom model, Cdrom transform) {
        assertNotNull(transform);
        assertNotNull(transform.getFile());
        assertEquals(model.getFile().getId(), transform.getFile().getId());
    }
}
