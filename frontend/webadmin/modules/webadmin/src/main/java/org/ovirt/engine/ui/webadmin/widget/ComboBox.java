package org.ovirt.engine.ui.webadmin.widget;

import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.editor.ListModelListBoxEditor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ComboBox extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, ComboBox> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    WidgetStyle style;

    @UiField(provided = true)
    ListModelListBoxEditor<Object> listBoxEditor;

    @UiField(provided = true)
    EntityModelTextBoxEditor textBoxEditor;

    public ComboBox(ListModelListBoxEditor<Object> listBoxEditor, EntityModelTextBoxEditor textBoxEditor) {
        this.listBoxEditor = listBoxEditor;
        this.textBoxEditor = textBoxEditor;
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        addStyles();
    }

    void addStyles() {
        listBoxEditor.addLabelStyleName(style.listBoxLabel());
        textBoxEditor.addLabelStyleName(style.textBoxLabel());
        textBoxEditor.addContentWidgetStyleName(style.textBoxWidget());

        Element textBox = textBoxEditor.getContentWidgetContainer().getElement();
        Element input = textBox.getElementsByTagName("input").getItem(0);
        input.addClassName(style.textBoxInput());

        Element selectBox = listBoxEditor.getContentWidgetContainer().getElement();
        Element select = selectBox.getElementsByTagName("select").getItem(0);
        select.addClassName(style.selectBoxInput());
    }

    interface WidgetStyle extends CssResource {
        String textBoxWidget();

        String textBoxInput();

        String selectBoxInput();

        String textBoxLabel();

        String listBoxLabel();
    }
}
