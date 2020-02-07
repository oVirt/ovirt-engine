package org.ovirt.engine.core.bll;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.branding.BrandingManager;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.SetVmTicketParameters;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.KubevirtProviderProperties;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.console.ConsoleOptions;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.ConfigureConsoleOptionsParams;
import org.ovirt.engine.core.common.queries.IdAndNameQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.vdsbroker.kubevirt.KubevirtUtils;

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

    static final String ENGINE_BASE_URL = "${engine_base_url}";

    static final String CONSOLE_CLIENT_RESOURCES_URL = "${console_client_resources_url}";

    public ConfigureConsoleOptionsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Inject
    private VdsDynamicDao vdsDynamicDao;

    @Inject
    private VdsStaticDao vdsStaticDao;

    @Inject
    private ClusterDao clusterDao;

    @Inject
    private ProviderDao providerDao;

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
        } else if (!getCachedVm().isQualifiedForConsoleConnect()) {
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
        try {
            executeCommandQueryUnchecked();
        } catch (TicketGenerationException ex) {
            handleTicketGenerationError(ex.getCommandResult());
        }
    }

    private void executeCommandQueryUnchecked() {
        ConsoleOptions options = getParameters().getOptions();
        fillCommonPart(options);

        // fill additional SPICE data
        if (options.getGraphicsType() == GraphicsType.SPICE) {
            fillSpice(options);
        }

        // fill additional VNC data
        if (options.getGraphicsType() == GraphicsType.VNC) {
            fillVnc(options);
        }

        if (getQueryReturnValue().getSucceeded()) {
            setReturnValue(options);
        }
    }

    private void fillCommonPart(ConsoleOptions options) {
        GraphicsInfo graphicsInfo = getCachedVm().getGraphicsInfos().get(options.getGraphicsType());

        if (getCachedVm().getOrigin() == OriginType.KUBEVIRT) {
            KubevirtUtils.updateConsoleOptions(options, getCachedVm(), getProvider());
        } else {
            options.setHost(determineHost());
            options.setPort(graphicsInfo.getPort());
        }
        options.setSmartcardEnabled(getCachedVm().isSmartcardEnabled());
        if (getParameters().isSetTicket()) {
            options.setTicket(generateTicket());
            if (isKernelFips()) {
                options.setUsername(ConfigureConsoleOptionsParams.VNC_USERNAME_PREFIX + getCachedVm().getId());
            }
        }
        options.setTitle(getCachedVm().getName());
        options.setToggleFullscreenHotKey(Config.getValue(ConfigValues.ConsoleToggleFullScreenKeys));
        options.setReleaseCursorHotKey(Config.getValue(ConfigValues.ConsoleReleaseCursorKeys));
        options.setRemapCtrlAltDelete(Config.getValue(ConfigValues.RemapCtrlAltDelDefault));
        options.setFullScreen(Config.getValue(ConfigValues.FullScreenWebadminDefault));

        fillRemoteViewerVersions(options);

        final String engineHost = EngineLocalConfig.getInstance().getHost()
                + ':'
                + (EngineLocalConfig.getInstance().isProxyEnabled()
                    ? EngineLocalConfig.getInstance().getProxyHttpsPort()
                    : EngineLocalConfig.getInstance().getHttpsPort());
        options.setOvirtHost(engineHost);
        final String ssoToken = getSessionDataContainer().getSsoAccessToken(getParameters().getSessionId());
        options.setSsoToken(ssoToken);
        options.setAdminConsole(!getParameters().isFiltered());

        final QueryReturnValue caCertificateReturnValue = getCACertificate();
        if (!caCertificateReturnValue.getSucceeded()) {
            getQueryReturnValue().setExceptionString("No CA found!");
            getQueryReturnValue().setSucceeded(false);
            return;
        }
        options.setTrustStore(caCertificateReturnValue.getReturnValue());

        options.setCustomHttpsCertificateUsed(
                !Objects.equals(
                        EngineLocalConfig.getInstance().getPKITrustStorePath(),
                        EngineLocalConfig.getInstance().getHttpsPKITrustStorePath()));
    }

    private void fillRemoteViewerVersions(ConsoleOptions options) {
        String remoteViewerSupportedVersions = Config.getValue(ConfigValues.RemoteViewerSupportedVersions);
        String remoteViewerNewerVersionUrl = Config.getValue(ConfigValues.RemoteViewerNewerVersionUrl);
        if (StringUtils.isEmpty(remoteViewerSupportedVersions) || StringUtils.isEmpty(remoteViewerNewerVersionUrl)) {
            return;
        }

        String engineBaseUrlString = calculateEngineBaseUrl(sanitizeUrl(getParameters().getEngineBaseUrl()));
        String consoleClientResourcesUrl = calculateResourcesUrl(sanitizeUrl(getParameters().getConsoleClientResourcesUrl()));

        options.setRemoteViewerSupportedVersions(remoteViewerSupportedVersions);

        fillRemoteViewerUrl(options, remoteViewerNewerVersionUrl, engineBaseUrlString, consoleClientResourcesUrl);
    }

    private String calculateResourcesUrl(String passedUrl) {
        if (!StringUtils.isEmpty(passedUrl)) {
            return passedUrl;
        }

        return sanitizeUrl(BrandingManager.getInstance().getMessage("obrand.common.console_client_resources_url"));
    }

    /**
     * If passed explicitly, just return it. If not, return the one calculated from ENGINE_FQDN and ENGINE_PROXY_HTTPS_PORT
     */
    private String calculateEngineBaseUrl(String passedUrl) {
        if (!StringUtils.isEmpty(passedUrl)) {
            return passedUrl;
        }

        try {
            return EngineLocalConfig.getInstance().getExternalHttpsBaseUrl("").toString();
        } catch (MalformedURLException e) {
            return "";
        }
    }

    private String sanitizeUrl(String url) {
        if (StringUtils.isEmpty(url)) {
            return "";
        }

        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }

        return url;
    }

    void fillRemoteViewerUrl(ConsoleOptions options, String remoteViewerNewerVersionUrl, String engineBaseUrlString, String consoleClientResourcesUrl) {
        URL engineBaseUrl = null;
        try {
            engineBaseUrl = new URL(engineBaseUrlString);
        } catch (MalformedURLException e) {
            log.warn("Malformed engineBaseUrl sent '{}'", engineBaseUrlString);
            engineBaseUrlString = null;
        }

        URI consoleClientResourcesUri = null;
        try {
            consoleClientResourcesUri = new URI(consoleClientResourcesUrl);
        } catch (URISyntaxException e) {
            log.warn("Malformed consoleClientResourcesUrl '{}'", consoleClientResourcesUrl);
            consoleClientResourcesUrl = null;
        }

        if (engineBaseUrlString != null) {
            remoteViewerNewerVersionUrl = StringUtils.replace(remoteViewerNewerVersionUrl, ENGINE_BASE_URL, engineBaseUrlString);
        }

        if (consoleClientResourcesUrl != null) {
            if (!consoleClientResourcesUri.isAbsolute() && engineBaseUrlString != null) {
                try {
                    consoleClientResourcesUrl = new URL(engineBaseUrl, consoleClientResourcesUrl).toString();
                } catch (MalformedURLException e) {
                    log.warn("Can not create a join engineBaseUrl '{}' with consoleClientResourcesUrl '{}'", engineBaseUrlString, consoleClientResourcesUrl);
                }
            } else if (!consoleClientResourcesUri.isAbsolute()) {
                log.warn("ConsoleClientResourcesUrl is relative but the engineBaseUrl is not set");
            }
        }

        if (consoleClientResourcesUrl != null) {
            remoteViewerNewerVersionUrl = StringUtils.replace(remoteViewerNewerVersionUrl, CONSOLE_CLIENT_RESOURCES_URL, consoleClientResourcesUrl);
        }

        options.setRemoteViewerNewerVersionUrl(remoteViewerNewerVersionUrl);
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
        if (graphicsInfo.getTlsPort() != null) {
            options.setSecurePort(graphicsInfo.getTlsPort());
        }

        if (Config.getValue(ConfigValues.SSLEnabled)) {
            String spiceSecureChannels = Config.getValue(
                ConfigValues.SpiceSecureChannels,
                cachedVm.getCompatibilityVersion().toString()
            );
            if (!StringUtils.isBlank(spiceSecureChannels)) {
                options.setSslChanels(spiceSecureChannels);
            }
            String cipherSuite = Config.getValue(ConfigValues.CipherSuite);
            if (!StringUtils.isBlank(cipherSuite)) {
                options.setCipherSuite(cipherSuite);
            }
        }

        options.setHostSubject(
                Config.getValue(ConfigValues.EnableSpiceRootCertificateValidation)
                ? getVdsCertificateSubject()
                : null);
        options.setSpiceProxy(determineSpiceProxy());
    }

    private void fillVnc(ConsoleOptions options) {
        final VdsDynamic host = getHost();
        boolean useSsl = host != null && host.isVncEncryptionEnabled();
        options.setUseSsl(useSsl);
        if (useSsl) {
            options.setSecurePort(options.getPort());
        }
    }

    protected VdsDynamic getHost() {
        return vdsDynamicDao.get(getCachedVm().getRunOnVds());
    }

    protected boolean isKernelFips() {
        return vdsStaticDao.get(getCachedVm().getRunOnVds()).isKernelCmdlineFips();
    }

    protected boolean isVncEncryptionEnabled() {
        return clusterDao.getByName(getCachedVm().getClusterName()).isVncEncryptionEnabled();
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

        ActionReturnValue result = backend.runAction(ActionType.SetVmTicket, parameters);

        if (!result.getSucceeded()) {
            throw new TicketGenerationException(result);
        }
        return result.getActionReturnValue();
    }

    private void handleTicketGenerationError(ActionReturnValue commandResult) {
        getQueryReturnValue().setSucceeded(false);
        if (commandResult.getValidationMessages().contains(
                EngineMessage.USER_CANNOT_FORCE_RECONNECT_TO_VM.name())) {
            getQueryReturnValue().setExceptionString(
                    EngineMessage.USER_CANNOT_FORCE_RECONNECT_TO_VM.name());
            return;
        }
        getQueryReturnValue().setExceptionString(EngineMessage.SETTING_VM_TICKET_FAILED.name());
    }

    protected String getVdsCertificateSubject() {
        return backend.runInternalQuery(
                QueryType.GetVdsCertificateSubjectByVmId,
                new IdQueryParameters(getCachedVm().getId())).getReturnValue();

    }

    private QueryReturnValue getCACertificate() {
        return backend.runInternalQuery(QueryType.GetCACertificate, new QueryParametersBase());
    }

    @SuppressWarnings("unchecked")
    private Provider<KubevirtProviderProperties> getProvider() {
        return (Provider<KubevirtProviderProperties>) providerDao.get(getCachedVm().getClusterId());
    }

    private String determineHost() {
        GraphicsInfo graphicsInfo = getCachedVm().getGraphicsInfos().get(getParameters().getOptions().getGraphicsType());
        String result = graphicsInfo.getIp();

        // if we don't have display ip, we try management network of host
        if (StringUtils.isBlank(result) || "0".equals(result)) {
            QueryReturnValue returnValue = backend.runInternalQuery(
                    QueryType.GetManagementInterfaceAddressByVmId,
                    new IdQueryParameters(getCachedVm().getId()));
            result = returnValue.getReturnValue();
        } else if (getParameters().getOptions().getGraphicsType() == GraphicsType.VNC
                   && (isKernelFips() || isVncEncryptionEnabled())) {
            // If VNC encyption is enabled (at cluster level or because of FIPS mode)
            // the console descriptor must contain host name,
            // to match TLS certificate for connection
            String hostName = getCachedVm().getRunOnVdsName();
            QueryReturnValue queryReturnValue = backend.runInternalQuery(QueryType.GetVdsByName,
                    new IdAndNameQueryParameters(getCachedVm().getClusterId(), hostName));
            VDS vds = queryReturnValue.getReturnValue();
            if (vds != null) {
                result = vds.getHostName();
            } else {
                log.warn("Could not find host's FQDN, falling back to IP");
            }
        }

        return result;
    }

    private String determineSpiceProxy() {
        if (StringUtils.isNotBlank(getCachedVm().getVmPoolSpiceProxy())) {
            return getCachedVm().getVmPoolSpiceProxy();
        }

        if (StringUtils.isNotBlank(getCachedVm().getClusterSpiceProxy())) {
            return getCachedVm().getClusterSpiceProxy();
        }

        String globalSpiceProxy = Config.getValue(ConfigValues.SpiceProxyDefault);
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
            cachedVm = backend.runInternalQuery(
                QueryType.GetVmByVmId,
                    params).getReturnValue();
        }

        return cachedVm;
    }

    private static class TicketGenerationException extends RuntimeException {

        private final ActionReturnValue commandResult;

        public TicketGenerationException(ActionReturnValue commandResult) {
            this.commandResult = commandResult;
        }

        public ActionReturnValue getCommandResult() {
            return commandResult;
        }
    }
}
