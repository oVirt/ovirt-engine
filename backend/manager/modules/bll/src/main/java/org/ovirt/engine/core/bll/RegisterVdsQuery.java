package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AddVdsActionParameters;
import org.ovirt.engine.core.common.action.ApproveVdsParameters;
import org.ovirt.engine.core.common.action.UpdateVdsActionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsOperationActionParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.queries.RegisterVdsParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInMaintenanceMode
public class RegisterVdsQuery<P extends RegisterVdsParameters> extends QueriesCommandBase<P> {

    private AuditLogType error = AuditLogType.UNASSIGNED;
    private String strippedVdsUniqueId;
    private final AuditLogableBase logable;
    private List<VDS> _vdssByUniqueId;

    private static Object doubleRegistrationLock = new Object();
    /**
     * 'z' has the highest ascii value from the acceptable characters, so the bit set size should be initiated to it.
     * the size is 'z'+1 so that each char will be represented in the BitSet with index which equals to it's char value.
     */
    private static final BitSet validChars = new BitSet('z' + 1);

    static {
        // the set method sets the bits from the specified from index(inclusive) to the specified toIndex(exclusive) to
        // true so the toIndex should be incremented in 1 in order to include the char represented by that index in the
        // range.
        validChars.set('a', 'z' + 1);
        validChars.set('A', 'Z' + 1);
        validChars.set('0', '9' + 1);
        validChars.set('-');
        validChars.set('.');
        validChars.set(':');
        validChars.set('_');
    }

    public RegisterVdsQuery(P parameters) {
        super(parameters);
        logable = new AuditLogableBase(parameters.getVdsId());
    }

    protected String getStrippedVdsUniqueId() {
        if (strippedVdsUniqueId == null) {
            // since we use the management IP field, makes sense to remove the
            // illegal characters in advance
            StringBuilder builder = new StringBuilder();
            for (char ch : getParameters().getVdsUniqueId().toCharArray()) {
                if (validChars.get(ch)) {
                    builder.append(ch);
                }
            }
            strippedVdsUniqueId = builder.toString();
        }
        return strippedVdsUniqueId;
    }

    private List<VDS> getVdssByUniqueId() {
        if (_vdssByUniqueId == null) {
            _vdssByUniqueId = DbFacade.getInstance().getVdsDao().getAllWithUniqueId(getStrippedVdsUniqueId());
        }
        return _vdssByUniqueId;
    }

    @Override
    protected boolean validateInputs() {
        if (!super.validateInputs()) {
            return false;
        }

        VdcQueryReturnValue returnValue = getQueryReturnValue();
        returnValue.setExceptionString("");
        try {
            String hostName = getParameters().getVdsHostName();
            if (StringUtils.isEmpty(hostName)) {
                returnValue.setExceptionString("Cannot register Host - no Hostname address specified.");
                return false;
            }

            String vdsUniqueId = getParameters().getVdsUniqueId();
            if (StringUtils.isEmpty(vdsUniqueId)) {
                returnValue.setExceptionString(
                    String.format(
                        "Cannot register host '%1$s' - host id is empty.",
                        hostName
                    )
                );
                AuditLogableBase logable = new AuditLogableBase();
                logable.addCustomValue("VdsHostName", hostName);
                auditLogDirector.log(logable, AuditLogType.VDS_REGISTER_EMPTY_ID);
                return false;
            }

            List<VDS> vdssByUniqueId = getVdssByUniqueId();
            if (vdssByUniqueId.size() > 1) {
                returnValue.setExceptionString("Cannot register Host - unique id is ambigious.");
                return false;
            }
            if (vdssByUniqueId.size() == 1) {
                VDS vds = vdssByUniqueId.get(0);
                if (!VdsHandler.isPendingOvirt(vds)) {
                    returnValue.setExceptionString(VdcBllMessages.VDS_STATUS_NOT_VALID_FOR_UPDATE.name());
                    return false;
                }
            }

        } catch (RuntimeException ex) {
            log.error("Exception", ex);
            returnValue.setExceptionString(String.format("Cannot register Host - An exception has been thrown: %1$s",
                    ex.getMessage()));
            return false;
        }

        return true;
    }

