package org.ovirt.engine.core.bll;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.backendinterfaces.BaseHandler;
import org.ovirt.engine.core.common.businessentities.EditableField;
import org.ovirt.engine.core.common.businessentities.EditableOnVdsStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.utils.ObjectIdentityChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VdsHandler extends BaseHandler {
    private static final Logger log = LoggerFactory.getLogger(VdsHandler.class);
    private static ObjectIdentityChecker updateVdsStatic;

    /**
     * Initialize static list containers, for identity and permission check. The initialization should be executed
     * before calling ObjectIdentityChecker.
     *
     * @see Backend#InitHandlers
     */
    public static void init() {
        Class<?>[] inspectedClasses = new Class<?>[] { VDS.class, VdsStatic.class, VdsDynamic.class };
        updateVdsStatic =
                new ObjectIdentityChecker(VdsHandler.class, Arrays.asList(inspectedClasses));


        for (Pair<EditableField, Field> pair : extractAnnotatedFields(EditableField.class, inspectedClasses)) {
            updateVdsStatic.addPermittedFields(pair.getSecond().getName());
        }

        for (Pair<EditableOnVdsStatus, Field> pair : extractAnnotatedFields(EditableOnVdsStatus.class, inspectedClasses)) {
            updateVdsStatic.addField(Arrays.asList(pair.getFirst().statuses()), pair.getSecond().getName());
        }
    }

    public VdsHandler() {
        updateVdsStatic.setContainer(this);
    }

    public static boolean isUpdateValid(VdsStatic source, VdsStatic distination, VDSStatus status) {

        return updateVdsStatic.isUpdateValid(source, distination, status);
    }

    public static boolean isFieldsUpdated(VdsStatic source, VdsStatic destination, Iterable<String> list) {
        return updateVdsStatic.isFieldsUpdated(source, destination, list);
    }

    public static boolean isPendingOvirt(VDS vds) {
        return vds.isOvirtVintageNode() && vds.getStatus() == VDSStatus.PendingApproval;
    }

    /**
     * Extracts the oVirt OS version from raw material of {@code VDS.getHostOs()} field.
     *
     * @param vds
     *            the ovirt host which its OS version in a format of: [OS Name - OS Version - OS release]
     * @return a version class of the oVirt OS version, or null if failed to parse.
     */
    public static RpmVersion getOvirtHostOsVersion(VDS vds) {
        try {
            return new RpmVersion(vds.getHostOs(), "RHEV Hypervisor -", true);
        } catch (RuntimeException e) {
            log.error("Failed to parse version of Host '{}','{}' and Host OS '{}': {}",
                    vds.getId(),
                    vds.getName(),
                    vds.getHostOs(),
                    e.getMessage());
            log.debug("Exception", e);
        }
        return null;
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
            throw new EngineException(
                    result.getVdsError() != null ? result.getVdsError().getCode() : EngineError.ENGINE,
                    result.getExceptionString(), result);
        }
        return result;
    }
}
