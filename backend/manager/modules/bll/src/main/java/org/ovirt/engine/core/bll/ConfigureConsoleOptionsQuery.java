package org.ovirt.engine.core.bll;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.action.SetVmTicketParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.console.ConsoleOptions;
import org.ovirt.engine.core.common.queries.ConfigureConsoleOptionsParams;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;

/**
 * Query for filling required backend data to given ConsoleOptions.
 * Clients (frontend and restapi) use this query before initiating console session.
 *
 * @param <P> ConsoleOptions instance filled with 2 required parameters
 *           - vmId - id of VM for which options are configured
 *           - graphicsType - protocol for which options are configured
 */
public class ConfigureConsoleOptionsQuery<P extends ConfigureConsoleOptionsParams> extends QueriesCommandBase<P> {

    private VM cachedVm;

    public ConfigureConsoleOptionsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected boolean validateInputs() {
        if (!super.validateInputs()) {
            return false;
        }

        ConsoleOptions options = getParameters().getOptions();
        if (options == null) {
            getQueryReturnValue().setExceptionString("Console options can't be null.");
            return false;
        }

        if (options.getGraphicsType() == null) {
            getQueryReturnValue().setExceptionString("Graphics type must be filled in console options.");
            return false;
        }

        if (options.getVmId() == null) {
            getQueryReturnValue().setExceptionString("VM id must be filled in console options.");
            return false;
        }

        return validateVm();
    }

    private boolean validateVm() {
        if (getCachedVm() == null) {
            getQueryReturnValue().setExceptionString(String.format("Can't find VM with id %s",
                    getParameters().getOptions().getVmId()));
            return false;
        } else if (!getCachedVm().isRunning()) {
            getQueryReturnValue().setExceptionString(String.format("Vm %s is not running.", getCachedVm().getName()));
            return false;
        } else {
            GraphicsType graphicsType = getParameters().getOptions().getGraphicsType();
            if (!getCachedVm().getGraphicsInfos().containsKey(graphicsType)) {
                getQueryReturnValue().setExceptionString(String.format("Vm %s doesn't have %s console.",
                        getCachedVm().getName(), graphicsType));
                return false;
            } else {
                return true;
            }
        }
    }

    @Override
    protected void executeQueryCommand() {
        ConsoleOptions options = getParameters().getOptions();

        fillCommonPart(options);

        // fill additional SPICE data
        if (options.getGraphicsType() == GraphicsType.SPICE) {
            fillSpice(options);
        }

        if (getQueryReturnValue().getSucceeded()) {
            setReturnValue(options);
        }
    }

    private void fillCommonPart(ConsoleOptions options) {
        GraphicsInfo graphicsInfo = getCachedVm().getGraphicsInfos().get(options.getGraphicsType());

        options.setHost(determineHost());
        options.setPort(graphicsInfo.getPort());
        options.setSmartcardEnabled(getCachedVm().isSmartcardEnabled());
        if (getParameters().isSetTicket()) {
            options.setTicket(generateTicket());
        }
        options.setToggleFullscreenHotKey((String) getConfigValue(ConfigValues.ConsoleToggleFullScreenKeys));
        options.setReleaseCursorHotKey((String) getConfigValue(ConfigValues.ConsoleReleaseCursorKeys));
        options.setRemapCtrlAltDelete((Boolean) getConfigValue(ConfigValues.RemapCtrlAltDelDefault));
    }

