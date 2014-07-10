package org.ovirt.engine.ui.userportal.widget.basic;

import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.userportal.ApplicationResourcesWithLookup;

import com.google.gwt.uibinder.client.UiConstructor;

/**
 * OS Type such as Windows, Linux, RHEL etc...
 */
public class OsTypeImage extends AbstractDynamicImage<Integer, ApplicationResourcesWithLookup> {

    private String nameUniquePart;

    private static final String IMAGE = "Image"; //$NON-NLS-1$

    @UiConstructor
    public OsTypeImage(ApplicationResourcesWithLookup resources, String nameUniquePart) {
        super(resources);
        this.nameUniquePart = nameUniquePart;
    }

    @Override
    protected String imageName(Integer value) {
        return AsyncDataProvider.getInstance().getOsUniqueOsNames().get(value) + nameUniquePart + IMAGE;
    }

    @Override
    protected String defaultImageName(Integer value) {
        return "other" + nameUniquePart + IMAGE; //$NON-NLS-1$
    }

}
