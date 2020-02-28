package org.ovirt.engine.ui.common.widget;

import java.util.List;

import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.label.EnableableFormLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;

public class EntityModelWidgetWithInfo extends Composite implements HasValidation, HasEnabled {

    interface WidgetUiBinder extends UiBinder<Widget, EntityModelWidgetWithInfo> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    protected interface Style extends CssResource {
        String floatLeft();
        String overflowAuto();
    }

    @UiField(provided = true)
    EnableableFormLabel label;

    @UiField(provided = true)
    InfoIcon infoIcon;

    @UiField
    FlowPanel labelPanel;

    @UiField
    FlowPanel container;

    @UiField
    FlowPanel contentWidgetContainer;

    @UiField
    Style style;

    protected Widget contentWidget;

    Align alignment;
    boolean usePatternfly = false;

    public EntityModelWidgetWithInfo(EnableableFormLabel label, Widget contentWidget) {
        this(label, contentWidget, Align.RIGHT);
    }

    /**
     * The content will provide its own label.
     * @param contentWidget The content widget
     * @param alignment The alignment (LEFT/RIGHT);
     */
    public EntityModelWidgetWithInfo(Widget contentWidget, Align alignment) {
        this(null, contentWidget, alignment);
    }

    public EntityModelWidgetWithInfo(EnableableFormLabel label, Widget contentWidget, Align alignment) {
        if (label != null) {
            this.label = label;
        } else {
            this.label = new EnableableFormLabel();
            this.label.setVisible(false);
        }
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

    public void setLabelColSize(ColumnSize size) {
        labelPanel.addStyleName(size.getCssName());
        switchToBootstrapMode();
    }

    public void setWidgetColSize(ColumnSize size) {
        contentWidgetContainer.addStyleName(size.getCssName());
        switchToBootstrapMode();
    }

    private void switchToBootstrapMode() {
        container.removeStyleName(style.overflowAuto());
    }

    @Override
    public void onAttach() {
        super.onAttach();
        if (contentWidget instanceof PatternFlyCompatible) {
            ((PatternFlyCompatible)contentWidget).setUsePatternFly(usePatternfly);
        }
        if (alignment == Align.LEFT) {
            labelPanel.insert(contentWidgetContainer, 0);
            contentWidgetContainer.addStyleName(style.floatLeft());
        }

        contentWidgetContainer.add(contentWidget);
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

    public void setAddInfoIconStyleName(String styleName) {
        infoIcon.addStyleName(styleName);
    }

    public void setLabel(String text) {
        label.setText(text);
    }

    public void hideInfoIcon() {
        infoIcon.setVisible(false);
    }

    public void showInfoIcon() {
        infoIcon.setVisible(true);
    }
}
