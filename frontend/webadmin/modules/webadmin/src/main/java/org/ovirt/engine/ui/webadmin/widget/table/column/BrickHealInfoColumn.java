package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class BrickHealInfoColumn extends AbstractUptimeColumn<GlusterBrickEntity> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Override
    protected Double getRawValue(GlusterBrickEntity object) {
        return object.getSelfHealEta();
    }

    @Override
    public String getValue(GlusterBrickEntity object) {
        if (object.getSelfHealEta() > 0) {
            return super.getValue(object);
        } else {
            return object.getUnSyncedEntries() == null ? constants.notAvailableLabel()
                    : object.getUnSyncedEntries() > 0
                            ? messages.unSyncedEntriesPresent(object.getUnSyncedEntries())
                            : constants.GlusterSelfHealOk();
        }
    }

    @Override
    public SafeHtml getTooltip(GlusterBrickEntity object) {
        if (object.getSelfHealEta() > 0) {
            String toolTip = messages.unSyncedEntriesPresent(object.getUnSyncedEntries());
            return SafeHtmlUtils.fromString(toolTip);
        } else {
            return null;
        }
    }
}
