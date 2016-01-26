package org.ovirt.engine.ui.userportal.widget.table.cell;

import org.ovirt.engine.ui.common.widget.table.cell.AbstractImageButtonCell;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;
import com.google.gwt.resources.client.ImageResource;

public abstract class VmButtonsImageButtonCell extends AbstractImageButtonCell<UserPortalItemModel> {

    private static final ApplicationResources resources = AssetProvider.getResources();

    public VmButtonsImageButtonCell(ImageResource enabledImage, ImageResource disabledImage) {
        super(enabledImage, resources.sideTabExtendedVmStyle().vmButtonEnabled(),
                disabledImage, resources.sideTabExtendedVmStyle().vmButtonDisabled());
    }
}
