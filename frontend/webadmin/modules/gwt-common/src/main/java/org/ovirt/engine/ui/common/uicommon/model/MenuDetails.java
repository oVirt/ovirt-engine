package org.ovirt.engine.ui.common.uicommon.model;

import com.google.gwt.dom.client.Style;

public interface MenuDetails {

    String getPrimaryTitle();

    String getSecondaryTitle();

    int getPrimaryPriority();

    int getSecondaryPriority();

    Style.HasCssName getIcon();

}
