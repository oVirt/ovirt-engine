package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.ui.common.widget.table.column.AbstractSafeHtmlColumn;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * Image column that corresponds to XAML {@code VmTypeTemplate}.
 */
public class VmTypeColumn extends AbstractSafeHtmlColumn<VM> {

    private static final ApplicationResources resources = AssetProvider.getResources();

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Override
    public SafeHtml getValue(VM vm) {
        return getRenderedValue(vm);
    }

    private static ImageResource getPoolVmImageResource(VmType vmType, boolean nextRunConfigurationExists) {
        switch (vmType) {
            case Server:
               return nextRunConfigurationExists ? resources.manyServersChangesImage() : resources.manyServersImage();
            case HighPerformance:
               return nextRunConfigurationExists ? resources.manyHighPerformancesChangesImage() : resources.manyHighPerformancesImage();
            case Desktop:
            default:
                return nextRunConfigurationExists ? resources.manyDesktopsChangesImage() : resources.manyDesktopsImage();
        }
    }

    @Override
    public SafeHtml getTooltip(VM vm) {
        Map<SafeHtml, String> imagesToText = getImagesToTooltipTextMap(vm);

        return imagesToText.isEmpty() ? null : MultiImageColumnHelper.getTooltip(imagesToText);
    }

    private static SafeHtml getImageSafeHtml(ImageResource imageResource) {
        return SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.
                create(imageResource).getHTML());
    }

    private Map<SafeHtml, String> getImagesToTooltipTextMap(VM vm) {
        Map<SafeHtml, String> res = new LinkedHashMap<>();

        if (vm.getVmPoolId() == null) {
            VmTypeConfig config = VmTypeConfig.from(vm.getVmType(), vm.isStateless(), vm.isNextRunConfigurationExists());
            res.put(getImageSafeHtml(config.getImageResource()), config.getTooltip());
        } else {
            ImageResource img = getPoolVmImageResource(vm.getVmType(), vm.isNextRunConfigurationExists());
            res.put(getImageSafeHtml(img), getPoolVmTooltip(vm.getVmType()));
        }

        if (vm.isHostedEngine()) {
            res.put(getImageSafeHtml(resources.mgmtNetwork()), constants.isHostedEngineVmTooltip());
        }

        return res;
    }

    private String getPoolVmTooltip(VmType vmType) {
        switch (vmType) {
            case Server:
                return constants.pooledServer();
            case Desktop:
                return constants.pooledDesktop();
            case HighPerformance:
                return constants.pooledHighPerformance();
            default:
                return constants.pooledDesktop();
        }

    }

    public static SafeHtml getRenderedValue(VM vm) {
        List<SafeHtml> images = new ArrayList<>();

        if (vm.getVmPoolId() == null) {
            VmTypeConfig config = VmTypeConfig.from(vm.getVmType(), vm.isStateless(), vm.isNextRunConfigurationExists());
            images.add(getImageSafeHtml(config.getImageResource()));
        } else {
            ImageResource img = getPoolVmImageResource(vm.getVmType(), vm.isNextRunConfigurationExists());
            images.add(getImageSafeHtml(img));
        }

        if (vm.isHostedEngine()) {
            images.add(getImageSafeHtml(resources.mgmtNetwork()));
        }

        return images.isEmpty() ? null : MultiImageColumnHelper.getValue(images);
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

        HIGH_PERFORMANCE_STATEFUL(VmType.HighPerformance, false, false) {
            @Override
            public ImageResource getImageResource() {
                return resources.highPerformanceImage();
            }

            @Override
            public String getTooltip() {
                return constants.highPerformance();
            }
        },

        HIGH_PERFORMANCE_STATELESS(VmType.HighPerformance, true, false) {
            @Override
            public ImageResource getImageResource() {
                return resources.highPerformanceStateless();
            }

            @Override
            public String getTooltip() {
                return constants.statelessHighPerformance();
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

        HIGH_PERFORMANCE_STATEFUL_WITH_NEXT_RUN_CONFIG(VmType.HighPerformance, false, true) {
            @Override
            public ImageResource getImageResource() {
                return resources.highPerformanceChanges();
            }

            @Override
            public String getTooltip() {
                return constants.highPerformanceChanges();
            }
        },

        HIGH_PERFORMANCE_STATELESS_WITH_NEXT_RUN_CONFIG(VmType.HighPerformance, true, true) {
            @Override
            public ImageResource getImageResource() {
                return resources.highPerformanceStatelessChanges();
            }

            @Override
            public String getTooltip() {
                return constants.statelessHighPerformanceChanges();
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
}
