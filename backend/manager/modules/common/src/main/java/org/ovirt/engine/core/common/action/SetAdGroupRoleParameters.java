package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "SetAdGroupRoleParameters")
public class SetAdGroupRoleParameters extends AdGroupElementParametersBase {
    private static final long serialVersionUID = 3039952946152646137L;
    @XmlElement
    private boolean _isRestored;

    public SetAdGroupRoleParameters(ad_groups adGroup, boolean isRestored) {
        super(adGroup);
        _isRestored = isRestored;
    }

    public boolean getIsRestored() {
        return _isRestored;
    }

    public SetAdGroupRoleParameters() {
    }
}
