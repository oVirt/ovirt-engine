package org.ovirt.engine.ui.common.view;

import org.gwtbootstrap3.client.ui.AnchorButton;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.NavbarBrand;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.PatternflyIconType;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base implementation of the header view.
 */
public abstract class AbstractHeaderView extends AbstractView {

    protected static final String NAV_ITEM_ICONIC = "nav-item-iconic"; //$NON-NLS-1$

    @UiField
    @WithElementId("userName")
    public AnchorButton userName;

    @UiField
    public WidgetTooltip userNameTooltip;

    @UiField
    public NavbarBrand logoLink;

    @UiField(provided = true)
    @WithElementId
    public AnchorListItem logoutLink = null;

    @UiField(provided = true)
    @WithElementId
    public AnchorListItem guideLink = null;

    @UiField(provided = true)
    @WithElementId
    public AnchorListItem aboutLink = null;

    @UiField(provided = true)
    @WithElementId
    public AnchorListItem optionsLink = null;

    protected void setUserIcon() {
        AnchorElement.as(this.userName.getElement()).addClassName(NAV_ITEM_ICONIC);
    }

    public void setUserName(String userName) {
        userNameTooltip.setText(userName);
        // Put PF user icon on the drop down instead of the FA one.
        Widget userNameWidget = this.userName.getWidget(0);
        userNameWidget.removeStyleName(Styles.FONT_AWESOME_BASE);
        userNameWidget.removeStyleName(IconType.USER.getCssName());
        userNameWidget.addStyleName(PatternflyIconType.PF_BASE.getCssName());
        userNameWidget.addStyleName(PatternflyIconType.PF_USER.getCssName());
    }

    public HasClickHandlers getLogoutLink() {
        return logoutLink;
    }

    public HasClickHandlers getAboutLink() {
        return aboutLink;
    }

    public HasClickHandlers getGuideLink() {
        return guideLink;
    }

    public HasClickHandlers getOptionsLink() {
        return optionsLink;
    }

}
