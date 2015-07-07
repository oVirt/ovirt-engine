package org.ovirt.engine.ui.userportal.widget.tab;

import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.tab.AbstractTab;
import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.common.widget.tab.TabDefinition;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.TabData;

public class UserPortalMainTab extends AbstractTab implements TabDefinition {

    public Anchor hyperlink;
    protected HTMLPanel root;

    public UserPortalMainTab(TabData tabData, AbstractTabPanel tabPanel) {
        super(tabData, tabPanel);

        root = new HTMLPanel("li",  ""); //$NON-NLS-1$ //$NON-NLS-2$
        hyperlink = new Anchor();
        hyperlink.getElement().getStyle().setProperty("fontFamily", "'Open Sans', Helvetica, Arial, sans-serif !important"); //$NON-NLS-1$ //$NON-NLS-2$
        root.add(hyperlink);
        root.setVisible(true);
        accessible = true;
    }
    @Override
    public void activate() {
        root.addStyleName("active"); //$NON-NLS-1$
    }

    @Override
    public Widget asWidget() {
        return root.asWidget();
    }

    @Override
    public void deactivate() {
        root.removeStyleName("active"); //$NON-NLS-1$
    }

    @Override
    public void setAlign(Align align) {
        // no-op, these can only be LEFT
    }

    @Override
    public String getText() {
        return hyperlink.getText();
    }

    @Override
    public void setText(String text) {
        hyperlink.setText(text);
    }

    @Override
    public void setTargetHistoryToken(String historyToken) {
        hyperlink.setHref("#" + historyToken); //$NON-NLS-1$
    }

}
