package org.ovirt.engine.ui.common.widget.uicommon.users;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.RadioButton;
import org.ovirt.engine.core.searchbackend.VdcUserConditionFieldAutoCompleter.UserOrGroup;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class UsersTypeRadioGroup extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, UsersTypeRadioGroup> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private final List<UserTypeChangeHandler> changeHandlers = new ArrayList<>();

    @UiField
    RadioButton users;

    @UiField
    RadioButton groups;

    public UsersTypeRadioGroup() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @UiHandler("users")
    void handleUsersClick(ClickEvent event) {
        fireTypeChangedHandlers(UserOrGroup.User);
    }

    @UiHandler("groups")
    void handleGroupsClick(ClickEvent event) {
        fireTypeChangedHandlers(UserOrGroup.Group);
    }

    public void addChangeHandler(UserTypeChangeHandler handler) {
        if (!changeHandlers.contains(handler)) {
            changeHandlers.add(handler);
        }
    }

    public void updateSelectedValue(UserOrGroup newType, boolean fireEvents) {
        switch (newType) {
            case User:
                users.setValue(true, fireEvents);
                break;
            case Group:
                groups.setValue(true, fireEvents);
        }
    }

    private void fireTypeChangedHandlers(UserOrGroup newType) {
        for (UserTypeChangeHandler handler: changeHandlers) {
            handler.userTypeChanged(newType);
        }
    }
}
