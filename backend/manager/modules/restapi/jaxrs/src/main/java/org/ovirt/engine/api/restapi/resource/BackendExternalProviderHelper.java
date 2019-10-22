/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.ExternalNetworkProviderConfiguration;
import org.ovirt.engine.api.model.ExternalNetworkProviderConfigurations;
import org.ovirt.engine.api.model.ExternalProvider;
import org.ovirt.engine.api.model.ExternalProviders;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

/**
 * A collection of functions useful for dealing with external providers.
 */
public class BackendExternalProviderHelper {
    /**
     * Finds the provider that corresponds to the given identifier.
     *
     * @param resource the resource that will be used to perform the required queries
     * @param id the identifier of the provider
     * @return the reference to the provider or {@code null} if no such provider exists
     */
    public static Provider getProvider(BackendResource resource, String id) {
        Guid guid = Guid.createGuidFromString(id);
        IdQueryParameters parameters = new IdQueryParameters(guid);
        return resource.getEntity(Provider.class, QueryType.GetProviderById, parameters, id, true);
    }

    /**
     * Completes the identifier that corresponds to the given name of the external network providers in the external
     * network provider configurations the provider. If no such provider exists, an exception will be thrown.
     *
     * @param resource the resource that will be used to perform the required queries
     * @param externalNetworkProviderConfigurations the external network provider configurations which contains the
     *                                              external network providers
     */
    public static void completeExternalNetworkProviderConfigurations(
            BackendResource resource,
            ExternalNetworkProviderConfigurations externalNetworkProviderConfigurations) {
        if (externalNetworkProviderConfigurations != null) {
            for (ExternalNetworkProviderConfiguration externalNetworkProviderConfiguration :
                    externalNetworkProviderConfigurations.getExternalNetworkProviderConfigurations()) {
                resource.validateParameters(externalNetworkProviderConfiguration,
                        "externalNetworkProvider.id|name");
                completeProviderId(resource, externalNetworkProviderConfiguration.getExternalNetworkProvider());
            }
        }
    }

    /**
     * Completes the identifier that corresponds to the given name of the external network providers. If no such
     * provider exists, an exception will be thrown.
     *
     * @param resource the resource that will be used to perform the required queries
     * @param externalProviders the external network providers container
     */
    public static void completeExternalProviders(BackendResource resource, ExternalProviders externalProviders) {
        if (externalProviders != null) {
            for (ExternalProvider externalProvider : externalProviders.getExternalProviders()) {
                completeProviderId(resource, externalProvider);
            }
        }
    }

    private static void completeProviderId(BackendResource resource, BaseResource provider) {
        resource.validateParameters(provider, "id|name");
        if (provider.isSetId()) {
            return;
        }
        provider.setId(getIdProviderByName(resource, provider.getName()).toString());
    }

    private static Guid getIdProviderByName(BackendResource resource, String name) {
        return resource.getEntity(Provider.class, QueryType.GetProviderByName, new NameQueryParameters(name),
                "Provider: name=" + name, true).getId();
    }
}
