package org.ovirt.engine.ui.webadmin.widget.table.cell;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class VolumeInfoCell extends AbstractCell<GlusterVolumeEntity> {

    private static final ApplicationResources resources = GWT.create(ApplicationResources.class);
    private static final ApplicationConstants constants = GWT.create(ApplicationConstants.class);
    private static final ApplicationTemplates applicationTemplates = GWT.create(ApplicationTemplates.class);
    private static final ApplicationMessages messages = GWT.create(ApplicationMessages.class);

    protected ImageResource geoRepMasterImage = resources.volumeGeoRepMaster();
    protected ImageResource geoRepSlaveImage = resources.volumeGeoRepSlave();
    protected ImageResource snapshotScheduledImage = resources.snapshotScheduledImage();

    @Override
    public void render(Context context, GlusterVolumeEntity volume, SafeHtmlBuilder sb) {
        // Nothing to render if no volume is provided:
        if (volume == null) {
            return;
        }
        if (volume.getIsGeoRepMaster()) {
            SafeHtml geoRepMasterHtml =
                    SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(geoRepMasterImage).getHTML());
            sb.append(applicationTemplates.statusTemplate(geoRepMasterHtml, constants.geoRepMasterVolumeToolTip()));
        }
        if (volume.getIsGeoRepSlave()) {
            SafeHtml geoRepSlaveHtml =
                    SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(geoRepSlaveImage).getHTML());
            String[] volClusterNames = formatVolClusterName(volume.getGeoRepMasterVolAndClusterName());
            String volName = volClusterNames[0];
            String clusterName = volClusterNames.length == 2 ? volClusterNames[1] : "UNKNOWN"; //$NON-NLS-1$
            sb.append(applicationTemplates.statusTemplate(geoRepSlaveHtml,
                    messages.geoRepSlaveVolumeToolTip(volName, clusterName)));
        }
        if (volume.getSnapshotScheduled()) {
            SafeHtml snapshotScheduledHtml =
                    SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(snapshotScheduledImage).getHTML());
            sb.append(applicationTemplates.statusTemplate(snapshotScheduledHtml,
                    constants.glusterVolumeSnapshotsScheduledToolTip()));
        }
    }

    private String[] formatVolClusterName(String volClusterName) {
        if (volClusterName == null) {
            return null;
        }
        String[] names = volClusterName.split("\\|"); //$NON-NLS-1$
        return names;
    }
}
