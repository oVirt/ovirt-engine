package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.SignStringParameters;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.engine.core.uutils.crypto.ticket.TicketEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignStringQuery<P extends SignStringParameters> extends QueriesCommandBase<P> {

    private static final Logger log = LoggerFactory.getLogger(SignStringQuery.class);

    public SignStringQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setSucceeded(false);

        try {
            getQueryReturnValue().setReturnValue(
                new TicketEncoder(
                    EngineEncryptionUtils.getPrivateKeyEntry().getCertificate(),
                    EngineEncryptionUtils.getPrivateKeyEntry().getPrivateKey(),
                    Config.<Integer> getValue (ConfigValues.WebSocketProxyTicketValiditySeconds)
                ).encode(getParameters().getString())
            );
            getQueryReturnValue().setSucceeded(true);
        } catch (Exception e) {
            log.error("Ticket encoding failed: {}", e.getMessage());
            log.debug("Exception", e);
        }
    }

}
