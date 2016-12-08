package org.ovirt.engine.ui.common.widget;

import java.util.List;

import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.label.EnableableFormLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityModelWidgetWithInfo extends Composite implements HasValidation, HasEnabled {

    interface WidgetUiBinder extends UiBinder<Widget, EntityModelWidgetWithInfo> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField(provided = true)
    EnableableFormLabel label;

    @UiField(provided = true)
    InfoIcon infoIcon;

    @UiField(provided = true)
    Widget contentWidget;

    @Inject
    public EntityModelWidgetWithInfo(EnableableFormLabel label, Widget contentWidget) {
        this.label = label;
        this.contentWidget = contentWidget;
        infoIcon = new InfoIcon(SafeHtmlUtils.EMPTY_SAFE_HTML);
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public void setExplanation(String text) {
        infoIcon.setTitle(text);
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

    @Override
    public void setEnabled(boolean enabled) {
        label.setEnabled(enabled);
        if (contentWidget instanceof HasEnabled) {
            HasEnabled enableableWidget = (HasEnabled) this.contentWidget;
            enableableWidget.setEnabled(enabled);
        }
    }

    @Override
    public boolean isEnabled() {
        return label.isEnabled();
    }
}
