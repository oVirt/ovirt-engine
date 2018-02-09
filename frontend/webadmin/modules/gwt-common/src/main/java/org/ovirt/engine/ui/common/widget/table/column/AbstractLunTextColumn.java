package org.ovirt.engine.ui.common.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.ui.common.widget.table.cell.ScrollableTextCell;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.TextDecoration;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.DOM;

public abstract class AbstractLunTextColumn extends AbstractSafeHtmlColumn<LunModel> {

    @Override
    public final SafeHtml getValue(LunModel object) {
        // TODO this should use a cell to render, not return HTML itself
        ScrollableTextCell.CellTemplate template = GWT.create(ScrollableTextCell.CellTemplate.class);
        SafeStylesBuilder builder = new SafeStylesBuilder();

        if (object != null) {
            boolean isLunUnExtendable = object.getIsIncluded() && object.getAdditionalAvailableSize() == 0;

            if (object.isRemoveLunSelected()) {
                builder.trustedColor("black"); //$NON-NLS-1$
                builder.textDecoration(TextDecoration.LINE_THROUGH);
            } else if (object.getIsLunRemovable()) {
                builder.trustedColor("black"); //$NON-NLS-1$
            } else if ((!object.getIsIncluded() && object.getIsGrayedOut()) || isLunUnExtendable) {
                builder.trustedColor("gray"); //$NON-NLS-1$
            } else if (!object.getIsAccessible() && !object.getIsGrayedOut()) {
                builder.trustedColor("orange"); //$NON-NLS-1$
            } else if (object.getIsSelected() || object.isAdditionalAvailableSizeSelected()) {
                builder.trustedColor("black"); //$NON-NLS-1$
                builder.fontWeight(FontWeight.BOLD);
            } else {
                builder.trustedColor("black"); //$NON-NLS-1$
            }
        }

        // TODO use a proper ID
        return template.input(getRawValue(object), builder.toSafeStyles(), DOM.createUniqueId()); //$NON-NLS-1$
    }

    public abstract String getRawValue(LunModel object);

    public void makeSortable() {
        makeSortable(Comparator.comparing(this::getRawValue, new LexoNumericComparator()));
    }

}
