package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class ClusterAdditionalStatusColumn extends EntityAdditionalStatusColumn<Cluster> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Override
    public SafeHtml getEntityValue(Cluster object) {
        if (object.isClusterCompatibilityLevelUpgradeNeeded()) {
            return getImageSafeHtml(IconType.EXCLAMATION);
        }
        return null;
    }

    @Override
    public SafeHtml getEntityTooltip(Cluster object) {
        if (object.isClusterCompatibilityLevelUpgradeNeeded()) {
            return SafeHtmlUtils.fromTrustedString(constants.clusterLevelUpgradeNeeded());
        }
        return null;
    }

    @Override
    protected Cluster getEntityObject(Cluster object) {
        return object;
    }
}
