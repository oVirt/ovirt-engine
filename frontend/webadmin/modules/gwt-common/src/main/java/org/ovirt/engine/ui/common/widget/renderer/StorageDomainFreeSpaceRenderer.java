package org.ovirt.engine.ui.common.widget.renderer;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.ui.common.CommonApplicationMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.text.shared.AbstractRenderer;

public class StorageDomainFreeSpaceRenderer<T extends StorageDomain> extends AbstractRenderer<T> {
    private static final CommonApplicationMessages MESSAGES = GWT.create(CommonApplicationMessages.class);

    @Override
    public String render(T storageDomain) {
        if (storageDomain == null) {
            return ""; //$NON-NLS-1$
        }

        return MESSAGES.storageDomainFreeSpace(storageDomain.getstorage_name(),
                storageDomain.getavailable_disk_size(),
                storageDomain.getTotalDiskSize());
    }
}
