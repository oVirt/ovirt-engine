package org.ovirt.engine.ui.userportal.gin;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.uicommonweb.models.configure.UserPortalPermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.resources.ResourcesModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.UserPortalTemplateDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.UserPortalTemplateEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalBasicListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.VmBasicDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.AdElementListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserPortalAdElementListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UserPortalVmEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UserPortalVmSnapshotListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmAppListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmMonitorModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSessionsModel;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

public class UserPortalModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(AdElementListModel.class).to(UserPortalAdElementListModel.class);

        // Basic tab
        bind(UserPortalBasicListModel.class).in(Singleton.class);
        bind(VmBasicDiskListModel.class).in(Singleton.class);

        // Extended tab: Virtual Machine
        bind(UserPortalListModel.class).in(Singleton.class);
        bind(VmGeneralModel.class).in(Singleton.class);
        bind(PoolGeneralModel.class).in(Singleton.class);
        bind(VmInterfaceListModel.class).in(Singleton.class);
        bind(VmDiskListModel.class).in(Singleton.class);
        bind(PoolDiskListModel.class).in(Singleton.class);
        bind(UserPortalVmSnapshotListModel.class).in(Singleton.class);
        bind(new TypeLiteral<UserPortalPermissionListModel<UserPortalListModel>>(){}).in(Singleton.class);
        bind(UserPortalVmEventListModel.class).in(Singleton.class);
        bind(VmAppListModel.class).in(Singleton.class);
        bind(VmMonitorModel.class).in(Singleton.class);
        bind(PoolInterfaceListModel.class).in(Singleton.class);
        bind(VmSessionsModel.class).in(Singleton.class);

        // Extended tab: Template
        bind(UserPortalTemplateListModel.class).in(Singleton.class);
        bind(TemplateGeneralModel.class).in(Singleton.class);
        bind(TemplateInterfaceListModel.class).in(Singleton.class);
        bind(new TypeLiteral<UserPortalPermissionListModel<VmTemplate>>(){}).in(Singleton.class);
        bind(UserPortalTemplateDiskListModel.class).in(Singleton.class);
        bind(UserPortalTemplateEventListModel.class).in(Singleton.class);

        // Extended tab: Resources
        bind(ResourcesModel.class).in(Singleton.class);

    }

}
