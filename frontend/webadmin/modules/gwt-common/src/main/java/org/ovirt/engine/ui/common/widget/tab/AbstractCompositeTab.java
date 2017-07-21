package org.ovirt.engine.ui.common.widget.tab;

import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Panel;
import com.gwtplatform.mvp.client.TabData;

/**
 * Base class used to implement composite tab widgets.
 */
public abstract class AbstractCompositeTab extends AbstractTab implements TabDefinition {

    public AbstractCompositeTab(TabData tabData, AbstractTabPanel tabPanel) {
        super(tabData, tabPanel);
    }

    @UiField
    public Panel tabContainer;

    private String text;

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void activate() {
    }

    @Override
    public void deactivate() {
    }

}
