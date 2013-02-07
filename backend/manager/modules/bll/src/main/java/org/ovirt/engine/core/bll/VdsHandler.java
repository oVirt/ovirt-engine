package org.ovirt.engine.core.bll;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.backendinterfaces.BaseHandler;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class VdsHandler extends BaseHandler {
    private static Log log = LogFactory.getLog(VdsHandler.class);
    private static ObjectIdentityChecker mUpdateVdsStatic;

    /**
     * Initialize static list containers, for identity and permission check. The initialization should be executed
     * before calling ObjectIdentityChecker.
     *
     * @see Backend#InitHandlers
     */
    public static void Init()
    {
        mUpdateVdsStatic =
            new ObjectIdentityChecker(
                    VdsHandler.class,
                    new java.util.ArrayList<String>(java.util.Arrays.asList(new String[] { "VDS", "VdsStatic",
                            "VdsDynamic" })),
                    VDSStatus.class);
        mUpdateVdsStatic.AddPermittedField("vdsName");
        mUpdateVdsStatic.AddPermittedField("managmentIp");
        mUpdateVdsStatic.AddPermittedField("pmType");
        mUpdateVdsStatic.AddPermittedField("pmUser");
        mUpdateVdsStatic.AddPermittedField("pmPassword");
        mUpdateVdsStatic.AddPermittedField("pmPort");
        mUpdateVdsStatic.AddPermittedField("pmOptions");
        mUpdateVdsStatic.AddPermittedField("pmEnabled");
        mUpdateVdsStatic.AddPermittedField("pmProxyPreferences");
        mUpdateVdsStatic.AddPermittedField("PmOptionsMap");
        mUpdateVdsStatic.AddPermittedField("managmentIp");
        mUpdateVdsStatic.AddPermittedField("pmSecondaryIp");
        mUpdateVdsStatic.AddPermittedField("pmSecondaryType");
        mUpdateVdsStatic.AddPermittedField("pmSecondaryUser");
        mUpdateVdsStatic.AddPermittedField("pmSecondaryPassword");
        mUpdateVdsStatic.AddPermittedField("pmSecondaryPort");
        mUpdateVdsStatic.AddPermittedField("pmSecondaryOptions");
        mUpdateVdsStatic.AddPermittedField("pmSecondaryOptionsMap");
        mUpdateVdsStatic.AddPermittedField("pmSecondaryConcurrent");
        mUpdateVdsStatic.AddPermittedField("vdsSpmPriority");
        mUpdateVdsStatic.AddPermittedField("otpValidity");
        mUpdateVdsStatic.AddPermittedField("consoleAddress");
        mUpdateVdsStatic.AddFields(
                java.util.Arrays.asList(new Enum<?>[] { VDSStatus.NonResponsive, VDSStatus.Maintenance, VDSStatus.Down,
                        VDSStatus.Unassigned, VDSStatus.InstallFailed, VDSStatus.PendingApproval }),
                java.util.Arrays.asList(new String[] { "ip", "vdsUniqueId", "hostName", "port", "vdsGroupId" }));
    }

    public VdsHandler() {
        mUpdateVdsStatic.setContainer(this);
    }

    public static boolean IsUpdateValid(VdsStatic source, VdsStatic distination, VDSStatus status) {

        return mUpdateVdsStatic.IsUpdateValid(source, distination, status);
    }

    public static boolean IsFieldsUpdated(VdsStatic source, VdsStatic destination, Iterable<String> list) {
        return mUpdateVdsStatic.IsFieldsUpdated(source, destination, list);
    }

    public static void HandleVdsCpuFlagsOrClusterChanged(Guid vdsId) {

    }

    public boolean isVdsWithSameNameExist(String vdsName) {
        return isVdsWithSameNameExistStatic(vdsName);
    }

    public boolean isVdsWithSameHostExist(String hostName) {
        return isVdsWithSameHostExistStatic(hostName);
    }

    public boolean isVdsWithSameIpExists(String ipAddress) {
        return isVdsWithSameIpExistsStatic(ipAddress);
    }

    public static boolean isVdsWithSameNameExistStatic(String vdsName) {
        VdsStatic vds = DbFacade.getInstance().getVdsStaticDao().get(vdsName);
        return (vds != null);
    }

    public static boolean isVdsWithSameHostExistStatic(String hostName) {
        List<VdsStatic> vds = DbFacade.getInstance().getVdsStaticDao().getAllForHost(hostName);
        return (vds.size() != 0);
    }

    public static boolean isVdsWithSameIpExistsStatic(String ipAddress) {
        List<VdsStatic> vds = DbFacade.getInstance().getVdsStaticDao().getAllWithIpAddress(ipAddress);
        return (vds.size() != 0);
    }

    static private boolean isPendingOvirt(VDSType type, VDSStatus status) {
        return type == VDSType.oVirtNode && status == VDSStatus.PendingApproval;
    }

    static public boolean isPendingOvirt(VDS vds) {
        return isPendingOvirt(vds.getVdsType(), vds.getStatus());
    }

    /**
     * Extracts the oVirt OS version from raw material of {@code VDS.gethost_os()} field.
     *
     * @param vds
     *            the ovirt host which its OS version in a format of: [OS Name - OS Version - OS release]
     * @return a version class of the oVirt OS version, or null if failed to parse.
     */
    static public RpmVersion getOvirtHostOsVersion(VDS vds) {
        RpmVersion vdsOsVersion = null;
        try {
            vdsOsVersion = new RpmVersion(vds.getHostOs(), "RHEV Hypervisor -", true);
        } catch (RuntimeException e) {
            log.errorFormat("Failed to parse version of Host {0},{1} and Host OS '{2}' with error {3}",
                    vds.getId(),
                    vds.getVdsName(),
                    vds.getHostOs(),
                    ExceptionUtils.getMessage(e));
        }
        return vdsOsVersion;
    }

    /**
     * Checks if an ISO file is compatible for upgrading a given oVirt host
     *
     * @param ovirtOsVersion
     *            oVirt host version
     * @param isoVersion
     *            suggested ISO version for upgrade
     * @return true is version matches or if a any version isn't provided, else false.
     */
    public static boolean isIsoVersionCompatibleForUpgrade(RpmVersion ovirtOsVersion, RpmVersion isoVersion) {
        return (isoVersion.getMajor() == ovirtOsVersion.getMajor() &&
                ovirtOsVersion.getMinor() <= isoVersion.getMinor())
                || ovirtOsVersion.getMajor() == -1
                || isoVersion.getMajor() == -1;
    }

    /**
     * Handle the result of the VDS command, throwing an exception if one was thrown by the command or returning the
     * result otherwise.
     *
     * @param result
     *            The result of the command.
     * @return The result (if no exception was thrown).
     */
    public static VDSReturnValue handleVdsResult(VDSReturnValue result) {
        if (StringUtils.isNotEmpty(result.getExceptionString())) {
            VdcBLLException exp;
            if (result.getVdsError() != null) {
                exp = new VdcBLLException(result.getVdsError().getCode(), result.getExceptionString());
            } else {
                exp = new VdcBLLException(VdcBllErrors.ENGINE, result.getExceptionString());
            }
            throw exp;
        }
        return result;
    }
}
