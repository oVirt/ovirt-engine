package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import java.util.List;

import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

public final class HostDeviceColumnHelper {

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    public static String renderNameId(String name, String id) {
        if (StringHelper.isNullOrEmpty(name)) {
            return id;
        }
        // we assume that VDSM will never report name != null && id == null
        return messages.nameId(name, id);
    }

    public static String renderVmNamesList(List<String> names) {
        if (names != null) {
            return String.join(", ", names); //$NON-NLS-1$
        }
        return "";
    }

    public static String renderIommuGroup(Integer group) {
        return group == null ? constants.notAvailableLabel() : group.toString();
    }
}
