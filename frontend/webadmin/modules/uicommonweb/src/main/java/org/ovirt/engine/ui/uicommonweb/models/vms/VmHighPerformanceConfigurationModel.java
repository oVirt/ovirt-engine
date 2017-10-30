package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

public class VmHighPerformanceConfigurationModel extends ConfirmationModel {

    final UIConstants constants = ConstantsManager.getInstance().getConstants();
    final UIMessages messages = ConstantsManager.getInstance().getMessages();

    private List<String> recommendationsList;

    public VmHighPerformanceConfigurationModel() {
        recommendationsList = new ArrayList<>();
    }

    public List<String> getRecommendationsList() {
        return recommendationsList;
    }

    public void addRecommendationForCpuPinning(final boolean isVmAssignedToSpecificHosts, final boolean isVmCpuPinningSet) {
        if (!isVmAssignedToSpecificHosts) {
            recommendationsList.add(constants.highPerformancePopupRecommendationMsgForCpuSpecificHostPin());
        } else if (!isVmCpuPinningSet) {
            recommendationsList.add(constants.highPerformancePopupRecommendationMsgForCpuPin());
        }
    }

    public void addRecommendationForVirtNumaSetAndPinned(final boolean isVmVirtNumaSet, final boolean isVmVirtNumaPinned) {
        if (!isVmVirtNumaSet) {
            recommendationsList.add(constants.highPerformancePopupRecommendationMsgForNumaSetAndPinned());
        } else if (!isVmVirtNumaPinned) {
            recommendationsList.add(constants.highPerformancePopupRecommendationMsgForNumaPinned());
        }
    }

    public void addRecommendationForKsm(final boolean isKsmEnabled, final String clusterName) {
        if (isKsmEnabled) {
            recommendationsList.add(messages.highPerformancePopupRecommendationMsgForKsm(clusterName));
        }
    }

    public void addRecommendationForHugePages(final boolean isVmHugePagesSet) {
        if (!isVmHugePagesSet) {
            recommendationsList.add(constants.highPerformancePopupRecommendationMsgForHugePages());
        }
    }
}
