package org.ovirt.engine.core.bll;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.VdsIdParametersBase;
import org.ovirt.engine.core.common.utils.RpmVersionUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.Version;

/**
 * The {@code GetoVirtISOsQuery} is responsible to detect all available oVirt images installed on engine server. It detects
 * the available ISOs files by there associated version files, read the iSOs' version from within the version files,
 * verifies image files exist, and returns list of ISOs sorted by their version.
 */
public class GetoVirtISOsQuery<P extends VdsIdParametersBase> extends QueriesCommandBase<P> {
    private static Pattern isoPattern;
    private static final String OVIRT_ISO_VERSION_PREFIX = "version";
    private static final String OVIRT_ISO_VDSM_COMPATIBILITY_PREFIX = "vdsm-compatibility";
    private static final String OVIRT_ISO_VERSION_PATTERN = OVIRT_ISO_VERSION_PREFIX + "-.*.txt";
    private static final Pattern isoVersionPattern = Pattern.compile(OVIRT_ISO_VERSION_PATTERN);

    public GetoVirtISOsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {

        RpmVersion vdsOsVersion = getOvirtOsVersion();

        List<RpmVersion> availableISOsList = new ArrayList<RpmVersion>();
        File directory = new File(Config.resolveOVirtISOsRepositoryPath());

        if (directory.isDirectory()) {
            List<String> listOfIsoFiles = getListOfIsoFiles(directory);
            if (!listOfIsoFiles.isEmpty()) {

                File[] ovirtVersionFiles = filterOvirtFiles(directory, isoVersionPattern);

                for (File versionFile : ovirtVersionFiles) {
                    try {
                        IsoData isoData = new IsoData();
                        isoData.setVersion(readIsoVersion(versionFile));
                        String isoVersionText = isoData.getVersion();
                        isoData.setVdsmCompitibilityVersion(readVdsmCompatibiltyVersion((
                                versionFile.getAbsolutePath().replace(OVIRT_ISO_VERSION_PREFIX,
                                        OVIRT_ISO_VDSM_COMPATIBILITY_PREFIX))));

                        if (StringUtils.isBlank(isoVersionText)) {
                            log.debugFormat("Iso version file {0} is empty.", versionFile.getAbsolutePath());
                            continue;
                        }

                        String[] versionParts = isoVersionText.split(",");
                        if (versionParts.length < 2) {
                            log.debugFormat("Iso version file {0} contains invalid content. Expected: <major-version>,<release> format.",
                                    versionFile.getAbsolutePath());
                            continue;
                        }

                        String majorVersionStr = versionParts[0];
                        String releaseStr = versionParts[1];
                        String isoFileName = getIsoFileNameByVersion(listOfIsoFiles, majorVersionStr, releaseStr);
                        if (isoFileName == null) {
                            log.debugFormat("Iso version file {0} has no matching iso file searched by version parts: {1} and {2}.",
                                    versionFile.getAbsolutePath(),
                                    majorVersionStr,
                                    releaseStr);
                            continue;
                        }

                        RpmVersion isoVersion = new RpmVersion(isoFileName, getOvirtIsoPrefix(), true);
                        boolean shouldAdd = false;

                        String rpmParts[] = RpmVersionUtils.splitRpmToParts(isoFileName);
                        if (isoVersion != null && isIsoVersionSupported(rpmParts[1])) {
                            if (isoData.getVdsmCompatibilityVersion() != null) {
                                shouldAdd = isIsoCompatibleForUpgradeByClusterVersion(isoData);
                            } else if (vdsOsVersion != null) {
                                if (VdsHandler.isIsoVersionCompatibleForUpgrade(vdsOsVersion, isoVersion)) {
                                    shouldAdd = true;
                                }
                            } else {
                                shouldAdd = true;
                            }
                        }

                        if (shouldAdd) {
                            availableISOsList.add(isoVersion);
                        }
                    } catch (RuntimeException e) {
                        log.errorFormat("Failed to parse ovirt iso version {0} with error {1}",
                                versionFile.getAbsolutePath(),
                                ExceptionUtils.getMessage(e));
                    }

                }
            }
        } else {
            log.errorFormat("ovirt ISOs directory not found. Search in: {0}", directory.getPath());
        }
        Collections.sort(availableISOsList);
        getQueryReturnValue().setReturnValue(availableISOsList);
    }

