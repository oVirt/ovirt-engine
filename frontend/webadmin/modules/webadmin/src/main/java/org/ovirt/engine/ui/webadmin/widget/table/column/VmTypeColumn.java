package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Image column that corresponds to XAML {@code VmTypeTemplate}.
 */
public class VmTypeColumn extends AbstractImageResourceColumn<VM> {

    private static final ApplicationResources resources = AssetProvider.getResources();

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Override
    public ImageResource getValue(VM vm) {
            if (vm.getVmPoolId() == null) {
                VmTypeConfig config = VmTypeConfig.from(vm.getVmType(), vm.isStateless(), vm.isNextRunConfigurationExists());
                return config.getImageResource();
            } else {
                return getPoolVmImageResource(vm.getVmType(), vm.isNextRunConfigurationExists());
            }
    }

    private static ImageResource getPoolVmImageResource(VmType vmType, boolean nextRunConfigurationExists) {
        switch (vmType) {
            case Server:
               return nextRunConfigurationExists ? resources.manyServersChangesImage() : resources.manyServersImage();
            case Desktop:
            default:
                return nextRunConfigurationExists ? resources.manyDesktopsChangesImage() : resources.manyDesktopsImage();
        }
    }

    @Override
    public SafeHtml getTooltip(VM vm) {
        String tooltipContent;
        if (vm.getVmPoolId() == null) {
            VmTypeConfig config = VmTypeConfig.from(vm.getVmType(), vm.isStateless(), vm.isNextRunConfigurationExists());
            tooltipContent = config.getTooltip();
        } else {
            tooltipContent = getPoolVmTooltip(vm.getVmType());
        }

        return SafeHtmlUtils.fromString(tooltipContent);
    }

    private String getPoolVmTooltip(VmType vmType) {
        switch (vmType) {
            case Server:
                return constants.pooledServer();
            case Desktop:
                return constants.pooledDesktop();
            default:
                return constants.pooledDesktop();
        }

    }

}

enum VmTypeConfig {

    DESKTOP_STATELESS(VmType.Desktop, true, false) {
        @Override
        public ImageResource getImageResource() {
            return resources.desktopStateless();
        }

        @Override
        public String getTooltip() {
            return constants.statelessDesktop();
        }
    },

    DESKTOP_STATEFUL(VmType.Desktop, false, false) {
        @Override
        public ImageResource getImageResource() {
            return resources.desktopImage();
        }

        @Override
        public String getTooltip() {
            return constants.desktop();
        }
    },

    SERVER_STATEFUL(VmType.Server, false, false) {
        @Override
        public ImageResource getImageResource() {
            return resources.serverImage();
        }

        @Override
        public String getTooltip() {
            return constants.server();
        }
    },

    SERVER_STATELESS(VmType.Server, true, false) {
        @Override
        public ImageResource getImageResource() {
            return resources.serverStateless();
        }

        @Override
        public String getTooltip() {
            return constants.statelessServer();
        }
    },

    DESKTOP_STATELESS_WITH_NEXT_RUN_CONFIG(VmType.Desktop, true, true) {
        @Override
        public ImageResource getImageResource() {
            return resources.desktopStatelessChanges();
        }

        @Override
        public String getTooltip() {
            return constants.statelessDesktopChanges();
        }
    },

    DESKTOP_STATEFUL_WITH_NEXT_RUN_CONFIG(VmType.Desktop, false, true) {
        @Override
        public ImageResource getImageResource() {
            return resources.desktopChanges();
        }

        @Override
        public String getTooltip() {
            return constants.desktopChanges();
        }
    },

    SERVER_STATEFUL_WITH_NEXT_RUN_CONFIG(VmType.Server, false, true) {
        @Override
        public ImageResource getImageResource() {
            return resources.serverChanges();
        }

        @Override
        public String getTooltip() {
            return constants.serverChanges();
        }
    },

    SERVER_STATELESS_WITH_NEXT_RUN_CONFIG(VmType.Server, true, true) {
        @Override
        public ImageResource getImageResource() {
            return resources.serverStatelessChanges();
        }

        @Override
        public String getTooltip() {
            return constants.statelessServerChanges();
        }
    },

    DEFAULT(null, false, false) {
        @Override
        public ImageResource getImageResource() {
            return resources.manyDesktopsImage();
        }

        @Override
        public String getTooltip() {
            return ""; //$NON-NLS-1$
        }
    };

    private final VmType vmType;
    private final boolean stateless;
    private final boolean nextRunConfigurationExists;

    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

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


    public abstract ImageResource getImageResource();

    public abstract String getTooltip();
}
