package org.ovirt.engine.core.bll.validator.network;

import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class NetworkExclusivenessValidatorResolver {

    private final NetworkExclusivenessValidator vlanUntaggedNetworkExclusivenessValidator;

    @Inject
    NetworkExclusivenessValidatorResolver(
            @Named("vlanUntaggedNetworkExclusivenessValidator")
            NetworkExclusivenessValidator vlanUntaggedNetworkExclusivenessValidator) {

        Objects.requireNonNull(vlanUntaggedNetworkExclusivenessValidator,
                "vlanUntaggedNetworkExclusivenessValidator cannot be null");

        this.vlanUntaggedNetworkExclusivenessValidator = vlanUntaggedNetworkExclusivenessValidator;
    }

    public NetworkExclusivenessValidator resolveNetworkExclusivenessValidator() {
        return vlanUntaggedNetworkExclusivenessValidator;
    }
}