    @Override
    protected void executeQueryCommand() {
        try {
            log.info("Running Command: RegisterVds");
            executeRegisterVdsCommand();
        } catch (RuntimeException ex) {
            log.error("RegisterVdsQuery::ExecuteWithoutTransaction: An exception has been thrown.", ex);
        } finally {
            writeToAuditLog();
        }
    }

    protected void executeRegisterVdsCommand() {
        synchronized (doubleRegistrationLock) {
            List<VDS> hostsByHostName = DbFacade.getInstance().getVdsDao().getAllForHostname(getParameters().getVdsName());
            VDS provisionedVds = hostsByHostName.size() != 0 ? hostsByHostName.get(0) : null;
            if (provisionedVds != null && provisionedVds.getStatus() != VDSStatus.InstallingOS) {
                // if not in InstallingOS status, this host is not provisioned.
                provisionedVds = null;
            }

            // force to reload vdss by unique ID used later on
            _vdssByUniqueId = null;
            VDS vdsByUniqueId = getVdssByUniqueId().size() != 0 ? getVdssByUniqueId().get(0) : null;

            // in case oVirt host was added for the second time - perform approval
            if (vdsByUniqueId != null && vdsByUniqueId.getStatus() == VDSStatus.PendingApproval) {
                getQueryReturnValue().setSucceeded(dispatchOvirtApprovalCommand(vdsByUniqueId.getId()));
                return;
            }

            log.debug("RegisterVdsQuery::ExecuteCommand - Entering");

            if (StringUtils.isEmpty(getParameters().getVdsName())) {
                getParameters().setVdsName(getParameters().getVdsUniqueId());
                log.debug("RegisterVdsQuery::ExecuteCommand - VdsName empty, using VdsUnique ID as name");
            }

            logable.addCustomValue("VdsName1", getParameters().getVdsName());

            Guid vdsGroupId;
            if (Guid.Empty.equals(getParameters().getVdsGroupId())) {
                vdsGroupId = Guid.createGuidFromStringDefaultEmpty(
                        Config.<String> getValue(ConfigValues.AutoRegistrationDefaultVdsGroupID));
                log.debug(
                        "RegisterVdsQuery::ExecuteCommand - VdsGroupId received as -1, using AutoRegistrationDefaultVdsGroupID: '{}'",
                        vdsGroupId);
            } else {
                vdsGroupId = getParameters().getVdsGroupId();
            }
            if (provisionedVds != null) {
                // In provision don't set host on pending - isPending = false
                getQueryReturnValue().setSucceeded(Register(provisionedVds, vdsGroupId, false));
            } else {
                // TODO: always add in pending state, and if auto approve call
                // approve command action after registration
                RefObject<Boolean> isPending = new RefObject<Boolean>(Boolean.FALSE);
                getQueryReturnValue().setSucceeded(
                        HandleOldVdssWithSameHostName(vdsByUniqueId) && HandleOldVdssWithSameName(vdsByUniqueId)
                                && CheckAutoApprovalDefinitions(isPending)
                                && Register(vdsByUniqueId, vdsGroupId, isPending.argvalue.booleanValue()));
            }
            log.debug("RegisterVdsQuery::ExecuteCommand - Leaving Succeded value is '{}'",
                    getQueryReturnValue().getSucceeded());
        }
    }

