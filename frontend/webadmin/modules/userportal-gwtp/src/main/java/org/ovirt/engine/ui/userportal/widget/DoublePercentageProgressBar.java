package org.ovirt.engine.ui.userportal.widget;

import com.google.gwt.resources.client.CssResource;
import org.ovirt.engine.ui.common.idhandler.HasElementId;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class DoublePercentageProgressBar extends Composite implements IsEditor<TakesValueEditor<Object>>, TakesValue<Object>, HasElementId {

    private static final String ZERO = "0%"; //$NON-NLS-1$
    private String title;

    interface WidgetUiBinder extends UiBinder<Widget, DoublePercentageProgressBar> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    public DoublePercentageProgressBar() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    private Object valueA;
    private Object valueB;

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
        String percentage = value + "%"; //$NON-NLS-1$
        percentageLabelA.setText(value < 10 ? "" : percentage); //$NON-NLS-1$
        percentageLabelA.setStyleName(style.percentageLabel());
        percentageBarA.setWidth(percentage);
        percentageBarA.setVisible(value != 0);
        percentageBarA.setTitle(percentage);
    }

    public void setValueB(Integer value) {
        this.valueB = value;
        String percentage = value + "%"; //$NON-NLS-1$
        percentageLabelB.setText(value < 10 ? "" : percentage); //$NON-NLS-1$
        percentageBarB.setWidth(percentage);
        percentageBarB.setVisible(value != 0);
        percentageBarB.setTitle(percentage);
    }

    public void setZeroValue() {
        percentageBarA.setVisible(true);
        percentageBarA.setTitle(title);
        percentageBarA.setStyleName(style.empty());
        percentageBarA.setWidth("99%"); //$NON-NLS-1$
        percentageLabelA.setText(ZERO);
        percentageLabelA.setStyleName(style.percentageLabelBlack());
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

        String empty();

        String percentageBarA();

        String percentageLabelBlack();

        String percentageLabel();
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
        super.setTitle(title);
    }
}
