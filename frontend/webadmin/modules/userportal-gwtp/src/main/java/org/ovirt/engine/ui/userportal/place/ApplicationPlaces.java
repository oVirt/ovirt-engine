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

    // Important: the value after SUB_TAB_PREFIX must correspond to given UiCommon model
    // title, transformed to lower case, with spaces replaced with underscores ('_')

    public static final String SUB_TAB_PREFIX = "-";

    public static final String POOL_SUFFIX = "-pool";

    // Virtual Machines

    public static final String extendedVirtualMachineGeneralSubTabPlace = extendedVirtualMachineSideTabPlace
            + SUB_TAB_PREFIX + "general";

    public static final String extendedPoolGeneralSubTabPlace = extendedVirtualMachineGeneralSubTabPlace + POOL_SUFFIX;

    public static final String extendedVirtualMachineNetworkInterfaceSubTabPlace = extendedVirtualMachineSideTabPlace
            + SUB_TAB_PREFIX + "network_interfaces";

    public static final String extendedPoolNetworkInterfaceSubTabPlace =
            extendedVirtualMachineNetworkInterfaceSubTabPlace + POOL_SUFFIX;

    public static final String extendedVirtualMachineVirtualDiskSubTabPlace = extendedVirtualMachineSideTabPlace
            + SUB_TAB_PREFIX + "virtual_disks";

    public static final String extendedVirtualPoolDiskSubTabPlace = extendedVirtualMachineVirtualDiskSubTabPlace
            + POOL_SUFFIX;

    public static final String extendedVirtualMachineSnapshotSubTabPlace = extendedVirtualMachineSideTabPlace
            + SUB_TAB_PREFIX + "snapshots";

    public static final String extendedVirtualMachinePermissionSubTabPlace = extendedVirtualMachineSideTabPlace
            + SUB_TAB_PREFIX + "permissions";

    public static final String extendedVirtualMachineEventSubTabPlace = extendedVirtualMachineSideTabPlace
            + SUB_TAB_PREFIX + "events";

    public static final String extendedVirtualMachineApplicationSubTabPlace = extendedVirtualMachineSideTabPlace
            + SUB_TAB_PREFIX + "applications";

    public static final String extendedVirtualMachineMonitorSubTabPlace = extendedVirtualMachineSideTabPlace
            + SUB_TAB_PREFIX + "monitor";

    // Templates

    public static final String extendedTempplateGeneralSubTabPlace = extendedTemplateSideTabPlace
            + SUB_TAB_PREFIX + "general";

    public static final String extendedTempplateNetworkInterfacesSubTabPlace = extendedTemplateSideTabPlace
            + SUB_TAB_PREFIX + "network_interfaces";

    public static final String extendedTempplateVirtualDisksSubTabPlace = extendedTemplateSideTabPlace
            + SUB_TAB_PREFIX + "virtual_disks";

    public static final String extendedTempplateEventsSubTabPlace = extendedTemplateSideTabPlace
            + SUB_TAB_PREFIX + "events";

    public static final String extendedTempplatePersmissionsSubTabPlace = extendedTemplateSideTabPlace
            + SUB_TAB_PREFIX + "permissions";

    // Default places

    public static final String DEFAULT_LOGIN_SECTION_PLACE = loginPlace;
    public static final String DEFAULT_MAIN_SECTION_BASIC_PLACE = basicMainTabPlace;
    public static final String DEFAULT_MAIN_SECTION_EXTENDED_PLACE = extendedVirtualMachineSideTabPlace;

}
