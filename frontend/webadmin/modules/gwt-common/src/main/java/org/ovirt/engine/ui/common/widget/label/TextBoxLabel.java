package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.ui.common.widget.renderer.EmptyValueRenderer;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class TextBoxLabel extends TextBoxLabelBase<String> {

    private boolean hasFocus = false;
    private String tooltipCaption;

    public TextBoxLabel() {
        super(new EmptyValueRenderer<String>() {
            @Override
            public String render(String value) {
                String renderedText = super.render(value);
                renderedText = SafeHtmlUtils.htmlEscape(renderedText);
                return renderedText;
            }
        });
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

        String renderedText = text;
        if (getTooltipCaption() != null) {
            renderedText = getTooltipCaption() + ": " + renderedText; //$NON-NLS-1$
        }
        setTitle(renderedText);

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

    public String getTooltipCaption() {
        return tooltipCaption;
    }

    public void setTooltipCaption(String tooltipCaption) {
        this.tooltipCaption = tooltipCaption;
    }

}
