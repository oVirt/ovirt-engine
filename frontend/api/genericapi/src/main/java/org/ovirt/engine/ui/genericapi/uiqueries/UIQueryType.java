package org.ovirt.engine.ui.genericapi.uiqueries;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "UIQueryType")
public enum UIQueryType {

    GetUserActionGroups,
    Get64bitOsTypes,
    GetLinuxOsTypes,
    GetWindowsOsTypes;
}
