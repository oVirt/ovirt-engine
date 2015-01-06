package org.ovirt.engine.ui.common.view;

import org.ovirt.engine.ui.common.idhandler.WithElementId;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * Base implementation of the header view.
 */
public abstract class AbstractHeaderView extends AbstractView {


    @UiField
    @WithElementId("userName")
    public InlineLabel userNameLabel;

    @UiField
    public AnchorElement logoLink;

    @UiField(provided = true)
    @WithElementId
    public Anchor logoutLink = null;

    @UiField(provided = true)
    @WithElementId
    public Anchor guideLink = null;

    @UiField(provided = true)
    @WithElementId
    public Anchor aboutLink = null;

    @UiField(provided = true)
    @WithElementId
    public Anchor optionsLink = null;

    public void setUserName(String userName) {
        userNameLabel.setText(userName);
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
