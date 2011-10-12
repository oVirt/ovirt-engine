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
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * The {@code GetoVirtISOsQuery} responsible to detect all available oVirt images installed on engine server. It detects
 * the available ISOs files by there associated version files, read the iSOs' version from within the version files,
 * verifies image files exist, and returns list of ISOs sorted by their version.
 */
public class GetoVirtISOsQuery<P extends VdsIdParametersBase> extends QueriesCommandBase<P> {
    private static final String OVIRT_ISO_PREFIX = Config.<String> GetValue(ConfigValues.OvirtIsoPrefix);
    private static final String OVIRT_ISO_PATTERN = OVIRT_ISO_PREFIX + "-.*.iso";
    private static Pattern isoPattern = Pattern.compile(OVIRT_ISO_PATTERN);
    private static final String OVIRT_ISO_VERSION_PATTERN = "version-.*.txt";
    private static Pattern isoVersionPattern = Pattern.compile(OVIRT_ISO_VERSION_PATTERN);
    private static LogCompat log = LogFactoryCompat.getLog(GetoVirtISOsQuery.class);

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
                        String isoVersionText = readIsoVersion(versionFile);

                        if (StringUtils.isBlank(isoVersionText)) {
                            log.debugFormat("Iso version file {0} is empty.", versionFile.getAbsolutePath());
                            continue;
                        }

                        String[] versionParts = isoVersionText.split(",");
                        if (versionParts.length < 2) {
                            log.debugFormat("Iso version file {0} contains invalid content. Excpected: <major-version>,<release> format.",
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

                        RpmVersion isoVersion = parseIsoFileVersion(isoFileName, majorVersionStr);
                        boolean shouldAdd = false;
                        if (isoVersion != null && isIsoVersionSupported(isoVersion)) {
                            if (vdsOsVersion != null) {
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

    private RpmVersion getOvirtOsVersion() {
        VDS vds = getVdsByVdsId(getParameters().getVdsId());
        RpmVersion vdsOsVersion = null;
        if (vds != null && vds.getvds_type() == VDSType.oVirtNode) {
            vdsOsVersion = VdsHandler.getOvirtHostOsVersion(vds);
        }
        return vdsOsVersion;
    }

    private RpmVersion parseIsoFileVersion(String isoFileName, String majorVersionStr) {
        RpmVersion isoVersion = null;
        try {
            String rpmLike = isoFileName.replaceFirst(majorVersionStr + "-", majorVersionStr + ".");
            isoVersion = new RpmVersion(rpmLike, OVIRT_ISO_PREFIX, true);
            isoVersion.setRpmName(isoFileName);
        } catch (RuntimeException e) {
            log.errorFormat("Failed to extract RpmVersion for iso file {0} with major version {1} due to {2}",
                    isoFileName,
                    majorVersionStr,
                    ExceptionUtils.getMessage(e));
        }
        return isoVersion;
    }

    private String getIsoFileNameByVersion(List<String> listOfIsoFiles, String majorVersionStr, String releaseStr) {
        Pattern pattern = Pattern.compile(majorVersionStr + ".*" + releaseStr);
        for (String fileName : listOfIsoFiles) {
            if (pattern.matcher(fileName).find()) {
                return fileName;
            }
        }
        return null;
    }

    private List<String> getListOfIsoFiles(File directory) {
        List<String> isoFileList = new ArrayList<String>();
        File[] filterOvirtFiles = filterOvirtFiles(directory, isoPattern);
        for (File file : filterOvirtFiles) {
            isoFileList.add(file.getName());
        }
        return isoFileList;
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
                } catch (IOException e) {
                }
            }
        }
        return isoVersionText;
    }

    private File[] filterOvirtFiles(File directory, final Pattern pattern) {
        return directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return pattern.matcher(name).find();
            }
        });
    }

    private boolean isIsoVersionSupported(Version isoVersion){
        Version supported = new Version(Config.<String>GetValue(ConfigValues.OvirtInitialSupportedIsoVersion));
        return isoVersion.compareTo(supported) >= 0;
    }

    public VDS getVdsByVdsId(Guid vdsId) {
        VDS vds = null;

        if (vdsId != null) {
            vds = DbFacade.getInstance().getVdsDAO().get(vdsId);
        }
        return vds;
    }

}
