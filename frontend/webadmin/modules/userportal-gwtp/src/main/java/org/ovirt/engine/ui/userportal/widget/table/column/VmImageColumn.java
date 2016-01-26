package org.ovirt.engine.ui.userportal.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.userportal.ApplicationResourcesWithLookup;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ResourcePrototype;

public class VmImageColumn<T> extends AbstractImageResourceColumn<T> {

    private static final ApplicationResourcesWithLookup resourcesWithLookup = AssetProvider.getResourcesWithLookup();

    public interface OsTypeExtractor<T> {
        int extractOsType(T item);
    }

    private final OsTypeExtractor<T> extractor;

    public VmImageColumn(OsTypeExtractor<T> extractor) {
        super();
        this.extractor = extractor;
    }

    @Override
    public ImageResource getValue(T item) {
        String osTypeName = AsyncDataProvider.getInstance().getOsUniqueOsNames().get(extractor.extractOsType(item));
        ResourcePrototype resource = resourcesWithLookup.getResource(osTypeName + "SmallImage"); //$NON-NLS-1$

        if (!(resource instanceof ImageResource)) {
            resource = resourcesWithLookup.otherSmallImage();
        }

        return (ImageResource) resource;
    }

}
