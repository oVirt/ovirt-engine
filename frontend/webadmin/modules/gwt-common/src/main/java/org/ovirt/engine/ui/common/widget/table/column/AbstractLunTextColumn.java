package org.ovirt.engine.ui.common.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.ui.common.widget.table.cell.ScrollableTextCell;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.DOM;

public abstract class AbstractLunTextColumn extends AbstractSafeHtmlColumn<LunModel> {

    @Override
    public final SafeHtml getValue(LunModel object) {
        // TODO this should use a cell to render, not return HTML itself
        ScrollableTextCell.CellTemplate template = GWT.create(ScrollableTextCell.CellTemplate.class);
        String color = ""; //$NON-NLS-1$

        if (object != null) {
            if (!object.getIsIncluded() && (!object.getIsSelected() || object.getIsGrayedOut()) ||
                    object.isRemoveLunSelected()) {
                color = "gray"; //$NON-NLS-1$
            } else if (object.getIsSelected()) {
                color = "midnightblue"; //$NON-NLS-1$
            } else if (!object.getIsAccessible() && !object.getIsGrayedOut()) {
                color = "orange"; //$NON-NLS-1$
            } else {
                color = "black"; //$NON-NLS-1$
            }
        }

        // TODO use a proper ID
        return template.input(getRawValue(object), "color:" + color, DOM.createUniqueId()); //$NON-NLS-1$
    }

    public abstract String getRawValue(LunModel object);

    public void makeSortable() {
        makeSortable(Comparator.comparing(this::getRawValue, new LexoNumericComparator()));
    }

}
