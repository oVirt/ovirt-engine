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


"""Java plugin."""


import os
import re
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import constants as otopicons
from otopi import util
from otopi import filetransaction
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """Java plugin."""

    JAVA_CONFIG_JVM = 'icedtea-7'
    JAVA_VERSION = '1.7.0'

    _RE_JAVA_VERSION = re.compile(
        flags=re.VERBOSE | re.MULTILINE,
        pattern=r"""
            ^
            java
            \s
            version
            \s
            "
            (?P<version>[^"]*)
            "
            $
        """,
    )

    _RE_JAVA_OPENJDK = re.compile(
        flags=re.VERBOSE | re.MULTILINE,
        pattern=r"""
            ^
            OpenJDK
            \s
            .*
            $
        """,
    )

    def _checkJvm(self, jvmPath):
        self.logger.debug('Checking %s', jvmPath)

        # Check that it contains the Java launcher:
        javaLauncher = os.path.join(jvmPath, 'bin', 'java')
        if not os.path.exists(javaLauncher):
            self.logger.debug('does not has bin/java')
            return False

        # Check that Java launcher is executable:
        if not os.access(javaLauncher, os.X_OK):
            self.logger.debug('bin/java is not execuable')
            return False

        rc, stdout, stderr = self.execute(
            (
                javaLauncher,
                '-version',
            ),
        )

        # Extract version number:
        match = self._RE_JAVA_VERSION.search('\n'.join(stderr))
        if match is None:
            self.logger.debug('Cannot determine java version')
            return False
        javaVersion = match.group('version')

        self.logger.debug(
            'Testing version %s against %s',
            javaVersion,
            self.JAVA_VERSION,
        )
        # Check that the version is supported:
        if not javaVersion.startswith(self.JAVA_VERSION):
            self.logger.debug('unsupported java version')
            return False

        # Check that it is an OpenJDK:
        match = self._RE_JAVA_OPENJDK.search('\n'.join(stderr))
        if match is None:
            self.logger.debug('Not OpenJDK')
            return False

        # It passed all the checks, so it is valid JVM:
        return True

    def _checkJdk(self, jvmPath):
        # We assume that this JVM path has already been checked and that it
        # contains a valid JVM, so we only need to check that it also
        # contains a Java compiler:
        return os.path.exists(
            os.path.join(jvmPath, 'bin', 'javac')
        )

    def _findJavaHome(self):
        # Find links in the search directories that point to real things,
        # not to other symlinks (this is to avoid links that point to things
        # that can be changed by the user, specially "alternatives" managed
        # links):
        jvmLinks = []
        for javaDir in ['/usr/lib/jvm']:
            for fileName in os.listdir(javaDir):
                filePath = os.path.join(javaDir, fileName)
                if os.path.islink(filePath):
                    if not os.path.islink(
                        os.path.join(
                            javaDir,
                            os.readlink(filePath),
                        )
                    ):
                        jvmLinks.append(filePath)

        # For each possible JVM path check that it really contain a JVM and
        # that the version is supported:
        jvmLinks = [x for x in jvmLinks if self._checkJvm(x)]

        # We prefer JRE over JDK, mainly because it is more stable, I mean,
        # a JRE will be always present if there is a JDK, but not the other
        # way around:
        jreLinks = [x for x in jvmLinks if not self._checkJdk(x)]
        if jreLinks:
            jvmLinks = jreLinks

        # Sort the list alphabetically (this is only to get a predictable
        # result):
        jvmLinks.sort()

        if len(jvmLinks) == 0:
            raise RuntimeError(_('Cannot detect java location'))

        return jvmLinks[0]

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)
        self._enabled = False

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.ConfigEnv.JAVA_HOME,
            None
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
    )
    def _setup(self):
        self.command.detect('java-config')

    @plugin.event(
        stage=plugin.Stages.STAGE_VALIDATION,
        condition=lambda self: self.environment[
            osetupcons.ConfigEnv.JAVA_HOME
        ] is None,
    )
    def _validation(self):
        if self.command.get(
            'java-config',
            optional=True
        ) is not None:
            rc, stdout, stderr = self.execute(
                (
                    self.command.get('java-config'),
                    '--select-vm=%s' % self.JAVA_CONFIG_JVM,
                    '--jre-home',
                ),
            )
            javaHome = stdout[0]
        else:
            javaHome = self._findJavaHome()

        self.environment[
            osetupcons.ConfigEnv.JAVA_HOME
        ] = javaHome

    @plugin.event(
        stage=plugin.Stages.STAGE_MISC,
    )
    def _misc(self):
        for f in (
            osetupcons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG_JAVA,
            osetupcons.FileLocations.OVIRT_ENGINE_NOTIFIER_SERVICE_CONFIG_JAVA,
        ):
            self.environment[otopicons.CoreEnv.MAIN_TRANSACTION].append(
                filetransaction.FileTransaction(
                    name=f,
                    content=(
                        'JAVA_HOME="{javaHome}"\n'
                    ).format(
                        javaHome=self.environment[
                            osetupcons.ConfigEnv.JAVA_HOME
                        ],
                    ),
                    modifiedList=self.environment[
                        otopicons.CoreEnv.MODIFIED_FILES
                    ],
                )
            )


# vim: expandtab tabstop=4 shiftwidth=4
