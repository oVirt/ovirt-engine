package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;

public class AdGroupElementParametersBase extends AdElementParametersBase {
    private static final long serialVersionUID = 407769818057698987L;
    private LdapGroup _adGroup;

    public AdGroupElementParametersBase(LdapGroup adGroup) {
        super(adGroup.getid());
        _adGroup = adGroup;
    }

    public LdapGroup getAdGroup() {
        return _adGroup;
    }

    public AdGroupElementParametersBase() {
    }
}
