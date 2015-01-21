package org.ovirt.engine.ui.common.widget.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.ovirt.engine.ui.common.widget.renderer.StringRenderer;

import com.google.gwt.core.client.JsDate;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.datepicker.client.DateBox;

/**
 * The DateTimeBox allows the user to select a date and a time.<br>
 * It currently utilises the gwt DateBox for the date component and has the following 3 dropdowns<br>
 * 1. Hours component of time.<br>
 * 2. Minutes component of time.<br>
 * 3. Am/Pm (This widget hence assumes 12hr format on display to user and 24 hour format while returning the selected
 * date object)<br>
 */

public class DateTimeBox extends Composite implements TakesValue<Date>, HasValue<Date> {

    private static final int HOUR_CONSTANT = 24;
    private static final int MINUTE_MAX = 59;
    private static final int constantRowIndex = 0;

    private DateTimeFormat dateFormat;

    private FlowPanel wrapperPanel = new FlowPanel();
    FlexTable containerTable = new FlexTable();

    private DateBox dateBox;
    private ValueListBox<Integer> hoursBox;
    private ValueListBox<Integer> minutesBox;

    private int minuteSteps = 5;
    private int hours = 1;
    private int minutes = 0;
    private int hourSteps = 1;

    private Date selectedDate = new Date();

    private int currentColumnMax = 0;

    private boolean timeRequired;
    private boolean dateRequired;

    /**
     * Use this if both date and time components of the widget are required. And the default date format of dd-MMM-yyyy
     * is required.
     */
    public DateTimeBox() {
        this(true, true, DateTimeFormat.getFormat("dd-MMM-yyyy"));//$NON-NLS-1$
    }

    /**
     * Use if any/both of the components are required.(Date and time). If only one of the components is required, then
     * the other component would be assumed to be the value as returned by the new Date object which is the browser's
     * value.
     */
    public DateTimeBox(boolean dateRequired, boolean timeRequired) {
        this(dateRequired, timeRequired, DateTimeFormat.getFormat("dd-MMM-yyyy"));//$NON-NLS-1$
    }

    /**
     * Use if date is required but not in default format of dd-MMM-yyyy
     */
    public DateTimeBox(boolean timeRequired, DateTimeFormat dateFormat) {
        this(true, timeRequired, dateFormat);
    }

    private DateTimeBox(boolean dateRequired, boolean timeRequired, DateTimeFormat dateFormat) {
        this.dateRequired = dateRequired;
        this.timeRequired = timeRequired;
        this.dateFormat = dateFormat;
        showDateTimeBox(dateRequired, timeRequired);
        initWidget(wrapperPanel);
    }

