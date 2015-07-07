package org.ovirt.engine.ui.common.widget.tab;

import org.ovirt.engine.ui.common.widget.Align;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.gwtplatform.mvp.client.TabData;

/**
 * Base class used to implement composite tab widgets.
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
public abstract class AbstractCompositeTab extends AbstractTab implements TabDefinition {

    public AbstractCompositeTab(TabData tabData, AbstractTabPanel tabPanel) {
        super(tabData, tabPanel);
    }

    public interface Style extends CssResource {

        String activeLeft();

        String inactiveLeft();

        String activeRight();

        String inactiveRight();

        String obrand_activeMiddle();

        String obrand_inactiveMiddle();

        String obrand_activeMiddleLink();

        String obrand_inactiveMiddleLink();

        String activeArrow();

        String inactiveArrow();

        String alignLeft();

        String alignRight();

    }

    @UiField
    public Label hyperlinkLabel;

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
    public Style style;

    @Override
    public String getText() {
        return hyperlinkLabel.getText();
    }

    @Override
    public void setText(String text) {
        hyperlinkLabel.setText(text);
    }

    @Override
    public void activate() {
        leftElement.replaceClassName(style.inactiveLeft(), style.activeLeft());
        rightElement.replaceClassName(style.inactiveRight(), style.activeRight());
        middleElement.replaceClassName(style.obrand_inactiveMiddle(), style.obrand_activeMiddle());
        hyperlinkLabel.getElement().replaceClassName(style.obrand_inactiveMiddleLink(), style.obrand_activeMiddleLink());
        arrowElement.replaceClassName(style.inactiveArrow(), style.activeArrow());
    }

    @Override
    public void deactivate() {
        leftElement.replaceClassName(style.activeLeft(), style.inactiveLeft());
        rightElement.replaceClassName(style.activeRight(), style.inactiveRight());
        middleElement.replaceClassName(style.obrand_activeMiddle(), style.obrand_inactiveMiddle());
        hyperlinkLabel.getElement().replaceClassName(style.obrand_activeMiddleLink(), style.obrand_inactiveMiddleLink());
        arrowElement.replaceClassName(style.activeArrow(), style.inactiveArrow());
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
