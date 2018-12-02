package org.ovirt.engine.ui.common.widget.form.key_value;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueLineModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class KeyValueLineWidget extends BaseKeyLineWidget<KeyValueLineModel> {

    interface WidgetUiBinder extends UiBinder<Widget, KeyValueLineWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface Driver extends UiCommonEditorDriver<KeyValueLineModel, KeyValueLineWidget> {
    }

    protected final Driver driver = GWT.create(Driver.class);

    @UiField
    @Path(value = "value.entity")
    StringEntityModelTextBoxEditor valueField;

    @UiField
    @Path(value = "values.selectedItem")
    ListModelListBoxEditor<String> valuesField;

    @UiField
    @Path(value = "editableKey.entity")
    StringEntityModelTextBoxEditor editableKeyField;

    @UiField
    @Path(value = "passwordValueField.entity")
    StringEntityModelPasswordBoxEditor passwordValueField;

    KeyValueLineWidget() {
        super();
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        init();
    }

    @Override
    protected void init() {
        driver.initialize(this);
        hideLabels();
    }

    @Override
    protected void hideLabels() {
        super.hideLabels();
        valueField.hideLabel();
        valuesField.hideLabel();
        editableKeyField.hideLabel();
        passwordValueField.hideLabel();
    }

    @Override
    public void setUsePatternFly(boolean use) {
        super.setUsePatternFly(use);
        valueField.setUsePatternFly(use);
        valuesField.setUsePatternFly(use);
        editableKeyField.setUsePatternFly(use);
        passwordValueField.setUsePatternFly(use);
    }

    @Override
    public void edit(final KeyValueLineModel object) {
        super.doEdit(object);
        driver.edit(object);
    }

    @Override
    public void setEnabled(boolean enabled) {
        keyField.setEnabled(enabled);
        valueField.setEnabled(enabled);
        valuesField.setEnabled(enabled);
        valuesField.setEnabled(enabled);
        editableKeyField.setEnabled(enabled);
        passwordValueField.setEnabled(enabled);
    }

    @Override
    public KeyValueLineModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}
