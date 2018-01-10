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
        String style = ""; //$NON-NLS-1$

        if (object != null) {
            boolean isLunExtendable = object.getIsIncluded() && !object.getIsLunRemovable() &&
                    object.getAdditionalAvailableSize() == 0;

            if (object.isRemoveLunSelected()) {
                style = "color: black; text-decoration: line-through"; //$NON-NLS-1$
            } else if ((!object.getIsIncluded() && object.getIsGrayedOut()) || isLunExtendable) {
                style = "color: gray"; //$NON-NLS-1$
            } else if (!object.getIsAccessible() && !object.getIsGrayedOut()) {
                style = "color: orange"; //$NON-NLS-1$
            } else if (object.getIsSelected() || object.isAdditionalAvailableSizeSelected()) {
                style = "color: black; font-weight: bold"; //$NON-NLS-1$
            } else {
                style = "color: black"; //$NON-NLS-1$
            }
        }

        // TODO use a proper ID
        return template.input(getRawValue(object), style, DOM.createUniqueId()); //$NON-NLS-1$
    }

    public abstract String getRawValue(LunModel object);

    public void makeSortable() {
        makeSortable(Comparator.comparing(this::getRawValue, new LexoNumericComparator()));
    }

}
