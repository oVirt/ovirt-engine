package org.ovirt.engine.ui.common.widget.table.cell;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class LunSelectionCell extends AbstractCell<LunModel> {

    private boolean multiSelection;

    private final static CommonApplicationResources resources = AssetProvider.getResources();

    public LunSelectionCell() {
    }

    public LunSelectionCell(boolean multiSelection) {
        this.multiSelection = multiSelection;
    }

    @Override
    public void render(Context context, LunModel value, SafeHtmlBuilder sb, String id) {
        ImageResourceCell imageCell = new ImageResourceCell();
        imageCell.setStyle("text-align: center;"); //$NON-NLS-1$

        if (value.getIsIncluded()) {
            // ImageResourceCell sets the id
            imageCell.render(context, resources.okSmallImage(), sb, id);
        } else if (!value.getIsAccessible()) {
            // ImageResourceCell sets the id
            imageCell.render(context, resources.logWarningImage(), sb, id);
        } else {
            sb.append(SafeHtmlUtils.fromTrustedString("<span id=\"" + id + " style=\"padding-left: 1px;\">")); //$NON-NLS-1$ //$NON-NLS-2$

            String type = multiSelection ? "type='checkbox' " : "type='radio' "; //$NON-NLS-1$ //$NON-NLS-2$
            String checked = value.getIsSelected() ? "checked='checked' " : ""; //$NON-NLS-1$ //$NON-NLS-2$
            String disabled = value.getIsGrayedOut() ? "disabled='disabled' " : ""; //$NON-NLS-1$ //$NON-NLS-2$
            // include the id
            String input = "<input id=\"" + id + "_input" + "\" " + type + checked + disabled + " tabindex='-1'/>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

            sb.append(SafeHtmlUtils.fromTrustedString(input));
            sb.append(SafeHtmlUtils.fromTrustedString("</span>")); //$NON-NLS-1$
        }
    }

}
