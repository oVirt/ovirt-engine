package org.ovirt.engine.ui.common.widget.table.cell;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * LunSelectionCell. Supports tooltips.
 *
 */
public class LunSelectionCell extends AbstractCell<LunModel> {

    interface CellTemplate extends SafeHtmlTemplates {

        @Template("<span id=\"{0}\" style=\"padding-left: 1px;\">{1}</span>")
        SafeHtml span(String id, SafeHtml html);

        @Template("<input id=\"{0}\" tabindex='-1' type=\"{1}\" checked />")
        SafeHtml inputChecked(String id, String type);

        @Template("<input id=\"{0}\" tabindex='-1' type=\"{1}\" checked disabled />")
        SafeHtml inputCheckedDisabled(String id, String type);

        @Template("<input id=\"{0}\" tabindex='-1' type=\"{1}\" />")
        SafeHtml inputUnchecked(String id, String type);

        @Template("<input id=\"{0}\" tabindex='-1' type=\"{1}\" disabled />")
        SafeHtml inputUncheckedDisabled(String id, String type);
    }

    private static final CellTemplate templates = GWT.create(CellTemplate.class);

    private boolean multiSelection;

    private static final CommonApplicationResources resources = AssetProvider.getResources();

    public LunSelectionCell() {
    }

    public LunSelectionCell(boolean multiSelection) {
        this.multiSelection = multiSelection;
    }

    @Override
    public void render(Context context, LunModel value, SafeHtmlBuilder sb, String id) {
        ImageResourceCell imageCell = new ImageResourceCell();
        imageCell.setStyle("text-align: center;"); //$NON-NLS-1$

        if (value.isRemoveLunSelected()) {
            imageCell.setStyle("text-align: center; opacity: 0.2; filter: alpha(opacity=20);"); //$NON-NLS-1$
        }
        if (value.getIsIncluded()) {
            // ImageResourceCell sets the id
            imageCell.render(context, resources.okSmallImage(), sb, id);
        } else if (!value.getIsAccessible()) {
            // ImageResourceCell sets the id
            imageCell.render(context, resources.logWarningImage(), sb, id);
        }  else if (!multiSelection){
            boolean checked = value.getIsSelected();
            boolean disabled = value.getIsGrayedOut();
            String inputId = id + "_input"; //$NON-NLS-1$

            String type = "radio"; //$NON-NLS-1$
            SafeHtml input;

            if (checked && !disabled) {
                input = templates.inputChecked(inputId, type);
            } else if (checked && disabled) {
                input = templates.inputCheckedDisabled(inputId, type);
            } else if (!checked && !disabled) {
                input = templates.inputUnchecked(inputId, type);
            } else {
                input = templates.inputUncheckedDisabled(inputId, type);
            }

            sb.append(templates.span(id, input));
        }
    }

}
