package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class DcAdditionalStatusColumn extends EntityAdditionalStatusColumn<StoragePool> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Override
    public SafeHtml getEntityValue(StoragePool object) {
        if (object.isStoragePoolCompatibilityLevelUpgradeNeeded()) {
            return getImageSafeHtml(IconType.EXCLAMATION);
        }
        return null;
    }

    @Override
    public SafeHtml getEntityTooltip(StoragePool object) {
        if (object.isStoragePoolCompatibilityLevelUpgradeNeeded()) {
            return SafeHtmlUtils.fromTrustedString(constants.clusterLevelUpgradeNeeded());
        }
        return null;
    }

    @Override
    protected StoragePool getEntityObject(StoragePool object) {
        return object;
    }
}
