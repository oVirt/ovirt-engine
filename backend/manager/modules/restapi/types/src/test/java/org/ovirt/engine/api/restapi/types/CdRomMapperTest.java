package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.CdRom;
import org.ovirt.engine.core.common.businessentities.VM;

public class CdRomMapperTest extends AbstractInvertibleMappingTest<CdRom, VM, VM> {

    public CdRomMapperTest() {
        super(CdRom.class, VM.class, VM.class);
    }

    @Override
    protected void verify(CdRom model, CdRom transform) {
        assertNotNull(transform);
        assertNotNull(transform.getFile());
        assertEquals(model.getFile().getId(), transform.getFile().getId());
    }
}
