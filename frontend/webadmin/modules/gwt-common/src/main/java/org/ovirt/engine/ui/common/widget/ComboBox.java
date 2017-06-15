package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelTextBoxEditor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Combines {@link ListModelListBoxEditor} and {@link EntityModelTextBoxEditor} into a single widget.
 */
public class ComboBox<T> extends Composite {

    interface WidgetUiBinder extends UiBinder<FlowPanel, ComboBox<?>> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField(provided = true)
    final ListModelListBoxEditor<T> listBoxEditor;

    @UiField(provided = true)
    final EntityModelTextBoxEditor<?> textBoxEditor;

    public ComboBox(ListModelListBoxEditor<T> listBoxEditor, EntityModelTextBoxEditor<?> textBoxEditor) {
        this.listBoxEditor = listBoxEditor;
        this.textBoxEditor = textBoxEditor;
        listBoxEditor.setUsePatternFly(true);
        textBoxEditor.setUsePatternFly(true);
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        this.listBoxEditor.hideLabel();
        this.textBoxEditor.hideLabel();
    }
}
