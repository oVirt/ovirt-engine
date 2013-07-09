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


import os
import locale
import glob
import StringIO
import configparser


from otopi import base
from otopi import util


from . import config


@util.export
class Branding(base.Base):

    def _readProperties(self, name):
        with open(name, 'r') as f:
            c = configparser.ConfigParser()
            c.optionxform = str
            c.readfp(StringIO.StringIO('[root]\n' + f.read()))
            return c

    def __init__(self, application='setup'):
        super(Branding, self).__init__()

        self._application = application
        self._messages = {}

        for branding in sorted(
            glob.glob(
                os.path.join(
                    config.ENGINE_SYSCONFDIR,
                    'branding',
                    '*.brand',
                    'branding.properties'
                )
            )
        ):
            brnd = self._readProperties(branding)
            if brnd.has_option('root', 'messages'):
                msgfname = os.path.join(
                    os.path.dirname(branding),
                    brnd.get('root', 'messages')
                )
                for f in (
                    msgfname,
                    msgfname.replace(
                        '.properties',
                        '_%s.properties' % locale.getdefaultlocale()[0]
                    )
                ):
                    if os.path.exists(f):
                        msgs = self._readProperties(f)
                        for m in msgs.options('root'):
                            self._messages[m] = msgs.get('root', m)

    def getMessage(self, message):
        k = 'obrand.%s.%s' % (self._application, message)
        if k in self._messages:
            return self._messages[k]
        return self._messages.get('obrand.common.%s' % message, '')


branding = Branding()


# vim: expandtab tabstop=4 shiftwidth=4
