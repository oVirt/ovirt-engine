package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.widget.table.cell.StorageDomainsCell;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;

public class StorageDomainsColumn extends Column<Disk, String> implements ColumnWithElementId {

    private static final CommonApplicationConstants CONSTANTS = GWT.create(CommonApplicationConstants.class);

    public StorageDomainsColumn() {
        super(new StorageDomainsCell());
    }

    @Override
    public String getValue(Disk object) {
        if (object.getDiskStorageType() != DiskStorageType.IMAGE) {
            return CONSTANTS.empty();
        }

        DiskImage diskImage = (DiskImage) object;
        getCell().setTitle(StringUtils.join(diskImage.getStoragesNames(), ", ")); //$NON-NLS-1$

        int numOfStorageDomains = diskImage.getStoragesNames() != null ?
                diskImage.getStoragesNames().size() : 0;
        if (numOfStorageDomains == 0) {
            return CONSTANTS.empty();
        }
        else if (numOfStorageDomains == 1) {
            return diskImage.getStoragesNames().get(0);
        }
        else {
            return numOfStorageDomains + CONSTANTS.space() + CONSTANTS.storageDomainsLabelDisk();
        }
    }

    @Override
    public void configureElementId(String elementIdPrefix, String columnId) {
        getCell().setElementIdPrefix(elementIdPrefix);
        getCell().setColumnId(columnId);
    }

    @Override
    public StorageDomainsCell getCell() {
        return (StorageDomainsCell) super.getCell();
    }
}