    private boolean isIsoCompatibleForUpgradeByClusterVersion(IsoData isoData) {
        for (String v : isoData.getVdsmCompatibilityVersion()) {
            Version isoClusterVersion = new Version(v);
            if (isNewerVersion(isoClusterVersion)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNewerVersion(Version isoClusterVersion) {
        VDS vds = getVdsByVdsId(getParameters().getVdsId());
        Version vdsClusterVersion = vds.getVdsGroupCompatibilityVersion();
        return (vdsClusterVersion.getMajor() == isoClusterVersion.getMajor() && vdsClusterVersion.getMinor() <= isoClusterVersion.getMinor());
    }

    private RpmVersion getOvirtOsVersion() {
        VDS vds = getVdsByVdsId(getParameters().getVdsId());
        RpmVersion vdsOsVersion = null;
        if (vds != null && vds.getVdsType() == VDSType.oVirtNode) {
            vdsOsVersion = VdsHandler.getOvirtHostOsVersion(vds);
        }
        return vdsOsVersion;
    }

    private static String getIsoFileNameByVersion(List<String> listOfIsoFiles, String majorVersionStr, String releaseStr) {
        Pattern pattern = Pattern.compile(majorVersionStr + ".*" + releaseStr);
        for (String fileName : listOfIsoFiles) {
            if (pattern.matcher(fileName).find()) {
                return fileName;
            }
        }
        return null;
    }

    private static List<String> getListOfIsoFiles(File directory) {
        List<String> isoFileList = new ArrayList<String>();
        File[] filterOvirtFiles = filterOvirtFiles(directory, getIsoPattern());
        for (File file : filterOvirtFiles) {
            isoFileList.add(file.getName());
        }
        return isoFileList;
    }

    private String[] readVdsmCompatibiltyVersion(String fileName) {
        File file = new File(fileName);
        String[] versions = null;
        if (file.exists()) {
            BufferedReader input = null;
            try {
                input = new BufferedReader(new FileReader(file));
                String lineRead = input.readLine();
                if (lineRead != null) {
                    versions = lineRead.split(",");
                }
            } catch (FileNotFoundException e) {
                log.errorFormat("Failed to open version file {0} with error {1}",
                        file.getParent(),
                        ExceptionUtils.getMessage(e));
            } catch (IOException e1) {
                log.errorFormat("Failed to read version from {0} with error {1}",
                        file.getAbsolutePath(),
                        ExceptionUtils.getMessage(e1));
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException ignored) {
                        // Ignore exception on closing a file
                    }
                }
            }
        }
        return versions;
    }

    private String readIsoVersion(File versionFile) {
        String isoVersionText = null;
        BufferedReader input = null;
        try {
            input = new BufferedReader(new FileReader(versionFile));
            isoVersionText = input.readLine();

        } catch (FileNotFoundException e) {
            log.errorFormat("Failed to open version file {0} with error {1}",
                    versionFile.getAbsolutePath(),
                    ExceptionUtils.getMessage(e));
        } catch (IOException e1) {
            log.errorFormat("Failed to read version from {0} with error {1}",
                    versionFile.getAbsolutePath(),
                    ExceptionUtils.getMessage(e1));
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ignored) {
                    // Ignore exception on closing a file
                }
            }
        }
        return isoVersionText;
    }

    private static File[] filterOvirtFiles(File directory, final Pattern pattern) {
        return directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return pattern.matcher(name).find();
            }
        });
    }

    private boolean isIsoVersionSupported(String isoVersion) {
        String supported = Config.<String> GetValue(ConfigValues.OvirtInitialSupportedIsoVersion);
        return RpmVersionUtils.compareRpmParts(isoVersion, supported) >= 0;
    }

    public VDS getVdsByVdsId(Guid vdsId) {
        VDS vds = null;

        if (vdsId != null) {
            vds = getDbFacade().getVdsDao().get(vdsId);
        }
        return vds;
    }

    /** @return The prefix for oVirt ISO files, from the configuration */
    private static String getOvirtIsoPrefix() {
        return Config.<String> GetValue(ConfigValues.OvirtIsoPrefix);
    }

    /**
     * Returns the pattern for ISO files.
     * Since the prefix from the configuration may change (reloadable configuration), it is checked each time.
     * A cached version of pattern is saved, though, to avoid the overhead of re-compiling it.
     */
    private static Pattern getIsoPattern() {
        String expectedPattern = getOvirtIsoPrefix() + "-.*.iso";
        if (isoPattern == null || !expectedPattern.equals(isoPattern.toString())) {
            isoPattern = Pattern.compile(expectedPattern);
        }
        return isoPattern;
    }

    private class IsoData {
        private String version;
        private String[] vdsmCompatibilityVersion;

        public void setVersion(String version) {
            this.version = version;
        }

        public String getVersion() {
            return version;
        }

        public void setVdsmCompitibilityVersion(String[] supportedClusterVersion) {
            this.vdsmCompatibilityVersion = supportedClusterVersion;
        }

        public String[] getVdsmCompatibilityVersion() {
            return vdsmCompatibilityVersion;
        }
    }
}
