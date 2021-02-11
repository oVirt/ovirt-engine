package org.ovirt.engine.ui.uicommonweb.models.configure.roles_ui;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.common.SelectionTreeNodeModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class RoleTreeView {
    public static ArrayList<SelectionTreeNodeModel> getRoleTreeView(boolean isReadOnly, boolean isAdmin) {
        RoleNode tree = initTreeView();
        ArrayList<SelectionTreeNodeModel> roleTreeView = new ArrayList<>();
        SelectionTreeNodeModel firstNode;
        SelectionTreeNodeModel secondNode;
        SelectionTreeNodeModel thirdNode;

        for (RoleNode first : tree.getLeafRoles()) {
            firstNode = createSelectionTreeNodeModel(isReadOnly, first);

            for (RoleNode second : first.getLeafRoles()) {
                secondNode = createSelectionTreeNodeModel(isReadOnly, second);
                secondNode.setTooltip(second.getTooltip());

                for (RoleNode third : second.getLeafRoles()) {
                    thirdNode = createLeafSelectionTreeNodeModel(isReadOnly, third);

                    if (isAdmin || isUser(thirdNode)) {
                        secondNode.getChildren().add(thirdNode);
                    }
                }
                if (secondNode.getChildren().size() > 0) {
                    firstNode.getChildren().add(secondNode);
                }
            }
            if (firstNode.getChildren().size() > 0) {
                roleTreeView.add(firstNode);
            }
        }

        return roleTreeView;
    }

    protected static SelectionTreeNodeModel createLeafSelectionTreeNodeModel(boolean isReadOnly, RoleNode third) {
        SelectionTreeNodeModel thirdNode;
        thirdNode = createSelectionTreeNodeModel(isReadOnly, third);
        thirdNode.setIsSelectedNotificationPrevent(true);
        thirdNode.setIsSelectedNullable(false);
        thirdNode.setTooltip(third.getTooltip());

        return thirdNode;
    }

    protected static boolean isUser(SelectionTreeNodeModel thirdNode) {
        return ActionGroup.valueOf(thirdNode.getTitle()).getRoleType() == RoleType.USER;
    }

    protected static SelectionTreeNodeModel createSelectionTreeNodeModel(boolean isReadOnly, RoleNode roleNode) {
        SelectionTreeNodeModel nodeModel;
        nodeModel = new SelectionTreeNodeModel();
        nodeModel.setTitle(roleNode.getName());
        nodeModel.setDescription(roleNode.getDesc());
        nodeModel.setIsChangeable(!isReadOnly);
        return nodeModel;
    }

    private static RoleNode categoryNode(String name, RoleNode... leaves) {
        return new RoleNode(name, leaves);
    }

    private static RoleNode categoryNode(String name, String tooltip, RoleNode... leaves) {
        return new RoleNode(name, tooltip, leaves);
    }

    private static RoleNode roleNode(ActionGroup actionGroup, String tooltip) {
        return new RoleNode(actionGroup, tooltip);
    }

    private static RoleNode initTreeView() {
        RoleNode tree = categoryNode(getConstants().rootRoleTree(),
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
                createDiskRoleTree(),
                createCpuRoleTree(),
                createMacPoolRoleTree());

        // nothing to filter
        if (!ApplicationModeHelper.getUiMode().equals(ApplicationMode.AllModes)) {
            ApplicationModeHelper.filterActionGroupTreeByApplictionMode(tree);
        }
        return tree;
    }

    protected static RoleNode createMacPoolRoleTree() {
        return categoryNode(getConstants().macPoolTree(),
                categoryNode(getConstants().basicOperationsRoleTree(),
                        roleNode(ActionGroup.CREATE_MAC_POOL, getConstants().allowToCreateMacPoolTooltip()),
                        roleNode(ActionGroup.EDIT_MAC_POOL, getConstants().allowToEditMacPoolTooltip()),
                        roleNode(ActionGroup.DELETE_MAC_POOL, getConstants().allowToDeleteMacPoolTooltip()),
                        roleNode(ActionGroup.CONFIGURE_MAC_POOL, getConstants().allowToUseMacPoolTooltip())
                )
        );
    }

    protected static RoleNode createDiskRoleTree() {
        return categoryNode(getConstants().diskRoleTree(),
                        categoryNode(getConstants().provisioningOperationsRoleTree(),
                                getConstants().notePermissionsContainingDiskOperationsRoleTreeTooltip(),

                                roleNode(ActionGroup.CREATE_DISK, getConstants().allowToCreateDiskRoleTreeTooltip()),
                                roleNode(ActionGroup.DELETE_DISK, getConstants().allowToDeleteDiskRoleTreeTooltip()),
                                roleNode(ActionGroup.CONFIGURE_DISK_STORAGE, getConstants().allowToMoveDiskToAnotherStorageDomainRoleTreeTooltip()),
                                roleNode(ActionGroup.ATTACH_DISK, getConstants().allowToAttachDiskToVmRoleTreeTooltip()),
                                roleNode(ActionGroup.SPARSIFY_DISK, getConstants().allowToSparsifyDiskToVmRoleTreeTooltip()),
                                roleNode(ActionGroup.EDIT_DISK_PROPERTIES, getConstants().allowToChangePropertiesOfTheDiskRoleTreeTooltip()),
                                roleNode(ActionGroup.CONFIGURE_SCSI_GENERIC_IO, getConstants().allowToChangeSGIORoleTreeTooltip()),
                                roleNode(ActionGroup.ACCESS_IMAGE_STORAGE, getConstants().allowAccessImageDomainRoleTreeTooltip()),
                                roleNode(ActionGroup.DISK_LIVE_STORAGE_MIGRATION, getConstants().allowToLiveMigrateDiskToAnotherStorageDomainRoleTreeTooltip()),
                                roleNode(ActionGroup.BACKUP_DISK, getConstants().allowToBackupDiskRoleTreeTooltip())),
                        categoryNode(getConstants().attachDiskProfileRoleTree(),
                                getConstants().notePermissionsContainingDiskProfileOperationsRoleTreeTooltip(),
                                roleNode(ActionGroup.ATTACH_DISK_PROFILE, getConstants().allowToAttachDiskProfileToDiskRoleTreeTooltip()) ) );
    }

    protected static RoleNode createCpuRoleTree() {
        return categoryNode(getConstants().cpuProfileRoleTree(),
                categoryNode(getConstants().provisioningOperationsRoleTree(),
                        getConstants().notePermissionsContainingCpuProfileProvisioningOperationsRoleTreeTooltip(),
                        roleNode(ActionGroup.CREATE_CPU_PROFILE, getConstants().allowToCreateCpuRoleTreeTooltip()),
                        roleNode(ActionGroup.DELETE_CPU_PROFILE, getConstants().allowToDeleteCpuRoleTreeTooltip()),
                        roleNode(ActionGroup.UPDATE_CPU_PROFILE, getConstants().allowToUpdateCpuProfileRoleTreeTooltip())),
                categoryNode(getConstants().administrationOperationsRoleTree(),
                        getConstants().notePermissionsContainingCpuProfileAdministrationOperationsRoleTreeTooltip(),
                        roleNode(ActionGroup.ASSIGN_CPU_PROFILE, getConstants().allowToAssignCpuRoleTreeToolTip())));
    }

    protected static RoleNode createVmPoolRoleTree() {
        return categoryNode(getConstants().vmPoolRoleTree(),
                categoryNode(getConstants().basicOperationsRoleTree(),
                        roleNode(ActionGroup.VM_POOL_BASIC_OPERATIONS, getConstants().allowToRunPauseStopVmFromVmPoolRoleTreeTooltip())
                ),
                categoryNode(getConstants().provisioningOperationsRoleTree(),
                        getConstants().notePermissionsContainigTheseOperationsShuoldAssociatSdOrAboveRoleTreeTooltip(),

                        roleNode(ActionGroup.CREATE_VM_POOL, getConstants().allowToCreateVmPoolRoleTreeTooltip()),
                        roleNode(ActionGroup.DELETE_VM_POOL, getConstants().allowToDeleteVmPoolRoleTreeTooltip()),
                        roleNode(ActionGroup.EDIT_VM_POOL_CONFIGURATION, getConstants().allowToChangePropertiesOfTheVmPoolRoleTreeTooltip()))
        );
    }

    protected static RoleNode createVmRoleTree() {
        return categoryNode(getConstants().vmRoleTree(),

                categoryNode(getConstants().basicOperationsRoleTree(),
                        roleNode(ActionGroup.REBOOT_VM, getConstants().allowBasicVmOperationsRoleTreeTooltip()),
                        roleNode(ActionGroup.RESET_VM, getConstants().allowBasicVmOperationsRoleTreeTooltip()),
                        roleNode(ActionGroup.STOP_VM, getConstants().allowBasicVmOperationsRoleTreeTooltip()),
                        roleNode(ActionGroup.SHUT_DOWN_VM, getConstants().allowBasicVmOperationsRoleTreeTooltip()),
                        roleNode(ActionGroup.HIBERNATE_VM, getConstants().allowBasicVmOperationsRoleTreeTooltip()),
                        roleNode(ActionGroup.RUN_VM, getConstants().allowBasicVmOperationsRoleTreeTooltip()),
                        roleNode(ActionGroup.CHANGE_VM_CD, getConstants().allowToAttachCdToTheVmRoleTreeTooltip()),
                        roleNode(ActionGroup.CONNECT_TO_VM, getConstants().allowViewingTheVmConsoleScreenRoleTreeTooltip())),
                categoryNode(getConstants().provisioningOperationsRoleTree(),
                        getConstants().notePermissionsContainigTheseOperationsShuoldAssociatSdOrAboveRoleTreeTooltip(),

                        roleNode(ActionGroup.EDIT_VM_PROPERTIES, getConstants().allowChangeVmPropertiesRoleTreeTooltip()),
                        roleNode(ActionGroup.CREATE_VM, getConstants().allowToCreateNewVmsRoleTreeTooltip()),
                        roleNode(ActionGroup.CREATE_INSTANCE, getConstants().allowToCreateNewInstnaceRoleTreeTooltip()),
                        roleNode(ActionGroup.DELETE_VM, getConstants().allowToRemoveVmsFromTheSystemRoleTreeTooltip()),
                        roleNode(ActionGroup.IMPORT_EXPORT_VM, getConstants().allowImportExportOperationsRoleTreeTooltip()),
                        roleNode(ActionGroup.CONFIGURE_VM_STORAGE, getConstants().allowToAddRemoveDiskToTheVmRoleTreeTooltip()),
                        roleNode(ActionGroup.MANIPULATE_VM_SNAPSHOTS, getConstants().allowToCreateDeleteSnapshotsOfTheVmRoleTreeTooltip())),
                categoryNode(getConstants().administrationOperationsRoleTree(),
                        getConstants().notePermissionsContainigTheseOperationsShuoldAssociatDcOrEqualRoleTreeTooltip(),

                        roleNode(ActionGroup.MOVE_VM, getConstants().allowToMoveVmImageToAnotherStorageDomainRoleTreeTooltip()),
                        roleNode(ActionGroup.MIGRATE_VM, getConstants().allowMigratingVmBetweenHostsInClusterRoleTreeTooltip()),
                        roleNode(ActionGroup.CHANGE_VM_CUSTOM_PROPERTIES, getConstants().allowToChangeVmCustomPropertiesRoleTreeTooltip()),
                        roleNode(ActionGroup.EDIT_ADMIN_VM_PROPERTIES, getConstants().allowChangingVmAdminPropertiesRoleTreeTooltip()),
                        roleNode(ActionGroup.CONNECT_TO_SERIAL_CONSOLE, getConstants().allowConnectingToVmSerialConsoleRoleTreeTooltip()),
                        roleNode(ActionGroup.RECONNECT_TO_VM, getConstants().allowReconnectToVmRoleTreeTooltip())));
    }

    protected static RoleNode createTemplateRoleTree() {
        return categoryNode(getConstants().templateRoleTree(),
                categoryNode(getConstants().basicOperationsRoleTree(),
                        roleNode(ActionGroup.EDIT_TEMPLATE_PROPERTIES, getConstants().allowToChangeTemplatePropertiesRoleTreeTooltip())
                ),
                categoryNode(getConstants().provisioningOperationsRoleTree(),
                        getConstants().notePermissionsContainigTheseOperationsShuoldAssociatSdOrAboveRoleTreeTooltip(),

                        roleNode(ActionGroup.CREATE_TEMPLATE, getConstants().allowToCreateNewTemplateRoleTreeTooltip()),
                        roleNode(ActionGroup.DELETE_TEMPLATE, getConstants().allowToRemoveExistingTemplateRoleTreeTooltip()),
                        roleNode(ActionGroup.IMPORT_EXPORT_VM, getConstants().allowImportExportOperationsRoleTreeTooltip()),
                        roleNode(ActionGroup.COPY_TEMPLATE, getConstants().allowToCopyTemplateBetweenStorageDomainsRoleTreeTooltip())),

                categoryNode(getConstants().administrationOperationsRoleTree(),
                        getConstants().notePermissionsContainigTheseOperationsShuoldAssociatDcOrEqualRoleTreeTooltip(),

                        roleNode(ActionGroup.EDIT_ADMIN_TEMPLATE_PROPERTIES, getConstants().allowChangingTemplateAdminPropertiesRoleTreeTooltip())
                )
        );
    }

    private static UIConstants getConstants() {
        return ConstantsManager.getInstance().getConstants();
    }

    protected static RoleNode createHostRoleTree() {
        return categoryNode(getConstants().hostRoleTree(),
                categoryNode(getConstants().configureHostRoleTree(),
                        roleNode(ActionGroup.CREATE_HOST, getConstants().allowToAddNewHostToTheClusterRoleTreeTooltip()),
                        roleNode(ActionGroup.DELETE_HOST, getConstants().allowToRemoveExistingHostFromTheClusterRoleTreeTooltip()),
                        roleNode(ActionGroup.EDIT_HOST_CONFIGURATION, getConstants().allowToEditHostPropertiesRoleTreeTooltip()),
                        roleNode(ActionGroup.MANIPULATE_HOST, getConstants().allowToChangeHostStatusRoleTreeTooltip()),
                        roleNode(ActionGroup.CONFIGURE_HOST_NETWORK, getConstants().allowToConfigureHostsNetworkPhysicalInterfacesRoleTreeTooltip())
                )
        );
    }

    protected static RoleNode createGlusterRoleTree() {
        return categoryNode(getConstants().volumeRoleTree(),
                categoryNode(getConstants().configureVolumesRoleTree(),
                        roleNode(ActionGroup.CREATE_GLUSTER_VOLUME, getConstants().allowToCreateGlusterVolumesRoleTree()),
                        roleNode(ActionGroup.DELETE_GLUSTER_VOLUME, getConstants().allowToDeleteGlusterVolumesRoleTree()),
                        roleNode(ActionGroup.MANIPULATE_GLUSTER_VOLUME, getConstants().allowToManipulateGlusterVolumesRoleTree())
                )
        );
    }

    protected static RoleNode createClusterRoleTree() {
        return categoryNode(getConstants().clusterRoleTree(),
                categoryNode(getConstants().configureClusterRoleTree(),
                        roleNode(ActionGroup.CREATE_CLUSTER, getConstants().allowToCreateNewClusterRoleTreeTooltip()),
                        roleNode(ActionGroup.DELETE_CLUSTER, getConstants().allowToRemoveClusterRoleTreeTooltip()),
                        roleNode(ActionGroup.EDIT_CLUSTER_CONFIGURATION, getConstants().allowToEditClusterPropertiesRoleTreeTooltip()),
                        roleNode(ActionGroup.CONFIGURE_CLUSTER_NETWORK, getConstants().allowToEditLogicalNetworksForTheClusterRoleTreeTooltip()),
                        roleNode(ActionGroup.MANIPULATE_AFFINITY_GROUPS, getConstants().allowToManipulateAffinityGroupsForClusterRoleTreeTooltip())
                )
        );
    }

    protected static RoleNode createStorageDomainRoleTree() {
        return categoryNode(getConstants().storageDomainRoleTree(),
                categoryNode(getConstants().configureStorageDomainRoleTree(),
                        roleNode(ActionGroup.CREATE_STORAGE_DOMAIN, getConstants().allowToCreateStorageDomainRoleTreeTooltip()),
                        roleNode(ActionGroup.DELETE_STORAGE_DOMAIN, getConstants().allowToDeleteStorageDomainRoleTreeTooltip()),
                        roleNode(ActionGroup.EDIT_STORAGE_DOMAIN_CONFIGURATION, getConstants().allowToModifyStorageDomainPropertiesRoleTreeTooltip()),
                        roleNode(ActionGroup.MANIPULATE_STORAGE_DOMAIN, getConstants().allowToChangeStorageDomainStatusRoleTreeTooltip())),
                categoryNode(getConstants().configureDiskProfileRoleTree(),
                        roleNode(ActionGroup.CREATE_STORAGE_DISK_PROFILE, getConstants().allowToCreateDiskProfileRoleTreeTooltip()),
                        roleNode(ActionGroup.DELETE_STORAGE_DISK_PROFILE, getConstants().allowToDeleteDiskProfileRoleTreeTooltip()),
                        roleNode(ActionGroup.CONFIGURE_STORAGE_DISK_PROFILE, getConstants().allowToUpdateDiskProfileRoleTreeTooltip())));
    }

    protected static RoleNode createNetworkRoleTree() {
        return categoryNode(getConstants().networkRoleTree(),
                categoryNode(getConstants().configureNetworkRoleTree(),
                        roleNode(ActionGroup.CREATE_STORAGE_POOL_NETWORK, getConstants().allowToCreateLogicalNetworkPerDataCenterRoleTreeTooltip()),
                        roleNode(ActionGroup.CONFIGURE_STORAGE_POOL_NETWORK, getConstants().allowToEditLogicalNetworkRoleTreeTooltip()),
                        roleNode(ActionGroup.DELETE_STORAGE_POOL_NETWORK, getConstants().allowToDeleteLogicalNetworkRoleTreeTooltip()),
                        roleNode(ActionGroup.ASSIGN_CLUSTER_NETWORK, getConstants().allowToAddRemoveLogicalNetworksForTheClusterRoleTreeTooltip())),
                categoryNode(getConstants().configureVnicProfileRoleTree(),
                        roleNode(ActionGroup.CREATE_NETWORK_VNIC_PROFILE, getConstants().allowToCreateVnicProfileRoleTreeTooltip()),
                        roleNode(ActionGroup.CONFIGURE_NETWORK_VNIC_PROFILE, getConstants().allowToEditVnicProfileRoleTreeTooltip()),
                        roleNode(ActionGroup.DELETE_NETWORK_VNIC_PROFILE, getConstants().allowToDeleteVnicProfileRoleTreeTooltip()),
                        roleNode(ActionGroup.CONFIGURE_VM_NETWORK, getConstants().allowToConfigureVMsNetworkRoleTreeTooltip()),
                        roleNode(ActionGroup.CONFIGURE_TEMPLATE_NETWORK, getConstants().allowToConfigureTemlateNetworkRoleTreeTooltip()))
        );
    }

    protected static RoleNode createDataCenterRoleTree() {
        return categoryNode(getConstants().dataCenterRoleTree(),
                categoryNode(getConstants().configureDataCenterRoleTree(),
                        roleNode(ActionGroup.CREATE_STORAGE_POOL, getConstants().allowToCreateDataCenterRoleTreeTooltip()),
                        roleNode(ActionGroup.DELETE_STORAGE_POOL, getConstants().allowToRemoveDataCenterRoleTreeTooltip()),
                        roleNode(ActionGroup.EDIT_STORAGE_POOL_CONFIGURATION, getConstants().allowToModifyDataCenterPropertiesRoleTreeTooltip())
                )
        );
    }

    protected static RoleNode createSystemRoleTree() {
        return categoryNode(getConstants().systemRoleTree(),
                categoryNode(getConstants().configureSystemRoleTree(),
                        roleNode(ActionGroup.MANIPULATE_USERS, getConstants().allowToAddRemoveUsersFromTheSystemRoleTreeTooltip()),
                        roleNode(ActionGroup.MANIPULATE_PERMISSIONS, getConstants().allowToAddRemovePermissionsForUsersOnObjectsInTheSystemRoleTreeTooltip()),
                        roleNode(ActionGroup.ADD_USERS_AND_GROUPS_FROM_DIRECTORY, getConstants().allowToAddUsersAndGroupsFromDirectoryOnObjectsInTheSystemRoleTreeTooltip()),
                        roleNode(ActionGroup.MANIPULATE_ROLES, getConstants().allowToDefineConfigureRolesInTheSystemRoleTreeTooltip()),
                        roleNode(ActionGroup.LOGIN, getConstants().allowToLoginToTheSystemRoleTreeTooltip()),
                        roleNode(ActionGroup.TAG_MANAGEMENT, getConstants().allowToManageTags()),
                        roleNode(ActionGroup.BOOKMARK_MANAGEMENT, getConstants().allowToManageBookmarks()),
                        roleNode(ActionGroup.EVENT_NOTIFICATION_MANAGEMENT, getConstants().allowToManageEventNotifications()),
                        roleNode(ActionGroup.AUDIT_LOG_MANAGEMENT, getConstants().allowToManageAuditLogs()),
                        roleNode(ActionGroup.CONFIGURE_ENGINE, getConstants().allowToGetOrSetSystemConfigurationRoleTreeTooltip())
                )
        );
    }
}
