package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public class DownloadImageManager {

    private static final Logger log = Logger.getLogger(DownloadImageManager.class.getName());

    private static DownloadImageManager instance;

    public static DownloadImageManager getInstance() {
        if (instance == null) {
            instance = new DownloadImageManager();
        }
        return instance;
    }

    public void startDownload(List<DiskImage> disks) {
        log.info("Start download for disks: " + Linq.getDiskAliases(disks)); //$NON-NLS-1$

        List<ActionParametersBase> transferDiskImageParameters = disks.stream()
                .map(DownloadImageHandler::createInitParams)
                .collect(Collectors.toList());
        Frontend.getInstance().runMultipleAction(ActionType.TransferDiskImage,
                transferDiskImageParameters, callback(), true, true);
    }

    private IFrontendMultipleActionAsyncCallback callback() {
        return result -> result.getReturnValue()
                .forEach(actionReturnValue -> {
                    if (actionReturnValue.getSucceeded()) {
                        new DownloadImageHandler(actionReturnValue.getActionReturnValue()).start();
                    }
                });
    }
}

