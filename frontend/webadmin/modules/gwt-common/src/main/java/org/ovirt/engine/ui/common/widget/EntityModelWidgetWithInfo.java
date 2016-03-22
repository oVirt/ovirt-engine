package org.ovirt.engine.ui.common.widget;

import java.util.List;

import org.gwtbootstrap3.client.ui.Column;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.label.EnableableFormLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtml;
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

    @UiField
    Column contentColumn;

    Widget contentWidget;

    Align alignment;

    boolean usePatternfly = false;

    @Inject
    public EntityModelWidgetWithInfo(EnableableFormLabel label, Widget contentWidget) {
        this(label, contentWidget, Align.RIGHT);
    }

    public EntityModelWidgetWithInfo(EnableableFormLabel label, Widget contentWidget, Align alignment) {
        this.label = label;
        this.contentWidget = contentWidget;
        this.alignment = alignment;
        infoIcon = new InfoIcon(SafeHtmlUtils.EMPTY_SAFE_HTML);
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public void setExplanation(SafeHtml text) {
        infoIcon.setText(text);
    }

    public void setUsePatternFly(boolean usePatternFly) {
        this.usePatternfly = usePatternFly;
    }

    @Override
    public void onAttach() {
        super.onAttach();
        if (contentWidget instanceof AbstractValidatedWidgetWithLabel) {
            ((AbstractValidatedWidgetWithLabel<?, ?>)contentWidget).setUsePatternFly(usePatternfly);
        }
        if (alignment == Align.RIGHT) {
            contentColumn.add(contentWidget);
        } else if (alignment == Align.LEFT) {
            contentColumn.insert(contentWidget, 0);
            contentWidget.getElement().getStyle().setFloat(Style.Float.LEFT);
        }
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
