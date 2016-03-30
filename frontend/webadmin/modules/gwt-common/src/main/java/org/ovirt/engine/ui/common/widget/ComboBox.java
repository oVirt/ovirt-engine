package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelTextBoxEditor;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Combines {@link ListModelListBoxEditor} and {@link EntityModelTextBoxEditor} into a single widget.
 */
public class ComboBox<T> extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, ComboBox<?>> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    WidgetStyle style;

    @UiField(provided = true)
    ListModelListBoxEditor<T> listBoxEditor;

    @UiField(provided = true)
    EntityModelTextBoxEditor<?> textBoxEditor;

    public ComboBox(ListModelListBoxEditor<T> listBoxEditor, EntityModelTextBoxEditor<?> textBoxEditor) {
        this.listBoxEditor = listBoxEditor;
        this.textBoxEditor = textBoxEditor;
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public void setUsePatternFly(boolean use) {
        listBoxEditor.setUsePatternFly(use);
        textBoxEditor.setUsePatternFly(use);
        listBoxEditor.addLabelStyleName(style.listBoxLabel());
        textBoxEditor.addLabelStyleName(style.textBoxLabel());
        if (!use) {
            addStyles();
        } else {
            textBoxEditor.removeStyleName(style.textBoxEditor());
            textBoxEditor.removeStyleName(style.textBoxWidget());
        }
    }

    void addStyles() {
        textBoxEditor.addContentWidgetContainerStyleName(style.textBoxWidget());

        Element textBox = textBoxEditor.getContentWidgetContainer().getElement();
        Element input = textBox.getElementsByTagName("input").getItem(0); //$NON-NLS-1$
        input.addClassName(style.textBoxInput());

        Element selectBox = listBoxEditor.getContentWidgetContainer().getElement();
        Element select = selectBox.getElementsByTagName("select").getItem(0); //$NON-NLS-1$
        select.addClassName(style.selectBoxInput());
    }

    interface WidgetStyle extends CssResource {
        String textBoxWidget();

        String textBoxInput();

        String selectBoxInput();

        String textBoxLabel();

        String listBoxLabel();

        String textBoxEditor();
    }

}
