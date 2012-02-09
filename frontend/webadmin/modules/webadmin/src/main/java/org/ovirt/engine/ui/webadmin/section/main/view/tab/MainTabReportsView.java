package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabReportsPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.dashboard.ReportsPanelManager;

import com.google.gwt.user.client.ui.SimplePanel;

public class MainTabReportsView extends AbstractView implements MainTabReportsPresenter.ViewDef {

    private final SimplePanel mainPanel = new SimplePanel();

    @Inject
    public MainTabReportsView() {
        initWidget(mainPanel);
    }

    @Override
    public void updateReportsPanel(SystemTreeItemType type, String url, Map<String, List<String>> params) {
        ReportsPanel reportsPanel = ReportsPanelManager.getInstance().getReportsPanel(type);
        mainPanel.clear();
        mainPanel.add(reportsPanel);
        if (reportsPanel != null) {
            reportsPanel.setCommonUrl(url);
            reportsPanel.setParams(params);
            reportsPanel.post();
        }
    }
}
