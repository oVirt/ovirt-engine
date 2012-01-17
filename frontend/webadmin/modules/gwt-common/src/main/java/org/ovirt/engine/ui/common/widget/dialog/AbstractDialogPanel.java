package org.ovirt.engine.ui.common.widget.dialog;

import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractDialogPanel extends DialogBoxWithKeyHandlers {

    public abstract void setHeader(Widget header);

    public abstract Widget getContent();

    public abstract void setContent(Widget content);

    public abstract void addContentStyleName(String styleName);

    public abstract void removeFooterButtons();

    public abstract void addFooterButton(Widget button);

    public abstract void setFooterPanelVisible(boolean visible);

}
