#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Keycloak Utilities"""


from ovirt_engine import configfile

from ovirt_engine_setup.engine_common import constants as oengcommcons


def keycloak_env_from_engine_conf(content):
    """
    Parse content of 12-setup-keycloak.conf and return a dictionary
    that can be merged into the otopi environment for KeycloakEnv.

    Input: content of a 12-setup-keycloak.conf file, as a list/tuple
    of strings.

    Output: A dictionary with keys/values matching
    self.environment[KeycloakEnv] ones.

    In principle, this is the opposite of the code generating this file,
    in plugins/ovirt-engine-setup/ovirt-engine-keycloak/config/keycloak.py .

    The input is intentionally only the content and not a file, to make it
    useful also for a remote engine case.

    I only handle here keycloak-specific keys, and for now, only those I need
    for grafana. Specifically, not ENGINE_SSO_EXTERNAL_SSO_LOGOUT_URI, which
    refers to "${ENGINE_URI}", and not KEYCLOAK_OVIRT_ADMIN_USER_WITH_PROFILE,
    which is used by OVN. So it probably means that this isn't enough for
    configuring OVN via --reconfigure-optional-components.

    The result does not include ENABLE, which is not in the engine conf, but
    in the engine-setup conf, and should be checked beforehand.
    """
    res = {}
    config = configfile.ConfigFile()
    for line in content:
        # TODO: Expose _loadLine in ConfigFile and use that?
        # Some other interface?
        # Right now it only supports reading from files, which is not
        # very convenient/natural for a remote engine.
        config._loadLine(line)

    for e, k in (
        (
            oengcommcons.KeycloakEnv.KEYCLOAK_TOKEN_URL,
            'EXTERNAL_OIDC_TOKEN_END_POINT'
        ),
        (
            oengcommcons.KeycloakEnv.KEYCLOAK_USERINFO_URL,
            'EXTERNAL_OIDC_USER_INFO_END_POINT'
        ),
        (
            oengcommcons.KeycloakEnv.KEYCLOAK_OVIRT_INTERNAL_CLIENT_ID,
            'EXTERNAL_OIDC_CLIENT_ID'
        ),
        (
            oengcommcons.KeycloakEnv.KEYCLOAK_OVIRT_INTERNAL_CLIENT_SECRET,
            'EXTERNAL_OIDC_CLIENT_SECRET'
        ),
    ):
        res[e] = config.get(k)

    # The following are hard-coded, for now.
    # TODO: Somehow unite with the copy in ovirt-engine-keycloak constants.
    # Perhaps move to some common package that both can use.
    # Can't require it directly right now because it pulls the entire engine
    # with it, which we do not want to happen on a setup where grafana is
    # on a separate remote machine.
    for e, const in (
        (
            oengcommcons.KeycloakEnv.KEYCLOAK_GRAFANA_ADMIN_ROLE,
            'grafana-admin'
        ),
        (
            oengcommcons.KeycloakEnv.KEYCLOAK_GRAFANA_EDITOR_ROLE,
            'grafana-editor'
        ),
        (
            oengcommcons.KeycloakEnv.KEYCLOAK_GRAFANA_VIEWER_ROLE,
            'grafana-viewer'
        ),
    ):
        res[e] = const

    # Handle KEYCLOAK_AUTH_URL. This is not saved in the config as-is, so
    # need to be synthesized. In ovirt-engine-keycloak code, this is done
    # using a function called _build_endpoint_url which is used also for
    # other stuff, including the token, where the result is different only
    # in the final part after the last '/'. Let's rely on that for now.
    token_string_end = '/token'
    if res[
        oengcommcons.KeycloakEnv.KEYCLOAK_TOKEN_URL
    ].endswith(
        token_string_end
    ):
        res[
            oengcommcons.KeycloakEnv.KEYCLOAK_AUTH_URL
        ] = res[
            oengcommcons.KeycloakEnv.KEYCLOAK_TOKEN_URL
        ][:-len(token_string_end)] + '/auth'

    return res


# vim: expandtab tabstop=4 shiftwidth=4
