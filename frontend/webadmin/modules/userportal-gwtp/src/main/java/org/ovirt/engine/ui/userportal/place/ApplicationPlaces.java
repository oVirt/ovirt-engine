package org.ovirt.engine.ui.userportal.place;

/**
 * The central location of all application places.
 */
public class ApplicationPlaces {

    // Login section

    public static final String loginPlace = "login";

    // Main section: main tabs

    public static final String basicMainTabPlace = "basic";

    // Main section: side tabs

    public static final String extendedVirtualMachineSideTabPlace = "extended-vm";

    public static final String extendedTemplateSideTabPlace = "extended-template";

    public static final String extendedResourceSideTabPlace = "extended-resource";

    // Main section: sub tabs

    public static final String SUB_TAB_PREFIX = "-";

    // Virtual Machines

    public static final String extendedVirtualMachineGeneralSubTabPlace = extendedVirtualMachineSideTabPlace
            + SUB_TAB_PREFIX + "general";

}
