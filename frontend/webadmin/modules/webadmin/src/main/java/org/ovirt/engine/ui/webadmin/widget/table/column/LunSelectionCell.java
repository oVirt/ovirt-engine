package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.StyledImageResourceCell;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class LunSelectionCell extends AbstractCell<LunModel> {

    private static ApplicationResources resources = ClientGinjectorProvider.instance().getApplicationResources();

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
            new CheckboxCell(true, false).render(context, value.getIsSelected(), sb);
            sb.append(SafeHtmlUtils.fromTrustedString("</span>")); //$NON-NLS-1$
        }
    }

}
