package org.ovirt.engine.ui.webadmin.widget.table.column;

import com.google.gwt.resources.client.ImageResource;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

/**
 * Image column that corresponds to XAML {@code VmTypeTemplate}.
 */
public class VmTypeColumn extends WebAdminImageResourceColumn<VM> {

    private static final CommonApplicationConstants constants = ClientGinjectorProvider.getApplicationConstants();

    @Override
    public ImageResource getValue(VM vm) {
            if (vm.getVmPoolId() == null) {
                VmTypeConfig config = VmTypeConfig.from(vm.getVmType(), vm.isStateless());
                setTitle(config.getTooltip(constants));
                return config.getImageResource(getApplicationResources());
            } else {
                return getApplicationResources().manyDesktopsImage();
            }
    }

}

enum VmTypeConfig {
    DESKTOP_STATELESS(VmType.Desktop, true) {
        @Override
        public ImageResource getImageResource(ApplicationResources resources) {
            return resources.desktopStateless();
        }

        @Override
        public String getTooltip(CommonApplicationConstants constants) {
            return constants.statelessDesktop();
        }
    },

    DESKTOP_STATEFUL(VmType.Desktop, false) {
        @Override
        public ImageResource getImageResource(ApplicationResources resources) {
            return resources.desktopImage();
        }

        @Override
        public String getTooltip(CommonApplicationConstants constants) {
            return constants.desktop();
        }
    },

    SERVER_STATEFUL(VmType.Server, false) {
        @Override
        public ImageResource getImageResource(ApplicationResources resources) {
            return resources.serverImage();
        }

        @Override
        public String getTooltip(CommonApplicationConstants constants) {
            return constants.server();
        }
    },

    SERVER_STATELESS(VmType.Server, true) {
        @Override
        public ImageResource getImageResource(ApplicationResources resources) {
            return resources.serverStateless();
        }

        @Override
        public String getTooltip(CommonApplicationConstants constants) {
            return constants.statelessServer();
        }
    },

    DEFAULT(null, false) {
        @Override
        public ImageResource getImageResource(ApplicationResources resources) {
            return resources.manyDesktopsImage();
        }

        @Override
        public String getTooltip(CommonApplicationConstants constants) {
            return ""; //$NON-NLS-1$
        }
    },
    ;

    private final VmType vmType;
    private final boolean stateless;

    VmTypeConfig(VmType vmType, boolean stateless) {
        this.vmType = vmType;
        this.stateless = stateless;
    }

    public static VmTypeConfig from(VmType vmType, boolean stateless) {
        for (VmTypeConfig config : values()) {
            if (config.stateless == stateless && config.vmType == vmType) {
                return config;
            }
        }

        return DEFAULT;
    }

    public abstract ImageResource getImageResource(ApplicationResources resources);

    public abstract String getTooltip(CommonApplicationConstants constants);
}
