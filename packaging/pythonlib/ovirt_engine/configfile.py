#
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


import gettext
import glob
import os
import re

from . import base


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine')


class ConfigFile(base.Base):
    """
    Parsing of shell style config file.
    Follow closly the java LocalConfig implementaiton.
    """

    _EMPTY_LINE = re.compile(r'^\s*(#.*|)$')
    _KEY_VALUE_EXPRESSION = re.compile(r'^\s*(?P<key>\w+)=(?P<value>.*)$')

    @property
    def values(self):
        return self._values

    def _loadLine(self, line):
        emptyMatch = self._EMPTY_LINE.search(line)
        if emptyMatch is None:
            keyValueMatch = self._KEY_VALUE_EXPRESSION.search(line)
            if keyValueMatch is None:
                raise RuntimeError(_('Invalid sytax'))
            self._values[keyValueMatch.group('key')] = self.expandString(
                keyValueMatch.group('value')
            )

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
            index = 0
            try:
                with open(file, 'r') as f:
                    for line in f:
                        index += 1
                        self._loadLine(line)
            except Exception as e:
                self.logger.error(
                    "File '%s' index %d error" % (file, index),
                    exc_info=True,
                )
                raise RuntimeError(
                    _(
                        "Cannot parse configuration file "
                        "'{file}' line {line}: {error}"
                    ).format(
                        file=file,
                        line=index,
                        error=e
                    )
                )

    def expandString(self, value):
        ret = ""

        escape = False
        inQuotes = False
        index = 0
        while (index < len(value)):
            c = value[index]
            index += 1
            if escape:
                escape = False
                ret += c
            else:
                if c == '\\':
                    escape = True
                elif c == '$':
                    if value[index] != '{':
                        raise RuntimeError('Malformed variable assignment')
                    index += 1
                    i = value.find('}', index)
                    if i == -1:
                        raise RuntimeError('Malformed variable assignment')
                    name = value[index:i]
                    index = i + 1
                    ret += self._values.get(name, "")
                elif c == '"':
                    inQuotes = not inQuotes
                elif c in (' ', '#'):
                    if inQuotes:
                        ret += c
                    else:
                        index = len(value)
                else:
                    ret += c

        return ret

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


# vim: expandtab tabstop=4 shiftwidth=4
