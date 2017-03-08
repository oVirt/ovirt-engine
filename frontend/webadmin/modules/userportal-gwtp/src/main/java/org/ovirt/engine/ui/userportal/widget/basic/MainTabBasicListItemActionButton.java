package org.ovirt.engine.ui.userportal.widget.basic;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.widget.action.SimpleActionButton;

import com.google.gwt.safehtml.shared.SafeHtml;

public class MainTabBasicListItemActionButton extends SimpleActionButton implements HasElementId {

    public MainTabBasicListItemActionButton(SafeHtml tooltip, IconType icon) {
        setTooltip(tooltip);
        setIcon(icon);
    }

    @Override
    public void setElementId(String elementId) {
        asButton().getElement().setId(elementId);
    }
}
