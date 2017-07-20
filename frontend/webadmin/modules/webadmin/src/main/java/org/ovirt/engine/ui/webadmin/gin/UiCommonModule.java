package org.ovirt.engine.ui.webadmin.gin;

import org.ovirt.engine.ui.common.gin.BaseUiCommonModule;
import org.ovirt.engine.ui.common.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;
import org.ovirt.engine.ui.uicommonweb.models.autocomplete.SearchSuggestModel;
import org.ovirt.engine.ui.uicommonweb.models.bookmarks.BookmarkListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.SystemPermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.instancetypes.InstanceTypeGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.instancetypes.InstanceTypeListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.roles_ui.RoleListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.roles_ui.RolePermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.ClusterPolicyClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.ClusterPolicyListModel;
import org.ovirt.engine.ui.uicommonweb.models.events.AlertListModel;
import org.ovirt.engine.ui.uicommonweb.models.events.TaskListModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagListModel;
import org.ovirt.engine.ui.webadmin.gin.uicommon.ClusterModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.DataCenterModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.DiskModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.ErrataModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.EventModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.HostModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.MacPoolModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.NetworkModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.PoolModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.ProviderModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.QuotaModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.SessionModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.StorageModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.TemplateModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.UserModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.VirtualMachineModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.VnicProfileModule;
import org.ovirt.engine.ui.webadmin.gin.uicommon.VolumeModule;
import org.ovirt.engine.ui.webadmin.uicommon.WebAdminConfigurator;
import org.ovirt.engine.ui.webadmin.uicommon.model.AlertModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.BookmarkModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.ClusterPolicyClusterModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.ClusterPolicyModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.CpuProfilePermissionModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.DiskProfilePermissionModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.InstanceTypeModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.RoleModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.RolePermissionModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SystemPermissionModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.TagModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.TaskModelProvider;

import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

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
        // all model providers should be bound as singletons
        install(new DataCenterModule());
        install(new StorageModule());
        install(new ClusterModule());
        install(new VirtualMachineModule());
        install(new HostModule());
        install(new PoolModule());
        install(new TemplateModule());
        install(new UserModule());
        install(new EventModule());
        install(new QuotaModule());
        install(new VolumeModule());
        install(new DiskModule());
        install(new NetworkModule());
        install(new ProviderModule());
        install(new VnicProfileModule());
        install(new MacPoolModule());
        install(new ErrataModule());
        install(new SessionModule());

        bindCommonModels();

        // BookmarkListModel
        bind(BookmarkModelProvider.class).in(Singleton.class);

        // TagListModel
        bind(TagModelProvider.class).in(Singleton.class);

        // AlertListModel
        bind(AlertModelProvider.class).in(Singleton.class);

        // TaskListModel
        bind(TaskModelProvider.class).in(Singleton.class);

        // RoleListModel
        bind(RoleModelProvider.class).in(Singleton.class);

        // RolePermissionListModel
        bind(RolePermissionModelProvider.class).in(Singleton.class);

        // SystemPermissionListModel
        bind(SystemPermissionModelProvider.class).in(Singleton.class);

        // ClusterPolicyListModel
        bind(ClusterPolicyModelProvider.class).in(Singleton.class);

        // ClusterPolicyClusterListModel
        bind(ClusterPolicyClusterModelProvider.class).in(Singleton.class);

        bind(InstanceTypeModelProvider.class).in(Singleton.class);
        bind(new TypeLiteral<DetailTabModelProvider<InstanceTypeListModel, InstanceTypeGeneralModel>>(){}).in(Singleton.class);

        // disk profiles permissions
        bind(DiskProfilePermissionModelProvider.class).in(Singleton.class);

        // cpu profiles permissions
        bind(CpuProfilePermissionModelProvider.class).in(Singleton.class);
    }

    void bindIntegration() {
        bindCommonIntegration();
        bindConfiguratorIntegration(WebAdminConfigurator.class);
        bind(LoginModel.class).in(Singleton.class);
    }

    void bindCommonModels() {
        bind(PermissionListModel.class).in(Singleton.class);
        bind(RoleListModel.class).in(Singleton.class);
        bind(RolePermissionListModel.class).in(Singleton.class);
        bind(SystemPermissionListModel.class).in(Singleton.class);
        bind(ClusterPolicyListModel.class).in(Singleton.class);
        bind(ClusterPolicyClusterListModel.class).in(Singleton.class);
        bind(InstanceTypeListModel.class).in(Singleton.class);
        bind(InstanceTypeGeneralModel.class).in(Singleton.class);
        bind(SearchSuggestModel.class).in(Singleton.class);
        bind(BookmarkListModel.class).in(Singleton.class);
        bind(TagListModel.class).in(Singleton.class);
        bind(AlertListModel.class).in(Singleton.class);
        bind(TaskListModel.class).in(Singleton.class);
    }
}
