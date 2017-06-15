package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class NetworkStatusColumn extends AbstractImageResourceColumn<Network> {

    private static final ApplicationResources resources = AssetProvider.getResources();

    @Override
    public ImageResource getValue(Network nwk) {
        return getValue(nwk.getCluster());
    }

    public ImageResource getValue(NetworkCluster net_cluster) {
        switch (net_cluster.getStatus()) {
        case OPERATIONAL:
            return resources.upImage();
        case NON_OPERATIONAL:
            return resources.downImage();
        default:
            return resources.downImage();
        }
    }

    @Override
    public SafeHtml getTooltip(Network nwk) {
        String tooltipContent = EnumTranslator.getInstance().translate(nwk.getCluster().getStatus());
        return SafeHtmlUtils.fromString(tooltipContent);
    }

}
