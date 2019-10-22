#
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Base class for project."""


import logging

from . import util


@util.export
class Base(object):
    """Base class for all objects."""

    _LOG_PREFIX = 'ovirt.engine.'

    @property
    def logger(self):
        """Logger."""
        return self._logger

    def __init__(self):
        """Contructor."""

        prefix = ''
        if not self.__module__.startswith(self._LOG_PREFIX):
            prefix = self._LOG_PREFIX

        self._logger = logging.getLogger(prefix + self.__module__)


# vim: expandtab tabstop=4 shiftwidth=4
