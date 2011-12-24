package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.backendinterfaces.BaseHandler;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;

public class VdsHandler extends BaseHandler {
    private static LogCompat log = LogFactoryCompat.getLog(VdsHandler.class);
    public static ObjectIdentityChecker mUpdateVdsStatic;

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
        mUpdateVdsStatic.AddPermittedField("vds_name");
        mUpdateVdsStatic.AddPermittedField("managmentIp");
        mUpdateVdsStatic.AddPermittedField("pm_type");
        mUpdateVdsStatic.AddPermittedField("pm_user");
        mUpdateVdsStatic.AddPermittedField("pm_password");
        mUpdateVdsStatic.AddPermittedField("pm_port");
        mUpdateVdsStatic.AddPermittedField("pm_options");
        mUpdateVdsStatic.AddPermittedField("pm_enabled");
        mUpdateVdsStatic.AddPermittedField("PmOptionsMap");
        mUpdateVdsStatic.AddPermittedField("vdsSpmPriority");
        mUpdateVdsStatic.AddFields(
                java.util.Arrays.asList(new Enum[] { VDSStatus.NonResponsive, VDSStatus.Maintenance, VDSStatus.Down,
                        VDSStatus.Unassigned, VDSStatus.InstallFailed, VDSStatus.PendingApproval }),
                java.util.Arrays.asList(new String[] { "ip", "vds_unique_id", "host_name", "port", "vds_group_id",
                        "otpValidity" }));
    }

    public VdsHandler() {
        mUpdateVdsStatic.setContainer(this);
    }

    public static boolean IsUpdateValid(VdsStatic source, VdsStatic distination, VDSStatus status) {

        return mUpdateVdsStatic.IsUpdateValid(source, distination, status);
    }

    public static boolean IsFieldsUpdated(VdsStatic source, VdsStatic distination, Iterable<String> list) {
        return mUpdateVdsStatic.IsFieldsUpdated(source, distination, list);
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
        VdsStatic vds = DbFacade.getInstance().getVdsStaticDAO().get(vdsName);
        return (vds != null);
    }

    public static boolean isVdsWithSameHostExistStatic(String hostName) {
        List<VdsStatic> vds = DbFacade.getInstance().getVdsStaticDAO().getAllForHost(hostName);
        return (vds.size() != 0);
    }

    public static boolean isVdsWithSameIpExistsStatic(String ipAddress) {
        List<VdsStatic> vds = DbFacade.getInstance().getVdsStaticDAO().getAllWithIpAddress(ipAddress);
        return (vds.size() != 0);
    }

    public static boolean isVdsExist(VdsStatic vdsStatic, java.util.ArrayList<String> messages) {
        boolean exist = false;
        if (isVdsWithSameNameExistStatic(vdsStatic.getvds_name())) {
            messages.add(VdcBllMessages.VDS_TRY_CREATE_WITH_EXISTING_PARAMS.toString());
            exist = true;
        } else if (isVdsWithSameHostExistStatic(vdsStatic.gethost_name())) {
            messages.add(VdcBllMessages.ACTION_TYPE_FAILED_VDS_WITH_SAME_HOST_EXIST.toString());
            exist = true;
        }
        return exist;
    }

    /**
     * Verify if an existing entity of oVirt type shares same unique-id as queried host. If verification detects a
     * violation, e.g existing host with same name for other host or address already associated with a host differ from
     * the host which shares the same unique-id, adds an error message.
     *
     * @param vdsStatic
     *            the oVirt host to query
     * @param messages
     *            a list which should be updated with violations
     * @param oVirtId
     *            the id which represents the existed oVirt host
     * @return
     */
    public static boolean isVdsExistForPendingOvirt(VdsStatic vdsStatic, ArrayList<String> messages, Guid oVirtId) {
        boolean exists = false;

        VdsStatic existVds = DbFacade.getInstance().getVdsStaticDAO().get(vdsStatic.getvds_name());
        if (existVds != null && !oVirtId.equals(existVds.getId())) {
            VdsDynamic vdsDynamic = DbFacade.getInstance().getVdsDynamicDAO().get(existVds.getId());
            if (vdsDynamic != null && !isPendingOvirt(existVds.getvds_type(), vdsDynamic.getstatus())) {
                messages.add(VdcBllMessages.VDS_TRY_CREATE_WITH_EXISTING_PARAMS.toString());
                exists = true;
            }
        }

        if (!exists) {
            List<VDS> vdsList = DbFacade.getInstance().getVdsDAO().getAllForHostname(vdsStatic.gethost_name());
            for (VDS vds : vdsList) {
                if (!isPendingOvirt(vds) || (!oVirtId.equals(vds.getvds_id()))) {
                    messages.add(VdcBllMessages.ACTION_TYPE_FAILED_VDS_WITH_SAME_HOST_EXIST.toString());
                    exists = true;
                    break;
                }
            }
        }

        return exists;
    }

    static private boolean isPendingOvirt(VDSType type, VDSStatus status) {
        return type == VDSType.oVirtNode && status == VDSStatus.PendingApproval;
    }

    static public boolean isPendingOvirt(VDS vds) {
        return isPendingOvirt(vds.getvds_type(), vds.getstatus());
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
            vdsOsVersion = new RpmVersion(vds.gethost_os(), "RHEV Hypervisor -", true);
        } catch (RuntimeException e) {
            log.errorFormat("Failed to parse version of Host {0},{1} and Host OS '{2}' with error {3}",
                    vds.getvds_id(),
                    vds.getvds_name(),
                    vds.gethost_os(),
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
        return isoVersion.getMajor() == ovirtOsVersion.getMajor()
                || ovirtOsVersion.getMajor() == -1
                || isoVersion.getMajor() == -1;
    }

}
