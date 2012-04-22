package org.ovirt.engine.ui.userportal.widget.table.column;

import org.ovirt.engine.core.common.businessentities.VmOsType;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ResourcePrototype;

public class VmImageColumn<T> extends UserPortalImageResourceColumn<T> {

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
        String osTypeName = extractor.extractOsType(item).name();
        ResourcePrototype resource = getApplicationResourcesWithLookup().getResource(osTypeName + "SmallImage"); //$NON-NLS-1$

        if (resource == null || !(resource instanceof ImageResource)) {
            resource = getApplicationResourcesWithLookup().UnassignedSmallImage();
        }

        return (ImageResource) resource;
    }

}
