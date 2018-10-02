package org.ovirt.engine.ui.common.widget.uicommon.network;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.html.Span;
import org.ovirt.engine.ui.common.css.PatternflyConstants;

public class BondedNetworkIcon extends Span {

    public BondedNetworkIcon() {
        addStyleName(Styles.FONT_AWESOME_BASE);
        addStyleName(IconType.CHAIN.getCssName());
        addStyleName(PatternflyConstants.PF_LIST_VIEW_ICON_SM);
    }
}
