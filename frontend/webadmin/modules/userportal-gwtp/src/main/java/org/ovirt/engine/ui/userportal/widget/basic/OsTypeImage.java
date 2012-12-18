package org.ovirt.engine.ui.userportal.widget.basic;

import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.ui.userportal.ApplicationResourcesWithLookup;

import com.google.gwt.uibinder.client.UiConstructor;

/**
 * OS Type such as Windows, Linux, RHEL etc...
 */
public class OsTypeImage extends AbstractDynamicImage<VmOsType, ApplicationResourcesWithLookup> {

    private String nameUniquePart;

    private static final String IMAGE = "Image"; //$NON-NLS-1$

    @UiConstructor
    public OsTypeImage(ApplicationResourcesWithLookup resources, String nameUniquePart) {
        super(resources);
        this.nameUniquePart = nameUniquePart;
    }

    @Override
    protected String imageName(VmOsType value) {
        return value.name() + nameUniquePart + IMAGE;
    }

    @Override
    protected String defaultImageName(VmOsType value) {
        return "OtherOs" + nameUniquePart + IMAGE; //$NON-NLS-1$
    }

}
