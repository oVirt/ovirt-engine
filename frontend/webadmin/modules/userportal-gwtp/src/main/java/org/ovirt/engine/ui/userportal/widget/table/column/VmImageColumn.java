package org.ovirt.engine.ui.userportal.widget.table.column;

import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;

import com.google.gwt.resources.client.ImageResource;

public class VmImageColumn extends ImageResourceColumn<UserPortalItemModel> {

    @Override
    public ImageResource getValue(UserPortalItemModel item) {
        switch (item.getOsType()) {
        case WindowsXP:
            return getApplicationResources().WindowsXPSmallImage();
        case Windows2003:
            return getApplicationResources().Windows2003SmallImage();
        case Windows2003x64:
            return getApplicationResources().Windows2003x64SmallImage();
        case Windows2008:
            return getApplicationResources().Windows2008SmallImage();
        case Windows2008x64:
            return getApplicationResources().Windows2008x64SmallImage();
        case Windows2008R2x64:
            return getApplicationResources().Windows2008R2x64SmallImage();
        case Windows7:
            return getApplicationResources().Windows7SmallImage();
        case Windows7x64:
            return getApplicationResources().Windows7x64SmallImage();
        case RHEL3:
            return getApplicationResources().RHEL3SmallImage();
        case RHEL3x64:
            return getApplicationResources().RHEL3x64SmallImage();
        case RHEL4:
            return getApplicationResources().RHEL4SmallImage();
        case RHEL4x64:
            return getApplicationResources().RHEL4x64SmallImage();
        case RHEL5:
            return getApplicationResources().RHEL5SmallImage();
        case RHEL5x64:
            return getApplicationResources().RHEL5x64SmallImage();
        case RHEL6:
            return getApplicationResources().RHEL6SmallImage();
        case RHEL6x64:
            return getApplicationResources().RHEL6x64SmallImage();
        case OtherLinux:
            return getApplicationResources().otherLinuxSmallImage();
        case Other:
            return getApplicationResources().otherOsSmallImage();
        case Unassigned:
        default:
            return getApplicationResources().unassignedSmallImage();
        }
    }

}