    /**
     * Set the required date and time in accordance with the date passed as the parameter. This is invoked in 2 cases : <br>
     * 1. If user invokes setSelectedItem from the mapped ListModel<br>
     * 2. UiCommonEditorVisitor on "ItemsChanged" for default first item selection.<br>
     * Just In case if this is invoked with no checkboxes present, it will create one and set it selected (Possible if
     * the setSelectedItem of the mapped ListModel is invoked before setItems).
     */
    @Override
    public void setValue(Date value, boolean fireEvents) {
        if (value == null) {
            return;
        }
        dateBox.setValue(value);

        JsDate tDate = JsDate.create(value.getTime());
        hours = tDate.getHours();
        minutes = tDate.getMinutes();
        selectedDate = new Date((long) tDate.getTime());

        hoursBox.setValue(hours);
        minutesBox.setValue(getClosestMinuteInBox(minutes));

        if (!selectedDate.equals(value)) {
            setSelectedDate();
        }
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    private Integer getClosestMinuteInBox(int minutes) {
        int minuteDistance = minutes % minuteSteps;
        int nthNeighbour = minutes / minuteSteps;
        if (minuteDistance > 2) {
            nthNeighbour += 1;
        }
        return minuteSteps * nthNeighbour == 60 ? 0 : minuteSteps * nthNeighbour;
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Select the required date in accordance with the date passed. This is invoked through EntityModel when setEntity
     * is done using the EntityModel bound to this widget.
     */
    @Override
    public void setValue(Date value) {
        setValue(value, false);
    }

    /**
     * Used to get the date selected in the widget<br>
     * Date object is returned and hence, if the api user decides to use only the time component of the widget,<br>
     * the returned value will contain the time component as selected in the widget but/and with the date component
     * equal to that of the browser's date.
     */
    @Override
    public Date getValue() {
        return selectedDate;
    }

    private void showDateTimeBox(boolean dateRequired, boolean timeRequired) {
        initDateBox();
        placeDateBox();

        initTimeBoxes();
        placeTimeBoxes();

        setComponentVisibilities();

        wrapperPanel.add(containerTable);
    }

    private void placeDateBox() {
        containerTable.setWidget(constantRowIndex, currentColumnMax, dateBox);
        containerTable.getColumnFormatter().setStyleName(currentColumnMax,
                dateRequired ? "dateBoxColumnWidth" : "dateBoxShrink");//$NON-NLS-1$//$NON-NLS-2$
        currentColumnMax++;
    }

    private void initDateBox() {
        dateBox = new DateBox();
        dateBox.getDatePicker().addStyleName("dateBoxPopup");//$NON-NLS-1$
        dateBox.setStyleName(dateRequired ? "dateBoxWidth" : "dateBoxShrink");//$NON-NLS-1$//$NON-NLS-2$
        dateBox.setFormat(new DateBox.DefaultFormat(dateFormat));
        dateBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                selectedDate = event.getValue();
                setSelectedDate();
            }
        });
    }

    private void setComponentVisibilities() {
        dateBox.setVisible(dateRequired);

        hoursBox.setVisible(timeRequired);
        minutesBox.setVisible(timeRequired);
    }

    private void placeTimeBoxes() {
        containerTable.setWidget(constantRowIndex, currentColumnMax, hoursBox);
        containerTable.getCellFormatter().getElement(constantRowIndex, currentColumnMax).addClassName("timeComponentWidth");//$NON-NLS-1$
        hoursBox.setValue(HOUR_CONSTANT);
        currentColumnMax++;

        containerTable.setWidget(constantRowIndex, currentColumnMax, minutesBox);
        containerTable.getCellFormatter().getElement(constantRowIndex, currentColumnMax).addClassName("timeComponentWidth");//$NON-NLS-1$
        minutesBox.setValue(0);
        currentColumnMax++;

    }

    private void initTimeBoxes() {
        hoursBox = initIntegerValueBox(formStepValues(hourSteps, 1, HOUR_CONSTANT));
        addHourSelectionListener();

        minutesBox = initIntegerValueBox(formStepValues(minuteSteps, 0, MINUTE_MAX));
        addMinuteSelectionListener();

    }

    private ValueListBox<Integer> initIntegerValueBox(Collection<Integer> acceptableValues) {
        ValueListBox<Integer> valueBox = new ValueListBox<Integer>(new StringRenderer<Integer>());
        for(Integer value : acceptableValues) {
            valueBox.setValue(value);
        }
        return valueBox;
    }

    private void addHourSelectionListener() {
        hoursBox.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                hours = event.getValue();
                setSelectedDate();
            }
        });
    }

    private void addMinuteSelectionListener() {
        minutesBox.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                minutes = event.getValue();
                setSelectedDate();
            }
        });
    }

    private Collection<Integer> formStepValues(int step, int min, int max) {
        ArrayList<Integer> items = new ArrayList<Integer>();
        for (int i = min; i <= max; i += step) {
            items.add(i);
        }
        return items;
    }

    private void setSelectedDate() {
        JsDate tDate = JsDate.create(selectedDate.getTime());
        tDate.setHours(hours);
        tDate.setMinutes(minutes);
        selectedDate = new Date((long) tDate.getTime());
        ValueChangeEvent.fire(this, selectedDate);
    }

    /**
     * The default minutes step is 5. But this method can be used to alter this step.
     */
    public void setMinuteSteps(int minuteSteps) {
        this.minuteSteps = minuteSteps;
        minutesBox.setAcceptableValues(formStepValues(minuteSteps, 0, MINUTE_MAX));
    }

    /**
     * Used to dynamically hide or display the time component.
     */
    public void setTimeRequired(boolean timeRequired) {
        this.timeRequired = timeRequired;
        setComponentVisibilities();
    }

    /**
     * Used to dynamically hide or display the date component.
     */
    public void setDateRequired(boolean dateRequired) {
        this.dateRequired = dateRequired;
        dateBox.setStyleName(dateRequired ? "dateBoxWidth" : "dateBoxShrink");//$NON-NLS-1$//$NON-NLS-2$
        containerTable.getColumnFormatter().setStyleName(0,
                dateRequired ? "dateBoxColumnWidth" : "dateBoxShrink");//$NON-NLS-1$//$NON-NLS-2$
        setComponentVisibilities();
    }

    /**
     * Used to programatically check if the date component is hidden/visible.
     */
    public boolean isDateRequired() {
        return dateRequired;
    }

    /**
     * The default hours step is 1. But this method can be used to alter this step.
     */
    public void setHourSteps(int hourSteps) {
        this.hourSteps = hourSteps;
    }
}
