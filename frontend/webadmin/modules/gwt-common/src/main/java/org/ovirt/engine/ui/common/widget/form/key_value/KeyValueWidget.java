package org.ovirt.engine.ui.common.widget.form.key_value;

import java.util.LinkedList;

import org.ovirt.engine.ui.common.widget.AddRemoveRowWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.BaseKeyModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueLineModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class KeyValueWidget<T extends BaseKeyModel> extends AddRemoveRowWidget<T, KeyValueLineModel, KeyValueLineWidget> {

    interface WidgetUiBinder extends UiBinder<Widget, KeyValueWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private T model;
    private final LinkedList<KeyValueLineWidget> widgets = new LinkedList<KeyValueLineWidget>();
    private boolean enabled = true;
    String rowWidth = null;

    KeyValueWidget() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public KeyValueWidget(String rowWidth) {
        this();
        this.rowWidth = rowWidth;
    }

    @Override
    protected void init(T model) {
        this.model = model;
        widgets.clear();
        super.init(model);

        if (!enabled) { // get rid of ghost entry if it exists and the widget isn't editable
            KeyValueLineWidget lastWidget = widgets.getLast();
            if (isGhost(lastWidget.flush())) {
                removeWidget(lastWidget);
            }
        }
    }

    @Override
    public T flush() {
        super.flush();
        for (KeyValueLineWidget lineWidget : widgets) {
            lineWidget.flush();
        }
        return model;
    }

    public void setEnabled(boolean value) {
        enabled = value;
        for (KeyValueLineWidget widget : widgets) {
            widget.setEnabled(enabled);
        }
    }

    @Override
    protected KeyValueLineWidget createWidget(KeyValueLineModel value) {
        KeyValueLineWidget keyValueLineWidget = new KeyValueLineWidget(rowWidth);
        keyValueLineWidget.edit(value);
        widgets.add(keyValueLineWidget);
        return keyValueLineWidget;
    }

    @Override
    protected KeyValueLineModel createGhostValue() {
        return model.createNewLineModel();
    }

    @Override
    protected boolean isGhost(KeyValueLineModel value) {
        return !model.isKeyValid(value.getKeys().getSelectedItem());
    }

    @Override
    protected void toggleGhost(KeyValueLineModel value, KeyValueLineWidget widget, boolean becomingGhost) {
        widget.setEnabled(!becomingGhost && enabled);
        widget.keyField.setEnabled(enabled);
        setButtonEnabled(widget, !becomingGhost && enabled);
    }

    @Override
    protected void onRemove(KeyValueLineModel value, KeyValueLineWidget widget) {
        super.onRemove(value, widget);
        model.updateKeys();
        widgets.remove(widget);
    }

}
