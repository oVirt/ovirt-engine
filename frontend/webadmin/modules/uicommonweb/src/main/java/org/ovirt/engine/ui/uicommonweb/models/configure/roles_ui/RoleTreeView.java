package org.ovirt.engine.ui.uicommonweb.models.configure.roles_ui;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.ui.uicommonweb.models.common.SelectionTreeNodeModel;

@SuppressWarnings("unused")
public class RoleTreeView
{
    public static java.util.ArrayList<SelectionTreeNodeModel> GetRoleTreeView(boolean isReadOnly, boolean isAdmin)
    {
        RoleNode tree = initTreeView();
        java.util.ArrayList<ActionGroup> userActionGroups = null;
        if (isAdmin == false)
        {
            userActionGroups = GetUserActionGroups();
        }

        java.util.ArrayList<SelectionTreeNodeModel> roleTreeView = new java.util.ArrayList<SelectionTreeNodeModel>();

        SelectionTreeNodeModel firstNode = null, secondNode = null, thirdNode = null;
        for (RoleNode first : tree.getLeafRoles())
        {
            firstNode = new SelectionTreeNodeModel();
            firstNode.setTitle(first.getName());
            firstNode.setDescription(first.getName());
            firstNode.setIsChangable(!isReadOnly);

            for (RoleNode second : first.getLeafRoles())
            {
                secondNode = new SelectionTreeNodeModel();
                secondNode.setTitle(second.getName());
                secondNode.setDescription(second.getName());
                secondNode.setIsChangable(!isReadOnly);
                secondNode.setTooltip(second.getTooltip());
                for (RoleNode third : second.getLeafRoles())
                {
                    thirdNode = new SelectionTreeNodeModel();
                    thirdNode.setTitle(third.getName());
                    thirdNode.setDescription(third.getDesc());
                    thirdNode.setIsSelectedNotificationPrevent(true);
                    // thirdNode.IsSelected =
                    // attachedActions.Contains((VdcActionType) Enum.Parse(typeof (VdcActionType), name)); //TODO:
                    // suppose to be action group
                    thirdNode.setIsChangable(!isReadOnly);
                    thirdNode.setIsSelectedNullable(false);
                    thirdNode.setTooltip(third.getTooltip());
                    if (!isAdmin)
                    {
                        if (userActionGroups.contains(ActionGroup.valueOf(thirdNode.getTitle())))
                        {
                            secondNode.getChildren().add(thirdNode);
                        }
                    }
                    else
                    {
                        secondNode.getChildren().add(thirdNode);
                    }

                }
                if (secondNode.getChildren().size() > 0)
                {
                    firstNode.getChildren().add(secondNode);
                }
            }
            if (firstNode.getChildren().size() > 0)
            {
                roleTreeView.add(firstNode);
            }
        }

        return roleTreeView;
    }

    private static ArrayList<ActionGroup> GetUserActionGroups() {
        ArrayList<ActionGroup> array = new ArrayList<ActionGroup>();
        array.add(ActionGroup.CREATE_VM);
        array.add(ActionGroup.DELETE_VM);
        array.add(ActionGroup.EDIT_VM_PROPERTIES);
        array.add(ActionGroup.VM_BASIC_OPERATIONS);
        array.add(ActionGroup.CHANGE_VM_CD);
        array.add(ActionGroup.MIGRATE_VM);
        array.add(ActionGroup.CONNECT_TO_VM);
        array.add(ActionGroup.CONFIGURE_VM_NETWORK);
        array.add(ActionGroup.CONFIGURE_VM_STORAGE);
        array.add(ActionGroup.MOVE_VM);
        array.add(ActionGroup.MANIPULATE_VM_SNAPSHOTS);
        array.add(ActionGroup.CREATE_TEMPLATE);
        array.add(ActionGroup.EDIT_TEMPLATE_PROPERTIES);
        array.add(ActionGroup.DELETE_TEMPLATE);
        array.add(ActionGroup.COPY_TEMPLATE);
        array.add(ActionGroup.CONFIGURE_TEMPLATE_NETWORK);
        array.add(ActionGroup.CREATE_VM_POOL);
        array.add(ActionGroup.EDIT_VM_POOL_CONFIGURATION);
        array.add(ActionGroup.DELETE_VM_POOL);
        array.add(ActionGroup.VM_POOL_BASIC_OPERATIONS);
        array.add(ActionGroup.MANIPULATE_PERMISSIONS);
        return array;

    }

