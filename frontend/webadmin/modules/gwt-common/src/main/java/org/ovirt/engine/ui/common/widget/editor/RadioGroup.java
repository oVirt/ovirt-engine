package org.ovirt.engine.ui.common.widget.editor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasConstrainedValue;
import com.google.gwt.user.client.ui.RadioButton;

public class RadioGroup<K> extends Composite implements TakesValue<K>, HasConstrainedValue<K> {

    private static final String MAX_WIDTH = "maxWidth"; //$NON-NLS-1$

    private final Map<K, RadioButton> buttons = new HashMap<>();
    private final Map<K, FlowPanel> panels = new HashMap<>();
    private final FlowPanel wrapperPanel = new FlowPanel();
    private final Renderer<K> renderer;
    private boolean enabled;
    private K selectedValue;
    private K oldSelectedValue;
    private final String groupString;
    private boolean usePatternfly = false;

    int tabIndex;
    char accessKey = 0;

    public RadioGroup(Renderer<K> renderer) {
        this.renderer = renderer;
        initWidget(wrapperPanel);
        groupString = getElement().getString();
    }

    public int getTabIndex() {
        return tabIndex;
    }

    public void setAccessKey(char key) {
        this.accessKey = key;
    }

    public void setFocus(boolean focused) {
        RadioButton selectedButton = buttons.get(selectedValue);
        if (selectedButton != null) {
            selectedButton.setFocus(focused);
        }
    }

    protected LabelElement getRadioButtonWidgetLabel(RadioButton radioButton) {
        return LabelElement.as(Element.as(radioButton.getElement().getChild(1)));
    }

    public void setUsePatternFly(boolean use) {
        usePatternfly = use;
    }

    @Override
    public K getValue() {
        return selectedValue;
    }

    @Override
    public void setValue(K value) {
        setValue(value, false);
    }

    @Override
    public void setValue(K value, boolean fireEvents) {
        if (value == selectedValue
                || (selectedValue != null && selectedValue.equals(value))) {
            return;
        }

        if (!buttons.containsKey(value)) {
            addValue(value);
        }

        oldSelectedValue = selectedValue;
        selectedValue = value;

        updateButtons();

        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    public void setTabIndex(int index) {
        this.tabIndex = index;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        for (RadioButton radioButton : buttons.values()) {
            radioButton.setEnabled(enabled);
        }
    }

    @Override
    public void setAcceptableValues(Collection<K> values) {
        buttons.clear();
        panels.clear();
        wrapperPanel.clear();

        if (values != null) {
            for (final K value : values) {
                addValue(value);
            }
        }

        updateButtons();
    }

    private void updateButtons() {
        for (Map.Entry<K, RadioButton> entry : buttons.entrySet()) {
            RadioButton radioButton = entry.getValue();
            if (usePatternfly) {
                // patternfly hacks
                Element.as(radioButton.getElement().getChild(0)).getStyle().setVerticalAlign(VerticalAlign.TOP);
                getRadioButtonWidgetLabel(radioButton).getStyle().setPaddingLeft(10, Unit.PX);
                getRadioButtonWidgetLabel(radioButton).getStyle().setPosition(Position.RELATIVE);
                getRadioButtonWidgetLabel(radioButton).getStyle().setProperty(MAX_WIDTH, "94%"); //$NON-NLS-1$
                getRadioButtonWidgetLabel(radioButton).getStyle().setDisplay(Display.INLINE);
        }
            if (entry.getKey().equals(selectedValue)) {
                radioButton.setValue(true);

                radioButton.setTabIndex(tabIndex);
                if (accessKey != 0) {
                    radioButton.setAccessKey(accessKey);
                }
            } else if (Objects.equals(oldSelectedValue, entry.getKey())) {
                radioButton.setTabIndex(-1);
                radioButton.getElement().removeAttribute("accessKey"); //$NON-NLS-1$
            }
        }
    }

    private void addValue(final K value) {
        if (value == null) {
            throw new IllegalArgumentException("null value is not permited"); //$NON-NLS-1$
        }

        if (buttons.containsKey(value)) {
            throw new IllegalArgumentException("Duplicate value: " + value); //$NON-NLS-1$
        }

        RadioButton radioButton =
                new RadioButton(groupString, SafeHtmlUtils.fromTrustedString(renderer.render(value))) {
                    @Override
                    public void onBrowserEvent(Event event) {
                        super.onBrowserEvent(event);

                        boolean value = getValue();

                        // This hack is needed because there are cases RadioButton.oldValue is not properly updated
                        // (e.g- when changing the radio button selection with the arrows buttons there is no event that
                        // catches
                        // that the RadioButton.oldValue should be changed to false),
                        // and it causes the event not to be fired although it should be.
                        if (Event.ONCLICK == DOM.eventGetType(event) && value && this != buttons.get(selectedValue)) {
                            ValueChangeEvent.fire(this, value);
                        }
                    }
                };

        radioButton.addValueChangeHandler(event -> {
            if (event.getValue()) {
                setValue(value, true);
            }
        });
        radioButton.getElement().getStyle().setMarginRight(20, Unit.PX);
        buttons.put(value, radioButton);
        FlowPanel panel = new FlowPanel();
        panels.put(value, panel);
        panel.add(radioButton);
        wrapperPanel.add(panel);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<K> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    public FlowPanel getPanel(K key) {
        return panels.get(key);
    }

    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
        return addDomHandler(handler, KeyUpEvent.getType());
    }

    public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
        return addDomHandler(handler, KeyDownEvent.getType());
    }

    public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
        return addDomHandler(handler, KeyPressEvent.getType());
    }
}
