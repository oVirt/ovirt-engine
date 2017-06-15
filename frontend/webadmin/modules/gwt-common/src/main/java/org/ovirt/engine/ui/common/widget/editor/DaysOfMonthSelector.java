package org.ovirt.engine.ui.common.widget.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;

/**
 * This widget can be used to capture dates for monthly recurrent events. Currently it has been built to facilitate
 * Quartz scheduling.
 */
public class DaysOfMonthSelector extends Composite implements TakesValue<String>, HasValue<String> {

    private static final int DAYS_IN_WEEK = 7;
    private static final int LAST_DAY_OF_MONTH_INDEX = 31;
    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();

    private static Resources RESOURCES = GWT.create(Resources.class);
    private final DaysOfMonthSelectorCss style;

    private final FlowPanel wrapperPanel = new FlowPanel();
    private final FlexTable daysOfMonth = new FlexTable();

    // Starts from index 0 and goes upto 31(Assumed to be last day of month(recurrence))
    List<Boolean> clickedList = new ArrayList<>();

    public interface Resources extends ClientBundle {
        @Source("org/ovirt/engine/ui/common/css/DaysOfMonthSelector.css")
        DaysOfMonthSelectorCss daysOfMonthSelectorCSS();
    }

    /**
     * DaysOfMonthSelector
     */
    public DaysOfMonthSelector() {
        initWidget(wrapperPanel);
        style = RESOURCES.daysOfMonthSelectorCSS();
        style.ensureInjected();
        daysOfMonth.setStyleName(style.daysOfMonthWidget());
        showDaysOfMonth();
        daysOfMonth.addClickHandler(event -> {
            Cell cellClicked = daysOfMonth.getCellForEvent(event);
            if (cellClicked != null) {
                int cellColumn = cellClicked.getCellIndex();
                int cellRow = cellClicked.getRowIndex();
                int actualCellIndex = (cellRow - 1) * DAYS_IN_WEEK + cellColumn;
                if (!clickedList.get(actualCellIndex)) {
                    ValueChangeEvent.fire(DaysOfMonthSelector.this,
                        addSelectedDate(getValue(), getDateFromIndex(actualCellIndex)));
                } else {
                    ValueChangeEvent.fire(DaysOfMonthSelector.this,
                        removeSelectedDate(getValue(), getDateFromIndex(actualCellIndex)));
                }
            }
        });
    }

    private void onSelectedItemsChange(int date, boolean clicked) {
        String className = style.normalFlexTableCell();
        if (clicked) {
            className = style.selectedFlexTableCell();
        }
        daysOfMonth.getCellFormatter()
        .getElement(getRowForTheDay(date), getColumnForTheDay(date))
        .setClassName(className);
    }

    private void showDaysOfMonth() {
        int row = 0;
        int column = 0;
        for (int i = 0; i < LAST_DAY_OF_MONTH_INDEX; i++) {
            if ((i + 1) % DAYS_IN_WEEK == 1 && i != 1) {
                row++;
                column = 0;
            }
            daysOfMonth.setWidget(row, column, new Label(Integer.toString(i + 1)));
            daysOfMonth.getCellFormatter().getElement(row, column).addClassName(style.normalFlexTableCell());
            clickedList.add(i, false);
            column++;
        }
        Label widget = new Label(constants.lastDay());
        clickedList.add(LAST_DAY_OF_MONTH_INDEX, false);
        daysOfMonth.setWidget(row, column, widget);
        daysOfMonth.getFlexCellFormatter().setColSpan(row, column, 4);
        wrapperPanel.add(daysOfMonth);
    }

    /**
     * When the mapped ListModel does a setSelectedItem, this is invoked. This method sets the passed dates selected
     * @param value
     *            Comma separated string of dates to mark selected
     * @param fireEvents
     *            whether to fire ValueChangeEvent
     */
    @Override
    public void setValue(String value, boolean fireEvents) {
        clearSelections();
        if (value != null && !value.isEmpty()) {
            for (String valueInProcess : Arrays.asList(value.split(","))) {//$NON-NLS-1$
                int selectedIndex = getIndexFromDate(valueInProcess);
                if (clickedList.get(selectedIndex) != null) {
                    clickedList.set(selectedIndex, true);
                    onSelectedItemsChange(selectedIndex + 1, true);
                }
            }
            if (fireEvents) {
                ValueChangeEvent.fire(this, value);
            }
        }
    }

    /**
     * When the mapped ListModel does a setSelectedItem, this is invoked. This method sets the passed dates selected
     * @param value
     *            Comma separated string of dates to mark selected
     */
    @Override
    public void setValue(String value) {
        setValue(value, false);
    }

    private void clearSelections() {
        for (int index = 0; index <= LAST_DAY_OF_MONTH_INDEX; index++) {
            clickedList.set(index, false);
            onSelectedItemsChange(index + 1, false);
        }
    }

    /**
     * This method calculates and returns the comma-separated string of dates selected in the widget.
     * @return String Comma-separated string of dates selected in the widget.
     */
    @Override
    public String getValue() {
        String selectedValues = null;
        for (int dayInProcess = 0; dayInProcess <= LAST_DAY_OF_MONTH_INDEX; dayInProcess++) {
            if (clickedList.get(dayInProcess)) {
                selectedValues = addSelectedDate(selectedValues, getDateFromIndex(dayInProcess));
            }
        }
        return selectedValues;
    }

    private String addSelectedDate(String selectedValues, String dateString) {
        if (selectedValues == null) {
            selectedValues = dateString;
        } else {
            selectedValues = selectedValues.concat(",");//$NON-NLS-1$
            selectedValues = selectedValues.concat(dateString);
        }
        return selectedValues;
    }

    private String removeSelectedDate(String selectedValues, String dayInProcess) {
        List<String> selectedDatesList = new ArrayList<>(Arrays.asList(selectedValues.split(",")));//$NON-NLS-1$
        selectedDatesList.remove(dayInProcess);
        String selectedDatesString = null;
        for (String currentSelectedDate : selectedDatesList) {
            selectedDatesString = addSelectedDate(selectedDatesString, currentSelectedDate);
        }
        return selectedDatesString;
    }

    private String getDateFromIndex(int dayInProcess) {
        return dayInProcess == LAST_DAY_OF_MONTH_INDEX ? "L" : Integer.toString(dayInProcess + 1);//$NON-NLS-1$
    }

    private int getIndexFromDate(String value) {
        return value.equals("L") ? LAST_DAY_OF_MONTH_INDEX : Integer.parseInt(value) - 1;//$NON-NLS-1$
    }

    private int getRowForTheDay(int date) {
        int row = date / DAYS_IN_WEEK;
        return date % DAYS_IN_WEEK == 0 ? row : row + 1;
    }

    private int getColumnForTheDay(int date) {
        int probableColumn = date % DAYS_IN_WEEK;
        int cellColumn = probableColumn == 0 ? 6 : probableColumn - 1;
        return cellColumn;
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
}
