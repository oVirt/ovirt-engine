package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.widget.table.cell.TextCell;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class DiskContainersColumn extends AbstractTextColumn<Disk> implements ColumnWithElementId {

    @Override
    public String getValue(Disk object) {

        if (object.getNumberOfVms() == 0) {
            return ""; //$NON-NLS-1$
        }

        String entityType = EnumTranslator.getInstance().translate(object.getVmEntityType());

        if (object.getNumberOfVms() == 1) {
            String entityName = object.getVmNames().get(0);
            return entityName;
        }

        else {
            return object.getNumberOfVms() + " " + entityType + "s"; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    @Override
    public TextCell getCell() {
        return (TextCell) super.getCell();
    }

    @Override
    public SafeHtml getTooltip(Disk object) {
        if (object.getNumberOfVms() < 2) {
            return null;
        }
        return SafeHtmlUtils.fromString(StringUtils.join(object.getVmNames(), ", ")); //$NON-NLS-1$
    }

}
