package org.ovirt.engine.ui.common.widget.label;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import org.ovirt.engine.ui.common.widget.renderer.EmptyValueRenderer;

public class TextBoxLabel extends TextBoxLabelBase<String> {

    private boolean hasFocus = false;

    public TextBoxLabel() {
        super(new EmptyValueRenderer<String>());
    }

    public TextBoxLabel(String text) {
        this();
        setText(text);
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();

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

    /**
     * Overridden to return "" from an empty text box.
     *
     * @see com.google.gwt.user.client.ui.TextBoxBase#getValue()
     */
    @Override
    public String getValue() {
        String raw = super.getValue();
        return raw == null ? "" : raw; //$NON-NLS-1$
    }

    @Override
    public void setText(String text) {
        super.setText(text);

        final int cursorPosition = getCursorPos();
        final int selectionLength = getSelectionLength();

        if (hasFocus) {
            // Needs to be deferred, because the setSelection works
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

}
