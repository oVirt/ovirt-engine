#!/usr/bin/python

# Copyright (C) 2012-2015 Red Hat, Inc.
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


import config
import gettext
import os
import re
import shlex
import sys

from Cheetah.Template import Template
from ovirt_engine import configfile, java, service


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine')


class Daemon(service.Daemon):

    def __init__(self):
        super(Daemon, self).__init__()
        self._tempDir = None
        self._jbossRuntime = None
        self._defaults = os.path.abspath(
            os.path.join(
                os.path.dirname(sys.argv[0]),
                'ovirt-engine.conf',
            )
        )

    def _processTemplate(self, template, dir, mode=None):
        out = os.path.join(
            dir,
            re.sub('\.in$', '', os.path.basename(template)),
        )
        with open(out, 'w') as f:
            if mode is not None:
                os.chmod(out, mode)
            f.write(
                '%s' % (
                    Template(
                        file=template,
                        searchList=[
                            self._config,
                            {
                                'jboss_runtime': self._jbossRuntime.directory,
                            },
                        ],
                    )
                ),
            )
        return out

    def _linkModules(self, directory, modulePath):
        """
        Link all the JBoss modules into a temporary directory.
        This required because jboss tries to automatically update
        indexes based on timestamp even if there is no permission to do so.
        """

        modifiedModulePath = []
        for index, element in enumerate(modulePath.split(':')):
            modulesTmpDir = os.path.join(
                directory,
                '%02d-%s' % (
                    index,
                    '-'.join(element.split(os.sep)[-2:]),
                ),
            )
            modifiedModulePath.append(modulesTmpDir)

            # For each directory in the modules directory create the
            # same in the temporary directory and populate with symlinks
            # pointing to the original files (excluding indexes):
            for parentDir, childrenDirs, childrenFiles in os.walk(element):
                parentTmpDir = os.path.join(
                    modulesTmpDir,
                    os.path.relpath(
                        parentDir,
                        element
                    ),
                )
                if not os.path.exists(parentTmpDir):
                    os.makedirs(parentTmpDir)
                for childFile in childrenFiles:
                    if childFile.endswith('.index'):
                        continue
                    os.symlink(
                        os.path.join(parentDir, childFile),
                        os.path.join(parentTmpDir, childFile)
                    )

        return ':'.join(modifiedModulePath)

    def _checkInstallation(
        self,
        pidfile,
        jbossModulesJar,
    ):
        # Check the required JBoss directories and files:
        self.check(
            name=self._config.get('JBOSS_HOME'),
            directory=True,
        )
        self.check(
            name=jbossModulesJar,
        )

        # Check the required engine directories and files:
        self.check(
            os.path.join(
                self._config.get('ENGINE_USR'),
                'services',
            ),
            directory=True,
        )
        self.check(
            self._config.get('ENGINE_CACHE'),
            directory=True,
            writable=True,
        )
        self.check(
            self._config.get('ENGINE_TMP'),
            directory=True,
            writable=True,
            mustExist=False,
        )
        self.check(
            self._config.get('ENGINE_LOG'),
            directory=True,
            writable=True,
        )
        self.check(
            name=os.path.join(
                self._config.get("ENGINE_LOG"),
                'host-deploy',
            ),
            directory=True,
            writable=True,
        )
        for log in ('engine.log', 'console.log', 'server.log'):
            self.check(
                name=os.path.join(self._config.get("ENGINE_LOG"), log),
                mustExist=False,
                writable=True,
            )
        if pidfile is not None:
            self.check(
                name=pidfile,
                writable=True,
                mustExist=False,
            )

    def _setupEngineApps(self):

        deploymentsDir = os.path.join(
            self._jbossRuntime.directory,
            'deployments',
        )
        os.mkdir(deploymentsDir)

        # The list of applications to be deployed:
        for engineAppDir in shlex.split(self._config.get('ENGINE_APPS')):
            self.logger.debug('Deploying: %s', engineAppDir)
            if not os.path.isabs(engineAppDir):
                engineAppDir = os.path.join(
                    self._config.get('ENGINE_USR'),
                    engineAppDir,
                )
            if not os.path.exists(engineAppDir):
                self.logger.warning(
                    _(
                        "Application directory '{directory}' "
                        "does not exist, it will be ignored"
                    ).format(
                        directory=engineAppDir,
                    ),
                )
                continue

            engineAppLink = os.path.join(
                deploymentsDir,
                os.path.basename(engineAppDir),
            )
            os.symlink(engineAppDir, engineAppLink)
            with open('%s.dodeploy' % engineAppLink, 'w'):
                pass

    def daemonSetup(self):

        if os.geteuid() == 0:
            raise RuntimeError(
                _('This service cannot be executed as root')
            )

        if not os.path.exists(self._defaults):
            raise RuntimeError(
                _(
                    "The configuration defaults file '{file}' "
                    "required but missing"
                ).format(
                    file=self._defaults,
                )
            )

        self._config = configfile.ConfigFile(
            (
                self._defaults,
                config.ENGINE_VARS,
            ),
        )

        #
        # the earliest so we can abort early.
        #
        self._executable = os.path.join(
            java.Java().getJavaHome(),
            'bin',
            'java',
        )

        jbossModulesJar = os.path.join(
            self._config.get('JBOSS_HOME'),
            'jboss-modules.jar',
        )

        self._checkInstallation(
            pidfile=self.pidfile,
            jbossModulesJar=jbossModulesJar,
        )

        self._tempDir = service.TempDir(self._config.get('ENGINE_TMP'))
        self._tempDir.create()

        self._jbossRuntime = service.TempDir(self._config.get('JBOSS_RUNTIME'))
        self._jbossRuntime.create()

        self._setupEngineApps()

        jbossTempDir = os.path.join(
            self._jbossRuntime.directory,
            'tmp',
        )

        jbossConfigDir = os.path.join(
            self._jbossRuntime.directory,
            'config',
        )

        javaModulePath = self._linkModules(
            os.path.join(
                self._jbossRuntime.directory,
                'modules',
            ),
            '%s:%s' % (
                self._config.get('ENGINE_JAVA_MODULEPATH'),
                os.path.join(
                    self._config.get('JBOSS_HOME'),
                    'modules',
                ),
            ),
        )

        os.mkdir(jbossTempDir)
        os.mkdir(jbossConfigDir)
        os.chmod(jbossConfigDir, 0o700)

        jbossBootLoggingFile = self._processTemplate(
            template=os.path.join(
                os.path.dirname(sys.argv[0]),
                'ovirt-engine-logging.properties.in'
            ),
            dir=jbossConfigDir,
        )

        jbossConfigFile = self._processTemplate(
            template=os.path.join(
                os.path.dirname(sys.argv[0]),
                'ovirt-engine.xml.in',
            ),
            dir=jbossConfigDir,
            mode=0o600,
        )

        # We start with an empty list of arguments:
        self._engineArgs = []

        # Add arguments for the java virtual machine:
        self._engineArgs.extend([
            # The name or the process, as displayed by ps:
            'ovirt-engine',

            # Virtual machine options:
            '-server',
            '-XX:+TieredCompilation',
            '-Xms%s' % self._config.get('ENGINE_HEAP_MIN'),
            '-Xmx%s' % self._config.get('ENGINE_HEAP_MAX'),
            '-XX:PermSize=%s' % self._config.get('ENGINE_PERM_MIN'),
            '-XX:MaxPermSize=%s' % self._config.get(
                'ENGINE_PERM_MAX'
            ),
        ])

        # Add extra system properties provided in the configuration:
        for engineProperty in shlex.split(
            self._config.get('ENGINE_PROPERTIES')
        ):
            if not engineProperty.startswith('-D'):
                engineProperty = '-D' + engineProperty
            self._engineArgs.append(engineProperty)

        # Add extra jvm arguments provided in the configuration:
        for arg in shlex.split(self._config.get('ENGINE_JVM_ARGS')):
            self._engineArgs.append(arg)

        # Add arguments for remote debugging of the java virtual machine:
        engineDebugAddress = self._config.get('ENGINE_DEBUG_ADDRESS')
        if engineDebugAddress:
            self._engineArgs.append(
                (
                    '-Xrunjdwp:transport=dt_socket,address=%s,'
                    'server=y,suspend=n'
                ) % (
                    engineDebugAddress
                )
            )

        # Enable verbose garbage collection if required:
        if self._config.getboolean('ENGINE_VERBOSE_GC'):
            self._engineArgs.extend([
                '-verbose:gc',
                '-XX:+PrintGCTimeStamps',
                '-XX:+PrintGCDetails',
            ])

        # Add arguments for JBoss:
        self._engineArgs.extend([
            '-Djava.util.logging.manager=org.jboss.logmanager',
            '-Dlogging.configuration=file://%s' % jbossBootLoggingFile,
            '-Dorg.jboss.resolver.warning=true',
            '-Djboss.modules.system.pkgs=org.jboss.byteman',
            '-Djboss.modules.write-indexes=false',
            '-Djboss.server.default.config=ovirt-engine',
            '-Djboss.home.dir=%s' % self._config.get(
                'JBOSS_HOME'
            ),
            '-Djboss.server.base.dir=%s' % self._config.get(
                'ENGINE_USR'
            ),
            '-Djboss.server.data.dir=%s' % self._config.get(
                'ENGINE_VAR'
            ),
            '-Djboss.server.log.dir=%s' % self._config.get(
                'ENGINE_LOG'
            ),
            '-Djboss.server.config.dir=%s' % jbossConfigDir,
            '-Djboss.server.temp.dir=%s' % jbossTempDir,
            '-Djboss.controller.temp.dir=%s' % jbossTempDir,
            '-jar', jbossModulesJar,
            '-mp', javaModulePath,
            '-jaxpmodule', 'javax.xml.jaxp-provider',
            'org.jboss.as.standalone',
            '-c', os.path.basename(jbossConfigFile),
        ])

        self._engineEnv = os.environ.copy()
        self._engineEnv.update({
            'PATH': (
                '/usr/local/sbin:/usr/local/bin:'
                '/usr/sbin:/usr/bin:/sbin:/bin'
            ),
            'LANG': 'en_US.UTF-8',
            'LC_ALL': 'en_US.UTF-8',
            'ENGINE_DEFAULTS': self._defaults,
            'ENGINE_VARS': config.ENGINE_VARS,
            'ENGINE_ETC': self._config.get('ENGINE_ETC'),
            'ENGINE_LOG': self._config.get('ENGINE_LOG'),
            'ENGINE_TMP': self._tempDir.directory,
            'ENGINE_USR': self._config.get('ENGINE_USR'),
            'ENGINE_VAR': self._config.get('ENGINE_VAR'),
            'ENGINE_CACHE': self._config.get('ENGINE_CACHE'),
        })

    def daemonStdHandles(self):
        consoleLog = open(
            os.path.join(
                self._config.get('ENGINE_LOG'),
                'console.log'
            ),
            'w+',
        )
        return (consoleLog, consoleLog)

    def daemonContext(self):
        try:
            #
            # create mark file to be used by notifier service
            #
            with open(self._config.get('ENGINE_UP_MARK'), 'w') as f:
                f.write('%s\n' % os.getpid())

            self.daemonAsExternalProcess(
                executable=self._executable,
                args=self._engineArgs,
                env=self._engineEnv,
                stopTime=self._config.getinteger(
                    'ENGINE_STOP_TIME'
                ),
                stopInterval=self._config.getinteger(
                    'ENGINE_STOP_INTERVAL'
                ),
            )

            raise self.TerminateException()

        except self.TerminateException:
            if os.path.exists(self._config.get('ENGINE_UP_MARK')):
                os.remove(self._config.get('ENGINE_UP_MARK'))

    def daemonCleanup(self):
        if self._tempDir:
            self._tempDir.destroy()
        if self._jbossRuntime:
            self._jbossRuntime.destroy()


if __name__ == '__main__':
    service.setupLogger()
    d = Daemon()
    d.run()


# vim: expandtab tabstop=4 shiftwidth=4
