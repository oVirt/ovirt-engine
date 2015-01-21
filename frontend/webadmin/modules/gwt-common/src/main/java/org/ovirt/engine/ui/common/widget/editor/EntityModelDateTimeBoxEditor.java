package org.ovirt.engine.ui.common.widget.editor;

import java.util.Date;
import java.util.List;

import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;

import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.editor.client.IsEditor;

/**
 * EntityModel bound composite editor that uses {@link EntityModelDateTimeBox}.
 */
public class EntityModelDateTimeBoxEditor extends AbstractValidatedWidgetWithLabel<Date, EntityModelDateTimeBox> implements IsEditor<WidgetWithLabelEditor<Date, EntityModelDateTimeBoxEditor>>{

    private final WidgetWithLabelEditor<Date, EntityModelDateTimeBoxEditor> editor;

    public EntityModelDateTimeBoxEditor() {
        this(true, true);
    }

    public EntityModelDateTimeBoxEditor(boolean dateRequired, boolean timeRequired) {
        super(new EntityModelDateTimeBox(dateRequired, timeRequired));
        this.editor = WidgetWithLabelEditor.of(getContentWidget().asEditor(), this);
    }

    public DateTimeBox asDateBox() {
        return getContentWidget();
    }

    @Override
    public EntityModelDateTimeBox getContentWidget() {
        return super.getContentWidget();
    }

    @Override
    public WidgetWithLabelEditor<Date, EntityModelDateTimeBoxEditor> asEditor() {
        return editor;
    }

    @Override
    public void markAsValid() {
        super.markAsValid();
        getValidatedWidgetStyle().setBorderStyle(BorderStyle.NONE);
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        super.markAsInvalid(validationHints);
        getValidatedWidgetStyle().setBorderStyle(BorderStyle.SOLID);
    }

}
