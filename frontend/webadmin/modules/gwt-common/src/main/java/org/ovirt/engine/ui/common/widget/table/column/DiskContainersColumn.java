package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.ui.common.widget.table.cell.DiskContainersCell;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

import com.google.gwt.user.cellview.client.Column;

public class DiskContainersColumn extends Column<Disk, String> implements ColumnWithElementId {

    public DiskContainersColumn() {
        super(new DiskContainersCell());
    }

    @Override
    public String getValue(Disk object) {
        getCell().setTitle(StringUtils.join(object.getVmNames(), ", ")); //$NON-NLS-1$

        if (object.getNumberOfVms() == 0) {
            return ""; //$NON-NLS-1$
        }

        String entityType = EnumTranslator.getInstance().get(object.getVmEntityType());

        if (object.getNumberOfVms() == 1) {
            String entityName = object.getVmNames().get(0);
            return entityName;
        }

        else {
            return object.getNumberOfVms() + " " + entityType + "s"; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    @Override
    public void configureElementId(String elementIdPrefix, String columnId) {
        getCell().setElementIdPrefix(elementIdPrefix);
        getCell().setColumnId(columnId);
    }

    @Override
    public DiskContainersCell getCell() {
        return (DiskContainersCell) super.getCell();
    }

}
