package org.ovirt.engine.ui.webadmin.uicommon;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;

public abstract class UiCommonModelUtil {

    public static VdsNetworkInterface getNetworkInterface(HostInterfaceLineModel model) {
        return model.getIsBonded() ? model.getInterface() : model.getInterfaces().get(0).getInterface();
    }

    public static List<VdsNetworkInterface> getNetworkInterfaces(List<HostInterfaceLineModel> models) {
        List<VdsNetworkInterface> result = new ArrayList<VdsNetworkInterface>(models.size());

        for (HostInterfaceLineModel m : models) {
            result.add(getNetworkInterface(m));
        }

        return result;
    }

    public static boolean contains(List<HostInterfaceLineModel> models, HostInterfaceLineModel target) {
        for (HostInterfaceLineModel m : models) {
            if (getNetworkInterface(m).getQueryableId().equals(getNetworkInterface(target).getQueryableId())) {
                return true;
            }
        }

        return false;
    }

    public static HostInterfaceLineModel findByInterface(List<HostInterfaceLineModel> models,
            VdsNetworkInterface networkInterface) {
        for (HostInterfaceLineModel m : models) {
            if (getNetworkInterface(m).getQueryableId().equals(networkInterface.getQueryableId())) {
                return m;
            }
        }

        return null;
    }

    public static void setIsSelected(HostInterfaceLineModel model, boolean selected) {
        model.setIsSelected(selected);

        // Set selection of corresponding HostInterface models
        for (Model m : model.getInterfaces()) {
            m.setIsSelected(selected);
        }

        // Set selection of corresponding HostVLan models
        for (Model m : model.getVLans()) {
            m.setIsSelected(selected);
        }
    }

}
