package org.ovirt.engine.ui.webadmin.gin;

import org.ovirt.engine.ui.common.gin.BaseUiCommonModule;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;
import org.ovirt.engine.ui.webadmin.gin.uicommon.ClusterModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.DataCenterModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.DiskModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.EventModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.HostModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.NetworkModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.PoolModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.ProviderModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.QuotaModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.ReportsModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.StorageModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.TemplateModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.UserModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.VirtualMachineModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.VnicProfileModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.VolumeModule;
import org.ovirt.engine.ui.webadmin.uicommon.WebAdminConfigurator;
import org.ovirt.engine.ui.webadmin.uicommon.model.AlertFirstRowModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.AlertModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.BookmarkModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.ClusterPolicyClusterModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.ClusterPolicyModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.DiskProfilePermissionModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.EventFirstRowModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.EventModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.InstanceTypeGeneralModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.InstanceTypeModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.RoleModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.RolePermissionModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SystemPermissionModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SystemTreeModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.TagModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.TaskFirstRowModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.TaskModelProvider;

import com.google.inject.Singleton;

/**
 * GIN module containing WebAdmin UiCommon model and integration bindings.
 */
public class UiCommonModule extends BaseUiCommonModule {

    @Override
    protected void configure() {
        bindModels();
        bindIntegration();
    }

    void bindModels() {
        // All model providers are ultimately referenced via ModelBoundTabData
        // and are therefore created early on, just like eager singletons
        install(new DataCenterModule());
        install(new StorageModule());
        install(new ClusterModule());
        install(new VirtualMachineModule());
        install(new HostModule());
        install(new PoolModule());
        install(new TemplateModule());
        install(new UserModule());
        install(new EventModule());
        install(new ReportsModule());
        install(new QuotaModule());
        install(new VolumeModule());
        install(new DiskModule());
        install(new NetworkModule());
        install(new ProviderModule());
        install(new VnicProfileModule());

        // SystemTreeModel
        bind(SystemTreeModelProvider.class).asEagerSingleton();

        // BookmarkListModel
        bind(BookmarkModelProvider.class).asEagerSingleton();

        // TagListModel
        bind(TagModelProvider.class).asEagerSingleton();

        // AlertListModel
        bind(AlertModelProvider.class).asEagerSingleton();
        bind(AlertFirstRowModelProvider.class).asEagerSingleton();

        // TaskListModel
        bind(TaskModelProvider.class).asEagerSingleton();
        bind(TaskFirstRowModelProvider.class).asEagerSingleton();

        // EventListModel
        bind(EventModelProvider.class).asEagerSingleton();
        bind(EventFirstRowModelProvider.class).asEagerSingleton();

        // RoleListModel
        bind(RoleModelProvider.class).asEagerSingleton();

        // RolePermissionListModel
        bind(RolePermissionModelProvider.class).asEagerSingleton();

        // SystemPermissionListModel
        bind(SystemPermissionModelProvider.class).asEagerSingleton();

        // ClusterPolicyListModel
        bind(ClusterPolicyModelProvider.class).asEagerSingleton();

        // ClusterPolicyClusterListModel
        bind(ClusterPolicyClusterModelProvider.class).asEagerSingleton();

        bind(InstanceTypeModelProvider.class).asEagerSingleton();
        bind(InstanceTypeGeneralModelProvider.class).asEagerSingleton();

        // disk profiles permissions
        bind(DiskProfilePermissionModelProvider.class).asEagerSingleton();
    }

    void bindIntegration() {
        bindCommonIntegration();
        bindConfiguratorIntegration(WebAdminConfigurator.class);
        bind(LoginModel.class).in(Singleton.class);
    }
}
