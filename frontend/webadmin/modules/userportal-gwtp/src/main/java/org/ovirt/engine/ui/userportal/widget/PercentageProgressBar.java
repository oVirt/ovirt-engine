package org.ovirt.engine.ui.userportal.widget;

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

public class PercentageProgressBar extends Composite implements IsEditor<TakesValueEditor<Object>>, TakesValue<Object>, HasElementId {

    interface WidgetUiBinder extends UiBinder<Widget, PercentageProgressBar> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    public PercentageProgressBar() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    private Object value;

    @UiField
    Label percentageLabel;

    @UiField
    FlowPanel percentageBar;

    @Override
    public void setValue(Object value) {
        assert value instanceof Integer : "Only integer values are accepted"; //$NON-NLS-1$
        setValue((Integer) value);
    }

    public void setValue(Integer value) {
        this.value = value;
        String percentage = value + "%"; //$NON-NLS-1$
        percentageLabel.setText(percentage);
        percentageBar.setWidth(percentage);
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public TakesValueEditor<Object> asEditor() {
        return TakesValueEditor.of(this);
    }

    @Override
    public void setElementId(String elementId) {
        // Set percentage label element ID
        percentageLabel.getElement().setId(elementId);
    }

}
