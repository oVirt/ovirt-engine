package org.ovirt.engine.ui.uicommonweb.models.configure.roles_ui;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.common.SelectionTreeNodeModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class RoleTreeView
{
    public static ArrayList<SelectionTreeNodeModel> GetRoleTreeView(boolean isReadOnly, boolean isAdmin)
    {
        RoleNode tree = initTreeView();
        ArrayList<ActionGroup> userActionGroups = null;
        if (isAdmin == false)
        {
            userActionGroups = GetUserActionGroups();
        }

        ArrayList<SelectionTreeNodeModel> roleTreeView = new ArrayList<SelectionTreeNodeModel>();

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
        array.add(ActionGroup.CREATE_DISK);
        array.add(ActionGroup.ATTACH_DISK);
        array.add(ActionGroup.DELETE_DISK);
        array.add(ActionGroup.CONFIGURE_DISK_STORAGE);
        array.add(ActionGroup.EDIT_DISK_PROPERTIES);
        array.add(ActionGroup.LOGIN);
        array.add(ActionGroup.CHANGE_VM_CUSTOM_PROPERTIES);
        array.add(ActionGroup.PORT_MIRRORING);
        return array;

    }

    private static RoleNode initTreeView()
    {
        RoleNode tree =
                new RoleNode(ConstantsManager.getInstance().getConstants().rootRoleTree(),
                        new RoleNode[] {
                                new RoleNode(ConstantsManager.getInstance().getConstants().systemRoleTree(),
                                        new RoleNode(ConstantsManager.getInstance()
                                                .getConstants()
                                                .configureSystemRoleTree(),
                                                new RoleNode[] {
                                                        new RoleNode(ActionGroup.MANIPULATE_USERS,
                                                                ConstantsManager.getInstance()
                                                                        .getConstants()
                                                                        .allowToAddRemoveUsersFromTheSystemRoleTreeTooltip()),
                                                        new RoleNode(ActionGroup.MANIPULATE_PERMISSIONS,
                                                                ConstantsManager.getInstance()
                                                                        .getConstants()
                                                                        .allowToAddRemovePermissionsForUsersOnObjectsInTheSystemRoleTreeTooltip()),
                                                        new RoleNode(ActionGroup.MANIPULATE_ROLES,
                                                                ConstantsManager.getInstance()
                                                                        .getConstants()
                                                                        .allowToDefineConfigureRolesInTheSystemRoleTreeTooltip()),
                                                        new RoleNode(ActionGroup.LOGIN,
                                                                ConstantsManager.getInstance()
                                                                        .getConstants()
                                                                        .allowToLoginToTheSystemRoleTreeTooltip()),
                                                        new RoleNode(ActionGroup.CONFIGURE_ENGINE,
                                                                ConstantsManager.getInstance()
                                                                        .getConstants()
                                                                        .allowToGetOrSetSystemConfigurationRoleTreeTooltip()) })),
                                new RoleNode(ConstantsManager.getInstance().getConstants().dataCenterRoleTree(),
                                        new RoleNode(ConstantsManager.getInstance()
                                                .getConstants()
                                                .configureDataCenterRoleTree(),
                                                new RoleNode[] {
                                                        new RoleNode(ActionGroup.CREATE_STORAGE_POOL,
                                                                ConstantsManager.getInstance()
                                                                        .getConstants()
                                                                        .allowToCreateDataCenterRoleTreeTooltip()),
                                                        new RoleNode(ActionGroup.DELETE_STORAGE_POOL,
                                                                ConstantsManager.getInstance()
                                                                        .getConstants()
                                                                        .allowToRemoveDataCenterRoleTreeTooltip()),
                                                        new RoleNode(ActionGroup.EDIT_STORAGE_POOL_CONFIGURATION,
                                                                ConstantsManager.getInstance()
                                                                        .getConstants()
                                                                        .allowToModifyDataCenterPropertiesRoleTreeTooltip()),
                                                        new RoleNode(ActionGroup.CONFIGURE_STORAGE_POOL_NETWORK,
                                                                ConstantsManager.getInstance()
                                                                        .getConstants()
                                                                        .allowToConfigureLogicalNetworkPerDataCenterRoleTreeTooltip()) })),
                                new RoleNode(ConstantsManager.getInstance().getConstants().storageDomainRoleTree(),
                                        new RoleNode(ConstantsManager.getInstance()
                                                .getConstants()
                                                .configureStorageDomainRoleTree(),
                                                new RoleNode[] {
                                                        new RoleNode(ActionGroup.CREATE_STORAGE_DOMAIN,
                                                                ConstantsManager.getInstance()
                                                                        .getConstants()
                                                                        .allowToCreateStorageDomainRoleTreeTooltip()),
                                                        new RoleNode(ActionGroup.DELETE_STORAGE_DOMAIN,
                                                                ConstantsManager.getInstance()
                                                                        .getConstants()
                                                                        .allowToDeleteStorageDomainRoleTreeTooltip()),
                                                        new RoleNode(ActionGroup.EDIT_STORAGE_DOMAIN_CONFIGURATION,
                                                                ConstantsManager.getInstance()
                                                                        .getConstants()
                                                                        .allowToModifyStorageDomainPropertiesRoleTreeTooltip()),
                                                        new RoleNode(ActionGroup.MANIPULATE_STORAGE_DOMAIN,
                                                                ConstantsManager.getInstance()
                                                                        .getConstants()
                                                                        .allowToChangeStorageDomainStatusRoleTreeTooltip()) })),
                                new RoleNode(ConstantsManager.getInstance().getConstants().clusterRoleTree(),
                                        new RoleNode(ConstantsManager.getInstance()
                                                .getConstants()
                                                .configureClusterRoleTree(),
                                                new RoleNode[] {
                                                        new RoleNode(ActionGroup.CREATE_CLUSTER,
                                                                ConstantsManager.getInstance()
                                                                        .getConstants()
                                                                        .allowToCreateNewClusterRoleTreeTooltip()),
                                                        new RoleNode(ActionGroup.DELETE_CLUSTER,
                                                                ConstantsManager.getInstance()
                                                                        .getConstants()
                                                                        .allowToRemoveClusterRoleTreeTooltip()),
                                                        new RoleNode(ActionGroup.EDIT_CLUSTER_CONFIGURATION,
                                                                ConstantsManager.getInstance()
                                                                        .getConstants()
                                                                        .allowToEditClusterPropertiesRoleTreeTooltip()),
                                                        new RoleNode(ActionGroup.CONFIGURE_CLUSTER_NETWORK,
                                                                ConstantsManager.getInstance()
                                                                        .getConstants()
                                                                        .allowToAddRemoveLogicalNetworksForTheClusterRoleTreeTooltip()) })),
                                new RoleNode(ConstantsManager.getInstance().getConstants().glusterRoleTree(),
                                        new RoleNode(ConstantsManager.getInstance()
                                                .getConstants()
                                                .configureVolumesRoleTree(),
                                                new RoleNode[] {
                                                        new RoleNode(ActionGroup.CREATE_GLUSTER_VOLUME,
                                                                ConstantsManager.getInstance()
                                                                        .getConstants()
                                                                        .allowToCreateGlusterVolumesRoleTree()),
                                                        new RoleNode(ActionGroup.MANIPULATE_GLUSTER_VOLUME,
                                                                ConstantsManager.getInstance()
                                                                        .getConstants()
                                                                        .allowToManipulateGlusterVolumesRoleTree()) })),
                                new RoleNode(ConstantsManager.getInstance().getConstants().hostRoleTree(),
                                        new RoleNode(ConstantsManager.getInstance()
                                                .getConstants()
                                                .configureHostRoleTree(),
                                                new RoleNode[] {
                                                        new RoleNode(ActionGroup.CREATE_HOST,
                                                                ConstantsManager.getInstance()
                                                                        .getConstants()
                                                                        .allowToAddNewHostToTheClusterRoleTreeTooltip()),
                                                        new RoleNode(ActionGroup.DELETE_HOST,
                                                                ConstantsManager.getInstance()
                                                                        .getConstants()
                                                                        .allowToRemoveExistingHostFromTheClusterRoleTreeTooltip()),
                                                        new RoleNode(ActionGroup.EDIT_HOST_CONFIGURATION,
                                                                ConstantsManager.getInstance()
                                                                        .getConstants()
                                                                        .allowToEditHostPropertiesRoleTreeTooltip()),
                                                        new RoleNode(ActionGroup.MANIPUTLATE_HOST,
                                                                ConstantsManager.getInstance()
                                                                        .getConstants()
                                                                        .allowToChangeHostStatusRoleTreeTooltip()),
                                                        new RoleNode(ActionGroup.CONFIGURE_HOST_NETWORK,
                                                                ConstantsManager.getInstance()
                                                                        .getConstants()
                                                                        .allowToConfigureHostsNetworkPhysicalInterfacesRoleTreeTooltip()) })),
                                new RoleNode(ConstantsManager.getInstance().getConstants().templateRoleTree(),
                                        new RoleNode[] {
                                                new RoleNode(ConstantsManager.getInstance()
                                                        .getConstants()
                                                        .basicOperationsRoleTree(),
                                                        new RoleNode[] {
                                                                new RoleNode(ActionGroup.EDIT_TEMPLATE_PROPERTIES,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowToChangeTemplatePropertiesRoleTreeTooltip()),
                                                                new RoleNode(ActionGroup.CONFIGURE_TEMPLATE_NETWORK,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowToConfigureTemlateNetworkRoleTreeTooltip()) }),
                                                new RoleNode(ConstantsManager.getInstance()
                                                        .getConstants()
                                                        .provisioningOperationsRoleTree(),
                                                        ConstantsManager.getInstance()
                                                                .getConstants()
                                                                .notePermissionsContainigTheseOperationsShuoldAssociatSdOrAboveRoleTreeTooltip(),
                                                        new RoleNode[] {
                                                                new RoleNode(ActionGroup.CREATE_TEMPLATE,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowToCreateNewTemplateRoleTreeTooltip()),
                                                                new RoleNode(ActionGroup.DELETE_TEMPLATE,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowToRemoveExistingTemplateRoleTreeTooltip()),
                                                                new RoleNode(ActionGroup.IMPORT_EXPORT_VM,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowImportExportOperationsRoleTreeTooltip()),
                                                                new RoleNode(ActionGroup.COPY_TEMPLATE,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowToCopyTemplateBetweenStorageDomainsRoleTreeTooltip()) }) }),
                                new RoleNode(ConstantsManager.getInstance().getConstants().vmRoleTree(),
                                        new RoleNode[] {
                                                new RoleNode(ConstantsManager.getInstance()
                                                        .getConstants()
                                                        .basicOperationsRoleTree(),
                                                        new RoleNode[] {
                                                                new RoleNode(ActionGroup.VM_BASIC_OPERATIONS,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowBasicVmOperationsRoleTreeTooltip()),
                                                                new RoleNode(ActionGroup.CHANGE_VM_CD,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowToAttachCdToTheVmRoleTreeTooltip()),
                                                                new RoleNode(ActionGroup.CONNECT_TO_VM,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowViewingTheVmConsoleScreenRoleTreeTooltip()) }),
                                                new RoleNode(ConstantsManager.getInstance()
                                                        .getConstants()
                                                        .provisioningOperationsRoleTree(),
                                                        ConstantsManager.getInstance()
                                                                .getConstants()
                                                                .notePermissionsContainigTheseOperationsShuoldAssociatSdOrAboveRoleTreeTooltip(),
                                                        new RoleNode[] {
                                                                new RoleNode(ActionGroup.EDIT_VM_PROPERTIES,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowChangeVmPropertiesRoleTreeTooltip()),
                                                                new RoleNode(ActionGroup.CREATE_VM,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowToCreateNewVmsRoleTreeTooltip()),
                                                                new RoleNode(ActionGroup.DELETE_VM,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowToRemoveVmsFromTheSystemRoleTreeTooltip()),
                                                                new RoleNode(ActionGroup.IMPORT_EXPORT_VM,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowImportExportOperationsRoleTreeTooltip()),
                                                                new RoleNode(ActionGroup.CONFIGURE_VM_NETWORK,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowToConfigureVMsNetworkRoleTreeTooltip()),
                                                                new RoleNode(ActionGroup.CONFIGURE_VM_STORAGE,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowToAddRemoveDiskToTheVmRoleTreeTooltip()),
                                                                new RoleNode(ActionGroup.MANIPULATE_VM_SNAPSHOTS,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowToCreateDeleteSnapshotsOfTheVmRoleTreeTooltip()) }),
                                                new RoleNode(ConstantsManager.getInstance()
                                                        .getConstants()
                                                        .administrationOperationsRoleTree(),
                                                        ConstantsManager.getInstance()
                                                                .getConstants()
                                                                .notePermissionsContainigTheseOperationsShuoldAssociatDcOrEqualRoleTreeTooltip(),
                                                        new RoleNode[] {
                                                                new RoleNode(ActionGroup.MOVE_VM,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowToMoveVmImageToAnotherStorageDomainRoleTreeTooltip()),
                                                                new RoleNode(ActionGroup.MIGRATE_VM,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowMigratingVmBetweenHostsInClusterRoleTreeTooltip()),
                                                                new RoleNode(ActionGroup.CHANGE_VM_CUSTOM_PROPERTIES,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowMigratingVmBetweenHostsInClusterRoleTreeTooltip()),
                                                                new RoleNode(ActionGroup.PORT_MIRRORING,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowVmNetworkPortMirroringRoleTreeTooltip()) }) }),
                                new RoleNode(ConstantsManager.getInstance().getConstants().vmPoolRoleTree(),
                                        new RoleNode[] {
                                                new RoleNode(ConstantsManager.getInstance()
                                                        .getConstants()
                                                        .basicOperationsRoleTree(),
                                                        new RoleNode[] { new RoleNode(ActionGroup.VM_POOL_BASIC_OPERATIONS,
                                                                ConstantsManager.getInstance()
                                                                        .getConstants()
                                                                        .allowToRunPauseStopVmFromVmPoolRoleTreeTooltip()) }),
                                                new RoleNode(ConstantsManager.getInstance()
                                                        .getConstants()
                                                        .provisioningOperationsRoleTree(),
                                                        ConstantsManager.getInstance()
                                                                .getConstants()
                                                                .notePermissionsContainigTheseOperationsShuoldAssociatSdOrAboveRoleTreeTooltip(),
                                                        new RoleNode[] {
                                                                new RoleNode(ActionGroup.CREATE_VM_POOL,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowToCreateVmPoolRoleTreeTooltip()),
                                                                new RoleNode(ActionGroup.DELETE_VM_POOL,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowToDeleteVmPoolRoleTreeTooltip()),
                                                                new RoleNode(ActionGroup.EDIT_VM_POOL_CONFIGURATION,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowToChangePropertiesOfTheVmPoolRoleTreeTooltip()) }) }),

                                new RoleNode(ConstantsManager.getInstance().getConstants().diskRoleTree(),
                                        new RoleNode[] {
                                                new RoleNode(ConstantsManager.getInstance()
                                                        .getConstants()
                                                        .provisioningOperationsRoleTree(),
                                                        ConstantsManager.getInstance()
                                                                .getConstants()
                                                                .notePermissionsContainingOperationsRoleTreeTooltip(),
                                                        new RoleNode[] {
                                                                new RoleNode(ActionGroup.CREATE_DISK,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowToCreateDiskRoleTreeTooltip()),
                                                                new RoleNode(ActionGroup.DELETE_DISK,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowToDeleteDiskRoleTreeTooltip()),
                                                                new RoleNode(ActionGroup.CONFIGURE_DISK_STORAGE,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowToMoveDiskToAnotherStorageDomainRoleTreeTooltip()),
                                                                new RoleNode(ActionGroup.ATTACH_DISK,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowToAttachDiskToVmRoleTreeTooltip()),
                                                                new RoleNode(ActionGroup.EDIT_DISK_PROPERTIES,
                                                                        ConstantsManager.getInstance()
                                                                                .getConstants()
                                                                                .allowToChangePropertiesOfTheDiskRoleTreeTooltip()) }) }) });

        // nothing to filter
        if (!ApplicationModeHelper.getUiMode().equals(ApplicationMode.AllModes)) {
            ApplicationModeHelper.filterActionGroupTreeByApplictionMode(tree);
        }
        return tree;
    }
}
