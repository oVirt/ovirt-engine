#
# Copyright (C) 2013-2015 Red Hat, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
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
