package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.backup;

import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.template.SubTabTemplateGeneralView;

import com.google.gwt.user.client.ui.IsWidget;

public class ImportTemplateGeneralSubTabView extends SubTabTemplateGeneralView implements IsWidget {

    public ImportTemplateGeneralSubTabView(DetailModelProvider<TemplateListModel, TemplateGeneralModel> modelProvider) {
        super(modelProvider);
    }

}
