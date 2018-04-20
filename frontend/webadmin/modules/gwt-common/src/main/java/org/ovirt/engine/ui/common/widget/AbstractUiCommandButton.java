package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.common.view.popup.FocusableComponentsContainer;
import org.ovirt.engine.ui.uicommonweb.HasCleanup;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

/**
 * Abstract button widget bound to UiCommon {@linkplain UICommand command}.
 *
 * The command isn't invoked automatically when using the button. You must attach a command,
 * and then also invoke it in a handler (usually this logic goes in your Presenter). Example:
 *
 * <pre>
 * {@code
 * getView().getLoginButton().setCommand(loginModel.getLoginCommand());
 * registerHandler(getView().getLoginButton().addClickHandler(new ClickHandler() {
 *     @Override
 *     public void onClick(ClickEvent event) {
 *         getView().getLoginButton().getCommand().execute();
 *     }
 * }));
 * }
 * </pre>
 */
public abstract class AbstractUiCommandButton extends Composite
        implements HasUiCommandClickHandlers, HasLabel, FocusableComponentsContainer, HasCleanup {

    private final IEventListener<PropertyChangedEventArgs> listener = (ev, sender, args) -> updateButton();

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

        command.getPropertyChangedEvent().addListener(listener);

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

    protected void updateButton() {
        Widget widget = getButtonWidget();
        widget.setVisible(command.getIsAvailable() && command.getIsVisible());
        if (widget instanceof HasEnabled) {
            ((HasEnabled)widget).setEnabled(command.getIsExecutionAllowed());
        }
        String label = getLabel();

        if (command.getTitle() == null) {
            setLabel(""); //$NON-NLS-1$
        } else if (label != null && label.equals("")) { //$NON-NLS-1$
            setLabel(command.getTitle());
        }
    }

    protected abstract Widget getButtonWidget();

    @Override
    public void cleanup() {
        if (getCommand() != null) {
            getCommand().getPropertyChangedEvent().removeListener(listener);
        }
    }
}
