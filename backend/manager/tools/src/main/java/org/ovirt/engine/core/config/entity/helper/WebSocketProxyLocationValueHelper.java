package org.ovirt.engine.core.config.entity.helper;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.config.entity.ConfigKey;

public class WebSocketProxyLocationValueHelper extends StringValueHelper {

    @Override
    public ValidationResult validate(ConfigKey key, String value) {
        if (StringUtils.isBlank(value)) {
            return new ValidationResult(false, "The WebSocketProxy can't be empty.");
        }

        if ("Off".equals(value) || validHostPort(value)) {
            return new ValidationResult(true);
        }

        return new ValidationResult(false, "Correct values are: Off (proxy is not deployed), " +
                "Engine:<port> (Engine is reserved keyword meaning proxy is deployed on the same machine as " +
                "the engine (on given port)), Host:<port> (Host is reserved keyword meaning proxy is deployed " +
                "on each host on given port (if the deployment has more hosts, proxy must be deployed on each " +
                "of them)), <hostname>:<port> (proxy is deployed on a machine identified by given hostname or ip " +
                "and port).");
    }

    private boolean validHostPort(String value) {
        try {
            URI uri = new URI("foo://" + value);
            return 0 < uri.getPort() && uri.getPort() < 65636;
        } catch (URISyntaxException e) {
            return false;
        }
    }

}

