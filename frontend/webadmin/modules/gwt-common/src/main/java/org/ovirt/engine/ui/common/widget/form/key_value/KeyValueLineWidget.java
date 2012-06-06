package org.ovirt.engine.ui.common.widget.form.key_value;

import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueLineModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

public class KeyValueLineWidget extends Composite implements HasEditorDriver<KeyValueLineModel> {

    interface WidgetUiBinder extends UiBinder<Widget, KeyValueLineWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface Driver extends SimpleBeanEditorDriver<KeyValueLineModel, KeyValueLineWidget> {
        Driver driver = GWT.create(Driver.class);
    }

    @UiField
    WidgetStyle style;

    @UiField
    @Path(value = "keys.selectedItem")
    ListModelListBoxEditor<Object> keyField;

    @UiField
    @Path(value = "value.entity")
    EntityModelTextBoxEditor valueField;

    @UiField
    @Path(value = "values.selectedItem")
    ListModelListBoxEditor<Object> valuesField;

    @UiField
    @Ignore
    PushButton plusButton;

    @UiField
    @Ignore
    PushButton minusButton;

    KeyValueLineWidget() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        Driver.driver.initialize(this);
        addStyles();
    }

    private void addStyles() {
        keyField.addContentWidgetStyleName(style.fieldWidth());
        valueField.addContentWidgetStyleName(style.fieldWidth());
        valuesField.addContentWidgetStyleName(style.fieldWidth());
        hideLabels();
    }

    private void hideLabels() {
        keyField.hideLabel();
        valueField.hideLabel();
        valuesField.hideLabel();
    }

    @Override
    public void edit(final KeyValueLineModel object) {
        //        plusButton.setCommand(object.getAddLine());
        plusButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                object.getAddLine().Execute();
            }
        });

        minusButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                object.getRemoveLine().Execute();
            }
        });

        Driver.driver.edit(object);
    }

    @Override
    public KeyValueLineModel flush() {
        return Driver.driver.flush();
    }

    interface WidgetStyle extends CssResource {
        String fieldWidth();
    }

}
