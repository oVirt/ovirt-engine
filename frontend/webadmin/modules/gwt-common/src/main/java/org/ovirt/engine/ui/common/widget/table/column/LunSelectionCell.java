package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class LunSelectionCell extends AbstractCell<LunModel> {

    protected static final CommonApplicationResources resources = GWT.create(CommonApplicationResources.class);

    private boolean multiSelection;

    public LunSelectionCell() {
    }

    public LunSelectionCell(boolean multiSelection) {
        this.multiSelection = multiSelection;
    }

    @Override
    public void render(Context context, LunModel value, SafeHtmlBuilder sb) {
        StyledImageResourceCell imageCell = new StyledImageResourceCell();
        imageCell.setStyle("text-align: center;"); //$NON-NLS-1$

        if (value.getIsIncluded()) {
            imageCell.render(context, resources.okSmallImage(), sb);
        } else if (!value.getIsAccessible()) {
            imageCell.render(context, resources.logWarningImage(), sb);
        } else {
            sb.append(SafeHtmlUtils.fromTrustedString("<span style=\"padding-left: 1px;\">")); //$NON-NLS-1$
            if (multiSelection) {
                new CheckboxCell(true, false).render(context, value.getIsSelected(), sb);
            }
            else {
                new RadioboxCell(true, false).render(context, value.getIsSelected(), sb);
            }
            sb.append(SafeHtmlUtils.fromTrustedString("</span>")); //$NON-NLS-1$
        }
    }

}
