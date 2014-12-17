package org.ovirt.engine.ui.uicommonweb.models.configure.roles_ui;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.common.SelectionTreeNodeModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class RoleTreeView
{
    public static ArrayList<SelectionTreeNodeModel> getRoleTreeView(boolean isReadOnly, boolean isAdmin)
    {
        RoleNode tree = initTreeView();
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
                    if (isAdmin || ActionGroup.valueOf(thirdNode.getTitle()).getRoleType() == RoleType.USER) {
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

    private static RoleNode initTreeView()
    {
        RoleNode tree =
                new RoleNode(getConstants().rootRoleTree(),
                        new RoleNode[] {
                                createSystemRoleTree(),
                                createDataCenterRoleTree(),
                                createNetworkRoleTree(),
                                createStorageDomainRoleTree(),
                                createClusterRoleTree(),
                                createGlusterRoleTree(),
                                createHostRoleTree(),
                                createTemplateRoleTree(),
                                createVmRoleTree(),
                                createVmPoolRoleTree(),
                                createDiskRoleTree() });

        // nothing to filter
        if (!ApplicationModeHelper.getUiMode().equals(ApplicationMode.AllModes)) {
            ApplicationModeHelper.filterActionGroupTreeByApplictionMode(tree);
        }
        return tree;
    }

    protected static RoleNode createDiskRoleTree() {
        return new RoleNode(getConstants().diskRoleTree(),
                new RoleNode[] {
                        new RoleNode(getConstants().provisioningOperationsRoleTree(),
                                getConstants().notePermissionsContainingOperationsRoleTreeTooltip(),
                                new RoleNode[] {
                                        new RoleNode(ActionGroup.CREATE_DISK,
                                                getConstants().allowToCreateDiskRoleTreeTooltip()),
                                        new RoleNode(ActionGroup.DELETE_DISK,
                                                getConstants().allowToDeleteDiskRoleTreeTooltip()),
                                        new RoleNode(ActionGroup.CONFIGURE_DISK_STORAGE,
                                                getConstants().allowToMoveDiskToAnotherStorageDomainRoleTreeTooltip()),
                                        new RoleNode(ActionGroup.ATTACH_DISK,
                                                getConstants().allowToAttachDiskToVmRoleTreeTooltip()),
                                        new RoleNode(ActionGroup.EDIT_DISK_PROPERTIES,
                                                getConstants().allowToChangePropertiesOfTheDiskRoleTreeTooltip()),
                                        new RoleNode(ActionGroup.CONFIGURE_SCSI_GENERIC_IO,
                                                getConstants().allowToChangeSGIORoleTreeTooltip()),
                                        new RoleNode(ActionGroup.ACCESS_IMAGE_STORAGE,
                                                getConstants().allowAccessImageDomainRoleTreeTooltip()) }),
                        new RoleNode(getConstants().attachDiskProfileRoleTree(),
                                getConstants().notePermissionsContainingDiskProfileOperationsRoleTreeTooltip(),
                                new RoleNode[] {
                                        new RoleNode(ActionGroup.ATTACH_DISK_PROFILE,
                                                getConstants().allowToAttachDiskProfileToDiskRoleTreeTooltip()) }) });
    }

    protected static RoleNode createVmPoolRoleTree() {
        return new RoleNode(getConstants().vmPoolRoleTree(), new RoleNode[] {
                new RoleNode(getConstants().basicOperationsRoleTree(),
                        new RoleNode[] { new RoleNode(ActionGroup.VM_POOL_BASIC_OPERATIONS,
                                getConstants().allowToRunPauseStopVmFromVmPoolRoleTreeTooltip()) }),
                new RoleNode(getConstants().provisioningOperationsRoleTree(),
                        getConstants().notePermissionsContainigTheseOperationsShuoldAssociatSdOrAboveRoleTreeTooltip(),
                        new RoleNode[] {
                                new RoleNode(ActionGroup.CREATE_VM_POOL,
                                        getConstants().allowToCreateVmPoolRoleTreeTooltip()),
                                new RoleNode(ActionGroup.DELETE_VM_POOL,
                                        getConstants().allowToDeleteVmPoolRoleTreeTooltip()),
                                new RoleNode(ActionGroup.EDIT_VM_POOL_CONFIGURATION,
                                        getConstants().allowToChangePropertiesOfTheVmPoolRoleTreeTooltip()) }) });
    }

    protected static RoleNode createVmRoleTree() {
        return new RoleNode(getConstants().vmRoleTree(),
                new RoleNode[] {
                        new RoleNode(getConstants().basicOperationsRoleTree(), new RoleNode[] {
                                new RoleNode(ActionGroup.VM_BASIC_OPERATIONS,
                                        getConstants().allowBasicVmOperationsRoleTreeTooltip()),
                                new RoleNode(ActionGroup.CHANGE_VM_CD,
                                        getConstants().allowToAttachCdToTheVmRoleTreeTooltip()),
                                new RoleNode(ActionGroup.CONNECT_TO_VM,
                                        getConstants().allowViewingTheVmConsoleScreenRoleTreeTooltip()) }),
                        new RoleNode(getConstants().provisioningOperationsRoleTree(),
                                getConstants().notePermissionsContainigTheseOperationsShuoldAssociatSdOrAboveRoleTreeTooltip(),
                                new RoleNode[] {
                                        new RoleNode(ActionGroup.EDIT_VM_PROPERTIES,
                                                getConstants().allowChangeVmPropertiesRoleTreeTooltip()),
                                        new RoleNode(ActionGroup.CREATE_VM,
                                                getConstants().allowToCreateNewVmsRoleTreeTooltip()),
                                        new RoleNode(ActionGroup.CREATE_INSTANCE,
                                                getConstants().allowToCreateNewInstnaceRoleTreeTooltip()),
                                        new RoleNode(ActionGroup.DELETE_VM,
                                                getConstants().allowToRemoveVmsFromTheSystemRoleTreeTooltip()),
                                        new RoleNode(ActionGroup.IMPORT_EXPORT_VM,
                                                getConstants().allowImportExportOperationsRoleTreeTooltip()),
                                        new RoleNode(ActionGroup.CONFIGURE_VM_STORAGE,
                                                getConstants().allowToAddRemoveDiskToTheVmRoleTreeTooltip()),
                                        new RoleNode(ActionGroup.MANIPULATE_VM_SNAPSHOTS,
                                                getConstants().allowToCreateDeleteSnapshotsOfTheVmRoleTreeTooltip()) }),
                        new RoleNode(getConstants().administrationOperationsRoleTree(),
                                getConstants().notePermissionsContainigTheseOperationsShuoldAssociatDcOrEqualRoleTreeTooltip(),
                                new RoleNode[] {
                                        new RoleNode(ActionGroup.MOVE_VM,
                                                getConstants().allowToMoveVmImageToAnotherStorageDomainRoleTreeTooltip()),
                                        new RoleNode(ActionGroup.MIGRATE_VM,
                                                getConstants().allowMigratingVmBetweenHostsInClusterRoleTreeTooltip()),
                                        new RoleNode(ActionGroup.CHANGE_VM_CUSTOM_PROPERTIES,
                                                getConstants().allowToChangeVmCustomPropertiesRoleTreeTooltip()),
                                        new RoleNode(ActionGroup.EDIT_ADMIN_VM_PROPERTIES,
                                                getConstants().allowChangingVmAdminPropertiesRoleTreeTooltip()),
                                        new RoleNode(ActionGroup.RECONNECT_TO_VM,
                                                getConstants().allowReconnectToVmRoleTreeTooltip()) }) });
    }

    protected static RoleNode createTemplateRoleTree() {
        return new RoleNode(getConstants().templateRoleTree(), new RoleNode[] {
                new RoleNode(getConstants().basicOperationsRoleTree(),
                        new RoleNode[] { new RoleNode(ActionGroup.EDIT_TEMPLATE_PROPERTIES,
                                getConstants().allowToChangeTemplatePropertiesRoleTreeTooltip()) }),
                new RoleNode(getConstants().provisioningOperationsRoleTree(),
                        getConstants().notePermissionsContainigTheseOperationsShuoldAssociatSdOrAboveRoleTreeTooltip(),
                        new RoleNode[] {
                                new RoleNode(ActionGroup.CREATE_TEMPLATE,
                                        getConstants().allowToCreateNewTemplateRoleTreeTooltip()),
                                new RoleNode(ActionGroup.DELETE_TEMPLATE,
                                        getConstants().allowToRemoveExistingTemplateRoleTreeTooltip()),
                                new RoleNode(ActionGroup.IMPORT_EXPORT_VM,
                                        getConstants().allowImportExportOperationsRoleTreeTooltip()),
                                new RoleNode(ActionGroup.COPY_TEMPLATE,
                                        getConstants().allowToCopyTemplateBetweenStorageDomainsRoleTreeTooltip()) }),

                new RoleNode(getConstants().administrationOperationsRoleTree(),
                        getConstants().notePermissionsContainigTheseOperationsShuoldAssociatDcOrEqualRoleTreeTooltip(),
                        new RoleNode[] { new RoleNode(ActionGroup.EDIT_ADMIN_TEMPLATE_PROPERTIES,
                                getConstants().allowChangingTemplateAdminPropertiesRoleTreeTooltip()) }) });
    }

    private static UIConstants getConstants() {
        return ConstantsManager.getInstance().getConstants();
    }

    protected static RoleNode createHostRoleTree() {
        return new RoleNode(getConstants().hostRoleTree(), new RoleNode(getConstants().configureHostRoleTree(),
                new RoleNode[] {
                        new RoleNode(ActionGroup.CREATE_HOST,
                                getConstants().allowToAddNewHostToTheClusterRoleTreeTooltip()),
                        new RoleNode(ActionGroup.DELETE_HOST,
                                getConstants().allowToRemoveExistingHostFromTheClusterRoleTreeTooltip()),
                        new RoleNode(ActionGroup.EDIT_HOST_CONFIGURATION,
                                getConstants().allowToEditHostPropertiesRoleTreeTooltip()),
                        new RoleNode(ActionGroup.MANIPULATE_HOST,
                                getConstants().allowToChangeHostStatusRoleTreeTooltip()),
                        new RoleNode(ActionGroup.CONFIGURE_HOST_NETWORK,
                                getConstants().allowToConfigureHostsNetworkPhysicalInterfacesRoleTreeTooltip()) }));
    }

    protected static RoleNode createGlusterRoleTree() {
        return new RoleNode(getConstants().volumeRoleTree(), new RoleNode(getConstants().configureVolumesRoleTree(),
                new RoleNode[] {
                        new RoleNode(ActionGroup.CREATE_GLUSTER_VOLUME,
                                getConstants().allowToCreateGlusterVolumesRoleTree()),
                        new RoleNode(ActionGroup.DELETE_GLUSTER_VOLUME,
                                getConstants().allowToDeleteGlusterVolumesRoleTree()),
                        new RoleNode(ActionGroup.MANIPULATE_GLUSTER_VOLUME,
                                getConstants().allowToManipulateGlusterVolumesRoleTree()) }));
    }

    protected static RoleNode createClusterRoleTree() {
        return new RoleNode(getConstants().clusterRoleTree(), new RoleNode(getConstants().configureClusterRoleTree(),
                new RoleNode[] {
                        new RoleNode(ActionGroup.CREATE_CLUSTER,
                                getConstants().allowToCreateNewClusterRoleTreeTooltip()),
                        new RoleNode(ActionGroup.DELETE_CLUSTER, getConstants().allowToRemoveClusterRoleTreeTooltip()),
                        new RoleNode(ActionGroup.EDIT_CLUSTER_CONFIGURATION,
                                getConstants().allowToEditClusterPropertiesRoleTreeTooltip()),
                        new RoleNode(ActionGroup.CONFIGURE_CLUSTER_NETWORK,
                                getConstants().allowToEditLogicalNetworksForTheClusterRoleTreeTooltip()),
                        new RoleNode(ActionGroup.MANIPULATE_AFFINITY_GROUPS,
                                getConstants().allowToManipulateAffinityGroupsForClusterRoleTreeTooltip()) }));
    }

    protected static RoleNode createStorageDomainRoleTree() {
        return new RoleNode(getConstants().storageDomainRoleTree(), new RoleNode[] {
                new RoleNode(getConstants().configureStorageDomainRoleTree(), new RoleNode[] {
                        new RoleNode(ActionGroup.CREATE_STORAGE_DOMAIN,
                                getConstants().allowToCreateStorageDomainRoleTreeTooltip()),
                        new RoleNode(ActionGroup.DELETE_STORAGE_DOMAIN,
                                getConstants().allowToDeleteStorageDomainRoleTreeTooltip()),
                        new RoleNode(ActionGroup.EDIT_STORAGE_DOMAIN_CONFIGURATION,
                                getConstants().allowToModifyStorageDomainPropertiesRoleTreeTooltip()),
                        new RoleNode(ActionGroup.MANIPULATE_STORAGE_DOMAIN,
                                getConstants().allowToChangeStorageDomainStatusRoleTreeTooltip()) }),
                new RoleNode(getConstants().configureDiskProfileRoleTree(), new RoleNode[] {
                        new RoleNode(ActionGroup.CREATE_STORAGE_DISK_PROFILE,
                                getConstants().allowToCreateDiskProfileRoleTreeTooltip()),
                        new RoleNode(ActionGroup.DELETE_STORAGE_DISK_PROFILE,
                                getConstants().allowToDeleteDiskProfileRoleTreeTooltip()),
                        new RoleNode(ActionGroup.CONFIGURE_STORAGE_DISK_PROFILE,
                                getConstants().allowToUpdateDiskProfileRoleTreeTooltip()) }) });
    }

    protected static RoleNode createNetworkRoleTree() {
        return new RoleNode(getConstants().networkRoleTree(),
                new RoleNode[] {
                        new RoleNode(getConstants().configureNetworkRoleTree(), new RoleNode[] {
                                new RoleNode(ActionGroup.CREATE_STORAGE_POOL_NETWORK,
                                        getConstants().allowToCreateLogicalNetworkPerDataCenterRoleTreeTooltip()),
                                new RoleNode(ActionGroup.CONFIGURE_STORAGE_POOL_NETWORK,
                                        getConstants().allowToEditLogicalNetworkRoleTreeTooltip()),
                                new RoleNode(ActionGroup.DELETE_STORAGE_POOL_NETWORK,
                                        getConstants().allowToDeleteLogicalNetworkRoleTreeTooltip()),
                                new RoleNode(ActionGroup.ASSIGN_CLUSTER_NETWORK,
                                        getConstants().allowToAddRemoveLogicalNetworksForTheClusterRoleTreeTooltip()) }),
                        new RoleNode(getConstants().configureVnicProfileRoleTree(), new RoleNode[] {
                                new RoleNode(ActionGroup.CREATE_NETWORK_VNIC_PROFILE,
                                        getConstants().allowToCreateVnicProfileRoleTreeTooltip()),
                                new RoleNode(ActionGroup.CONFIGURE_NETWORK_VNIC_PROFILE,
                                        getConstants().allowToEditVnicProfileRoleTreeTooltip()),
                                new RoleNode(ActionGroup.DELETE_NETWORK_VNIC_PROFILE,
                                        getConstants().allowToDeleteVnicProfileRoleTreeTooltip()),
                                new RoleNode(ActionGroup.CONFIGURE_VM_NETWORK,
                                        getConstants().allowToConfigureVMsNetworkRoleTreeTooltip()),
                                new RoleNode(ActionGroup.CONFIGURE_TEMPLATE_NETWORK,
                                        getConstants().allowToConfigureTemlateNetworkRoleTreeTooltip()) })
                });
    }

    protected static RoleNode createDataCenterRoleTree() {
        return new RoleNode(getConstants().dataCenterRoleTree(),
                new RoleNode(getConstants().configureDataCenterRoleTree(), new RoleNode[] {
                        new RoleNode(ActionGroup.CREATE_STORAGE_POOL,
                                getConstants().allowToCreateDataCenterRoleTreeTooltip()),
                        new RoleNode(ActionGroup.DELETE_STORAGE_POOL,
                                getConstants().allowToRemoveDataCenterRoleTreeTooltip()),
                        new RoleNode(ActionGroup.EDIT_STORAGE_POOL_CONFIGURATION,
                                getConstants().allowToModifyDataCenterPropertiesRoleTreeTooltip()) }));
    }

    protected static RoleNode createSystemRoleTree() {
        return new RoleNode(getConstants().systemRoleTree(),
                new RoleNode(getConstants().configureSystemRoleTree(),
                        new RoleNode[] {
                                new RoleNode(ActionGroup.MANIPULATE_USERS,
                                        getConstants().allowToAddRemoveUsersFromTheSystemRoleTreeTooltip()),
                                new RoleNode(ActionGroup.MANIPULATE_PERMISSIONS,
                                        getConstants().allowToAddRemovePermissionsForUsersOnObjectsInTheSystemRoleTreeTooltip()),
                                new RoleNode(ActionGroup.ADD_USERS_AND_GROUPS_FROM_DIRECTORY,
                                        getConstants().allowToAddUsersAndGroupsFromDirectoryOnObjectsInTheSystemRoleTreeTooltip()),
                                new RoleNode(ActionGroup.MANIPULATE_ROLES,
                                        getConstants().allowToDefineConfigureRolesInTheSystemRoleTreeTooltip()),
                                new RoleNode(ActionGroup.LOGIN, getConstants().allowToLoginToTheSystemRoleTreeTooltip()),
                                new RoleNode(ActionGroup.TAG_MANAGEMENT, getConstants().allowToManageTags()),
                                new RoleNode(ActionGroup.BOOKMARK_MANAGEMENT, getConstants().allowToManageBookmarks()),
                                new RoleNode(ActionGroup.EVENT_NOTIFICATION_MANAGEMENT, getConstants().allowToManageEventNotifications()),
                                new RoleNode(ActionGroup.AUDIT_LOG_MANAGEMENT, getConstants().allowToManageAuditLogs()),
                                new RoleNode(ActionGroup.CONFIGURE_ENGINE,
                                        getConstants().allowToGetOrSetSystemConfigurationRoleTreeTooltip()) }));
    }
}
