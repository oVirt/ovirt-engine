package org.ovirt.engine.ui.common.widget.editor;

import java.util.Date;

import org.gwtbootstrap3.extras.datetimepicker.client.ui.DateTimePicker;
import org.gwtbootstrap3.extras.datetimepicker.client.ui.base.constants.DateTimePickerView;

import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public class GwtBootstrapDateTimePicker implements IsWidget, TakesValue<Date> {
    public static final String DEFAULT_DATE_TIME_FORMAT = "mm/dd/yyyy hh:ii";//$NON-NLS-1$
    public static final String DEFAULT_TIME_FORMAT = "hh:ii P";//$NON-NLS-1$
    public static final String DEFAULT_DATE_FORMAT = "mm/dd/yyyy";//$NON-NLS-1$
    private final DateTimePicker dateTimePicker = new DateTimePicker();
    private String dateTimeFormat;

    public GwtBootstrapDateTimePicker() {
        this(DEFAULT_DATE_TIME_FORMAT, true);
    }

    public GwtBootstrapDateTimePicker(String format, boolean autoClose) {
        dateTimePicker.setFormat(format);
        dateTimePicker.setAutoClose(autoClose);
        dateTimePicker.setShowTodayButton(true);
        this.dateTimeFormat = format;
        this.dateTimePicker.addChangeDateHandler(evt -> ValueChangeEvent.fire(dateTimePicker, dateTimePicker.getValue()));
    }

    public String getDateTimeFormat() {
        return dateTimeFormat;
    }

    public void setDateTimeFormat(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
        dateTimePicker.setFormat(dateTimeFormat);
        dateTimePicker.reload();
    }

    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> handler) {
        return dateTimePicker.addValueChangeHandler(handler);
    }

    public void setValue(Date value, boolean fireEvents) {
        dateTimePicker.setValue(value, fireEvents);
    }

    public void showDateOnly() {
        show(DateTimePickerView.MONTH, DateTimePickerView.MONTH, DateTimePickerView.MONTH);
    }

    public void showTimeOnly() {
        show(DateTimePickerView.DAY, DateTimePickerView.HOUR, DateTimePickerView.DAY);
    }

    public void showDateAndTime() {
        show(DateTimePickerView.MONTH, DateTimePickerView.HOUR, DateTimePickerView.MONTH);
    }

    public void show(DateTimePickerView startView, DateTimePickerView minView, DateTimePickerView maxView) {
        dateTimePicker.setStartView(maxView);
        dateTimePicker.setMinView(minView);
        dateTimePicker.setMaxView(maxView);
        dateTimePicker.reload();
    }

    @Override
    public void setValue(Date value) {
        dateTimePicker.setValue(value);
    }

    public Widget asWidget() {
        return dateTimePicker.asWidget();
    }

    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
        return dateTimePicker.addDomHandler(handler, KeyUpEvent.getType());
    }

    public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
        return dateTimePicker.addDomHandler(handler, KeyDownEvent.getType());
    }

    public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
        return dateTimePicker.addDomHandler(handler, KeyPressEvent.getType());
    }

    public void fireEvent(GwtEvent<?> event) {
        dateTimePicker.fireEvent(event);
    }

    @Override
    public Date getValue() {
        return dateTimePicker.getValue();
    }

    public void setFocus(boolean focused) {
        dateTimePicker.getTextBox().setFocus(focused);
    }

    public void setDateRange(Date startDate, Date stopDate) {
        dateTimePicker.setStartDate(startDate);
        dateTimePicker.setEndDate(stopDate);
    }
}
