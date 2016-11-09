package org.ovirt.engine.ui.common.widget;

import com.google.gwt.dom.client.Style.HasCssName;

public enum PatternflyIconType implements HasCssName {

    PF_CLOSE("pficon-close"); //$NON-NLS-1$

    private final String cssName;

    private PatternflyIconType(String cssName) {
        this.cssName = cssName;
    }

    @Override
    public String getCssName() {
        return cssName;
    }

}
