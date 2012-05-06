package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.ui.common.widget.renderer.EmptyValueRenderer;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.ui.TextBox;

public class TextBoxLabel extends TextBox {

    private boolean handleEmptyValue = false;
    private String unAvailablePropertyLabel = ""; //$NON-NLS-1$
    private boolean hasFocus = false;
    private String tooltipCaption = null;

    public TextBoxLabel() {
        setReadOnly(true);
        getElement().getStyle().setBorderWidth(0, Unit.PX);
        getElement().getStyle().setWidth(100, Unit.PCT);
        addHandlers();
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

        addFocusHandler(new FocusHandler() {

            @Override
            public void onFocus(FocusEvent event) {
                hasFocus = true;
            }
        });

        addBlurHandler(new BlurHandler() {

            @Override
            public void onBlur(BlurEvent event) {
                hasFocus = false;
            }
        });
    }

    public TextBoxLabel(String text) {
        this();
        setText(text);
    }

    public TextBoxLabel(boolean handleEmptyValue, String unAvailablePropertyLabel) {
        this();
        this.handleEmptyValue = handleEmptyValue;
        this.unAvailablePropertyLabel = unAvailablePropertyLabel;
    }

    @Override
    public void setText(String text) {
        String renderedText = new EmptyValueRenderer<String>(
                handleEmptyValue ? unAvailablePropertyLabel : "").render(text); //$NON-NLS-1$
        renderedText = unEscapeRenderedText(renderedText);

        final int cursorPosition = getCursorPos();
        final int selectionLength = getSelectionLength();

        super.setText(renderedText);
        if (getTooltipCaption() != null) {
            renderedText = getTooltipCaption() + ": " + renderedText; //$NON-NLS-1$
        }
        setTitle(renderedText);

        if (hasFocus) {

            // needs to be deferred, because the setSelection works
            // only after the element has been attached to the document
            // which is not yet true
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                @Override
                public void execute() {
                    setFocus(true);
                    setSelectionRange(cursorPosition, selectionLength);
                }
            });
        }
    }

    private String unEscapeRenderedText(String renderedText) {
        renderedText = renderedText.replace("&lt;", "<"); //$NON-NLS-1$ //$NON-NLS-2$
        return renderedText;
    }

    public String getTooltipCaption() {
        return tooltipCaption;
    }

    public void setTooltipCaption(String tooltipCaption) {
        this.tooltipCaption = tooltipCaption;
    }

}
