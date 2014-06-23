package org.ovirt.engine.ui.userportal.gin;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.common.gin.BaseUiCommonModule;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.resources.ResourcesModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.UserPortalTemplateDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.UserPortalTemplateEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalLoginModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UserPortalVmEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmAppListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSessionsModel;
import org.ovirt.engine.ui.userportal.uicommon.UserPortalConfigurator;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDataBoundModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDetailModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.basic.UserPortalBasicListProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.template.TemplateInterfaceListModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.template.TemplatePermissionListModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.template.UserPortalTemplateListProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.UserPortalListProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmDiskListModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmInterfaceListModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmMonitorModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmPermissionListModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmSnapshotListModelProvider;

import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

/**
 * GIN module containing UserPortal UiCommon model and integration bindings.
 */
public class UiCommonModule extends BaseUiCommonModule {

    @Override
    protected void configure() {
        bindModels();
        bindIntegration();
        install(new UserPortalModule());
    }

    void bindModels() {
        // Basic tab
        bind(UserPortalBasicListProvider.class).in(Singleton.class);

        // Extended tab: Virtual Machine
        bind(UserPortalListProvider.class).in(Singleton.class);
        bind(new TypeLiteral<UserPortalDetailModelProvider<UserPortalListModel, VmGeneralModel>>(){})
            .in(Singleton.class);
        bind(new TypeLiteral<UserPortalDetailModelProvider<UserPortalListModel, PoolGeneralModel>>(){})
            .in(Singleton.class);
        bind(VmInterfaceListModelProvider.class).in(Singleton.class);
        bind(VmDiskListModelProvider.class).in(Singleton.class);
        bind(new TypeLiteral<UserPortalSearchableDetailModelProvider<Disk, UserPortalListModel, PoolDiskListModel>>(){})
            .in(Singleton.class);
        bind(VmSnapshotListModelProvider.class).in(Singleton.class);
        bind(VmPermissionListModelProvider.class).in(Singleton.class);
        bind(new TypeLiteral<UserPortalSearchableDetailModelProvider<AuditLog, UserPortalListModel,
                UserPortalVmEventListModel>>(){}).in(Singleton.class);
        bind(new TypeLiteral<UserPortalSearchableDetailModelProvider<String, UserPortalListModel, VmAppListModel>>(){})
            .in(Singleton.class);
        bind(VmMonitorModelProvider.class).in(Singleton.class);
        bind(new TypeLiteral<UserPortalSearchableDetailModelProvider<VmNetworkInterface, UserPortalListModel,
                PoolInterfaceListModel>>(){}).in(Singleton.class);
        bind(new TypeLiteral<UserPortalDetailModelProvider<UserPortalListModel, VmSessionsModel>>(){})
            .in(Singleton.class);

        // Extended tab: Template
        bind(UserPortalTemplateListProvider.class).in(Singleton.class);
        bind(new TypeLiteral<UserPortalDetailModelProvider<UserPortalTemplateListModel, TemplateGeneralModel>>(){})
            .in(Singleton.class);
        bind(TemplateInterfaceListModelProvider.class).in(Singleton.class);
        bind(new TypeLiteral<UserPortalSearchableDetailModelProvider<DiskImage, UserPortalTemplateListModel,
                UserPortalTemplateDiskListModel>>(){}).in(Singleton.class);
        bind(new TypeLiteral<UserPortalSearchableDetailModelProvider<AuditLog, UserPortalTemplateListModel,
                UserPortalTemplateEventListModel>>(){}).in(Singleton.class);
        bind(TemplatePermissionListModelProvider.class).in(Singleton.class);

        // Extended tab: Resources
        bind(new TypeLiteral<UserPortalDataBoundModelProvider<VM, ResourcesModel>>(){}).in(Singleton.class);
    }

    void bindIntegration() {
        bindCommonIntegration();
        bindConfiguratorIntegration(UserPortalConfigurator.class);
        bind(UserPortalLoginModel.class).in(Singleton.class);
    }

}
