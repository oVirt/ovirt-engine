#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#

import contextlib
import gettext
import json

import psycopg2

from ovirt_engine import base


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-fence-kdump-listener')


class DbManager(base.Base):
    """Manages database connection and executes SQL commands"""
    def __init__(
            self,
            host,
            port,
            database,
            username,
            password,
            secured,
            secure_validation,
            autocommit=True,
    ):
        super(DbManager, self).__init__()
        self._host = host
        self._port = port
        self._database = database
        self._username = username
        self._password = password
        self._sslmode = 'allow'
        if secured:
            if secure_validation:
                self._sslmode = 'verify-full'
            else:
                self._sslmode = 'require'
        self._autocommit = autocommit
        self._connection = None

    def __enter__(self):
        # connection is opened during listener db sync
        return self

    def __exit__(self, exc_type, exc_value, traceback):
        self._close_connection()

    def _open_connection(self):
        #
        # Old psycopg2 does not know how to ignore useless parameters
        #
        if not self._host:
            self._connection = psycopg2.connect(
                database=self._database,
            )
        else:
            #
            # Port cast is required as old psycopg2 does not support
            # unicode strings for port.
            # Do not cast to int to avoid breaking usock.
            #
            self._connection = psycopg2.connect(
                host=self._host,
                port=str(self._port),
                user=self._username,
                password=self._password,
                database=self._database,
                sslmode=self._sslmode,
            )

        # autocommit member is available at >= 2.4.2
        if hasattr(self._connection, 'autocommit'):
            self._connection.autocommit = self._autocommit
        else:
            self._connection.set_isolation_level(
                psycopg2.extensions.ISOLATION_LEVEL_AUTOCOMMIT
                if self._autocommit
                else psycopg2.extensions.ISOLATION_LEVEL_AUTOCOMMIT
            )

    def _close_connection(self):
        try:
            if self._connection is not None:
                self._connection.close()
        except (psycopg2.Error, psycopg2.Warning) as e:
            self.logger.warning(
                _("Error closing connection: {error}").format(
                    error=e,
                )
            )
            self.logger.debug('Exception',  exc_info=True)
        finally:
            self._connection = None

    def _connection_valid(self):
        valid = False
        if self._connection is not None:
            try:
                with contextlib.closing(
                        self._connection.cursor()
                ) as cursor:
                    cursor.execute('SELECT 1')
                valid = True
            except (psycopg2.Error, psycopg2.Warning):
                self.logger.debug(
                    'Error testing connection validity',
                    exc_info=True,
                )
        return valid

    def _process_results(self, cursor):
        ret = []
        if cursor.description is not None:
            cols = [d[0] for d in cursor.description]
            while True:
                entry = cursor.fetchone()
                if entry is None:
                    break
                ret.append(dict(zip(cols, entry)))
        return ret

    def validate_connection(self):
        self.logger.debug('Testing connection validity')
        valid = self._connection_valid()
        if not valid:
            self._close_connection()
            try:
                self._open_connection()
                valid = self._connection_valid()
            except (psycopg2.Error, psycopg2.Warning):
                valid = False
                self.logger.debug('Connection is not valid')
        return valid

    def call_procedure(
            self,
            name,
            args=None,
    ):
        self.logger.debug(
            "Database: '%s', Procedure: '%s', Args: '%s'",
            self._database,
            name,
            args,
        )

        try:
            with contextlib.closing(
                    self._connection.cursor()
            ) as cursor:
                cursor.callproc(
                    name,
                    args,
                )
                ret = self._process_results(cursor=cursor)
        except (psycopg2.Error, psycopg2.Warning) as e:
            raise DbException(
                message=(
                    "Error calling procedure '%s'" % name
                ),
                cause=e,
            )

        self.logger.debug("Result: '%s'", ret)
        return ret


class EngineDao(base.Base):
    """DAO to access ovirt-engine database"""
    # Variable name for heartbeat in external_variable table
    _HEARTBEAT_VAR_NAME = 'fence-kdump-listener-heartbeat'

    def __init__(
            self,
            db_manager
    ):
        super(EngineDao, self).__init__()
        self._db_mgr = db_manager

    def update_vds_kdump_status(self, status, address):
        res = self._db_mgr.call_procedure(
            name='UpsertKdumpStatusForIp',
            args=(
                address[0],                      # v_ip
                status,                          # v_status
                json.dumps(address),             # v_address
            ),
        )
        return res[0]['upsertkdumpstatusforip'] == 1

    def update_heartbeat(self):
        return self._db_mgr.call_procedure(
            name='UpsertExternalVariable',
            args=(
                self._HEARTBEAT_VAR_NAME,        # v_var_name
                None,                            # v_var_value
            ),
        )

    def get_unfinished_session_addresses(self):
        result = self._db_mgr.call_procedure(
            name='GetAllUnfinishedVdsKdumpStatus',
        )

        # address needs to be converted from string to tuple
        return [
            tuple(json.loads(record['address']))
            for record in result
        ]


class DbException(Exception):
    def __init__(self, message, cause):
        super(DbException, self).__init__(
            '%s: %s' % (
                message,
                repr(cause)
            )
        )
        self.cause = cause


# vim: expandtab tabstop=4 shiftwidth=4
