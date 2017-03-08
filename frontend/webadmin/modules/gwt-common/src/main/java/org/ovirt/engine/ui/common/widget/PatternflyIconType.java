package org.ovirt.engine.ui.common.widget;

import com.google.gwt.dom.client.Style.HasCssName;

public enum PatternflyIconType implements HasCssName {

    PF_BASE("pficon"), // $NON-NLS-1$
    PF_CLOSE("pficon-close"), // $NON-NLS-1$
    PF_FLAG("pficon-flag"), // $NON-NLS-1$
    PF_USER("pficon-user"), // $NON-NLS-1$
    PF_CPU("pficon-cpu"), // $NON-NLS-1$
    PF_NETWORK("pficon-network"); // $NON-NLS-1$

    private final String cssName;

    private PatternflyIconType(String cssName) {
        this.cssName = cssName;
    }

    @Override
    public String getCssName() {
        return cssName;
    }

}
