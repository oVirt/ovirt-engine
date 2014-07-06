package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.ui.uicompat.external.StringUtils;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HTML;

/**
 * This class represents a label with a tooltip.
 *
 * Both the label and the tooltip can be <code>String</code> or <code>SafeHtml</code>.
 *
 * The default tooltip is the label's text. Overriding the tooltip (<code>setTitle(String title)</code>) should be done
 * after setting the text (<code>setText(String text)</code>).
 */
public class LabelWithCustomToolTip extends HTML {

    private final HTML tooltip = new HTML();
    private final DecoratedPopupPanel tooltipPanel = new DecoratedPopupPanel();
    private String title;

    public LabelWithCustomToolTip() {
        initTooltipPanel();
    }

    public LabelWithCustomToolTip(String text) {
        this();
        setText(text);
    }

    public LabelWithCustomToolTip(SafeHtml html) {
        this();
        setHTML(html);
    }

    @Override
    public void setText(final String text) {
        super.setText(text);
        setTitle(text);
    }

    private void initTooltipPanel() {
        tooltipPanel.setWidget(tooltip);
        tooltipPanel.getElement().getStyle().setZIndex(1);
        registerHandlers();
    }

    private void registerHandlers() {
        addMouseOverHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                if (!StringUtils.isEmpty(title)) {
                    tooltip.setHTML(title);
                    tooltipPanel.showRelativeTo(LabelWithCustomToolTip.this);
                }
            }
        });
        addMouseOutHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {
                tooltipPanel.hide(true);
            }
        });
    }

    @Override
    public void setTitle(String text) {
        this.title = text;
    }

    public void setTitle(SafeHtml text) {
        setTitle(text == null ? (String) null : text.asString());
    }
}
