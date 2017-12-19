package org.ovirt.engine.ui.common.widget.dialog;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.view.popup.FocusableComponentsContainer;
import org.ovirt.engine.ui.common.widget.PatternFlyCompatible;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ToggleButton;

public class AdvancedParametersExpander extends Composite implements FocusableComponentsContainer,
    PatternFlyCompatible {

    @UiField
    ToggleButton expander;

    private Element expanderContent;

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private static final CommonApplicationResources resources = AssetProvider.getResources();
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    private String titleExpanded = constants.advancedParameters();
    private String titleCollapsed = constants.advancedParameters();

    @UiField
    Style style;

    interface ViewUiBinder extends UiBinder<ToggleButton, AdvancedParametersExpander> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface Style extends CssResource {
        String expander();
        String expanderPf();
    }

    public AdvancedParametersExpander() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
    }

    public void initWithContent(Element expanderContent) {
        this.expanderContent = expanderContent;

        initStyle();
        initListener();
        initContent();
    }

    public void setUsePatternFly(boolean use) {
        if (use) {
            expander.removeStyleName(style.expander());
            expander.addStyleName(style.expanderPf());
        }
    }

    private void initStyle() {
        SafeHtml expandImage =
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.expanderImage()).getHTML());
        expander.getUpFace().setHTML(templates.imageTextButton(expandImage, titleCollapsed));

        SafeHtml collapseImage =
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.expanderDownImage()).getHTML());
        expander.getDownFace().setHTML(templates.imageTextButton(collapseImage, titleExpanded));

    }

    private void initContent() {
        expanderContent.getStyle().setDisplay(Display.NONE);
    }

    private void initListener() {
        expander.addClickHandler(event -> updateContentDisplay());
    }

    private void updateContentDisplay() {
        expanderContent.getStyle().setDisplay(expander.isDown() ? Display.BLOCK : Display.NONE);
    }

    public void addClickHandler(ClickHandler clickHandler) {
        expander.addClickHandler(clickHandler);
    }

    public boolean isDown() {
        return expander.isDown();
    }

    @Override
    public int setTabIndexes(int nextTabIndex) {
        expander.setTabIndex(nextTabIndex++);
        return nextTabIndex;
    }

    public void setTitleWhenExpanded(String title) {
        titleExpanded = title;
        initStyle();
    }

    public void setTitleWhenCollapsed(String title) {
        titleCollapsed = title;
        initStyle();
    }

    public void toggleExpander(boolean expand) {
        expander.setValue(expand);
        updateContentDisplay();
    }
}