    /**
     * Fills SPICE specific data to options.
     *
     * If SPICE root certificate validation is enabled but the root certificate cannot be retrieved,
     * the query fails (succeeded flag is set to false).
     *
     * @param options to be filled
     */
    private void fillSpice(ConsoleOptions options) {
        GraphicsInfo graphicsInfo = getCachedVm().getGraphicsInfos().get(options.getGraphicsType());

        options.setSmartcardEnabled(getCachedVm().isSmartcardEnabled());
        options.setNumberOfMonitors(getCachedVm().getNumOfMonitors());
        options.setGuestHostName(getCachedVm().getVmHost().split("[ ]", -1)[0]);
        if (graphicsInfo.getTlsPort() != null) {
            options.setSecurePort(graphicsInfo.getTlsPort());
        }

        if (getConfigValue(ConfigValues.SSLEnabled)) {
            String spiceSecureChannels = getConfigValue(ConfigValues.SpiceSecureChannels);
            if (!StringUtils.isBlank(spiceSecureChannels)) {
                options.setSslChanels(spiceSecureChannels);
            }
            String cipherSuite = getConfigValue(ConfigValues.CipherSuite);
            if (!StringUtils.isBlank(cipherSuite)) {
                options.setCipherSuite(cipherSuite);
            }
        }

        String certificateSubject = "";
        String caCertificate = "";

        if (getConfigValue(ConfigValues.EnableSpiceRootCertificateValidation)) {
            VdcQueryReturnValue certificate = getCACertificate();
            if (!certificate.getSucceeded()) {
                getQueryReturnValue().setExceptionString("Spice Root Certificate Validation enforced, but no CA found!");
                getQueryReturnValue().setSucceeded(false);
                return;
            }
            certificateSubject = getVdsCertificateSubject();
            caCertificate = certificate.getReturnValue();
        }
        options.setHostSubject(certificateSubject);
        options.setTrustStore(caCertificate);

        options.setSpiceProxy(determineSpiceProxy());

        // Update 'UsbListenPort' value
        boolean getIsUsbEnabled = getConfigValue(ConfigValues.EnableUSBAsDefault);
        options.setUsbListenPort(getIsUsbEnabled && getCachedVm().getUsbPolicy() == UsbPolicy.ENABLED_LEGACY
                ? ConsoleOptions.SPICE_USB_DEFAULT_PORT
                : ConsoleOptions.SET_SPICE_DISABLE_USB_LISTEN_PORT);
    }

    private String generateTicket() {
        SetVmTicketParameters parameters = new SetVmTicketParameters(
                getParameters().getOptions().getVmId(),
                null,
                ConsoleOptions.TICKET_VALIDITY_SECONDS,
                getParameters().getOptions().getGraphicsType());
        // we need these two params because SetVmTicket needs to know current user
        parameters.setSessionId(getEngineContext().getSessionId());
        parameters.setParametersCurrentUser(getUser());

        VdcReturnValueBase result = getBackend().runInternalAction(VdcActionType.SetVmTicket, parameters);

        if (result.getSucceeded()) {
            return result.getActionReturnValue();
        }

        return null;
    }

    private String getVdsCertificateSubject() {
        return getBackend().runInternalQuery(
                VdcQueryType.GetVdsCertificateSubjectByVmId,
                new IdQueryParameters(getCachedVm().getId())).getReturnValue();

    }

    private VdcQueryReturnValue getCACertificate() {
        return getBackend().runInternalQuery(VdcQueryType.GetCACertificate, new VdcQueryParametersBase());
    }

    private String determineHost() {
        GraphicsInfo graphicsInfo = getCachedVm().getGraphicsInfos().get(getParameters().getOptions().getGraphicsType());
        String result = graphicsInfo.getIp();

        // if we don't have display ip, we try management network of host
        if (StringUtils.isBlank(result) || "0".equals(result)) {
            VdcQueryReturnValue returnValue = getBackend().runInternalQuery(
                    VdcQueryType.GetManagementInterfaceAddressByVmId,
                    new IdQueryParameters(getCachedVm().getId()));
            result = returnValue.getReturnValue();
        }

        return result;
    }

    private String determineSpiceProxy() {
        if (!StringUtils.isNotBlank(getCachedVm().getVmPoolSpiceProxy())) {
            return getCachedVm().getVmPoolSpiceProxy();
        }

        if (!StringUtils.isNotBlank(getCachedVm().getVdsGroupSpiceProxy())) {
            return getCachedVm().getVdsGroupSpiceProxy();
        }

        String globalSpiceProxy = getConfigValue(ConfigValues.SpiceProxyDefault);
        if (StringUtils.isNotBlank(globalSpiceProxy)) {
            return globalSpiceProxy;
        }

        return null;
    }

    VM getCachedVm() {
        if (cachedVm == null) {
            IdQueryParameters params = new IdQueryParameters(getParameters().getOptions().getVmId());
            params.setFiltered(getParameters().isFiltered());
            params.setSessionId(getParameters().getSessionId());
            cachedVm = getBackend().runInternalQuery(
                VdcQueryType.GetVmByVmId,
                    params).getReturnValue();
        }

        return cachedVm;
    }

   <T> T getConfigValue(ConfigValues value) {
        return Config.getValue(value);
    }
}
