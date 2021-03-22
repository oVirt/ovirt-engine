package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.ui.common.widget.table.column.AbstractSafeHtmlColumn;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.NextRunFieldMessages;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
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

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();

    private static final NextRunFieldMessages nextRunMessages = ConstantsManager.getInstance().getNextRunFieldMessages();

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
        Map<SafeHtml, SafeHtml> imagesToText = getImagesToTooltipTextMap(vm);
        return imagesToText.isEmpty() ? null : MultiImageColumnHelper.getTooltipFromSafeHtml(imagesToText);
    }

    private static SafeHtml getImageSafeHtml(ImageResource imageResource) {
        return SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.
                create(imageResource).getHTML());
    }

    private Map<SafeHtml, SafeHtml> getImagesToTooltipTextMap(VM vm) {
        Map<SafeHtml, SafeHtml> res = new LinkedHashMap<>();

        if (vm.getVmPoolId() == null) {
            VmTypeConfig config = VmTypeConfig.from(vm.getVmType(), vm.isStateless(), configurationWillChangeAfterRestart(vm));
            res.put(getImageSafeHtml(config.getImageResource()), config.getTooltip());
        } else {
            ImageResource img = getPoolVmImageResource(vm.getVmType(), configurationWillChangeAfterRestart(vm));
            res.put(getImageSafeHtml(img), getPoolVmTooltip(vm.getVmType()));
        }

        if (vm.isHostedEngine()) {
            res.put(getImageSafeHtml(resources.mgmtNetwork()), SafeHtmlUtils.fromString(constants.isHostedEngineVmTooltip()));
        }

        if (!vm.isManaged()) {
            res.put(getImageSafeHtml(resources.container()), SafeHtmlUtils.fromString(constants.isRunningInContainer()));
        }

        if (configurationWillChangeAfterRestart(vm)) {
            Set<String> nextRunFields = vm.getNextRunChangedFields() != null ? vm.getNextRunChangedFields() : new HashSet<>();
            if (clusterCpuChanged(vm) && !nextRunFields.contains("customCpuName")){ //$NON-NLS-1$
                nextRunFields.add("clusterCpuChange"); //$NON-NLS-1$
            }
            if (vm.isVnicsOutOfSync()) {
                nextRunFields.add("interfaces"); //$NON-NLS-1$
            }
            res.put(SafeHtmlUtils.EMPTY_SAFE_HTML, getNextRunChangedFieldsTooltip(nextRunFields));
        }
        return res;
    }

    private static boolean clusterCpuChanged(VM vm){
        return vm.isManaged()
                && !vm.isHostedEngine()
                && vm.isRunningOrPaused()
                && vm.getCustomCpuName() == null
                && !vm.isUsingCpuPassthrough()
                && !Objects.equals(vm.getCpuName(), vm.getClusterCpuVerb());
    }

    private static boolean configurationWillChangeAfterRestart(VM vm){
        return clusterCpuChanged(vm) || vm.isNextRunConfigurationExists() || vm.isVnicsOutOfSync();
    }

    private SafeHtml getNextRunChangedFieldsTooltip(Set<String> changedFields) {
        if (changedFields == null || changedFields.isEmpty()) {
            return SafeHtmlUtils.EMPTY_SAFE_HTML;
        }
        String title = ConstantsManager.getInstance().getConstants().pendingVMChanges();
        String listItems = changedFields.stream().map(v -> templates.listItem(localizeField(v)).asString()).collect(Collectors.joining());
        String tooltip = templates.unorderedListWithTitle(title, SafeHtmlUtils.fromTrustedString(listItems)).asString();
        return SafeHtmlUtils.fromTrustedString(tooltip);
    }

    private SafeHtml getPoolVmTooltip(VmType vmType) {
        String tooltip;
        switch (vmType) {
            case Server:
                tooltip = constants.pooledServer();
                break;
            case Desktop:
                tooltip = constants.pooledDesktop();
                break;
            case HighPerformance:
                tooltip = constants.pooledHighPerformance();
                break;
            default:
                tooltip = constants.pooledDesktop();
                break;
        }
        return SafeHtmlUtils.fromString(tooltip);
    }

    private String localizeField(String field) {
        try {
            return nextRunMessages.getString(field);
        } catch (MissingResourceException e) {
            return field;
        }
    }

    public static SafeHtml getRenderedValue(VM vm) {
        List<SafeHtml> images = new ArrayList<>();

        if (vm.getVmPoolId() == null) {
            VmTypeConfig config = VmTypeConfig.from(vm.getVmType(), vm.isStateless(), configurationWillChangeAfterRestart(vm));
            images.add(getImageSafeHtml(config.getImageResource()));
        } else {
            ImageResource img = getPoolVmImageResource(vm.getVmType(), configurationWillChangeAfterRestart(vm));
            images.add(getImageSafeHtml(img));
        }

        if (vm.isHostedEngine()) {
            images.add(getImageSafeHtml(resources.mgmtNetwork()));
        }

        if (!vm.isManaged()) {
            images.add(getImageSafeHtml(resources.container()));
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
            public String getTooltipString() {
                return constants.statelessDesktop();
            }
        },

        DESKTOP_STATEFUL(VmType.Desktop, false, false) {
            @Override
            public ImageResource getImageResource() {
                return resources.desktopImage();
            }

            @Override
            public String getTooltipString() {
                return constants.desktop();
            }
        },

        SERVER_STATEFUL(VmType.Server, false, false) {
            @Override
            public ImageResource getImageResource() {
                return resources.serverImage();
            }

            @Override
            public String getTooltipString() {
                return constants.server();
            }
        },

        SERVER_STATELESS(VmType.Server, true, false) {
            @Override
            public ImageResource getImageResource() {
                return resources.serverStateless();
            }

            @Override
            public String getTooltipString() {
                return constants.statelessServer();
            }
        },

        HIGH_PERFORMANCE_STATEFUL(VmType.HighPerformance, false, false) {
            @Override
            public ImageResource getImageResource() {
                return resources.highPerformanceImage();
            }

            @Override
            public String getTooltipString() {
                return constants.highPerformance();
            }
        },

        HIGH_PERFORMANCE_STATELESS(VmType.HighPerformance, true, false) {
            @Override
            public ImageResource getImageResource() {
                return resources.highPerformanceStateless();
            }

            @Override
            public String getTooltipString() {
                return constants.statelessHighPerformance();
            }
        },

        DESKTOP_STATELESS_WITH_NEXT_RUN_CONFIG(VmType.Desktop, true, true) {
            @Override
            public ImageResource getImageResource() {
                return resources.desktopStatelessChanges();
            }

            @Override
            public String getTooltipString() {
                return constants.statelessDesktopChanges();
            }
        },

        DESKTOP_STATEFUL_WITH_NEXT_RUN_CONFIG(VmType.Desktop, false, true) {
            @Override
            public ImageResource getImageResource() {
                return resources.desktopChanges();
            }

            @Override
            public String getTooltipString() {
                return constants.desktopChanges();
            }
        },

        SERVER_STATEFUL_WITH_NEXT_RUN_CONFIG(VmType.Server, false, true) {
            @Override
            public ImageResource getImageResource() {
                return resources.serverChanges();
            }

            @Override
            public String getTooltipString() {
                return constants.serverChanges();
            }
        },

        SERVER_STATELESS_WITH_NEXT_RUN_CONFIG(VmType.Server, true, true) {
            @Override
            public ImageResource getImageResource() {
                return resources.serverStatelessChanges();
            }

            @Override
            public String getTooltipString() {
                return constants.statelessServerChanges();
            }
        },

        HIGH_PERFORMANCE_STATEFUL_WITH_NEXT_RUN_CONFIG(VmType.HighPerformance, false, true) {
            @Override
            public ImageResource getImageResource() {
                return resources.highPerformanceChanges();
            }

            @Override
            public String getTooltipString() {
                return constants.highPerformanceChanges();
            }
        },

        HIGH_PERFORMANCE_STATELESS_WITH_NEXT_RUN_CONFIG(VmType.HighPerformance, true, true) {
            @Override
            public ImageResource getImageResource() {
                return resources.highPerformanceStatelessChanges();
            }

            @Override
            public String getTooltipString() {
                return constants.statelessHighPerformanceChanges();
            }
        },

        DEFAULT(null, false, false) {
            @Override
            public ImageResource getImageResource() {
                return resources.manyDesktopsImage();
            }

            @Override
            public String getTooltipString() {
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

        public SafeHtml getTooltip() {
            return SafeHtmlUtils.fromString(getTooltipString());
        }

        public abstract String getTooltipString();
    }
}
