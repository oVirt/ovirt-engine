"""
Utilities for upgrading PostgreSQL role password
verifiers from md5 to SCRAM.
"""

import gettext


from dataclasses import dataclass
from typing import Dict
from typing import Optional

from . import constants as oengcommcons
from . import database
from .postgres import AlternateUser
from .postgres import Provisioning

DEK = oengcommcons.DBEnvKeysConst


def _(m):
    return gettext.dgettext(message=m, domain="ovirt-engine-setup")


SCRAM_PREFIX = "SCRAM-SHA-256$"
MD5_PREFIX = "md5"


@dataclass
class RotationResult:
    component: str
    role: str
    status: str  # migrated | skipped-* | error
    detail: str = ""


def _superuser_env(dbenvkeys: Dict[str, str]) -> Dict[str, object]:
    return {
        dbenvkeys[DEK.HOST]: "",
        dbenvkeys[DEK.PORT]: "",
        dbenvkeys[DEK.SECURED]: False,
        dbenvkeys[DEK.HOST_VALIDATION]: False,
        dbenvkeys[DEK.USER]: "postgres",
        dbenvkeys[DEK.PASSWORD]: "",
        dbenvkeys[DEK.DATABASE]: "template1",
    }


def _get_verifier(stmt: database.Statement, role: str) -> Optional[str]:
    rows = stmt.execute(
        statement="SELECT rolpassword FROM pg_authid WHERE rolname = %(r)s",
        args={"r": role},
        ownConnection=True,
        transaction=False,
    )
    if not rows:
        return None
    return rows[0]["rolpassword"]


def _server_password_encryption(stmt: database.Statement) -> str:
    return stmt.execute(
        statement="SHOW password_encryption",
        ownConnection=True,
        transaction=False,
    )[0]["password_encryption"]


def rotate_role_if_needed(
    plugin, component: str, dbenvkeys: Dict[str, str]
) -> RotationResult:
    env = plugin.environment
    role = env.get(dbenvkeys[DEK.USER])
    password = env.get(dbenvkeys[DEK.PASSWORD])
    host = env.get(dbenvkeys[DEK.HOST])

    plugin.logger.debug(
        "SCRAM rotate check start: component=%s \
        role=%r host=%r has_password=%s",
        component,
        role,
        host,
        bool(password),
    )

    def rr(status: str, detail: str = ""):
        return RotationResult(component, role or "<unset>", status, detail)

    if not role:
        plugin.logger.debug(
            "SCRAM rotate skip: no role (component=%s)", component
        )
        return rr("skipped-no-role")
    if not password:
        plugin.logger.debug(
            "SCRAM rotate skip: no password (component=%s role=%r)",
            component,
            role,
        )
        return rr("skipped-no-password")
    if host not in (None, "", "localhost", "127.0.0.1", "::1"):
        plugin.logger.debug(
            "SCRAM rotate skip: remote host=%r role=%r component=%s",
            host,
            role,
            component,
        )
        return rr("skipped-remote", f"host={host}")

    su_env = _superuser_env(dbenvkeys)

    with AlternateUser(user=env[oengcommcons.SystemEnv.USER_POSTGRES]):
        prov = Provisioning(plugin=plugin, dbenvkeys=dbenvkeys, defaults={})
        prov.waitForDatabase(environment=su_env)

        stmt = database.Statement(dbenvkeys=dbenvkeys, environment=su_env)

        server_method = _server_password_encryption(stmt)
        plugin.logger.debug(
            "SCRAM rotate server password_encryption=%s component=%s role=%r",
            server_method,
            component,
            role,
        )
        if server_method != "scram-sha-256":
            return rr("skipped-server-not-scram")

        verifier = _get_verifier(stmt, role)
        plugin.logger.debug(
            "SCRAM rotate current verifier prefix=%r role=%r component=%s",
            None if verifier is None else verifier[:10],
            role,
            component,
        )
        if verifier is None:
            return rr("skipped-role-missing")
        if verifier == "" or verifier is None:
            return rr("skipped-empty-verifier")
        if verifier.startswith(SCRAM_PREFIX):
            return rr("skipped-already-scram")
        if not verifier.startswith(MD5_PREFIX):
            return rr("skipped-unknown-format", verifier[:15])

        plugin.logger.debug(
            "SCRAM rotate altering role %r component=%s", role, component
        )
        stmt.execute(
            statement=f"ALTER ROLE {role} WITH ENCRYPTED PASSWORD %(p)s",
            args={"p": password},
            ownConnection=True,
            transaction=False,
        )
        new_v = _get_verifier(stmt, role)
        plugin.logger.debug(
            "SCRAM rotate new verifier prefix=%r role=%r component=%s",
            None if new_v is None else new_v[:10],
            role,
            component,
        )
        if new_v and new_v.startswith(SCRAM_PREFIX):
            return rr("migrated")
        return rr("error-post-alter", (new_v or "<none>")[:20])
