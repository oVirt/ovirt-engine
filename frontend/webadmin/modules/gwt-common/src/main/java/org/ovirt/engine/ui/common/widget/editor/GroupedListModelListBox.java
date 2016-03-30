package org.ovirt.engine.ui.common.widget.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.ui.common.css.OvirtCss;
import org.ovirt.engine.ui.common.widget.label.EnableableFormLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.OptGroupElement;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * A {@code ListBox} widget that displays the list in a grouped fashion. The groups are created by using the
 * OptionGroup tag. How the items are grouped is determined by the group label associated with each model object.
 *
 * @param <T> The type of the model to use in the list box.
 */
public abstract class GroupedListModelListBox<T> extends ListModelListBox<T> {
    interface Style extends CssResource {
        String container();
        String container_legacy();
        String listBox();
        String listBoxPatternfly();
        String labelContainer();
    }

    interface GroupedListUiBinder extends UiBinder<FlowPanel, GroupedListModelListBox<?>> {
        GroupedListUiBinder uiBinder = GWT.create(GroupedListUiBinder.class);
    }

    @UiField
    FlowPanel container;

    @UiField
    FlowPanel groupLabelContainer;

    @UiField
    EnableableFormLabel groupLabel;

    @UiField
    public Style style;

    List<T> selectableObjects = new ArrayList<>();

    public GroupedListModelListBox(Renderer<T> renderer) {
        super(renderer);
    }

    /**
     * Set the value, do nothing if the value is null.
     */
    public void setValue(T value) {
        if (value != null) {
            super.setValue(value, true);
        }
    }

    @Override
    protected void initWidget(Widget widget) {
        container = GroupedListUiBinder.uiBinder.createAndBindUi(this);
        super.initWidget(container);
        container.insert(widget, 0);
        getListBox().addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                updateGroupLabel();
            }
        });
        addValueChangeHandler(new ValueChangeHandler<T>() {

            @Override
            public void onValueChange(ValueChangeEvent<T> event) {
                updateGroupLabel();
            }
        });
    }

    @Override
    protected Widget getWidget() {
        return container.getWidget(0);
    }

    /**
     * Set the acceptable values as well as generating the appropriate OptionGroups based on the values passed in
     * and the labels generated with getModelLabel and getGroupLabel.
     */
    @Override
    public void setAcceptableValues(Collection<T> newValues) {
        if (newValues instanceof List) {
            List<T> sortedValues = (List<T>) newValues;
            Collections.sort(sortedValues, getComparator());
            //Get the internal state of the ValueListBox correct.
            super.setAcceptableValues(sortedValues);
            //Store the current selected index.
            int index = getListBox().getSelectedIndex();

            ListBox listBox = asListBox();
            SelectElement selectElement = listBox.getElement().cast();
            selectElement.addClassName(Styles.FORM_CONTROL);
            //Wipe out the existing options and group option elements.
            selectElement.removeAllChildren();
            selectableObjects.clear();

            OptGroupElement groupElement = null;
            for (Map.Entry<String, List<T>> nextValue : getGroupedList(sortedValues).entrySet()) {
                groupElement = Document.get().createOptGroupElement();
                groupElement.setLabel(nextValue.getKey());
                selectElement.appendChild(groupElement);
                for (T model : nextValue.getValue()) {
                    OptionElement optionElement = Document.get().createOptionElement();
                    optionElement.setText(getModelLabel(model));
                    groupElement.appendChild(optionElement);
                    selectableObjects.add(model);
                }
            }
            //Since the group elements don't count for index purposes, we can restore the index on the list box
            //to be what it was before wiping out the original option elements.
            getListBox().setSelectedIndex(index);
            updateGroupLabel();
        }
    }

    public abstract SortedMap<String, List<T>> getGroupedList(List<T> acceptableValues);

    /**
     * Get the label that represents the model passed in.
     * @param model The model to get the label for.
     * @return The label as a {@code String} usually the name of the model.
     */
    public abstract String getModelLabel(T model);

    /**
     * Get the group label of the group this model is associated with.
     * @param model The model to get the group label for.
     * @return The label as a {@code String}
     */
    public abstract String getGroupLabel(T model);

    public abstract Comparator<T> getComparator();

    /**
     * Get the {@code ListBox} widget.
     * @return The widget.
     */
    public ListBox getListBox() {
        return (ListBox) getWidget();
    }

    public void setUsePatternFly(boolean usePatternFly) {
        getWidget().addStyleName(style.listBox());
        if (usePatternFly) {
            container.addStyleName(Styles.FORM_CONTROL);
            container.addStyleName(style.container());
            container.removeStyleName(style.container_legacy());
            getWidget().addStyleName(style.listBoxPatternfly());
            groupLabelContainer.addStyleName(style.labelContainer());
        } else {
            container.addStyleName(style.container_legacy());
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        final String oldStyleName = enabled ? OvirtCss.LABEL_DISABLED : OvirtCss.LABEL_ENABLED;
        final String newStyleName = enabled ? OvirtCss.LABEL_ENABLED : OvirtCss.LABEL_DISABLED;
        groupLabel.getElement().replaceClassName(oldStyleName, newStyleName);
    }

    private void updateGroupLabel() {
        if (selectableObjects.size() > getListBox().getSelectedIndex() && getListBox().getSelectedIndex() >= 0) {
            groupLabel.setText(getGroupLabel(selectableObjects.get(getListBox().getSelectedIndex())));
        }
    }
}
