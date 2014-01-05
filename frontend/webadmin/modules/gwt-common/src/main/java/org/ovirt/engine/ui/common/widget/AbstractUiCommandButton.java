package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.common.view.popup.FocusableComponentsContainer;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Focusable;

/**
 * Abstract button widget bound to UiCommon {@linkplain UICommand command}.
 */
public abstract class AbstractUiCommandButton extends Composite
        implements HasUiCommandClickHandlers, HasLabel, Focusable, FocusableComponentsContainer {

    private UICommand command;

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return getButtonWidget().addClickHandler(handler);
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
        return getButtonWidget().getText();
    }

    @Override
    public void setLabel(String label) {
        getButtonWidget().setText(label);
    }

    void updateButton() {
        getButtonWidget().setVisible(command.getIsAvailable() && command.getIsVisible());
        getButtonWidget().setEnabled(command.getIsExecutionAllowed());

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
            title = !getButtonWidget().getText().equals("") ? //$NON-NLS-1$
                    getButtonWidget().getText() : command.getTitle();
        }
        getButtonWidget().setTitle(title);

        if (command.getTitle() == null) {
            getButtonWidget().setText(""); //$NON-NLS-1$
        }
        else if (getButtonWidget().getText().equals("")) { //$NON-NLS-1$
            getButtonWidget().setText(command.getTitle());
        }
    }

    protected abstract ButtonBase getButtonWidget();

    @Override
    public int getTabIndex() {
        return getButtonWidget().getTabIndex();
    }

    @Override
    public void setAccessKey(char key) {
        getButtonWidget().setAccessKey(key);
    }

    @Override
    public void setFocus(boolean focused) {
        getButtonWidget().setFocus(focused);
    }

    @Override
    public void setTabIndex(int index) {
        getButtonWidget().setTabIndex(index);
    }

    @Override
    public int setTabIndexes(int nextTabIndex) {
        setTabIndex(nextTabIndex++);
        return nextTabIndex;
    }

}
