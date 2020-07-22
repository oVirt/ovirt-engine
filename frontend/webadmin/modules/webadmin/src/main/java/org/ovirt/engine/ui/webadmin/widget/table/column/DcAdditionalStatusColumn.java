package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class DcAdditionalStatusColumn extends EntityAdditionalStatusColumn<StoragePool> {

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Override
    public SafeHtml getEntityValue(StoragePool object) {
        SafeHtmlBuilder images = new SafeHtmlBuilder();

        if (object.isStoragePoolCompatibilityLevelUpgradeNeeded()) {
            images.append(getImageSafeHtml(IconType.EXCLAMATION));
        }
        if (!object.isManaged()) {
            images.append(getImageSafeHtml(resources.container()));
        }
        return templates.image(images.toSafeHtml());
    }

    @Override
    public SafeHtml getEntityTooltip(StoragePool object) {
        SafeHtmlBuilder tooltip = new SafeHtmlBuilder();
        boolean addLineBreaks = false;

        if (object.isStoragePoolCompatibilityLevelUpgradeNeeded()) {
            tooltip.appendHtmlConstant(constants.clusterLevelUpgradeNeeded());
            addLineBreaks = true;
        }

        if (!object.isManaged()) {
            if (addLineBreaks) {
                tooltip.appendHtmlConstant(constants.lineBreak());
                tooltip.appendHtmlConstant(constants.lineBreak());
            }
            tooltip.appendHtmlConstant(constants.supportsContainerPlatform());
            addLineBreaks = true;
        }
        return tooltip.toSafeHtml();
    }

    @Override
    protected StoragePool getEntityObject(StoragePool object) {
        return object;
    }
}
