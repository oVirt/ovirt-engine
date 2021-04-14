package org.ovirt.engine.ui.common.widget.renderer;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.gin.AssetProvider;

import com.google.gwt.text.shared.AbstractRenderer;

public class StorageDomainFreeSpaceRenderer<T extends StorageDomain> extends AbstractRenderer<T> {

    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    @Override
    public String render(T storageDomain) {
        if (storageDomain == null) {
            return ""; //$NON-NLS-1$
        }

        if (storageDomain.getAvailableDiskSize() == null) {
            // 'getAvailableDiskSize' may return null when there's a connectivity issue with the storage domain
            return storageDomain.getStorageName();
        }

        return messages.storageDomainFreeSpace(storageDomain.getStorageName(),
                storageDomain.getAvailableDiskSize(),
                storageDomain.getTotalDiskSize());
    }
}
