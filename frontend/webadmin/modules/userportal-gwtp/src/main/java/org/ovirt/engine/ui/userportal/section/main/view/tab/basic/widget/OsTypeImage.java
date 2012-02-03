package org.ovirt.engine.ui.userportal.section.main.view.tab.basic.widget;

import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.MainTabBasicListItemResources;

import com.google.gwt.uibinder.client.UiConstructor;

/**
 * OS Type such as Windows, Linux, RHEL etc...
 */
public class OsTypeImage extends AbstractDynamicImage<VmOsType, MainTabBasicListItemResources> {

    private String nameUniquePart;

    private static final String IMAGE = "Image";

    @UiConstructor
    public OsTypeImage(MainTabBasicListItemResources resources, String nameUniquePart) {
        super(resources);
        this.nameUniquePart = nameUniquePart;
    }

    @Override
    protected String imageName(VmOsType value) {
        return value.name() + nameUniquePart + IMAGE;
    }

    @Override
    protected String defaultImageName(VmOsType value) {
        return "otherOs" + nameUniquePart + IMAGE;
    }

}
