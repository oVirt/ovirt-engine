package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AdGroupElementParametersBase")
public class AdGroupElementParametersBase extends AdElementParametersBase {
    private static final long serialVersionUID = 407769818057698987L;
    @XmlElement
    private ad_groups _adGroup;

    public AdGroupElementParametersBase(ad_groups adGroup) {
        super(adGroup.getid());
        _adGroup = adGroup;
    }

    public ad_groups getAdGroup() {
        return _adGroup;
    }

    public AdGroupElementParametersBase() {
    }
}
