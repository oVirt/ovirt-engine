package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.backup;

import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine.SubTabVirtualMachineGeneralView;

import com.google.gwt.user.client.ui.IsWidget;

public class ImportVmGeneralSubTabView extends SubTabVirtualMachineGeneralView implements IsWidget {

    public ImportVmGeneralSubTabView(DetailModelProvider<VmListModel<Void>, VmGeneralModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider, constants);
    }

}
