#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#

import codecs
import datetime
import gettext
import socket
import time

import db


from ovirt_engine import base


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-fence-kdump-listener')


class FenceKdumpListener(base.Base):
    class InvalidMessage(Exception):
        pass

    # Vds kdump flow states
    SESSION_STATE_INITIAL = 'started'
    SESSION_STATE_DUMPING = 'dumping'
    SESSION_STATE_FINISHED = 'finished'
    SESSION_STATE_CLOSED = 'closed'

    # buffer size to receive message
    _BUF_SIZE = 0x20

    # fence_kdump message version 1
    _MSG_V1_SIZE = 8
    # message contains magic 0x1B302A40 and version 0x1 in BE byte order
    _MSG_V1_PREFIX = codecs.decode('402a301b01000000', 'hex')

    def __init__(
            self,
            bind,
            db_manager,
            heartbeat_interval,
            session_sync_interval,
            reopen_db_connection_interval,
            session_expiration_time,
    ):
        super(FenceKdumpListener, self).__init__()
        self._bind = bind

        self._db_manager = db_manager
        self._dao = db.EngineDao(db_manager)
        self._db_connection_valid = True
        self._afterFirstDbSync = False

        self._heartbeatInterval = heartbeat_interval
        self._sessionSyncInterval = session_sync_interval
        self._wakeupInterval = min(
            self._heartbeatInterval,
            self._sessionSyncInterval
        )
        self._reopenDbConnInterval = reopen_db_connection_interval
        self._sessionExpirationTime = session_expiration_time
        self._lastHeartbeat = None
        self._lastSessionSync = None
        self._lastWakeup = None
        self._lastDbConnectionAttempt = None
        self._sessions = {}

    def __enter__(self):
        self._socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self._socket.bind(self._bind)
        return self

    def __exit__(self, exc_type, exc_value, traceback):
        self._socket.close()

    def _total_seconds(self, dt1, dt2):
        return round(
            abs(
                time.mktime(dt1.timetuple()) - time.mktime(dt2.timetuple())
            )
        )

    def _interval_finished(self, interval, last):
        return (
            last is None or
            self._total_seconds(datetime.datetime.utcnow(), last) >= interval
        )

    def _recvfrom(self):
        ret = (None, None)

        try:
            if self._interval_finished(
                    interval=self._wakeupInterval,
                    last=self._lastWakeup,
            ):
                raise socket.timeout()

            self._socket.settimeout(
                max(
                    self._wakeupInterval - self._total_seconds(
                        datetime.datetime.utcnow(),
                        self._lastWakeup
                    ),
                    1,
                )
            )

            ret = self._socket.recvfrom(self._BUF_SIZE)
        except socket.timeout:
            self._lastWakeup = datetime.datetime.utcnow()

        return ret

    def _house_keeping(self):
        self._house_keeping_sessions()
        self._db_sync()

    def _verify_message(self, message):
        return (
            message and
            len(message) >= self._MSG_V1_SIZE and
            message[:len(self._MSG_V1_PREFIX)] == self._MSG_V1_PREFIX
        )

    def _handle_message(self, entry, message):
        try:
            if not self._verify_message(message):
                raise FenceKdumpListener.InvalidMessage(
                    _(
                        "Discarding invalid message '{msg}' from address "
                        "'{address}'."
                    ).format(
                        msg=message.encode('hex'),
                        address=entry['address'][0],
                    )
                )

            # message is valid, update timestamp
            entry['updated'] = datetime.datetime.utcnow()

            if entry['status'] == self.SESSION_STATE_INITIAL:
                self.logger.debug(
                    "Started to dump '%s'",
                    entry,
                )
                entry['status'] = self.SESSION_STATE_DUMPING

            elif entry['status'] == self.SESSION_STATE_DUMPING:
                self.logger.debug(
                    "Dumping '%s'",
                    entry,
                )
        except FenceKdumpListener.InvalidMessage as e:
            self.logger.debug(e)
            # if host just started the dump, close the session, otherwise
            # just ignore invalid message
            if entry['status'] == self.SESSION_STATE_INITIAL:
                entry['status'] = self.SESSION_STATE_CLOSED

    def _house_keeping_sessions(self):
        for session in list(self._sessions.values()):

            if (
                session['status'] == self.SESSION_STATE_DUMPING and
                self._interval_finished(
                    interval=self._sessionExpirationTime,
                    last=session['updated']
                )
            ):
                session['status'] = self.SESSION_STATE_FINISHED
                session['dirty'] = True
                self.logger.info(
                    _(
                        "Host '{address}' finished kdump flow."
                    ).format(
                        address=session['address'][0]
                    )
                )

        # remove finished sessions (engine will remove them from db)
        for address in (
            session['address']
            for session in list(self._sessions.values())
            if session['status'] == self.SESSION_STATE_CLOSED
        ):
            del self._sessions[address]

    def _heartbeat(self):
        if self._interval_finished(
                interval=self._heartbeatInterval,
                last=self._lastHeartbeat
        ):
            self._dao.update_heartbeat()
            self._lastHeartbeat = datetime.datetime.utcnow()

    def _save_sessions(self):
        if self._interval_finished(
                interval=self._sessionSyncInterval,
                last=self._lastSessionSync
        ):
            # update db state for all updated sessions
            for session in list(self._sessions.values()):
                if (
                    session['dirty'] and
                    session['status'] != self.SESSION_STATE_CLOSED
                ):
                    if not self._dao.update_vds_kdump_status(
                        status=session['status'],
                        address=session['address'],
                    ):
                        self.logger.debug(
                            (
                                "Discarding session for unknown host with "
                                "address '%s'."
                            ),
                            session['address'][0],
                        )
                        # set status to finished to be removed in next step
                        session['status'] = self.SESSION_STATE_CLOSED

                    session['dirty'] = False

                    if session['status'] == self.SESSION_STATE_FINISHED:
                        # mark finished session saved to db as close, so they
                        # can be removed from sessions on next house keeping
                        session['status'] = self.SESSION_STATE_CLOSED

            self._lastSessionSync = datetime.datetime.utcnow()

    def _create_session(
            self,
            status,
            address,
            dirty=True,
    ):
        return {
            'status': status,
            'address': address,
            'updated': datetime.datetime.utcnow(),
            'dirty': dirty,
        }

    def _load_sessions(self):
        if not self._afterFirstDbSync:
            # load sessions from db on 1st successful db connection
            for address in self._dao.get_unfinished_session_addresses():
                # if session is not in _sessions, add it, otherwise
                # _sessions contains more up to date session info
                if address not in self._sessions:
                    session = self._create_session(
                        status=self.SESSION_STATE_DUMPING,
                        address=address,
                        dirty=False,
                    )
                    self._sessions[session['address']] = session

            self._afterFirstDbSync = True

    def _db_sync(self):
        # check if connection is valid and if not postpone reopen db
        # connection after _reopenDbConnInterval
        if (
            self._db_connection_valid or
            self._interval_finished(
                interval=self._reopenDbConnInterval,
                last=self._lastDbConnectionAttempt
            )
        ):
            if self._db_manager.validate_connection():
                self._db_connection_valid = True
                try:
                    self._heartbeat()
                    # save sessions to db first, because memory usually
                    # contains more recent data and number of unfinished
                    # sessions loaded from db in next step will be lower
                    self._save_sessions()
                    self._load_sessions()
                except db.DbException as e:
                    self.logger.debug(
                        (
                            "Error during synchronization with database, "
                            "synchronization will be postponed: %s"
                        ),
                        e.cause,
                    )
                    self.logger.debug('Exception',  exc_info=True)
            else:
                if self._db_connection_valid:
                    self._db_connection_valid = False
                    self.logger.warning(
                        _(
                            "Database connection is not available, "
                            "synchronization will be postponed."
                        )
                    )
                self._lastDbConnectionAttempt = datetime.datetime.utcnow()

    def run(self):
        while True:
            (data, address) = self._recvfrom()
            if address is None:
                self._house_keeping()
            else:
                entry = self._sessions.get(address)
                if entry is None:
                    entry = self._create_session(
                        status=self.SESSION_STATE_INITIAL,
                        address=address,
                    )
                    self._sessions[address] = entry

                self._handle_message(
                    entry=entry,
                    message=data,
                )


# vim: expandtab tabstop=4 shiftwidth=4
