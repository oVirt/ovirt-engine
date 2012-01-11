package org.ovirt.engine.ui.webadmin.widget;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.webadmin.widget.dialog.SimpleDialogButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class UiCommandButton extends Composite implements HasUiCommandClickHandlers, HasLabel {

    interface WidgetUiBinder extends UiBinder<Widget, UiCommandButton> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private UICommand command;

    @UiField
    SimpleDialogButton button;

    public UiCommandButton() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public UiCommandButton(String label) {
        this(label, null);
    }

    public UiCommandButton(ImageResource image) {
        this("", image);
    }

    public UiCommandButton(String label, ImageResource image) {
        this();
        setLabel(label);
        setImage(image);
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return button.addClickHandler(handler);
    }

    @Override
    public UICommand getCommand() {
        return command;
    }

    @Override
    public void setCommand(UICommand command) {
        this.command = command;

        command.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                updateButton();
            }
        });

        updateButton();
    }

    @Override
    public String getLabel() {
        return button.getText();
    }

    @Override
    public void setLabel(String label) {
        button.setText(label);
    }

    public void setImage(ImageResource image) {
        button.setImage(image);
    }

    public void setCustomContentStyle(String customStyle) {
        button.setCustomContentStyle(customStyle);
    }

    void updateButton() {
        button.setVisible(command.getIsAvailable());
        button.setEnabled(command.getIsExecutionAllowed());
    }

}
