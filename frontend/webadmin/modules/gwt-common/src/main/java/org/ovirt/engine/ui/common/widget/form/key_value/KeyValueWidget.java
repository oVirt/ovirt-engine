package org.ovirt.engine.ui.common.widget.form.key_value;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.common.widget.AddRemoveRowWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueLineModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class KeyValueWidget<T extends KeyValueModel> extends AddRemoveRowWidget<T, KeyValueLineModel, KeyValueLineWidget> {

    interface WidgetUiBinder extends UiBinder<Widget, KeyValueWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private T model;
    private final List<KeyValueLineWidget> widgets = new ArrayList<>();

    KeyValueWidget() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    protected void init(T model) {
        this.model = model;
        widgets.clear();
        super.init(model);
    }

    @Override
    public T flush() {
        super.flush();
        for (KeyValueLineWidget lineWidget : widgets) {
            lineWidget.flush();
        }
        return model;
    }

    @Override
    public void setUsePatternFly(boolean use) {
        super.setUsePatternFly(use);
        if (use) {
            contentPanel.removeStyleName(contentPanel.getStyleName());
        }
    }

    @Override
    protected KeyValueLineWidget createWidget(KeyValueLineModel value) {
        KeyValueLineWidget keyValueLineWidget = new KeyValueLineWidget();
        keyValueLineWidget.edit(value);
        keyValueLineWidget.setUsePatternFly(usePatternFly);
        widgets.add(keyValueLineWidget);
        return keyValueLineWidget;
    }

    @Override
    protected KeyValueLineModel createGhostValue() {
        return model.createNewLineModel();
    }

    @Override
    protected boolean isGhost(KeyValueLineModel value) {
        if (model.isEditableKey()) {
            return false;
        }
        return !model.isKeyValid(value.getKeys().getSelectedItem());
    }

    @Override
    protected void toggleGhost(KeyValueLineModel value, KeyValueLineWidget widget, boolean becomingGhost) {
        if (!widget.isEnabled()) {
            return;
        }

        super.toggleGhost(value, widget, becomingGhost);
        widget.valueField.setEnabled(!becomingGhost);
        widget.valuesField.setEnabled(!becomingGhost);
    }

    @Override
    protected void onRemove(KeyValueLineModel value, KeyValueLineWidget widget) {
        super.onRemove(value, widget);
        model.updateKeys();
        widgets.remove(widget);
    }

}
