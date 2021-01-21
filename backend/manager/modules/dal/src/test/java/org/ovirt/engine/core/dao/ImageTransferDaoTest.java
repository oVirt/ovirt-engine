package org.ovirt.engine.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransferBackend;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransferPhase;
import org.ovirt.engine.core.common.businessentities.storage.TimeoutPolicyType;
import org.ovirt.engine.core.common.businessentities.storage.TransferClientType;
import org.ovirt.engine.core.common.businessentities.storage.TransferType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@code ImageTransferDaoTest} provides unit tests to validate {@link ImageTransferDao}.
 */
public class ImageTransferDaoTest extends BaseGenericDaoTestCase<Guid, ImageTransfer, ImageTransferDao> {

    private static final int TOTAL_IMAGE_TRANSFERS = 2;

    @Override
    protected int getEntitiesTotalCount() {
        return TOTAL_IMAGE_TRANSFERS;
    }

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.EXISTING_IMAGE_TRANSFER_ID;
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setPhase(ImageTransferPhase.PAUSED_SYSTEM);
    }

    @Override
    protected ImageTransfer generateNewEntity() {
        ImageTransfer imageTransfer = new ImageTransfer(FixturesTool.IMAGE_TRANSFER_ID);
        imageTransfer.setCommandType(ActionType.TransferDiskImage);
        imageTransfer.setPhase(ImageTransferPhase.TRANSFERRING);
        imageTransfer.setType(TransferType.Upload);
        imageTransfer.setActive(true);
        imageTransfer.setLastUpdated(new Date());
        imageTransfer.setVdsId(FixturesTool.HOST_ID);
        imageTransfer.setDiskId(FixturesTool.DISK_ID);
        imageTransfer.setBytesSent(0L);
        imageTransfer.setBytesTotal(SizeConverter.BYTES_IN_GB);
        imageTransfer.setImageFormat(VolumeFormat.COW);
        imageTransfer.setBackend(ImageTransferBackend.FILE);
        imageTransfer.setTransferClientType(TransferClientType.UNKNOWN);
        imageTransfer.setTimeoutPolicy(TimeoutPolicyType.LEGACY);
        imageTransfer.setShallow(false);
        return imageTransfer;
    }

    @Test
    public void testGetByDiskId() {
        ImageTransfer imageTransfer = dao.getByDiskId(FixturesTool.DISK_ID);
        assertNotNull(imageTransfer);
        assertEquals(FixturesTool.DISK_ID, imageTransfer.getDiskId());
    }

    @Test
    public void testGetByVdsId() {
        List<ImageTransfer> imageTransfers = dao.getByVdsId(FixturesTool.HOST_ID);
        assertNotNull(imageTransfers);
        assertTrue(imageTransfers.stream()
                        .allMatch(imageTransfer -> imageTransfer.getVdsId().equals(FixturesTool.HOST_ID)),
                "Transfers must be associated with the specified host");
    }
}
