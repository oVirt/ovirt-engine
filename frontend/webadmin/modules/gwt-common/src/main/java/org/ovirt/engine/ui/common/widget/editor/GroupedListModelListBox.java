package org.ovirt.engine.ui.common.widget.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.ui.common.css.OvirtCss;
import org.ovirt.engine.ui.common.widget.label.EnableableFormLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A {@code ListBox} widget that displays the list in a grouped fashion. The groups are created by using the
 * OptionGroup tag. How the items are grouped is determined by the group label associated with each model object.
 *
 * @param <T> The type of the model to use in the list box.
 */
public abstract class GroupedListModelListBox<T> extends ListModelListBox<T> {

    interface HeaderItemTemplate extends SafeHtmlTemplates {
        @Template("<span class=\"text\">{0}</span>")
        SafeHtml headerListItem(String text);
    }

    FlowPanel groupLabelContainer;

    EnableableFormLabel groupLabel;

    private final HeaderItemTemplate headerItemTemplate = GWT.create(HeaderItemTemplate.class);

    public GroupedListModelListBox(Renderer<T> renderer) {
        super(renderer);
        addValueChangeHandler(event -> updateGroupLabel(event.getValue()));
        groupLabelContainer = new FlowPanel();
        groupLabelContainer.addStyleName(style.labelContainer());
        groupLabel = new EnableableFormLabel();
        groupLabel.addStyleName(style.label());
        groupLabelContainer.add(groupLabel);
        container.add(groupLabelContainer);
    }

    protected UnorderedListPanel getListPanel() {
        return new GroupedUnorderedListPanel();
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
    protected Widget getWidget() {
        return container.getWidget(0);
    }

    /**
     * Set the acceptable values.
     */
    @Override
    public void setAcceptableValues(Collection<T> newValues) {
        if (newValues instanceof List) {
            List<T> sortedValues = (List<T>) newValues;
            Collections.sort(sortedValues, getComparator());
            super.setAcceptableValues(sortedValues);
        }
    }

    protected boolean ignoreChanging() {
        return true;
    }

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

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        final String oldStyleName = enabled ? OvirtCss.LABEL_DISABLED : OvirtCss.LABEL_ENABLED;
        final String newStyleName = enabled ? OvirtCss.LABEL_ENABLED : OvirtCss.LABEL_DISABLED;
        groupLabel.getElement().replaceClassName(oldStyleName, newStyleName);
    }

    private void updateGroupLabel(T value) {
        groupLabel.setText(getGroupLabel(value));
    }

    protected class GroupedUnorderedListPanel extends UnorderedListPanel {
        final List<String> shownGroupLabels = new ArrayList<>();

        void addListItem(String text, T value) {
            String groupLabel = getGroupLabel(value);
            if (!shownGroupLabels.contains(groupLabel)) {
                addGroupHeaderListItem(groupLabel);
                shownGroupLabels.add(groupLabel);
            }
            super.addListItem(text, value);
        }

        protected void addGroupHeaderListItem(String text) {
            add(new GroupHeaderListItem(text), getElement());
        }

        protected ListModelListBox<T>.ListItem getListItem(String text, T value) {
            return new ListItem(text, value);
        }

        @Override
        public void clear() {
            super.clear();
            shownGroupLabels.clear();
        }
    }

    protected class GroupHeaderListItem extends ComplexPanel {

        public GroupHeaderListItem(String text) {
            Element li = Document.get().createLIElement();
            li.addClassName(Styles.DROPDOWN_HEADER);
            li.setInnerHTML(headerItemTemplate.headerListItem(text).asString());
            setElement(li);
        }
    }

}
