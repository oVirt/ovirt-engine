package org.ovirt.engine.ui.common.widget;

import java.util.List;

import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityModelWidgetWithInfo<T> extends Composite implements HasValidation {

    interface WidgetUiBinder extends UiBinder<Widget, EntityModelWidgetWithInfo> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField(provided = true)
    EntityModelLabel<T> label;

    @UiField(provided = true)
    InfoIcon infoIcon;

    @UiField(provided = true)
    Composite contentWidget;

    @Inject
    public EntityModelWidgetWithInfo(EntityModelLabel<T> label, Composite contentWidget) {

        this.label = label;
        this.contentWidget = contentWidget;
        infoIcon = new InfoIcon(SafeHtmlUtils.EMPTY_SAFE_HTML);
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public void setExplanation(SafeHtml text) {
        infoIcon.setText(text);
    }

    @Override
    public void markAsValid() {
        if (contentWidget instanceof HasValidation) {
            ((HasValidation) contentWidget).markAsValid();
        }
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        if (contentWidget instanceof HasValidation) {
            ((HasValidation) contentWidget).markAsInvalid(validationHints);
        }
    }

    @Override
    public boolean isValid() {
        if (contentWidget instanceof HasValidation) {
            return ((HasValidation) contentWidget).isValid();
        }

        return true;
    }
}
