package org.ovirt.engine.ui.common.widget.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.ui.uicompat.external.StringUtils;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.Renderer;

/**
 * See ListModelMultipleSelectListBoxEditor for an explanation of how to use the ListBox
 * @see ListModelMultipleSelectListBoxEditor
 */
public class ListModelMultipleSelectListBox<T> extends ListModelListBox<List<T>> implements TakesListValue<T> {
    /**
     * The editor.
     */
    private TakesConstrainedValueListEditor<T> editor;

    /**
     * The current selected list of typed values.
     */
    protected final List<T> selectedList = new ArrayList<>();

    private boolean changing = false;

    /**
     * Constructor
     * @param renderer The rendered used to render the values.
     */
    public ListModelMultipleSelectListBox(Renderer<List<T>> renderer) {
        super(renderer);
        this.isMultiSelect = true;
    }

    @Override
    public void setValue(List<T> values, boolean fireEvents) {
        if (changing) {
            return;
        }
        for (T value: values) {
            if (selectedList.contains(value)) {
                selectedList.remove(value);
            } else {
                selectedList.add(value);
            }
        }
        listPanel.setSelected(selectedList);
        updateCurrentValue(selectedList, fireEvents);
    }

    private void updateCurrentValue(final List<T> value, boolean fireEvents) {
        changing = true;
        List<String> renderedValues = new ArrayList<>();
        for (T val: value) {
            renderedValues.add(getRenderer().render(Arrays.asList(val)));
        }
        String renderedValue = StringUtils.join(renderedValues, ","); //$NON-NLS-1$
        if (StringUtils.isEmpty(renderedValue)) {
            renderedValue = NBSP;
        } else {
            renderedValue = SafeHtmlUtils.htmlEscape(renderedValue);
        }
        ((Element)dropdownButton.getElement().getChild(0)).setInnerHTML(renderedValue);
        dropdownButton.setTitle(renderedValue);
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                listPanel.setSelected(value);
            }
        });
        if (fireEvents) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                @Override
                public void execute() {
                    ValueChangeEvent.fire(ListModelMultipleSelectListBox.this, selectedItems());
                    changing = false;
                }
            });
        } else {
            changing = false;
        }
    }

    @Override
    public void setListValue(List<T> value) {
        setValue(value, true);
    }

    @Override
    public TakesConstrainedValueListEditor<T> asEditor() {
        if (editor == null) {
            editor = TakesConstrainedValueListEditor.ofList(this, this, this);
        }
        return editor;
    }

    @Override
    public void setAcceptableValues(Collection<List<T>> newValues) {
        if (newValues == null) {
            newValues = Collections.emptyList();
        }
        // Set a value in the super class, so calling setAcceptableValues doesn't add a null value and
        // potentially NPE if the renderer doesn't take kindly to getting a null value passed to it.
        List<T> val = newValues.isEmpty() ? Collections.<T>emptyList() :  newValues.iterator().next();

        //Clone selection.
        List<T> currentSelected = new ArrayList<>();
        for (T value: selectedItems()) {
            if (val.contains(value)) {
                currentSelected.add(value);
            }
        }
        selectedList.clear();
        // Populate the list box.
        super.setAcceptableValues(newValues);
        setValue(currentSelected, false);
    }

    /**
     * Getter for the list of selected items.
     * @return The list of typed selected items.
     */
    public List<T> selectedItems() {
        return selectedList;
    }
}
