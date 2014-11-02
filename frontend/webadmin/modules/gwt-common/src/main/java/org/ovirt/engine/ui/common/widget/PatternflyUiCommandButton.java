package org.ovirt.engine.ui.common.widget;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class PatternflyUiCommandButton extends AbstractUiCommandButton {

    interface WidgetUiBinder extends UiBinder<Widget, PatternflyUiCommandButton> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    Button button;

    public PatternflyUiCommandButton() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    protected Widget getButtonWidget() {
        return button;
    }

    public void setTabIndex(int tabIndex) {
        button.getElement().setTabIndex(tabIndex);
    }

    @Override
    public int setTabIndexes(int nextTabIndex) {
        setTabIndex(nextTabIndex);
        return ++nextTabIndex;
    }
}
