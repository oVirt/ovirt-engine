package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.cell.ScrollableTextCell;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;

public abstract class LunTextColumn extends SafeHtmlColumn<LunModel> {

    @Override
    public final SafeHtml getValue(LunModel object) {
        ScrollableTextCell.CellTemplate template = GWT.create(ScrollableTextCell.CellTemplate.class);
        String color = ""; //$NON-NLS-1$

        if (object != null) {
            if (object.getIsGrayedOut()) {
                color = "gray"; //$NON-NLS-1$
            } else if (!object.getIsAccessible()) {
                color = "orange"; //$NON-NLS-1$
            } else {
                color = "black"; //$NON-NLS-1$
            }
        }

        return template.input(getRawValue(object), "color:" + color); //$NON-NLS-1$
    }

    public abstract String getRawValue(LunModel object);

}
