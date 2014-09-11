package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.common.view.popup.FocusableComponentsContainer;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

/**
 * Abstract button widget bound to UiCommon {@linkplain UICommand command}.
 */
public abstract class AbstractUiCommandButton extends Composite
        implements HasUiCommandClickHandlers, HasLabel, FocusableComponentsContainer {

    private UICommand command;

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        Widget widget = getButtonWidget();
        if (widget instanceof HasClickHandlers) {
            return ((HasClickHandlers)widget).addClickHandler(handler);
        } else {
            return null;
        }
    }

    @Override
    public UICommand getCommand() {
        return command;
    }

    @Override
    public void setCommand(UICommand command) {
        this.command = command;

        command.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                updateButton();
            }
        });

        updateButton();
    }

    @Override
    public String getLabel() {
        Widget widget = getButtonWidget();
        String result = null;
        if (widget instanceof HasText) {
            result = ((HasText)widget).getText();
        }
        return result;
    }

    @Override
    public void setLabel(String label) {
        Widget widget = getButtonWidget();
        if (widget instanceof HasText) {
            ((HasText)widget).setText(label);
        }
    }

    void updateButton() {
        Widget widget = getButtonWidget();
        widget.setVisible(command.getIsAvailable() && command.getIsVisible());
        if (widget instanceof HasEnabled) {
            ((HasEnabled)widget).setEnabled(command.getIsExecutionAllowed());
        }
        String label = getLabel();
        // Use prohibition reasons for tooltip if exist.
        String title = "";  //$NON-NLS-1$
        StringBuilder sb = new StringBuilder();
        if (!command.getExecuteProhibitionReasons().isEmpty()) {
            for (String reason : command.getExecuteProhibitionReasons()) {
                sb.append(reason).append(",");  //$NON-NLS-1$
            }
            title = sb.toString();
            if (title.length() != 0) {
                title = title.substring(0, title.length() -1);
            }
        } else {
            if (label != null && !label.isEmpty()) {
                title = label;
            }
            else {
                title = command.getTitle();
            }
        }
        getButtonWidget().setTitle(title);

        if (command.getTitle() == null) {
            setLabel(""); //$NON-NLS-1$
        }
        else if (label != null && label.equals("")) { //$NON-NLS-1$
            setLabel(command.getTitle());
        }
    }

    protected abstract Widget getButtonWidget();

}
