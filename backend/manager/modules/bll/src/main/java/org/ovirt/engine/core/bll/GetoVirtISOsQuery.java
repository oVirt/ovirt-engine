package org.ovirt.engine.core.bll;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.VdsIdParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.Version;

/**
 * The {@code GetoVirtISOsQuery} is responsible to detect all available oVirt images installed on engine server. It detects
 * the available ISOs files by there associated version files, read the iSOs' version from within the version files,
 * verifies image files exist, and returns list of ISOs sorted by their version.
 */
public class GetoVirtISOsQuery<P extends VdsIdParametersBase> extends QueriesCommandBase<P> {
    private static final String OVIRT_ISO_VERSION_PREFIX = "version";
    private static final String OVIRT_ISO_VDSM_COMPATIBILITY_PREFIX = "vdsm-compatibility";

    public GetoVirtISOsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<RpmVersion> availableISOsList = new ArrayList<RpmVersion>();

        VDS vds = getVdsByVdsId(getParameters().getVdsId());
        if (vds == null) {
            getQueryReturnValue().setReturnValue(availableISOsList);
            return;
        }

        RpmVersion vdsOsVersion = VdsHandler.getOvirtHostOsVersion(vds);
        String nodeOS = vds.getHostOs();
        if (nodeOS == null) {
            getQueryReturnValue().setReturnValue(new ArrayList<RpmVersion>());
            return;
        }
        for (OVirtNodeInfo.Entry info : OVirtNodeInfo.getInstance().get()) {
            log.debugFormat(
                "nodeOS [{0}] | osPattern [{1}] | minimumVersion [{2}]",
                nodeOS,
                info.osPattern,
                info.minimumVersion
            );

            Matcher matcher = info.osPattern.matcher(nodeOS);
            if (matcher.matches() && info.path.isDirectory()) {
                log.debugFormat("Looking for list of ISOs in [{0}], regex [{1}]", info.path, info.isoPattern);
                for (File file : info.path.listFiles()) {
                    matcher = info.isoPattern.matcher(file.getName());
                    if (matcher.matches()) {
                        log.debugFormat("ISO Found [{0}]", file);
                        String version = matcher.group(1);
                        log.debugFormat("ISO Version [{0}]", version);
                        File versionFile = new File(info.path, String.format("version-%s.txt", version));
                        log.debugFormat("versionFile [{0}]", versionFile);

                        // Setting IsoData Class to get further [version] and [vdsm compatibility version] data
                        IsoData isoData = new IsoData();
                        isoData.setVersion(readIsoVersion(versionFile));
                        String isoVersionText = isoData.getVersion();
                        isoData.setVdsmCompitibilityVersion(readVdsmCompatibiltyVersion((
                                versionFile.getAbsolutePath().replace(OVIRT_ISO_VERSION_PREFIX,
                                        OVIRT_ISO_VDSM_COMPATIBILITY_PREFIX))));

                        if (StringUtils.isEmpty(isoVersionText)) {
                            log.debugFormat("Iso version file {0} is empty.", versionFile.getAbsolutePath());
                            continue;
                        }

                        String[] versionParts = isoVersionText.split(",");
                        if (versionParts.length < 2) {
                            log.debugFormat("Iso version file {0} contains invalid content. Expected: <major-version>,<release> format.",
                                    versionFile.getAbsolutePath());
                            continue;
                        }

                        RpmVersion isoVersion = new RpmVersion(file.getName());

                        if (isoData.getVdsmCompatibilityVersion() != null && isIsoCompatibleForUpgradeByClusterVersion(isoData) ||
                            vdsOsVersion != null && VdsHandler.isIsoVersionCompatibleForUpgrade(vdsOsVersion, isoVersion)
                        ) {
                            availableISOsList.add(isoVersion);
                        }
                    }
                }
            }
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
        log.debugFormat(
            "vdsClusterVersion {0} isoClusterVersion {1}",
            vdsClusterVersion,
            isoClusterVersion
        );
        return (vdsClusterVersion.getMajor() == isoClusterVersion.getMajor() && vdsClusterVersion.getMinor() <= isoClusterVersion.getMinor());
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

    public VDS getVdsByVdsId(Guid vdsId) {
        VDS vds = null;

        if (vdsId != null) {
            vds = getDbFacade().getVdsDao().get(vdsId);
        }
        return vds;
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
