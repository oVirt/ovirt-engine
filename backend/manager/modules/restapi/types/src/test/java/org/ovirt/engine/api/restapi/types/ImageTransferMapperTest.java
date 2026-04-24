package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.ovirt.engine.api.model.ImageTransfer;

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

    @ParameterizedTest
    @EnumSource(value = org.ovirt.engine.core.common.businessentities.storage.ImageTransferPhase.class)
    public void testPhasesCorrelation(org.ovirt.engine.core.common.businessentities.storage.ImageTransferPhase phase) {
        org.ovirt.engine.core.common.businessentities.storage.ImageTransfer entity =
                new org.ovirt.engine.core.common.businessentities.storage.ImageTransfer();
        entity.setPhase(phase);
        ImageTransfer model = ImageTransferMapper.map(entity, null);
        assertNotNull(model.getPhase(), "REST phase must be set for engine phase " + phase);
    }
}
