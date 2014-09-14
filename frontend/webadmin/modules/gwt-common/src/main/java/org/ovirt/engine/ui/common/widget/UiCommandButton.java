package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Widget;

public class UiCommandButton extends AbstractUiCommandButton implements Focusable {

    interface WidgetUiBinder extends UiBinder<Widget, UiCommandButton> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    SimpleDialogButton button;

    public UiCommandButton() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public UiCommandButton(String label) {
        this(label, null);
    }

    public UiCommandButton(ImageResource image) {
        this("", image); //$NON-NLS-1$
    }

    public UiCommandButton(String label, ImageResource image) {
        this();
        setLabel(label);
        setImage(image);
    }

    @Override
    protected ButtonBase getButtonWidget() {
        return button;
    }

    public void setImage(ImageResource image) {
        button.setImage(image);
    }

    public void setCustomContentStyle(String customStyle) {
        button.setCustomContentStyle(customStyle);
    }

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

    public void setEnabled(boolean enabled) {
        getButtonWidget().setEnabled(enabled);
    }
}
