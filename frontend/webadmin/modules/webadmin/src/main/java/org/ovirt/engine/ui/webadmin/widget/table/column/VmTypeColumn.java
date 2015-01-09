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
public class VmTypeColumn extends AbstractWebAdminImageResourceColumn<VM> {

    private static final CommonApplicationConstants constants = ClientGinjectorProvider.getApplicationConstants();

    @Override
    public ImageResource getValue(VM vm) {
            if (vm.getVmPoolId() == null) {
                VmTypeConfig config = VmTypeConfig.from(vm.getVmType(), vm.isStateless(), vm.isNextRunConfigurationExists());
                setTitle(config.getTooltip(constants));
                return config.getImageResource(getApplicationResources());
            } else {
                if (!vm.isNextRunConfigurationExists()) {
                    return getApplicationResources().manyDesktopsImage();
                } else {
                    return getApplicationResources().manyDesktopsChangesImage();
                }

            }
    }

}

enum VmTypeConfig {
    DESKTOP_STATELESS(VmType.Desktop, true, false) {
        @Override
        public ImageResource getImageResource(ApplicationResources resources) {
            return resources.desktopStateless();
        }

        @Override
        public String getTooltip(CommonApplicationConstants constants) {
            return constants.statelessDesktop();
        }
    },

    DESKTOP_STATEFUL(VmType.Desktop, false, false) {
        @Override
        public ImageResource getImageResource(ApplicationResources resources) {
            return resources.desktopImage();
        }

        @Override
        public String getTooltip(CommonApplicationConstants constants) {
            return constants.desktop();
        }
    },

    SERVER_STATEFUL(VmType.Server, false, false) {
        @Override
        public ImageResource getImageResource(ApplicationResources resources) {
            return resources.serverImage();
        }

        @Override
        public String getTooltip(CommonApplicationConstants constants) {
            return constants.server();
        }
    },

    SERVER_STATELESS(VmType.Server, true, false) {
        @Override
        public ImageResource getImageResource(ApplicationResources resources) {
            return resources.serverStateless();
        }

        @Override
        public String getTooltip(CommonApplicationConstants constants) {
            return constants.statelessServer();
        }
    },

    DESKTOP_STATELESS_WITH_NEXT_RUN_CONFIG(VmType.Desktop, true, true) {
        @Override
        public ImageResource getImageResource(ApplicationResources resources) {
            return resources.desktopStatelessChanges();
        }

        @Override
        public String getTooltip(CommonApplicationConstants constants) {
            return constants.statelessDesktopChanges();
        }
    },

    DESKTOP_STATEFUL_WITH_NEXT_RUN_CONFIG(VmType.Desktop, false, true) {
        @Override
        public ImageResource getImageResource(ApplicationResources resources) {
            return resources.desktopChanges();
        }

        @Override
        public String getTooltip(CommonApplicationConstants constants) {
            return constants.desktopChanges();
        }
    },

    SERVER_STATEFUL_WITH_NEXT_RUN_CONFIG(VmType.Server, false, true) {
        @Override
        public ImageResource getImageResource(ApplicationResources resources) {
            return resources.serverChanges();
        }

        @Override
        public String getTooltip(CommonApplicationConstants constants) {
            return constants.serverChanges();
        }
    },

    SERVER_STATELESS_WITH_NEXT_RUN_CONFIG(VmType.Server, true, true) {
        @Override
        public ImageResource getImageResource(ApplicationResources resources) {
            return resources.serverStatelessChanges();
        }

        @Override
        public String getTooltip(CommonApplicationConstants constants) {
            return constants.statelessServerChanges();
        }
    },

    DEFAULT(null, false, false) {
        @Override
        public ImageResource getImageResource(ApplicationResources resources) {
            return resources.manyDesktopsImage();
        }

        @Override
        public String getTooltip(CommonApplicationConstants constants) {
            return ""; //$NON-NLS-1$
        }
    };

    private final VmType vmType;
    private final boolean stateless;
    private final boolean nextRunConfigurationExists;

    VmTypeConfig(VmType vmType, boolean stateless, boolean nextRunConfigurationExists) {
        this.vmType = vmType;
        this.stateless = stateless;
        this.nextRunConfigurationExists = nextRunConfigurationExists;
    }

    public static VmTypeConfig from(VmType vmType, boolean stateless, boolean nextRunConfigurationExists) {
        for (VmTypeConfig config : values()) {
            if (config.stateless == stateless && config.vmType == vmType
                    && config.nextRunConfigurationExists == nextRunConfigurationExists) {
                return config;
            }
        }

        return DEFAULT;
    }

    public abstract ImageResource getImageResource(ApplicationResources resources);

    public abstract String getTooltip(CommonApplicationConstants constants);
}