    private boolean dispatchOvirtApprovalCommand(Guid oVirtId) {
        boolean isApprovalDispatched = true;
        final ApproveVdsParameters params = new ApproveVdsParameters();
        params.setVdsId(oVirtId);
        params.setApprovedByRegister(true);

        try {
            ThreadPoolUtil.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        VdcReturnValueBase ret =
                            Backend.getInstance().runInternalAction(VdcActionType.ApproveVds, params);
                        if (ret == null || !ret.getSucceeded()) {
                            log.error("Approval of oVirt '{}' failed. ", params.getVdsId());
                        } else if (ret.getSucceeded()) {
                            log.info("Approval of oVirt '{}' ended successfully. ", params.getVdsId());
                        }
                    } catch (RuntimeException ex) {
                        log.error("Failed to Approve host", ex);
                    }
                }
            });
        } catch (Exception e) {
            isApprovalDispatched = false;
        }
        return isApprovalDispatched;
    }

    private boolean Register(VDS vds, Guid vdsGroupId, boolean IsPending) {
        boolean returnValue = true;
        log.debug("RegisterVdsQuery::Register - Entering");
        if (vds == null) {
            returnValue = registerNewHost(vdsGroupId, IsPending);
        } else {
            returnValue = updateExistingHost(vds, IsPending);
        }
        log.debug("RegisterVdsQuery::Register - Leaving with value {}", returnValue);

        return returnValue;
    }

    private boolean updateExistingHost(VDS vds, boolean pending) {
        boolean returnValue = true;
        vds.setHostName(vds.getHostName());
        vds.setPort(getParameters().getVdsPort());
        log.debug(
                "RegisterVdsQuery::Register - Will try now to update VDS with existing unique id; Name: '{}', Hostname: '{}', Unique: '{}', VdsPort: '{}', IsPending: '{}' with force synchronize",
                getParameters().getVdsHostName(),
                getStrippedVdsUniqueId(),
                getStrippedVdsUniqueId(),
                getParameters().getVdsPort(),
                pending);
        UpdateVdsActionParameters p = new UpdateVdsActionParameters(vds.getStaticData(), "", false);
        p.setInstallHost(!pending);
        p.setReinstallOrUpgrade(!pending);
        p.setAuthMethod(VdsOperationActionParameters.AuthenticationMethod.PublicKey);
        if (vds.isFenceAgentsExist()) {
            p.setFenceAgents(vds.getFenceAgents());
        }
        p.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
        VdcReturnValueBase rc = Backend.getInstance().runInternalAction(VdcActionType.UpdateVds, p);

        if (rc == null || !rc.getSucceeded()) {
            error = AuditLogType.VDS_REGISTER_EXISTING_VDS_UPDATE_FAILED;
            log.debug(
                    "RegisterVdsQuery::Register - Failed to update existing VDS Name: '{}', Hostname: '{}', Unique: '{}', VdsPort: '{}', IsPending: '{}'",
                    getParameters().getVdsHostName(),
                    getStrippedVdsUniqueId(),
                    getStrippedVdsUniqueId(),
                    getParameters().getVdsPort(),
                    pending);

            CaptureCommandErrorsToLogger(rc, "RegisterVdsQuery::Register");
            returnValue = false;
        } else {
            log.info(
                    "RegisterVdsQuery::Register - Updated a '{}' registered VDS - Name: '{}', Hostname: '{}', UniqueID: '{}'",
                    vds.getStatus() == VDSStatus.PendingApproval ? "Pending " : "",
                    getParameters().getVdsName(),
                    getParameters().getVdsHostName(),
                    getStrippedVdsUniqueId());
        }
        return returnValue;
    }

    private boolean registerNewHost(Guid vdsGroupId, boolean pending) {
        boolean returnValue = true;

        VdsStatic vds = new VdsStatic(getParameters().getVdsHostName(),
                    getStrippedVdsUniqueId(), getParameters().getVdsPort(),
                    getParameters().getSSHPort(),
                    getParameters().getSSHUser(),
                    vdsGroupId, Guid.Empty,
                    getParameters().getVdsName(), Config.<Boolean> getValue(ConfigValues.SSLEnabled),
                    VDSType.VDS, null);
        vds.setSshKeyFingerprint(getParameters().getSSHFingerprint());

                log.debug(
                        "RegisterVdsQuery::Register - Will try now to add VDS from scratch; Name: '{}', Hostname: '{}', Unique: '{}', VdsPort: '{}',Subnet mask: '{}', IsPending: '{}' with force synchronize",
                        getParameters().getVdsName(),
                        getParameters().getVdsHostName(),
                        getStrippedVdsUniqueId(),
                        getParameters().getVdsPort(),
                        pending);

            AddVdsActionParameters p = new AddVdsActionParameters(vds, "");
            p.setPending(pending);

            VdcReturnValueBase ret = Backend.getInstance().runInternalAction(VdcActionType.AddVds, p);

            if (ret == null || !ret.getSucceeded()) {
                log.error(
                        "RegisterVdsQuery::Register - Registration failed for VDS - Name: '{}', Hostname: '{}', UniqueID: '{}', Subnet mask: '{}'",
                        getParameters().getVdsName(),
                        getParameters().getVdsHostName(),
                        getStrippedVdsUniqueId());
                CaptureCommandErrorsToLogger(ret, "RegisterVdsQuery::Register");
                error = AuditLogType.VDS_REGISTER_FAILED;
                returnValue = false;
            } else {
                log.info(
                        "RegisterVdsQuery::Register - Registered a new VDS '{}' - Name: '{}', Hostname: '{}', UniqueID: '{}'",
                        pending ? "pending approval" : "automatically approved",
                        getParameters().getVdsName(),
                        getParameters().getVdsHostName(),
                        getStrippedVdsUniqueId());
            }
        return returnValue;
    }

    private boolean HandleOldVdssWithSameHostName(VDS vdsByUniqueId) {
        // handle old VDSs with same host_name (IP)
        log.debug("RegisterVdsQuery::HandleOldVdssWithSameHostName - Entering");

        boolean returnValue = true;
        List<VDS> vdss_byHostName = DbFacade.getInstance().getVdsDao().getAllForHostname(
                getParameters().getVdsHostName());
        int lastIteratedIndex = 1;
        if (vdss_byHostName.size() > 0) {
            log.debug(
                    "RegisterVdsQuery::HandleOldVdssWithSameHostName - found '{}' VDS(s) with the same host name '{}'.  Will try to change their hostname to a different value",
                    vdss_byHostName.size(),
                    getParameters().getVdsHostName());

            for (VDS vds_byHostName : vdss_byHostName) {
                /**
                 * looping foreach VDS found with similar hostnames and change to each one to available hostname
                 */
                if (
                    vdsByUniqueId == null ||
                    !vds_byHostName.getId().equals(vdsByUniqueId.getId())
                ) {
                    boolean unique = false;
                    String try_host_name = "";
                    for (int i = lastIteratedIndex; i <= 100; i++, lastIteratedIndex = i) {
                        try_host_name = String.format("hostname-was-%1$s-%2$s", getParameters()
                                .getVdsHostName(), i);
                        if (DbFacade.getInstance().getVdsDao().getAllForHostname(try_host_name).size() == 0) {
                            unique = true;
                            break;
                        }
                    }
                    if (unique) {
                        String old_host_name = vds_byHostName.getHostName();
                        vds_byHostName.setHostName(try_host_name);
                        UpdateVdsActionParameters parameters = new UpdateVdsActionParameters(
                                vds_byHostName.getStaticData(), "" , false);
                        parameters.setShouldBeLogged(false);
                        parameters.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
                        if (vds_byHostName.isFenceAgentsExist()) {
                            parameters.setFenceAgents(vds_byHostName.getFenceAgents());
                        }

                        // If host exists in InstallingOs status, remove it from DB and move on
                        final VDS foundVds = DbFacade.getInstance().getVdsDao().getByName(parameters.getVdsStaticData().getName());
                        if ((foundVds != null) && (foundVds.getDynamicData().getStatus() == VDSStatus.InstallingOS)) {
                            TransactionSupport.executeInScope(TransactionScopeOption.Required, new TransactionMethod<Void>() {
                                @Override
                                public Void runInTransaction() {
                                    getDbFacade().getVdsStatisticsDao().remove(foundVds.getId());
                                    getDbFacade().getVdsDynamicDao().remove(foundVds.getId());
                                    getDbFacade().getVdsStaticDao().remove(foundVds.getId());
                                    return null;
                                }
                            });
                        }

                        VdcReturnValueBase ret = Backend.getInstance().runInternalAction(VdcActionType.UpdateVds, parameters);

                        if (ret == null || !ret.getSucceeded()) {
                            error = AuditLogType.VDS_REGISTER_ERROR_UPDATING_HOST;
                            logable.addCustomValue("VdsName2", vds_byHostName.getStaticData().getName());
                            log.error(
                                    "RegisterVdsQuery::HandleOldVdssWithSameHostName - could not update VDS '{}'",
                                    vds_byHostName.getStaticData().getName());
                            CaptureCommandErrorsToLogger(ret,
                                    "RegisterVdsQuery::HandleOldVdssWithSameHostName");
                            return false;
                        } else {
                            log.info(
                                    "RegisterVdsQuery::HandleOldVdssWithSameHostName - Another VDS was using this IP '{}'. Changed to '{}'",
                                    old_host_name,
                                    try_host_name);
                        }
                    } else {
                        log.error(
                                "VdcBLL::HandleOldVdssWithSameHostName - Could not change the IP for an existing VDS. All available hostnames are taken (ID = '{}', name = '{}', management IP = '{}' , host name = '{}')",
                                vds_byHostName.getId(),
                                vds_byHostName.getName(),
                                vds_byHostName.getFenceAgents().isEmpty() ? "" : vds_byHostName.getFenceAgents()
                                        .get(0)
                                        .getIp(),
                                vds_byHostName.getHostName());
                        error = AuditLogType.VDS_REGISTER_ERROR_UPDATING_HOST_ALL_TAKEN;
                        returnValue = false;
                    }
                }
                log.info(
                        "RegisterVdsQuery::HandleOldVdssWithSameHostName - No Change required for VDS '{}'. Since it has the same unique Id",
                        vds_byHostName.getId());
            }
        }
        log.debug("RegisterVdsQuery::HandleOldVdssWithSameHostName - Leaving with value '{}'", returnValue);

        return returnValue;
    }

    /**
     * Check if another host has the same name as hostToRegister and if yes append a number to it. Eventually if the
     * host is in the db, persist the changes.
     * @param hostToRegister
     * @return
     */
    private boolean HandleOldVdssWithSameName(VDS hostToRegister) {
        log.debug("Entering");
        boolean returnValue = true;
        VdsDAO vdsDAO = DbFacade.getInstance().getVdsDao();
        VDS storedHost = vdsDAO.getByName(getParameters().getVdsName());
        List<String> allHostNames = getAllHostNames(vdsDAO.getAll());
        boolean hostExistInDB = hostToRegister != null;

        if (storedHost != null) {
            log.debug(
                    "found VDS with the same name {0}.  Will try to register with a new name",
                    getParameters().getVdsName());

            String nameToRegister = getParameters().getVdsName();
            String uniqueIdToRegister = getParameters().getVdsUniqueId();
            String newName;

            // check different uniqueIds but same name
            if (!uniqueIdToRegister.equals(storedHost.getUniqueId())
                    && nameToRegister.equals(storedHost.getName())) {
                if (hostExistInDB) {
                    // update the registered host if exist in db
                    allHostNames.remove(hostToRegister.getName());
                    newName = generateUniqueName(nameToRegister, allHostNames);
                    hostToRegister.setVdsName(newName);
                    UpdateVdsActionParameters parameters =
                            new UpdateVdsActionParameters(hostToRegister.getStaticData(), "", false);
                    if (hostToRegister.isFenceAgentsExist()) {
                        parameters.setFenceAgents(hostToRegister.getFenceAgents());
                    }
                    VdcReturnValueBase ret = Backend.getInstance().runInternalAction(VdcActionType.UpdateVds, parameters);
                    if (ret == null || !ret.getSucceeded()) {
                        error = AuditLogType.VDS_REGISTER_ERROR_UPDATING_NAME;
                        logable.addCustomValue("VdsName2", newName);
                        log.error("could not update VDS '{}'", nameToRegister);
                        CaptureCommandErrorsToLogger(ret, "RegisterVdsQuery::HandleOldVdssWithSameName");
                        return false;
                    } else {
                        log.info(
                                "Another VDS was using this name with IP '{}'. Changed to '{}'",
                                nameToRegister,
                                newName);
                    }
                } else {
                    // host doesn't exist in db yet. not persisting changes just object values.
                    newName = generateUniqueName(nameToRegister, allHostNames);
                    getParameters().setVdsName(newName);
                }
            }
        }
        log.debug("Leaving with value '{}'", returnValue);
        return returnValue;
    }

    private List<String> getAllHostNames(List<VDS> allHosts) {
        List<String> allHostNames = new ArrayList<String>(allHosts.size());
        for (VDS vds : allHosts) {
            allHostNames.add(vds.getName());
        }
        return allHostNames;
    }

    private String generateUniqueName(String val, List<String> allHostNames) {
        int i = 2;
        boolean postfixed = false;
        StringBuilder sb = new StringBuilder(val);
        while (allHostNames.contains(val)) {
            if (!postfixed) {
                val = sb.append("-").append(i).toString();
                postfixed = true;
            } else {
                val = sb.replace(sb.lastIndexOf("-"), sb.length(), "-").append(i).toString();
            }
            i++;
        }
        return val;
    }

    private boolean CheckAutoApprovalDefinitions(RefObject<Boolean> isPending) {
        // check auto approval definitions
        log.debug("RegisterVdsQuery::CheckAutoApprovalDefinitions - Entering");

        isPending.argvalue = true;
        if (!Config.<String> getValue(ConfigValues.AutoApprovePatterns).equals("")) {
            for (String pattern : Config.<String> getValue(ConfigValues.AutoApprovePatterns)
                    .split("[,]", -1)) {
                try {
                    String pattern_helper = pattern.toLowerCase();
                    Regex pattern_regex = new Regex(pattern_helper);
                    String vds_hostname_helper = getParameters().getVdsHostName().toLowerCase();
                    String vds_unique_id_helper = getParameters().getVdsUniqueId().toLowerCase()
                            .replace(":", "-");
                    if (vds_hostname_helper.startsWith(pattern) || vds_unique_id_helper.startsWith(pattern)
                            || pattern_regex.IsMatch(vds_hostname_helper)
                            || pattern_regex.IsMatch(vds_unique_id_helper)) {
                        isPending.argvalue = false;
                        break;
                    }
                } catch (RuntimeException ex) {
                    error = AuditLogType.VDS_REGISTER_AUTO_APPROVE_PATTERN;
                    log.error(
                            "RegisterVdsQuery ::CheckAutoApprovalDefinitions(out bool) -  Error in auto approve pattern: '{}'-'{}'",
                            pattern,
                            ex.getMessage());
                    return false;
                }
            }
        }
        log.debug("RegisterVdsQuery::CheckAutoApprovalDefinitions - Leaving - return value '{}'",
                    isPending.argvalue);
        return true;
    }

    private void CaptureCommandErrorsToLogger(VdcReturnValueBase retValue, String prefixToMessage) {
        if (retValue.getFault() != null) {
            log.error("{} - Fault - {}", prefixToMessage, retValue.getFault().getMessage());
        }
        if (retValue.getCanDoActionMessages().size() > 0) {
            List<String> msgs = retValue.getCanDoActionMessages();
            for (String s : msgs) {
                log.error("{} - CanDoAction Fault - {}", prefixToMessage, s);
            }
        }
        if (retValue.getExecuteFailedMessages().size() > 0) {
            // List<string> msgs =
            // ErrorTranslator.TranslateErrorText(retValue.ExecuteFailedMessages);
            for (String s : retValue.getExecuteFailedMessages()) {
                log.error("{} - Ececution Fault - {}", prefixToMessage, s);
            }
        }
    }

    private void writeToAuditLog() {
        try {
            auditLogDirector.log(logable, getAuditLogTypeValue());
        } catch (RuntimeException ex) {
            log.error("RegisterVdsQuery::WriteToAuditLog: An exception has been thrown.", ex);
        }
    }

    protected AuditLogType getAuditLogTypeValue() {
        return getQueryReturnValue().getSucceeded() ? AuditLogType.VDS_REGISTER_SUCCEEDED : error;
    }

}
