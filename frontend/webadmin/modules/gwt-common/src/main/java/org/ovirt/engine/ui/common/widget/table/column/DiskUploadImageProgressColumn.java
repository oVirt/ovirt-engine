package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.gin.AssetProvider;

import com.google.gwt.safehtml.shared.SafeHtml;

public class DiskUploadImageProgressColumn extends AbstractProgressBarColumn<Disk> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();
    private static final ProgressBarColors color = ProgressBarColors.GREEN;

    public DiskUploadImageProgressColumn() {
    }

    @Override
    protected Integer getProgressValue(Disk disk) {
        if (disk.getImageTransferBytesSent() == null) {
            return null;
        }

        switch (disk.getImageTransferPhase()) {
            // Fall-through is intentional
        case TRANSFERRING:
        case RESUMING:
        case PAUSED_SYSTEM:
        case PAUSED_USER:
        case FINALIZING_SUCCESS:
            if (disk.getImageTransferBytesTotal() != null && disk.getImageTransferBytesTotal() > 0) {
                return (int) (disk.getImageTransferBytesSent() * 100 / disk.getImageTransferBytesTotal());
            }
            // Without the total upload size, the disk size is the next best estimate
            return Math.min(100, (int) (disk.getImageTransferBytesSent() * 100 / disk.getSize()));
        }
        return null;
    }

    @Override
    public SafeHtml getValue(Disk object) {
        return object.getImageTransferPhase() != null ? super.getValue(object) : null;
    }

    @Override
    protected String getStyle() {
        return "engine-progress-box-migration"; //$NON-NLS-1$
    }

    @Override
    protected String getProgressText(Disk disk) {
        if (disk.getImageTransferPhase() != null) {
            switch (disk.getImageTransferPhase()) {
            case UNKNOWN:
                return constants.imageUploadUnknown();
            case INITIALIZING:
                return constants.imageUploadInitializing();
            case RESUMING:
                return constants.imageUploadResuming();
            case TRANSFERRING:
                if (disk.getImageTransferBytesSent() == null) {
                    return constants.imageUploadTransferring();
                }
                else if (disk.getImageTransferBytesTotal() == null
                        || disk.getImageTransferBytesTotal() == 0) {
                    return messages.imageUploadProgress(
                            (int) (disk.getImageTransferBytesSent() / SizeConverter.BYTES_IN_MB));
                }
                else {
                    return messages.imageUploadProgressWithTotal(
                            (int) (disk.getImageTransferBytesSent() / SizeConverter.BYTES_IN_MB),
                            (int) (disk.getImageTransferBytesTotal() / SizeConverter.BYTES_IN_MB));
                }
            case PAUSED_SYSTEM:
                return constants.imageUploadPausedSystem();
            case PAUSED_USER:
                return constants.imageUploadPausedUser();
            case CANCELLED:
                return constants.imageUploadCancelled();
            case FINALIZING_SUCCESS:
                return constants.imageUploadFinalizingSuccess();
            case FINALIZING_FAILURE:
                return constants.imageUploadFinalizingFailure();
            case FINISHED_SUCCESS:
                return constants.imageUploadFinishedSuccess();
            case FINISHED_FAILURE:
                return constants.imageUploadFinishedFailure();
            }
        }
        return constants.imageUploadUnknown();
    }

    @Override
    protected String getColorByProgress(int progress) {
        return color.asCode();
    }
}
