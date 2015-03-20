package org.ovirt.engine.ui.userportal.widget.basic;

import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;

import com.google.gwt.uibinder.client.UiConstructor;

/**
 * OS Type such as Windows, Linux, RHEL etc...
 */
public class OsTypeImage extends AbstractDynamicImage<Integer> {

    private String nameUniquePart;

    private static final String IMAGE = "Image"; //$NON-NLS-1$

    @UiConstructor
    public OsTypeImage(String nameUniquePart) {
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
