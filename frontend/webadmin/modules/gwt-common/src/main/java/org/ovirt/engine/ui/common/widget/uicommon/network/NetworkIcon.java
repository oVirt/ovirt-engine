package org.ovirt.engine.ui.common.widget.uicommon.network;

import org.gwtbootstrap3.client.ui.html.Span;
import org.ovirt.engine.ui.common.css.PatternflyConstants;

public class NetworkIcon extends Span {
    public NetworkIcon() {
        addStyleName(PatternflyConstants.PFICON);
        addStyleName(PatternflyConstants.PFICON_NETWORK);
        addStyleName(PatternflyConstants.PF_LIST_VIEW_ICON_SM);
    }
}
