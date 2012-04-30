package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;

public class AdGroupElementParametersBase extends AdElementParametersBase {
    private static final long serialVersionUID = 407769818057698987L;
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
