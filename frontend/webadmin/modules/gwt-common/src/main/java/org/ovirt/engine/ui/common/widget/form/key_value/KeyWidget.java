package org.ovirt.engine.ui.common.widget.form.key_value;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.common.widget.AddRemoveRowWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyLineModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class KeyWidget<T extends KeyModel> extends AddRemoveRowWidget<T, KeyLineModel, KeyLineWidget> {

    interface WidgetUiBinder extends UiBinder<Widget, KeyWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private T model;
    private final List<KeyLineWidget> widgets = new ArrayList<>();

    KeyWidget() {
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
        widgets.forEach(KeyLineWidget::flush);
        return super.flush();
    }

    @Override
    public void setUsePatternFly(boolean use) {
        super.setUsePatternFly(use);
        if (use) {
            contentPanel.removeStyleName(contentPanel.getStyleName());
        }
    }

    @Override
    protected KeyLineWidget createWidget(KeyLineModel value) {
        KeyLineWidget keyLineWidget = new KeyLineWidget();
        keyLineWidget.edit(value);
        keyLineWidget.setUsePatternFly(usePatternFly);
        widgets.add(keyLineWidget);
        return keyLineWidget;
    }

    @Override
    protected KeyLineModel createGhostValue() {
        return model.createNewLineModel();
    }

    @Override
    protected boolean isGhost(KeyLineModel value) {
        return !model.isKeyValid(value.getKeys().getSelectedItem());
    }

    @Override
    protected void toggleGhost(KeyLineModel value, KeyLineWidget widget, boolean becomingGhost) {
        if (!widget.isEnabled()) {
            return;
        }

        super.toggleGhost(value, widget, becomingGhost);
    }

    @Override
    protected void onRemove(KeyLineModel value, KeyLineWidget widget) {
        super.onRemove(value, widget);
        model.updateKeys();
        widgets.remove(widget);
    }

}
