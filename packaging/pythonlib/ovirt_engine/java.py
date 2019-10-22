#
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


import gettext
import os
import subprocess

from . import base
from . import config


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
