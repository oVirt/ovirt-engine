package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabReportsPresenter;
import org.ovirt.engine.ui.webadmin.widget.form.PostableFrame;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

public class MainTabReportsView extends AbstractView implements MainTabReportsPresenter.ViewDef {
    private static MainTabReportsViewUiBinder uiBinder = GWT.create(MainTabReportsViewUiBinder.class);

    @UiField(provided = true)
    PostableFrame reportPostableFrame;

    interface MainTabReportsViewUiBinder extends UiBinder<Widget, MainTabReportsView> {
    }

    public MainTabReportsView() {
        reportPostableFrame = new PostableFrame("dashboard"); //$NON-NLS-1$
        initWidget(uiBinder.createAndBindUi(this));

    }

    @Override
    public void updateReportsPanel(String url,
            Map<String, List<String>> params) {
        reportPostableFrame.setUrl(url);

        // Set parameters
        reportPostableFrame.removeOldParams();
        for (Entry<String, List<String>> entry : params.entrySet()) {
            for (String param : entry.getValue()) {
                reportPostableFrame.addParameter(entry.getKey(), param);
            }
        }

        reportPostableFrame.post();
    }
}
