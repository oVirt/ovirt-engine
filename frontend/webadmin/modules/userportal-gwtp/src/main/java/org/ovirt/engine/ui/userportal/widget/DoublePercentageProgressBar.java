package org.ovirt.engine.ui.userportal.widget;

import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class DoublePercentageProgressBar extends Composite implements IsEditor<TakesValueEditor<Object>>, TakesValue<Object>, HasElementId {

    private static final String ZERO = "0%"; //$NON-NLS-1$
    private static final int FULL_WIDTH = 99;
    private static final int MINIMUM_SIZE_TO_SHOW_TEXT = 10;

    protected WidgetTooltip tooltip;

    interface WidgetUiBinder extends UiBinder<Widget, DoublePercentageProgressBar> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    public DoublePercentageProgressBar() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        this.tooltip = new WidgetTooltip(this);
    }

    private Integer valueA;
    private Integer valueB;

    @UiField
    WidgetStyle style;

    @UiField
    Label percentageLabelA;

    @UiField
    Label percentageLabelB;

    @UiField
    FlowPanel percentageBarA;

    @UiField
    FlowPanel percentageBarB;

    private SafeHtml tooltipText;

    @Override
    public void setValue(Object value) {
        assert value instanceof Integer : "Only integer values are accepted"; //$NON-NLS-1$
    }

    @Override
    public Object getValue() {
        return valueB;
    }

    public void setValueA(Integer value) {
        this.valueA = value;
    }

    public void setValueB(Integer value) {
        this.valueB = value;
    }

    public void setZeroValue() {
        percentageBarB.setVisible(false);
        percentageBarB.setWidth("0px"); //$NON-NLS-1$
        percentageBarA.setVisible(true);
        tooltip.setHtml(tooltipText);
        tooltip.reconfigure();
        percentageBarA.setStyleName(style.empty());
        percentageBarA.setWidth(FULL_WIDTH + "%"); //$NON-NLS-1$
        percentageLabelA.setText(ZERO);
        percentageLabelA.setStyleName(style.percentageLabelBlack());
    }

    public void setBars() {
        if (valueA != null && valueB != null) {
            int fakeA = valueA;
            int fakeB = valueB;
            if (valueA + valueB >= FULL_WIDTH) {
                double factor = (double) (FULL_WIDTH - 1) / (valueA + valueB);
                fakeA = (int) Math.round(factor * valueA);
                fakeB = (int) Math.round(factor * valueB);

                fakeA = fakeB == 0 ? FULL_WIDTH : fakeA;
                fakeB = fakeA == 0 ? FULL_WIDTH : fakeB;
            }

            setBar(percentageBarA, percentageLabelA, valueA, fakeA, style.percentageLabelBlack());
            setBar(percentageBarB, percentageLabelB, valueB, fakeB, style.percentageLabel());
        }
    }

    private void setBar(FlowPanel percentageBar, Label percentageLabel, Integer value, int fakeValue, String style){
        if (value != null) {
            String percentage = value + "%"; //$NON-NLS-1$
            String fakePercentage = fakeValue + "%"; //$NON-NLS-1$
            percentageLabel.setText(value < MINIMUM_SIZE_TO_SHOW_TEXT ? "" : percentage); //$NON-NLS-1$
            percentageLabel.setStyleName(style);
            percentageBar.setWidth(fakePercentage);
            percentageBar.setVisible(value != 0);
        }
    }

    public Object getValueA() {
        return valueA;
    }

    public Object getValueB() {
        return valueB;
    }

    @Override
    public TakesValueEditor<Object> asEditor() {
        return TakesValueEditor.of(this);
    }

    @Override
    public void setElementId(String elementId) {
        // Set percentage label element ID
        percentageLabelB.getElement().setId(elementId);
    }

    interface WidgetStyle extends CssResource {

        String percentageBarUnlimited();

        String percentageBarExceeded();

        String empty();

        String percentageBarA();

        String percentageLabelBlack();

        String percentageLabel();
    }

    public void setTooltipText(SafeHtml tooltipText) {
        this.tooltipText = tooltipText;
    }
}
