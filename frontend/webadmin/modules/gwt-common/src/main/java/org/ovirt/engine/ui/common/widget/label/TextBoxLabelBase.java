package org.ovirt.engine.ui.common.widget.label;

import java.text.ParseException;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueBoxBase;

/**
 * Read-only text box widget used to render label values with added functionality.
 * <p>
 * This widget renders a border-less {@code <input type="text" readonly>} element. Right-clicking the input element
 * causes the element to gain focus and select all text, so that the user can simply choose "Copy" within the context
 * menu.
 * <p>
 * Implementation note: due to being a read-only text box, this widget intentionally suppresses rendered text parsing
 * logic, i.e. {@link #getValue()} just returns the value that was set previously via {@link #setValue(Object)}.
 *
 * @param <T>
 *            Value type.
 */
public abstract class TextBoxLabelBase<T> extends ValueBoxBase<T> {

    private T value;

    private TextBoxLabelBase(Renderer<T> renderer, Parser<T> parser) {
        super(Document.get().createTextInputElement(), renderer, parser);
        setReadOnly(true);
        initStyles();
        addHandlers();
    }

    @SuppressWarnings("unchecked")
    public TextBoxLabelBase(Renderer<? super T> renderer) {
        this((Renderer<T>) renderer, new Parser<T>() {
            @Override
            public T parse(CharSequence text) throws ParseException {
                // No-op null parser, not used at runtime
                return null;
            }
        });
    }

    @Override
    public void setValue(T value, boolean fireEvents) {
        super.setValue(value, fireEvents);
        this.value = value;
    }

    @Override
    public void setText(String text) {
        super.setText(text);
    }

    @Override
    public T getValueOrThrow() throws ParseException {
        // Suppress rendered text parsing logic,
        // return the value that was set previously
        return value;
    }

    protected void initStyles() {
        getElement().getStyle().setBorderWidth(0, Unit.PX);
        getElement().getStyle().setWidth(100, Unit.PCT);
    }

    protected void addHandlers() {
        addDomHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent event) {
                if (event.getNativeButton() == NativeEvent.BUTTON_RIGHT) {
                    setFocus(true);
                    selectAll();
                }
            }
        }, MouseDownEvent.getType());
    }
}
