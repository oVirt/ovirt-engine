package org.ovirt.engine.ui.common.widget;

import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.ovirt.engine.ui.common.widget.editor.EditorWidget;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.user.client.TakesValue;

public abstract class AbstractValidatedWidgetWithIcon<T, W extends EditorWidget<T, ?> & TakesValue<T> & HasValueChangeHandlers<T>> extends AbstractValidatedWidgetWithLabel<T, W> {

    private Span icon = new Span();
    // container the icon and the contentWidget
    private Div subContentWidgetContainer = new Div();
    private String iconName;

    /**
     * If use this constructor without the argument of iconName, please use the #setIcon(String iconName) to set the
     * icon.
     *
     * @param contentWidget
     */
    public AbstractValidatedWidgetWithIcon(W contentWidget) {
        this(contentWidget, new VisibilityRenderer.SimpleVisibilityRenderer());
    }

    public AbstractValidatedWidgetWithIcon(W contentWidget, VisibilityRenderer renderer) {
        super(contentWidget, renderer);
        initWidgetWithIcon(getIcon());
    }

    public AbstractValidatedWidgetWithIcon(W contentWidget, String iconName) {
        this(contentWidget, new VisibilityRenderer.SimpleVisibilityRenderer(), iconName);
    }

    public AbstractValidatedWidgetWithIcon(W contentWidget, VisibilityRenderer renderer, String iconName) {
        super(contentWidget, renderer);
        this.iconName = iconName;
        initWidgetWithIcon(iconName);
    }

    private void initWidgetWithIcon(String iconName) {
        // hide the label
        hideLabel();

        contentWidgetContainer.remove(getContentWidget());
        contentWidgetContainer.setWidget(subContentWidgetContainer);
        addIcon(iconName);

    }

    private void addIcon(String iconName) {
        this.iconName = iconName;
        // CSS definition,the concrete definition in the login-common.css
        getSubContentWidgetContainer().addStyleName(WidgetWithIconProfile.INNER_ADDON);
        getIconPanel().addStyleName(WidgetWithIconProfile.INNER_ADDON_ICON);
        getIconPanel().addStyleName(WidgetWithIconProfile.LEFT_ADDON_ICON);
        getContentWidget().asWidget().addStyleName(WidgetWithIconProfile.INNER_ADDON_INPUT_PULL_LEFT);
        getIconPanel().addStyleName(iconName);

        getSubContentWidgetContainer().add(icon);
        getSubContentWidgetContainer().add(getContentWidget());

    }

    private Span getIconPanel() {
        return icon;
    }

    private Div getSubContentWidgetContainer() {
        return subContentWidgetContainer;
    }

    public void removeIcon() {
        getIconPanel().setVisible(false);
    }

    public void addStyleNameToIcon(String style) {
        getIconPanel().addStyleName(style);
    }

    public void setIcon(String iconName) {
        this.iconName = iconName;
    }

    public String getIcon() {
        return iconName;
    }

    public void addIconStyleName(String style) {
        getIconPanel().addStyleName(style);
    }

    public void removeIconStyleName(String style) {
        getIconPanel().removeStyleName(style);
    }

    @Override
    public void hideLabel() {
        super.hideLabel();
    }

    public void displayLabel() {
        super.label.setVisible(true);
    }

    // Binding with the css
    private static class WidgetWithIconProfile {
        private final static String INNER_ADDON = "inner-addon";//$NON-NLS-1$
        private final static String INNER_ADDON_ICON = "inner-addon-icon";//$NON-NLS-1$
        private final static String LEFT_ADDON_ICON = "left-addon-icon";//$NON-NLS-1$
        private final static String INNER_ADDON_INPUT_PULL_LEFT = "inner-addon-input-pull-left";//$NON-NLS-1$
    }
}
