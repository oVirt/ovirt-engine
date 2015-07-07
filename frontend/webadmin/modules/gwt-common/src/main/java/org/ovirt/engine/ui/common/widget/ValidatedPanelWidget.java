package org.ovirt.engine.ui.common.widget;

import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class ValidatedPanelWidget extends AbstractValidatedWidget implements HasWidgets {

    @UiField
    FlowPanel panel;

    interface WidgetUiBinder extends UiBinder<Widget, ValidatedPanelWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    public ValidatedPanelWidget() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public ValidatedPanelWidget(Widget contentWidget) {
        this();
        setWidget(contentWidget);
    }

    public void setWidget(Widget widget) {
        panel.add(widget);
    }

    @Override
    public void add(Widget w) {
        panel.add(w);
    }

    @Override
    public void clear() {
        panel.clear();
    }

    @Override
    public Iterator<Widget> iterator() {
        return panel.iterator();
    }

    @Override
    public boolean remove(Widget w) {
        return panel.remove(w);
    }

    @Override
    protected Widget getValidatedWidget() {
        return panel;
    }
}
