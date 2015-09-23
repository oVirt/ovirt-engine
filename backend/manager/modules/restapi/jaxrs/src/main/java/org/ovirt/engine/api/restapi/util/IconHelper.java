package org.ovirt.engine.api.restapi.util;

import org.ovirt.engine.api.model.Icon;
import org.ovirt.engine.api.model.VmBase;
import org.ovirt.engine.core.common.action.HasVmIcon;
import org.ovirt.engine.core.common.businessentities.VmIcon;
import org.ovirt.engine.core.compat.Guid;

public class IconHelper {

    private IconHelper() {}

    public static boolean validateIconParameters(VmBase incoming) {
        final boolean isLargeIconUpload = incoming.isSetLargeIcon()
                && incoming.getLargeIcon().isSetData()
                && incoming.getLargeIcon().isSetMediaType()
                && !incoming.getLargeIcon().isSetId()
                && !incoming.isSetSmallIcon();
        final boolean isLargeIconIdSet = incoming.isSetLargeIcon()
                && incoming.getLargeIcon().isSetId()
                && !incoming.getLargeIcon().isSetData()
                && !incoming.getLargeIcon().isSetMediaType()
                && !incoming.isSetSmallIcon();
        final boolean isSmallIconIdSet = incoming.isSetSmallIcon()
                && incoming.getSmallIcon().isSetId()
                && !incoming.getSmallIcon().isSetData()
                && !incoming.getSmallIcon().isSetMediaType()
                && !incoming.isSetLargeIcon();
        final boolean isBothIconsIdSet = incoming.isSetLargeIcon()
                && incoming.getLargeIcon().isSetId()
                && !incoming.getLargeIcon().isSetData()
                && !incoming.getLargeIcon().isSetMediaType()
                && incoming.isSetSmallIcon()
                && incoming.getSmallIcon().isSetId()
                && !incoming.getSmallIcon().isSetData()
                && !incoming.getSmallIcon().isSetMediaType();
        final boolean isNoIconSet = !incoming.isSetLargeIcon()
                && !incoming.isSetSmallIcon();
        return isLargeIconUpload
                || isLargeIconIdSet
                || isSmallIconIdSet
                || isBothIconsIdSet
                || isNoIconSet;
    }

    public static void setIconToParams(VmBase incoming, HasVmIcon params) {
        if (isLargeIconSet(incoming)) {
            params.setVmLargeIcon(VmIcon.typeAndDataToDataUrl(
                    incoming.getLargeIcon().getMediaType(),
                    incoming.getLargeIcon().getData()));
        }
    }

    private static boolean isLargeIconSet(VmBase incoming) {
        return incoming.isSetLargeIcon()
                && incoming.getLargeIcon().isSetMediaType()
                && incoming.getLargeIcon().isSetData()
                && !incoming.getLargeIcon().isSetId()
                && !incoming.isSetSmallIcon();
    }

    public static Icon createIcon(Guid id) {
        final Icon result = new Icon();
        result.setId(id.toString());
        return result;
    }
}
