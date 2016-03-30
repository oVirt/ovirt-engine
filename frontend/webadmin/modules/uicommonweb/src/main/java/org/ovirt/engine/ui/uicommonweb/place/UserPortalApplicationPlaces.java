package org.ovirt.engine.ui.uicommonweb.place;

/**
 * The central location of all application places.
 */
public class UserPortalApplicationPlaces {

    // Main section: main tabs

    public static final String basicMainTabPlace = "basic"; //$NON-NLS-1$

    // Main section: side tabs

    public static final String extendedVirtualMachineSideTabPlace = "extended-vm"; //$NON-NLS-1$

    public static final String extendedTemplateSideTabPlace = "extended-template"; //$NON-NLS-1$

    public static final String extendedResourceSideTabPlace = "extended-resource"; //$NON-NLS-1$

    // Main section: sub tabs

    // Important: the value after SUB_TAB_PREFIX must correspond to given UiCommon model
    // title, transformed to lower case, with spaces replaced with underscores ('_')

    public static final String SUB_TAB_PREFIX = "-"; //$NON-NLS-1$

    public static final String POOL_SUFFIX = "-pool"; //$NON-NLS-1$

    // Virtual Machines

    public static final String extendedVirtualMachineGeneralSubTabPlace = extendedVirtualMachineSideTabPlace
            + SUB_TAB_PREFIX + "general"; //$NON-NLS-1$

    public static final String extendedPoolGeneralSubTabPlace = extendedVirtualMachineGeneralSubTabPlace + POOL_SUFFIX;

    public static final String extendedVirtualMachineNetworkInterfaceSubTabPlace = extendedVirtualMachineSideTabPlace
            + SUB_TAB_PREFIX + "network_interfaces"; //$NON-NLS-1$

    public static final String extendedPoolNetworkInterfaceSubTabPlace =
            extendedVirtualMachineNetworkInterfaceSubTabPlace + POOL_SUFFIX;

    public static final String extendedVirtualMachineVirtualDiskSubTabPlace = extendedVirtualMachineSideTabPlace
            + SUB_TAB_PREFIX + "disks"; //$NON-NLS-1$

    public static final String extendedVirtualPoolDiskSubTabPlace = extendedVirtualMachineVirtualDiskSubTabPlace
            + POOL_SUFFIX;

    public static final String extendedVirtualMachineSnapshotSubTabPlace = extendedVirtualMachineSideTabPlace
            + SUB_TAB_PREFIX + "snapshots"; //$NON-NLS-1$

    public static final String extendedVirtualMachinePermissionSubTabPlace = extendedVirtualMachineSideTabPlace
            + SUB_TAB_PREFIX + "permissions"; //$NON-NLS-1$

    public static final String extendedVirtualMachineEventSubTabPlace = extendedVirtualMachineSideTabPlace
            + SUB_TAB_PREFIX + "events"; //$NON-NLS-1$

    public static final String extendedVirtualMachineApplicationSubTabPlace = extendedVirtualMachineSideTabPlace
            + SUB_TAB_PREFIX + "applications"; //$NON-NLS-1$

    public static final String extendedVirtualMachineGuestContainerSubTabPlace = extendedVirtualMachineSideTabPlace
            + SUB_TAB_PREFIX + "containers"; //$NON-NLS-1$

    public static final String extendedVirtualMachineMonitorSubTabPlace = extendedVirtualMachineSideTabPlace
            + SUB_TAB_PREFIX + "monitor"; //$NON-NLS-1$

    public static final String extendedVirtualMachineGuestInfoSubTabPlace = extendedVirtualMachineSideTabPlace
            + SUB_TAB_PREFIX + "guest_info"; //$NON-NLS-1$

    // Templates

    public static final String extendedTempplateGeneralSubTabPlace = extendedTemplateSideTabPlace
            + SUB_TAB_PREFIX + "general"; //$NON-NLS-1$

    public static final String extendedTempplateNetworkInterfacesSubTabPlace = extendedTemplateSideTabPlace
            + SUB_TAB_PREFIX + "network_interfaces"; //$NON-NLS-1$

    public static final String extendedTempplateVirtualDisksSubTabPlace = extendedTemplateSideTabPlace
            + SUB_TAB_PREFIX + "disks"; //$NON-NLS-1$

    public static final String extendedTempplateEventsSubTabPlace = extendedTemplateSideTabPlace
            + SUB_TAB_PREFIX + "events"; //$NON-NLS-1$

    public static final String extendedTempplatePersmissionsSubTabPlace = extendedTemplateSideTabPlace
            + SUB_TAB_PREFIX + "permissions"; //$NON-NLS-1$

    // Default places
    public static final String DEFAULT_MAIN_SECTION_BASIC_PLACE = basicMainTabPlace;
    public static final String DEFAULT_MAIN_SECTION_EXTENDED_PLACE = extendedVirtualMachineSideTabPlace;

}
