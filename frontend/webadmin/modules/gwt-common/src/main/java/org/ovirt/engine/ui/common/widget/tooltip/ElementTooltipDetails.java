package org.ovirt.engine.ui.common.widget.tooltip;

/**
 * Wrapper for storing tooltip + its element's html in a map.
 */
public class ElementTooltipDetails {

    private ElementTooltip tooltip;
    private String innerHTML;

    public ElementTooltip getTooltip() {
        return tooltip;
    }

    public void setTooltip(ElementTooltip tooltip) {
        this.tooltip = tooltip;
    }

    public String getInnerHTML() {
        return innerHTML;
    }

    public void setInnerHTML(String innerHTML) {
        this.innerHTML = innerHTML;
    }

}
