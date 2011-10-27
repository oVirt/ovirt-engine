package org.ovirt.engine.ui.webadmin.section.main.view;

import java.util.Arrays;

import javax.inject.Inject;

import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainSectionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTabBarOffsetUiHandlers;
import org.ovirt.engine.ui.webadmin.system.InternalConfiguration;
import org.ovirt.engine.ui.webadmin.uicommon.ClientAgentType;
import org.ovirt.engine.ui.webadmin.uicommon.model.BookmarkModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SystemTreeModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.TagModelProvider;
import org.ovirt.engine.ui.webadmin.view.AbstractView;
import org.ovirt.engine.ui.webadmin.widget.bookmark.BookmarkList;
import org.ovirt.engine.ui.webadmin.widget.tags.TagList;
import org.ovirt.engine.ui.webadmin.widget.tree.SystemTree;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class MainSectionView extends AbstractView implements MainSectionPresenter.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, MainSectionView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    private MainTabBarOffsetUiHandlers uiHandlers;

    @UiField
    SimplePanel headerPanel;

    @UiField(provided = true)
    final StackLayoutPanel westStackPanel;

    @UiField
    LayoutPanel mainContentPanel;

    @UiField
    Label footerMessage;

    @Inject
    public MainSectionView(SystemTreeModelProvider treeModelProvider, BookmarkModelProvider bookmarkModelProvider,
            TagModelProvider tagModelProvider, ClientAgentType clientAgentType, InternalConfiguration intConf,
            ApplicationMessages appMessages) {
        this.westStackPanel = createWestStackPanel(treeModelProvider, bookmarkModelProvider, tagModelProvider);
        Widget widget = ViewUiBinder.uiBinder.createAndBindUi(this);
        initWidget(widget);
        headerPanel.getElement().getParentElement().getStyle().setOverflow(Overflow.VISIBLE);

        if ((!intConf.getSupportedBrowsers().containsKey(clientAgentType.browser))
                || (!Arrays.asList(intConf.getSupportedBrowsers().get(clientAgentType.browser))
                        .contains(clientAgentType.version))) {
            // Browser is not supported
            footerMessage.setText(appMessages.browserNotSupportedVersion(clientAgentType.browser,
                    clientAgentType.version.toString()));
        } else {
            DockLayoutPanel layout = (DockLayoutPanel) widget;
            layout.remove(footerMessage);
        }
    }

    StackLayoutPanel createWestStackPanel(SystemTreeModelProvider treeModelProvider,
            BookmarkModelProvider bookmarkModelProvider, TagModelProvider tagModelProvider) {
        final StackLayoutPanel panel = new StackLayoutPanel(Unit.PX) {
            @Override
            public void onResize() {
                super.onResize();

                if (uiHandlers != null) {
                    uiHandlers.setMainTabBarOffset(getOffsetWidth());
                }
            }
        };

        panel.add(new SystemTree(treeModelProvider), "Tree", 26);
        panel.add(new BookmarkList(bookmarkModelProvider), "Bookmarks", 26);
        panel.add(new TagList(tagModelProvider), "Tags", 26);

        return panel;
    }

    @Override
    public void setInSlot(Object slot, Widget content) {
        if (slot == MainSectionPresenter.TYPE_SetHeader) {
            setPanelContent(headerPanel, content);
        } else if (slot == MainSectionPresenter.TYPE_SetMainContent) {
            setPanelContent(mainContentPanel, content);
        } else {
            super.setInSlot(slot, content);
        }
    }

    @Override
    public void setUiHandlers(MainTabBarOffsetUiHandlers uiHandlers) {
        this.uiHandlers = uiHandlers;
    }

}
