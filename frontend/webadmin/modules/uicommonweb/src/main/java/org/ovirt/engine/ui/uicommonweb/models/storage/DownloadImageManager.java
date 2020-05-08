package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.List;
import java.util.logging.Logger;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.ui.uicommonweb.Linq;

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
        disks.forEach(disk -> new DownloadImageHandler(disk).start());
    }
}

