package org.ovirt.engine.ui.webadmin.gin.uicommon;

import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.macpool.SharedMacPoolListModel;
import org.ovirt.engine.ui.webadmin.uicommon.model.PermissionModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SharedMacPoolModelProvider;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

public class MacPoolModule extends AbstractGinModule {
    @Override
    protected void configure() {
        bind(SharedMacPoolListModel.class).in(Singleton.class);

        // SharedMacPoolListModel
        bind(SharedMacPoolModelProvider.class).in(Singleton.class);

        bind(new TypeLiteral<PermissionListModel<MacPool>>(){}).in(Singleton.class);

        // Permission Model
        bind(new TypeLiteral<PermissionModelProvider<MacPool, SharedMacPoolListModel>>(){}).in(Singleton.class);
    }
}