    private static RoleNode initTreeView()
    {
        RoleNode tree =
                new RoleNode("root",
                        new RoleNode[] {
                                new RoleNode("System", new RoleNode("Configure System", new RoleNode[] {
                                        new RoleNode(ActionGroup.MANIPULATE_USERS,
                                                "Allow to Add/Remove Users from the System"),
                                        new RoleNode(ActionGroup.MANIPULATE_PERMISSIONS,
                                                "Allow to add/remove permissions for Users on objects in the system"),
                                        new RoleNode(ActionGroup.MANIPULATE_ROLES,
                                                "Allow to define/configure roles in the System"),
                                        new RoleNode(ActionGroup.CONFIGURE_ENGINE,
                                                "Allow to get or set System Configuration") })),
                                // new RoleNode("Gluster", new RoleNode("Configure Volumes", new RoleNode[] {
                                // new RoleNode(ActionGroup.GLUSTER_CREATE_VOLUME,
                                // "Allow to create Gluster Volumes"),
                                // new RoleNode(ActionGroup.GLUSTER_VOLUME_OPERATIONS,
                                // "Allow to manipulate Gluster Volumes") })),
                                new RoleNode("Data Center", new RoleNode("Configure Data Center", new RoleNode[] {
                                        new RoleNode(ActionGroup.CREATE_STORAGE_POOL, "Allow to create Data Center"),
                                        new RoleNode(ActionGroup.DELETE_STORAGE_POOL, "Allow to remove Data Center"),
                                        new RoleNode(ActionGroup.EDIT_STORAGE_POOL_CONFIGURATION,
                                                "Allow to modify Data Center properties"),
                                        new RoleNode(ActionGroup.CONFIGURE_STORAGE_POOL_NETWORK,
                                                "Allow to configure Logical Network per Data Center") })),
                                new RoleNode("Storage Domain",
                                        new RoleNode("Configure Storage Domain",
                                                new RoleNode[] {
                                                        new RoleNode(ActionGroup.CREATE_STORAGE_DOMAIN,
                                                                "Allow to create Storage Domain"),
                                                        new RoleNode(ActionGroup.DELETE_STORAGE_DOMAIN,
                                                                "Allow to delete Storage Domain"),
                                                        new RoleNode(ActionGroup.EDIT_STORAGE_DOMAIN_CONFIGURATION,
                                                                "Allow to modify Storage Domain properties"),
                                                        new RoleNode(ActionGroup.MANIPULATE_STORAGE_DOMAIN,
                                                                "Allow to change Storage Domain status:  maintenance/activate; attach/detach") })),
                                new RoleNode("Cluster",
                                        new RoleNode("Configure Cluster",
                                                new RoleNode[] {
                                                        new RoleNode(ActionGroup.CREATE_CLUSTER,
                                                                "Allow to create new Cluster"),
                                                        new RoleNode(ActionGroup.DELETE_CLUSTER,
                                                                "Allow to remove Cluster"),
                                                        new RoleNode(ActionGroup.EDIT_CLUSTER_CONFIGURATION,
                                                                "Allow to Edit Cluster properties"),
                                                        new RoleNode(ActionGroup.CONFIGURE_CLUSTER_NETWORK,
                                                                "Allow to add/remove Logical Networks for the Cluster (from the list of Networks defined by the Data Center)") })),
                                new RoleNode("Host", new RoleNode("Configure Host", new RoleNode[] {
                                        new RoleNode(ActionGroup.CREATE_HOST, "Allow to add new Host to the Cluster"),
                                        new RoleNode(ActionGroup.DELETE_HOST,
                                                "Allow  to remove existing Host from the Cluster"),
                                        new RoleNode(ActionGroup.EDIT_HOST_CONFIGURATION,
                                                "Allow to Edit Host properties; upgrade/install"),
                                        new RoleNode(ActionGroup.MANIPUTLATE_HOST,
                                                "Allow to change Host status: activate/maintenance"),
                                        new RoleNode(ActionGroup.CONFIGURE_HOST_NETWORK,
                                                "Allow to configure Host's Network physical interfaces (Nics)") })),
                                new RoleNode("Template",
                                        new RoleNode[] {
                                                new RoleNode("Basic Operations", new RoleNode[] {
                                                        new RoleNode(ActionGroup.EDIT_TEMPLATE_PROPERTIES,
                                                                "Allow to change  Template properties"),
                                                        new RoleNode(ActionGroup.CONFIGURE_TEMPLATE_NETWORK,
                                                                "Allow to configure Temlate Network") }),
                                                new RoleNode("Provisioning Operations",
                                                        "note: Permissions containig these operations should be associated with Storage Domain Object (or above)",
                                                        new RoleNode[] {
                                                                new RoleNode(ActionGroup.CREATE_TEMPLATE,
                                                                        "Allow to create new Template"),
                                                                new RoleNode(ActionGroup.DELETE_TEMPLATE,
                                                                        "Allow to remove existing Template"),
                                                                new RoleNode(ActionGroup.IMPORT_EXPORT_VM,
                                                                        "Allow import/export operations"),
                                                                new RoleNode(ActionGroup.COPY_TEMPLATE,
                                                                        "Allow to copy Template between Storage Domains") }) }),
                                new RoleNode("VM",
                                        new RoleNode[] {
                                                new RoleNode("Basic Operations", new RoleNode[] {
                                                        new RoleNode(ActionGroup.VM_BASIC_OPERATIONS,
                                                                "Allow basic VM operations - Run/Stop/Pause"),
                                                        new RoleNode(ActionGroup.CHANGE_VM_CD,
                                                                "Allow to attach CD to the VM"),
                                                        new RoleNode(ActionGroup.CONNECT_TO_VM,
                                                                "Allow viewing the  the VM Console Screen") }),
                                                new RoleNode("Provisioning Operations",
                                                        "note: Permissions containig these operations should be associated with Storage Domain Object (or above)",
                                                        new RoleNode[] {
                                                                new RoleNode(ActionGroup.EDIT_VM_PROPERTIES,
                                                                        "Allow Change VM properties"),
                                                                new RoleNode(ActionGroup.CREATE_VM,
                                                                        "Allow to create new Vms"),
                                                                new RoleNode(ActionGroup.DELETE_VM,
                                                                        "Allow to remove Vms from the system"),
                                                                new RoleNode(ActionGroup.IMPORT_EXPORT_VM,
                                                                        "Allow import/export operations"),
                                                                new RoleNode(ActionGroup.CONFIGURE_VM_NETWORK,
                                                                        "Allow to configure VMs network"),
                                                                new RoleNode(ActionGroup.CONFIGURE_VM_STORAGE,
                                                                        "Allow to add/remove disk to the VM"),
                                                                new RoleNode(ActionGroup.MANIPULATE_VM_SNAPSHOTS,
                                                                        "Allow to create/delete snapshots of the VM") }),
                                                new RoleNode("Administration Operations",
                                                        "note: Permissions containig these operations should be associated with Data Center Object or equivalent)",
                                                        new RoleNode[] {
                                                                new RoleNode(ActionGroup.MOVE_VM,
                                                                        "Allow to move VM image to another Storage Domain"),
                                                                new RoleNode(ActionGroup.MIGRATE_VM,
                                                                        "Allow migrating VM between Hosts in a Cluster") }) }),
                                new RoleNode("VM Pool",
                                        new RoleNode[] {
                                                new RoleNode("Basic Operations",
                                                        new RoleNode[] { new RoleNode(ActionGroup.VM_POOL_BASIC_OPERATIONS,
                                                                "Allow to Run/Pause/Stop a VM from VM-Pool") }),
                                                new RoleNode("Provisioning Operations",
                                                        "note: Permissions containig these operations should be associated with Storage Domain Object (or above)",
                                                        new RoleNode[] {
                                                                new RoleNode(ActionGroup.CREATE_VM_POOL,
                                                                        "Allow to create VM-Pool"),
                                                                new RoleNode(ActionGroup.DELETE_VM_POOL,
                                                                        "Allow to delete VM-Pool"),
                                                                new RoleNode(ActionGroup.EDIT_VM_POOL_CONFIGURATION,
                                                                        "Allow to change properties of the VM-Pool") }) }) });
        return tree;
    }
}
