package org.ovirt.engine.ui.uicommonweb.models;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.EventNotificationEntity;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.configure.roles_ui.RoleNode;

public class ApplicationModeHelper {

    private static ApplicationMode UI_MODE = ApplicationMode.AllModes;

    public static boolean isAvailableInMode(int availableModes) {
        return (availableModes & UI_MODE.getValue()) > 0;
    }

    public static boolean isModeSupported(ApplicationMode mode) {
        return isAvailableInMode(mode.getValue());
    }

    public static ApplicationMode getUiMode() {
        return UI_MODE;
    }

    public static void setUiMode(ApplicationMode uiMode) {
        if (uiMode != null) {
            UI_MODE = uiMode;
        }
    }

    public static ArrayList<EventNotificationEntity> getModeSpecificEventNotificationTypeList() {
        ArrayList<EventNotificationEntity> subList = new ArrayList<>();
        for (EventNotificationEntity entity : AsyncDataProvider.getInstance().getEventNotificationTypeList()) {
            if ((entity.getAvailableInModes() & UI_MODE.getValue()) > 0) {
                subList.add(entity);
            }
        }
        return subList;
    }

    public static boolean filterActionGroupTreeByApplictionMode(RoleNode tree) {
        ArrayList<RoleNode> list = new ArrayList<>();
        for (RoleNode node : tree.getLeafRoles()) {
            if (node.getLeafRoles() == null || node.getLeafRoles().isEmpty()) {
                return (ActionGroup.valueOf(node.getName()).getAvailableInModes() & getUiMode().getValue()) == 0;
            }
            if (filterActionGroupTreeByApplictionMode(node)) {
                list.add(node);
            }
        }
        for (RoleNode roleNode : list) {
            tree.getLeafRoles().remove(roleNode);
        }

        return tree.getLeafRoles().size() == 0;
    }

    public static boolean filterSystemTreeByApplictionMode(SystemTreeItemModel systemItem) {
        List<SystemTreeItemModel> list = new ArrayList<>();
        for (SystemTreeItemModel item : systemItem.getChildren()) {
            if (filterSystemTreeByApplictionMode(item)) {
                list.add(item);
            }
        }
        if (list.size() > 0 && list.size() == systemItem.getChildren().size()) {
            List<SystemTreeItemModel> childItems = new ArrayList<>();
            for (SystemTreeItemModel item : list) {
                childItems.addAll(item.getChildren());
            }
            systemItem.setChildren(childItems);
        }

        for (SystemTreeItemModel systemTreeItemModel : list) {
            systemItem.getChildren().remove(systemTreeItemModel);
            if(systemTreeItemModel.getChildren() != null && !systemItem.getChildren().containsAll(systemTreeItemModel.getChildren())) {
                systemItem.getChildren().addAll(systemTreeItemModel.getChildren());
            }
        }
        return !((systemItem.getApplicationMode().getValue() & getUiMode().getValue()) > 0);
    }

}
