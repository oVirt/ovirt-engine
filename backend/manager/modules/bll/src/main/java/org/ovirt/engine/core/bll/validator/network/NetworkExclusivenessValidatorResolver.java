package org.ovirt.engine.core.bll.validator.network;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.compat.Version;

@Singleton
public class NetworkExclusivenessValidatorResolver {

    private final NetworkExclusivenessValidator legacyNetworkExclusivenessValidator;
    private final NetworkExclusivenessValidator vlanUntaggedNetworkExclusivenessValidator;

    @Inject
    NetworkExclusivenessValidatorResolver(
            @Named("legacyNetworkExclusivenessValidator")
            NetworkExclusivenessValidator legacyNetworkExclusivenessValidator,
            @Named("vlanUntaggedNetworkExclusivenessValidator")
            NetworkExclusivenessValidator vlanUntaggedNetworkExclusivenessValidator) {

        Objects.requireNonNull(legacyNetworkExclusivenessValidator,
                "legacyNetworkExclusivenessValidator cannot be null");
        Objects.requireNonNull(vlanUntaggedNetworkExclusivenessValidator,
                "vlanUntaggedNetworkExclusivenessValidator cannot be null");

        this.legacyNetworkExclusivenessValidator = legacyNetworkExclusivenessValidator;
        this.vlanUntaggedNetworkExclusivenessValidator = vlanUntaggedNetworkExclusivenessValidator;
    }

    public NetworkExclusivenessValidator resolveNetworkExclusivenessValidator(Set<Version> supportedVersions) {

        return isNetworkExclusivenessPermissiveValidationSupported(supportedVersions) ?
                vlanUntaggedNetworkExclusivenessValidator :
                legacyNetworkExclusivenessValidator;
    }

    private boolean isNetworkExclusivenessPermissiveValidationSupported(Set<Version> supportedVersions) {
        if (CollectionUtils.isEmpty(supportedVersions)) {
            return false;
        }

        final Version version = Collections.max(supportedVersions);

        return FeatureSupported.networkExclusivenessPermissiveValidation(version);
    }
}
