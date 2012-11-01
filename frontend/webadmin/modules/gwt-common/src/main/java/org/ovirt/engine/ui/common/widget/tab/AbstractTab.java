package org.ovirt.engine.ui.common.widget.tab;

import org.ovirt.engine.ui.common.widget.Align;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.gwtplatform.mvp.client.TabData;

/**
 * Base class used to implement tab widgets.
 * <p>
 * Subclasses are free to style the UI, given that they declare:
 * <ul>
 * <li>{@link #tabContainer} widget which wraps the tab content
 * <li>{@link #leftElement} that represents the left corner of the tab
 * <li>{@link #rightElement} that represents the right corner of the tab
 * <li>{@link #middleElement} that represents the middle area of the tab
 * <li>{@link #arrowElement} that represents the arrow area of the tab
 * <li>{@link #hyperlinkLabel} widget used to select (activate) the given tab
 * <li>{@link #style} with required CSS classes
 * </ul>
 */
public abstract class AbstractTab extends Composite implements TabDefinition {

    public interface Style extends CssResource {

        String activeLeft();

        String inactiveLeft();

        String activeRight();

        String inactiveRight();

        String activeMiddle();

        String inactiveMiddle();

        String activeMiddleLink();

        String inactiveMiddleLink();

        String activeArrow();

        String inactiveArrow();

        String alignLeft();

        String alignRight();

    }

    @UiField
    public Panel tabContainer;

    @UiField
    public Element leftElement;

    @UiField
    public Element rightElement;

    @UiField
    public Element middleElement;

    @UiField
    public Element arrowElement;

    @UiField
    public Label hyperlinkLabel;

    @UiField
    public AnchorElement hyperlink;

    @UiField
    public Style style;

    // Tab widgets are accessible by default
    public static final boolean DEFAULT_ACCESSIBLE = true;

    private final float priority;
    private final AbstractTabPanel tabPanel;

    private boolean accessible = DEFAULT_ACCESSIBLE;

    public AbstractTab(TabData tabData, AbstractTabPanel tabPanel) {
        this.priority = tabData.getPriority();
        this.tabPanel = tabPanel;
    }

    @Override
    public void activate() {
        leftElement.replaceClassName(style.inactiveLeft(), style.activeLeft());
        rightElement.replaceClassName(style.inactiveRight(), style.activeRight());
        middleElement.replaceClassName(style.inactiveMiddle(), style.activeMiddle());
        hyperlinkLabel.getElement().replaceClassName(style.inactiveMiddleLink(), style.activeMiddleLink());
        arrowElement.replaceClassName(style.inactiveArrow(), style.activeArrow());
    }

    @Override
    public void deactivate() {
        leftElement.replaceClassName(style.activeLeft(), style.inactiveLeft());
        rightElement.replaceClassName(style.activeRight(), style.inactiveRight());
        middleElement.replaceClassName(style.activeMiddle(), style.inactiveMiddle());
        hyperlinkLabel.getElement().replaceClassName(style.activeMiddleLink(), style.inactiveMiddleLink());
        arrowElement.replaceClassName(style.activeArrow(), style.inactiveArrow());
    }

    @Override
    public float getPriority() {
        return priority;
    }

    @Override
    public String getText() {
        return hyperlinkLabel.getText();
    }

    @Override
    public void setTargetHistoryToken(String historyToken) {
        hyperlink.setHref("#" + historyToken); //$NON-NLS-1$
    }

    @Override
    public void setText(String text) {
        hyperlinkLabel.setText(text);
    }

    @Override
    public boolean isAccessible() {
        return accessible;
    }

    @Override
    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
        tabPanel.updateTab(this);
    }

    @Override
    public void setAlign(Align align) {
        if (align == Align.RIGHT) {
            tabContainer.removeStyleName(style.alignLeft());
            tabContainer.addStyleName(style.alignRight());
        } else {
            tabContainer.removeStyleName(style.alignRight());
            tabContainer.addStyleName(style.alignLeft());
        }
    }

}
