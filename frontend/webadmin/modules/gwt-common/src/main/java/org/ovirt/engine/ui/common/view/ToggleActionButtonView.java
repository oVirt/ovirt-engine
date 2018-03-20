package org.ovirt.engine.ui.common.view;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.ui.common.presenter.ToggleButtonPresenterWidget;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class ToggleActionButtonView extends AbstractView implements ToggleButtonPresenterWidget.ViewDef {

    private String defaultText;
    private String secondaryText;
    private IconType defaultIcon;
    private IconType secondaryIcon;

    public interface ViewUiBinder extends UiBinder<Widget, ToggleActionButtonView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    Button expandAllButton;

    public ToggleActionButtonView(String defaultText,
            String secondaryText,
            IconType defaultIcon,
            IconType secondaryIcon) {
        this.defaultIcon = defaultIcon;
        this.secondaryIcon = secondaryIcon;
        this.defaultText = defaultText;
        this.secondaryText = secondaryText;
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        switchToDefault();
    }

    public ToggleActionButtonView(String defaultText, String secondaryText) {
        this(defaultText, secondaryText, null, null);
    }

    @Override
    public HasClickHandlers getButton() {
        return expandAllButton;
    }

    @Override
    public void switchToDefault() {
        expandAllButton.setText(defaultText);
        expandAllButton.setIcon(defaultIcon);
    }

    @Override
    public void switchToSecondary() {
        expandAllButton.setText(secondaryText);
        expandAllButton.setIcon(secondaryIcon);
    }
}
