# -*- coding: utf-8 -*-
"""Central plugin to rotate md5 PostgreSQL role password verifiers to SCRAM.

Runs after component provisioning MISC stages. Skips remote DBs and roles
without plaintext passwords in the environment.
"""

import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine_common import constants as oengcommcons
from ovirt_engine_setup.engine_common import scram_upgrade
from ovirt_engine_setup.engine_common.scram_upgrade import RotationResult

# Not all components may be installed, import defensively
try:
    from ovirt_engine_setup.dwh import constants as odwhcons
except ImportError:
    odwhcons = None
try:
    from ovirt_engine_setup.grafana_dwh import constants as ogdwhcons
except ImportError:
    ogdwhcons = None
try:
    from ovirt_engine_setup.keycloak import constants as okccons
except ImportError:
    okccons = None
try:
    from ovirt_engine_setup.cinderlib import constants as ocindercons
except ImportError:
    ocindercons = None


def _(m):
    return gettext.dgettext(message=m, domain="ovirt-engine-setup")


@util.export
class Plugin(plugin.PluginBase):
    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._results: list[RotationResult] = []
        self._reported = False  # whether final summary was already shown
        self._had_migration = False

    @plugin.event(stage=plugin.Stages.STAGE_INIT)
    def _init(self):
        self.environment.setdefault(
            oengcommcons.ProvisioningEnv.POSTGRES_SCRAM_ROTATION_ENABLED,
            True,
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
        name=oengcommcons.Stages.SCRAM_ROTATION,
        # Run at end of STAGE_MISC to ensure all provisioning
        # (create/alter ROLE) handlers already executed.
        # Using PRIORITY_LAST avoids hard dependency on optional
        # component stage names (some components do not name their
        # MISC handler).
        priority=plugin.Stages.PRIORITY_LAST,
        condition=lambda self: self.environment.get(
            oengcommcons.ProvisioningEnv.POSTGRES_SCRAM_ROTATION_ENABLED, True
        ),
    )
    def _rotate(self):
        self.logger.debug("SCRAM rotation stage start")
        components = self._collect_components()
        self.logger.debug(
            "SCRAM rotation components considered: %s",
            ", ".join(c for c, _ in components),
        )
        if self._is_fresh_install(components):
            self.logger.debug(
                "SCRAM rotation skipped: fresh install detected."
            )
            return
        for tag, dbenvkeys in components:
            try:
                self.logger.debug(
                    "SCRAM rotation processing component=%s", tag
                )
                res = scram_upgrade.rotate_role_if_needed(
                    plugin=self,
                    component=tag,
                    dbenvkeys=dbenvkeys,
                )
                self._results.append(res)
                if res.status == "migrated":
                    self._had_migration = True
            except Exception as e:  # pragma: no cover - defensive
                self.logger.debug("SCRAM rotation exception", exc_info=True)
                self._results.append(
                    RotationResult(tag, "<unknown>", "error-exception", str(e))
                )
        self.logger.debug(
            "SCRAM rotation stage end: migrated=%d skipped=%d errors=%d",
            sum(1 for r in self._results if r.status == "migrated"),
            sum(1 for r in self._results if r.status.startswith("skipped")),
            sum(1 for r in self._results if r.status.startswith("error")),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CLOSEUP,
        after=(oengcommcons.Stages.SCRAM_ROTATION,),
    )
    def _report(self):
        if not self._results or self._reported:
            return
        migrated = [r for r in self._results if r.status == "migrated"]
        errors = [r for r in self._results if r.status.startswith("error")]
        skipped = [r for r in self._results if r.status.startswith("skipped")]

        if not migrated and not errors:
            self._reported = True
            return

        if migrated:
            self.dialog.note(
                text=_("SCRAM migration: upgraded roles: {roles}").format(
                    roles=", ".join(
                        f"{r.component}:{r.role}" for r in migrated
                    )
                )
            )
            if skipped:
                self.dialog.note(
                    text=_("SCRAM migration: skipped roles: {roles}").format(
                        roles=", ".join(
                            f"{r.component}:{r.role}" for r in skipped
                        )
                    )
                )
        if errors:
            self.dialog.note(
                text=_("SCRAM migration: errors for roles: {roles}").format(
                    roles=", ".join(
                        f"{r.component}:{r.role}({r.detail})" for r in errors
                    )
                )
            )
        self._reported = True

    @plugin.event(
        stage=plugin.Stages.STAGE_CLEANUP,
    )
    def _cleanup_report(self):
        self._report()

    def _collect_components(self):
        comps = []
        if oenginecons:
            comps.append(("engine", oenginecons.Const.ENGINE_DB_ENV_KEYS))
        if odwhcons:
            comps.append(("dwh", odwhcons.Const.DWH_DB_ENV_KEYS))
        if ocindercons:
            comps.append(
                ("cinderlib", ocindercons.Const.CINDERLIB_DB_ENV_KEYS)
            )
        if ogdwhcons:
            comps.append(("grafana", ogdwhcons.Const.GRAFANA_DB_ENV_KEYS))
        if okccons:
            comps.append(("keycloak", okccons.Const.KEYCLOAK_DB_ENV_KEYS))
        return comps

    def _is_fresh_install(self, components):
        if not components:
            return False
        DEK = oengcommcons.DBEnvKeysConst
        for tag, dbenvkeys in components:
            new_key = dbenvkeys.get(DEK.NEW_DATABASE)
            if new_key is not None and self.environment.get(new_key) is False:
                self.logger.debug(
                    "Fresh install check: component=%s \
                    NEW_DATABASE=False -> not fresh",
                    tag,
                )
                return False
        self.logger.debug(
            "Fresh install check: all components NEW -> fresh install"
        )
        return True
