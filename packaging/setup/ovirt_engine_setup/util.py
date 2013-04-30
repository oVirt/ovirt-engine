#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013 Red Hat, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


"""Utils."""


import os
import re
import glob
import pwd
import grp
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import base


class ConfigFile(base.Base):
    _COMMENT_EXPR = re.compile(r'\s*#.*$')
    _BLANK_EXPR = re.compile(r'^\s*$')
    _VALUE_EXPR = re.compile(r'^\s*(?P<key>\w+)\s*=\s*(?P<value>.*?)\s*$')
    _REF_EXPR = re.compile(r'\$\{(?P<ref>\w+)\}')

    def _loadLine(self, line):
        # Remove comments:
        commentMatch = self._COMMENT_EXPR.search(line)
        if commentMatch is not None:
            line = line[:commentMatch.start()] + line[commentMatch.end():]

        # Skip empty lines:
        emptyMatch = self._BLANK_EXPR.search(line)
        if emptyMatch is None:
            # Separate name from value:
            keyValueMatch = self._VALUE_EXPR.search(line)
            if keyValueMatch is not None:
                key = keyValueMatch.group('key')
                value = keyValueMatch.group('value')

                # Strip quotes from value:
                if len(value) >= 2 and value[0] == '"' and value[-1] == '"':
                    value = value[1:-1]

                # Expand references to other parameters:
                while True:
                    refMatch = self._REF_EXPR.search(value)
                    if refMatch is None:
                        break
                    refKey = refMatch.group('ref')
                    refValue = self._values.get(refKey)
                    if refValue is None:
                        break
                    value = '%s%s%s' % (
                        value[:refMatch.start()],
                        refValue,
                        value[refMatch.end():],
                    )

                self._values[key] = value

    def __init__(self, files=[]):
        super(ConfigFile, self).__init__()

        self._values = {}

        for file in files:
            self.loadFile(file)
            for filed in sorted(
                glob.glob(
                    os.path.join(
                        '%s.d' % file,
                        '*.conf',
                    )
                )
            ):
                self.loadFile(filed)

    def loadFile(self, file):
        if os.path.exists(file):
            self.logger.debug("loading config '%s'", file)
            with open(file, 'r') as f:
                for line in f:
                    self._loadLine(line)

    def get(self, name, default=None):
        return self._values.get(name, default)

    def getboolean(self, name, default=None):
        text = self.get(name)
        if text is None:
            return default
        else:
            return text.lower() in ('t', 'true', 'y', 'yes', '1')

    def getinteger(self, name, default=None):
        value = self.get(name)
        if value is None:
            return default
        else:
            return int(value)


@util.export
def processTemplate(template, subst):
    content = ''
    with open(template, 'r') as f:
        content = f.read()
    for k, v in subst.items():
        content = content.replace(str(k), str(v))
    return content


@util.export
def getUid(user):
    return pwd.getpwnam(user)[2]


@util.export
def getGid(group):
    return grp.getgrnam(group)[2]


@util.export
def parsePort(port):
    try:
        port = int(port)
    except ValueError:
        raise ValueError(
            _('Invalid port {number}').format(
                number=port,
            )
        )
    if port < 0 or port > 0xffff:
        raise ValueError(
            _('Invalid number {number}').format(
                number=port,
            )
        )
    return port


# vim: expandtab tabstop=4 shiftwidth=4
