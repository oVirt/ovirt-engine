package org.ovirt.engine.ui.userportal.widget.table.column;

import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.ui.userportal.ApplicationResourcesWithLookup;
import org.ovirt.engine.ui.userportal.gin.ClientGinjectorProvider;

import com.google.gwt.resources.client.ImageResource;

public class VmImageColumn<T> extends ImageResourceColumn<T> {

    public interface OsTypeExtractor<T> {
        VmOsType extractOsType(T item);
    }

    private final OsTypeExtractor<T> extractor;

    public VmImageColumn(OsTypeExtractor<T> extractor) {
        super();
        this.extractor = extractor;
    }

    @Override
    public ImageResource getValue(T item) {
        // TODO(vszocs) use WithLookup method instead of big switch
        switch (extractor.extractOsType(item)) {
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
            return getApplicationResources().OtherLinuxSmallImage();
        case Other:
            return getApplicationResources().OtherOsSmallImage();
        case Unassigned:
        default:
            return getApplicationResources().UnassignedSmallImage();
        }
    }

    protected ApplicationResourcesWithLookup getApplicationResources() {
        return ClientGinjectorProvider.instance().getApplicationResourcesWithLookup();
    }

}
