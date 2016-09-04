package org.ovirt.engine.api.restapi.types;

import org.junit.Test;
import org.ovirt.engine.api.model.ImageTransfer;
import org.ovirt.engine.api.model.ImageTransferPhase;

public class ImageTransferMapperTest extends AbstractInvertibleMappingTest<ImageTransfer,
        org.ovirt.engine.core.common.businessentities.storage.ImageTransfer,
        org.ovirt.engine.core.common.businessentities.storage.ImageTransfer> {

    public ImageTransferMapperTest() {
        super(ImageTransfer.class,
                org.ovirt.engine.core.common.businessentities.storage.ImageTransfer.class,
                org.ovirt.engine.core.common.businessentities.storage.ImageTransfer.class);
    }

    @Override
    protected void verify(ImageTransfer model, ImageTransfer transform) {
        assertNotNull(transform);
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getHost().getId(), transform.getHost().getId());
        assertEquals(model.getImage().getId(), transform.getImage().getId());
    }

    @Test
    public void testPhasesCorrelation() {
        for (org.ovirt.engine.core.common.businessentities.storage.ImageTransferPhase phase :
                org.ovirt.engine.core.common.businessentities.storage.ImageTransferPhase.values()) {
            try {
                ImageTransferPhase.valueOf(phase.name());
            } catch (Exception ex) {
                fail();
            }
        }
    }
}
