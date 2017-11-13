package org.ovirt.engine.ui.common.widget.table.cell;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * LunSelectionCell. Supports tooltips.
 *
 */
public class LunSelectionCell extends AbstractCell<LunModel> {

    private static final CommonApplicationResources resources = AssetProvider.getResources();

    public LunSelectionCell() {
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
        }
    }

}
