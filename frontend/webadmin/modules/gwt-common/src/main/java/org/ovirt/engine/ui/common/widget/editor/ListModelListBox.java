package org.ovirt.engine.ui.common.widget.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.widget.HasEnabledWithHints;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasConstrainedValue;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;

/**
 * List box widget that adapts to UiCommon list model items.
 *
 * @param <T>
 *            List box item type.
 */
public class ListModelListBox<T> extends Composite implements EditorWidget<T, TakesValueEditor<T>>,
    HasConstrainedValue<T>, HasHandlers, HasElementId, HasEnabled, HasEnabledWithHints {

    protected static final String DATA_TOGGLE = "data-toggle"; //$NON-NLS-1$
    protected static final String DROPDOWN_MENU = "dropdownMenu"; //$NON-NLS-1$
    protected static final String ID = "id"; //$NON-NLS-1$
    protected static final String NBSP = "&nbsp;"; //$NON-NLS-1$
    protected static final String BTN_DEFAULT = "btn-default"; //$NON-NLS-1$
    protected static final String GWT_BUTTON = "gwt-Button"; //$NON-NLS-1$
    protected static final String FILTER_OPTION = "filter-option"; //$NON-NLS-1$
    protected static final String ARIA_LABELLEDBY = "aria-labelledby"; //$NON-NLS-1$
    protected static final String SELECTED = "selected"; //$NON-NLS-1$
    protected static final String ROLE = "role"; //$NON-NLS-1$
    protected static final String MENU = "menu"; //$NON-NLS-1$
    protected static final String PRESENTATION = "presentation"; //$NON-NLS-1$

    private TakesConstrainedValueEditor<T> editor;

    interface ListModelListBoxUiBinder extends UiBinder<FlowPanel, ListModelListBox<?>> {
        ListModelListBoxUiBinder uiBinder = GWT.create(ListModelListBoxUiBinder.class);
    }

    interface Style extends CssResource {
        String scrollableMenu();
        String selected();
        String liPosition();
        String inlineBlock();
        String checkIcon();
        String labelContainer();
        String label();
        String selectedValue();
        String button();
    }

    interface ListItemTemplate extends SafeHtmlTemplates {
        @Template("<span class=\"text {1}\">{0}</span><span class=\"glyphicon glyphicon-ok check-mark {1} {2}\"></span>")
        SafeHtml multiSelectListItem(String text, String textSpanStyle, String iconStyle);
    }

    interface ButtonTextSpan extends SafeHtmlTemplates {
        @Template("<span class=\"caret pull-right\" style=\"position: absolute; margin-top: 5px; right: 6px;\"></span>")
        SafeHtml selectedValue();
    }

    interface AnchorText extends SafeHtmlTemplates {
        @Template("<a tabindex=\"-1\" role=\"menuitem\" class=\"{1}\">{0}</a>")
        SafeHtml anchor(SafeHtml text, String className);
    }

    private final Renderer<T> renderer;

    private final List<T> valueList = new ArrayList<>();

    private T currentValue;

    @UiField
    protected FlowPanel container;

    @UiField
    protected FlowPanel dropdownPanel;

    @UiField(provided=true)
    protected Button dropdownButton;

    @UiField
    protected Style style;

    private final ListItemTemplate listItemTemplate = GWT.create(ListItemTemplate.class);

    private final ButtonTextSpan buttonSelectedValueSpan = GWT.create(ButtonTextSpan.class);

    private final AnchorText anchorText = GWT.create(AnchorText.class);

    protected SpanElement selectedValue;

    protected UnorderedListPanel listPanel;

    private boolean changing = false;

    protected boolean isMultiSelect = false;

    /**
     * Creates a list box that renders its items using the specified {@link Renderer}.
     *
     * @param renderer
     *            Renderer for list box items.
     */
    public ListModelListBox(Renderer<T> renderer) {
        this.renderer = renderer;
        dropdownButton = createButton();
        initWidget(ListModelListBoxUiBinder.uiBinder.createAndBindUi(this));
        listPanel = getListPanel();
        listPanel.addStyleName(style.scrollableMenu());
        dropdownButton.addStyleName(style.button());
        selectedValue.addClassName(style.selectedValue());
        dropdownButton.getElement().setInnerHTML(selectedValue.getString()
                + buttonSelectedValueSpan.selectedValue().asString());
        dropdownPanel.add(listPanel);
        dropdownButton.addClickHandler(event -> listPanel.scrollToSelected());
    }

    public void setDropdownHeight(String height) {
        listPanel.getElement().getStyle().setProperty("maxHeight", height); //$NON-NLS-1$
    }

    protected UnorderedListPanel getListPanel() {
        return new UnorderedListPanel();
    }

    private Button createButton() {
        Button button = new Button();
        button.removeStyleName(GWT_BUTTON);
        button.addStyleName(Styles.BTN);
        button.addStyleName(BTN_DEFAULT);
        button.addStyleName(Styles.DROPDOWN_TOGGLE);
        button.addStyleName(Styles.FORM_CONTROL);
        button.getElement().setAttribute(ID, DROPDOWN_MENU);
        button.getElement().setAttribute(DATA_TOGGLE, Styles.DROPDOWN);
        selectedValue = Document.get().createSpanElement();
        selectedValue.addClassName(FILTER_OPTION);
        selectedValue.addClassName(Styles.PULL_LEFT); //$NON-NLS-1$
        selectedValue.setInnerHTML(NBSP);
        return button;
    }

    @Override
    public TakesConstrainedValueEditor<T> asEditor() {
        if (editor == null) {
            editor = TakesConstrainedValueEditor.of(this, this, this);
        }
        return editor;
    }

    @Override
    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
        return dropdownPanel.addDomHandler(handler, KeyUpEvent.getType());
    }

    @Override
    public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
        return dropdownPanel.addDomHandler(handler, KeyDownEvent.getType());
    }

    @Override
    public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
        return dropdownPanel.addDomHandler(handler, KeyPressEvent.getType());
    }

    @Override
    public void onAttach() {
        super.onAttach();
        getElement().removeClassName(Styles.FORM_CONTROL);
    }

    @Override
    public void setValue(T value) {
        setValue(value, false);
    }

    @Override
    public void setValue(T value, boolean fireEvents) {
        setValue(value, fireEvents, false);
    }

    protected void setValue(T value, boolean fireEvents, boolean fromClick) {
        if (changing) {
            return;
        } else if (value == null) {
            updateCurrentValue(null, fireEvents);
        } else {
            boolean found = false;
            for (T listItem: this.valueList) {
                if (listItem == value || (listItem != null && listItem.equals(value))) {
                    //Found the value, show the right thing on the button.
                    updateCurrentValue(value, fireEvents);
                    found = true;
                    break;
                }
            }
            if (!found) {
                addValue(value);
                updateCurrentValue(value, fireEvents);
            }
        }
    }

    private void updateCurrentValue(final T value, boolean fireEvents) {
        setChanging(!ignoreChanging());
        updateTitle(value);
        String renderedValue = renderer.render(value);
        ((Element)dropdownButton.getElement().getChild(0)).setInnerHTML(renderedValue);
        Scheduler.get().scheduleDeferred(() -> listPanel.setSelected(value));
        currentValue = value;
        if (fireEvents) {
            Scheduler.get().scheduleDeferred(() -> {
                ValueChangeEvent.fire(ListModelListBox.this, currentValue);
                setChanging(false);
            });
        } else {
            setChanging(false);
        }
    }

    private void updateTitle(T value) {
        if (!dropdownButton.isEnabled()) {
            return;
        }

        String renderedValue = renderer.render(value);
        if (StringHelper.isNullOrEmpty(renderedValue)) {
            renderedValue = NBSP;
            dropdownButton.setTitle(""); //$NON-NLS-1$
        } else {
            renderedValue = SafeHtmlUtils.htmlEscape(renderedValue);
            dropdownButton.setTitle(renderedValue);
        }
    }

    /**
     * Return true if you want ignore the changing variable. This variable is used to avoid excessive changing
     * of values during setting acceptable values. Override if you want to ignore this optimization.
     * @return True if you want to ignore the 'changing' variable, false otherwise.
     */
    protected boolean ignoreChanging() {
        return false;
    }

    public T getValue() {
        return currentValue;
    }

    public void setChanging(boolean value) {
        changing = value;
    }

    @Override
    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<T> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public void setAcceptableValues(Collection<T> values) {
        valueList.clear();
        listPanel.clear();
        if (values.isEmpty()) {
            updateCurrentValue(null, false);
        }
        for(T value: values) {
            addValue(value);
        }
    }

    private void addValue(T value) {
        this.valueList.add(value);
        //Make sure to not pass null to the list panel or the SafeHtml.escapeHtml will bomb.
        String text = renderer.render(value) == null ? "" : renderer.render(value); //$NON-NLS-1$
        listPanel.addListItem(text, value);
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public boolean isEnabled() {
        return dropdownButton.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        dropdownButton.setEnabled(enabled);
        if (!enabled) {
            dropdownButton.addStyleName(Styles.DISABLED);
        } else {
            updateTitle(currentValue);
            dropdownButton.removeStyleName(Styles.DISABLED);
        }
    }

    @Override
    public void disable(String disabilityHint) {
        setEnabled(false);
        dropdownButton.setTitle(disabilityHint);
    }

    @Override
    public int getTabIndex() {
        return dropdownButton.getTabIndex();
    }

    @Override
    public void setAccessKey(char key) {
        dropdownButton.setAccessKey(key);
    }

    @Override
    public void setFocus(boolean focused) {
        dropdownButton.setFocus(focused);
    }

    @Override
    public void setTabIndex(int index) {
        dropdownButton.setTabIndex(index);
    }

    @Override
    public void setElementId(String elementId) {
        dropdownButton.getElement().setAttribute(ID, elementId);
        listPanel.setAriaLabelledBy(elementId);
    }

    public void addButtonStyleName(String styleName) {
        dropdownButton.addStyleName(styleName);
    }

    protected Renderer<T> getRenderer() {
        return this.renderer;
    }

    protected class UnorderedListPanel extends ComplexPanel {

        private final List<HandlerRegistration> clickHandlers = new ArrayList<>();
        private final UListElement uListElement;

        UnorderedListPanel() {
            uListElement = Document.get().createULElement();
            uListElement.addClassName(Styles.DROPDOWN_MENU);
            uListElement.setAttribute(ROLE, MENU);
            setAriaLabelledBy(DROPDOWN_MENU);
            setElement(uListElement);
        }

        public void scrollToSelected() {
            for (Widget child : getChildren()) {
                if (child instanceof ListModelListBox.ListItem) {
                    final ListItem item = (ListModelListBox<T>.ListItem) child;
                    if (item.isSelected()) {
                        Scheduler.get().scheduleDeferred(() -> item.getElement().scrollIntoView());
                    }
                }
            }
        }

        void setAriaLabelledBy(String id) {
            uListElement.setAttribute(ARIA_LABELLEDBY, id); //$NON-NLS-1$
        }

        void addListItem(String text, T value) {
            String nonEmptyText = "".equals(text) ? NBSP : text;
            ListItem li = getListItem(nonEmptyText, value);
            getClickHandlers().add(li.addClickHandler(event -> {
                @SuppressWarnings("unchecked")
                ListItem item = (ListItem) event.getSource();
                ListModelListBox.this.setValue(item.getValue(), true, true);
                if (ListModelListBox.this.isMultiSelect) {
                    event.stopPropagation();
                }
            }));
            add(li, getElement());
        }

        protected ListModelListBox<T>.ListItem getListItem(String text, T value) {
            return new ListItem(text, value);
        }

        @SuppressWarnings("unchecked")
        public void setSelected(T value) {
            for(Widget child: getChildren()) {
                if (child instanceof ListModelListBox.ListItem) {
                    ListItem item = (ListModelListBox<T>.ListItem) child;
                    //Clear any selection first
                    item.removeSelected();
                    if (value instanceof List) {
                        if (((List<T>)value).contains(((List<T>)item.getValue()).get(0))) {
                            item.setSelected();
                        }
                    } else {
                        if (item.getValue() == value || (item.getValue() != null && item.getValue().equals(value))) {
                            item.setSelected();
                        }
                    }
                }
            }
        }

        List<HandlerRegistration> getClickHandlers() {
            return clickHandlers;
        }

        @Override
        public void clear() {
            for (HandlerRegistration handler: getClickHandlers()) {
                handler.removeHandler();
            }
            getClickHandlers().clear();
            getElement().removeAllChildren();
        }
    }

    protected class ListItem extends ComplexPanel {
        private final String anchorText;
        private final T value;

        public ListItem(String text, T value) {
            this.value = value;
            Element li = Document.get().createLIElement();
            li.setAttribute(ROLE, PRESENTATION);
            li.addClassName(style.liPosition());
            this.anchorText = text;
            li.setInnerHTML(ListModelListBox.this.anchorText.anchor(SafeHtmlUtils.fromTrustedString(text), "").asString()); //$NON-NLS-1$
            setElement(li);
        }

        public T getValue() {
            return value;
        }

        public void setSelected() {
            getElement().addClassName(SELECTED);
            SafeHtml anchor;
            if (ListModelListBox.this.isMultiSelect) {
                anchor = ListModelListBox.this.anchorText.anchor(
                        ListModelListBox.this.listItemTemplate.multiSelectListItem(this.anchorText,
                        style.inlineBlock(), style.checkIcon()), style.selected());
            } else {
                anchor = ListModelListBox.this.anchorText.anchor(SafeHtmlUtils.fromTrustedString(anchorText),
                        style.selected());
            }
            getElement().setInnerHTML(anchor.asString());
        }

        public boolean isSelected() {
            return getElement().getClassName().contains(SELECTED);
        }

        public void removeSelected() {
            getElement().removeClassName(SELECTED);
            getElement().setInnerHTML(ListModelListBox.this.anchorText.anchor(
                    SafeHtmlUtils.fromTrustedString(anchorText), "").asString()); //$NON-NLS-1$
        }

        public HandlerRegistration addClickHandler(ClickHandler handler) {
            return addDomHandler(handler, ClickEvent.getType());
        }
    }
}
