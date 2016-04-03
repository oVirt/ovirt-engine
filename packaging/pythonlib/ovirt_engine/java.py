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


import gettext
import os
import subprocess

from . import base, config


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine')


class Java(base.Base):

    def __init__(self, component=None):
        super(Java, self).__init__()
        self._component = component if component else 'engine'

    def getJavaHome(self):
        p = subprocess.Popen(
            args=(
                os.path.join(
                    config.ENGINE_USR,
                    'bin',
                    'java-home',
                ),
                '--component=%s' % self._component,
            ),
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            close_fds=True,
        )
        stdout, stderr = p.communicate()
        stdout = stdout.decode('utf-8', 'replace').splitlines()
        stderr = stderr.decode('utf-8', 'replace').splitlines()
        if p.returncode != 0:
            raise RuntimeError(
                _(
                    'Cannot get JAVA_HOME{error} make sure supported '
                    'JRE is installed'
                ).format(
                    error='(%s)' % stderr if stderr else '',
                )
            )

        javaHome = stdout[0]
        self.logger.debug('JAVA_HOME: %s', javaHome)
        return javaHome


# vim: expandtab tabstop=4 shiftwidth=4
