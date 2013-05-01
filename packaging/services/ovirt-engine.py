#!/usr/bin/python

# Copyright 2012 Red Hat
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


import glob
import os
import re
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine')


from Cheetah.Template import Template


import config
import service


class Daemon(service.Daemon):

    def __init__(self):
        super(Daemon, self).__init__()

    def _processTemplate(self, template, dir):
        out = os.path.join(
            dir,
            re.sub('\.in$', '', os.path.basename(template)),
        )
        with open(out, 'w') as f:
            f.write(str(Template(file=template, searchList=[self._config])))
        return out

    def _linkModules(self, modulesDir):
        """Link all the JBoss modules into a temporary directory"""

        modulesTmpDir = os.path.join(
            self._config.getString('ENGINE_TMP'),
            'modules',
        )

        # For each directory in the modules directory create the same in the
        # temporary directory and populate with symlinks pointing to the
        # original files (excluding indexes):
        for parentDir, childrenDirs, childrenFiles in os.walk(modulesDir):
            parentTmpDir = parentDir.replace(modulesDir, modulesTmpDir, 1)
            if not os.path.exists(parentTmpDir):
                os.makedirs(parentTmpDir)
            for childFile in childrenFiles:
                if childFile.endswith('.index'):
                    continue
                childPath = os.path.join(parentDir, childFile)
                childTmpPath = os.path.join(parentTmpDir, childFile)
                os.symlink(childPath, childTmpPath)

        return modulesTmpDir

    def _checkInstallation(
        self,
        pidfile,
        jbossModulesJar,
        java,
    ):
        # Check that the Java home directory exists and that it contais at
        # least the java executable:
        self.check(
            name=self._config.getString('JAVA_HOME'),
            directory=True,
        )
        self.check(
            name=java,
            executable=True,
        )

        # Check the required JBoss directories and files:
        self.check(
            name=self._config.getString('JBOSS_HOME'),
            directory=True,
        )
        self.check(
            name=jbossModulesJar,
        )

        # Check the required engine directories and files:
        self.check(
            os.path.join(
                self._config.getString('ENGINE_USR'),
                'services',
            ),
            directory=True,
        )
        self.check(
            self._config.getString('ENGINE_CACHE'),
            directory=True,
            writable=True,
        )
        self.check(
            self._config.getString('ENGINE_TMP'),
            directory=True,
            writable=True,
            mustExist=False,
        )
        for dir in ('.', 'content', 'deployments'):
            self.check(
                os.path.join(
                    self._config.getString('ENGINE_VAR'),
                    dir
                ),
                directory=True,
                writable=True,
            )
        self.check(
            self._config.getString('ENGINE_LOG'),
            directory=True,
            writable=True,
        )
        self.check(
            name=os.path.join(
                self._config.getString("ENGINE_LOG"),
                'host-deploy',
            ),
            directory=True,
            writable=True,
        )
        for log in ('engine.log', 'console.log', 'server.log'):
            self.check(
                name=os.path.join(self._config.getString("ENGINE_LOG"), log),
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

        # The list of applications to be deployed:
        for engineApp in self._config.getString('ENGINE_APPS').split():
            # Do nothing if the application is not available:
            engineAppDir = os.path.join(
                self._config.getString('ENGINE_USR'),
                engineApp,
            )
            if not os.path.exists(engineAppDir):
                self._logger.warning(
                    _(
                        "Application '{application}' directory '{directory}' "
                        "does not exist, it will be ignored"
                    ).format(
                        application=engineApp,
                        directory=engineAppDir,
                    ),
                )
                continue

            # Make sure the application is linked in the deployments
            # directory, if not link it now:
            engineAppLink = os.path.join(
                self._config.getString('ENGINE_VAR'),
                'deployments',
                engineApp,
            )
            if not os.path.islink(engineAppLink):
                try:
                    os.symlink(engineAppDir, engineAppLink)
                except OSError as e:
                    self._logger.debug('exception', exc_info=True)
                    raise RuntimeError(
                        _(
                            "Cannot create symbolic link '{file}': "
                            "{error}"
                        ).format(
                            file=engineAppLink,
                            error=e,
                        ),
                    )

            # Remove all existing deployment markers:
            for markerFile in glob.glob('%s.*' % engineAppLink):
                try:
                    os.remove(markerFile)
                except OSError as e:
                    self._logger.debug('exception', exc_info=True)
                    raise RuntimeError(
                        _(
                            "Cannot remove deployment marker file '{file}': "
                            "{error}"
                        ).format(
                            file=markerFile,
                            error=e,
                        ),
                    )

            # Create the new marker file to trigger deployment
            # of the application:
            markerFile = "%s.dodeploy" % engineAppLink
            try:
                with open(markerFile, "w"):
                    pass
            except IOError as e:
                self._logger.debug('exception', exc_info=True)
                raise RuntimeError(
                    _(
                        "Cannot create deployment marker file '{file}': "
                        "{error}"
                    ).format(
                        file=markerFile,
                        error=e,
                    )
                )

    def daemonSetup(self):

        if os.geteuid() == 0:
            raise RuntimeError(
                _('This service cannot be executed as root')
            )

        if not os.path.exists(config.ENGINE_DEFAULT_FILE):
            raise RuntimeError(
                _(
                    "The configuration defaults file '{file}' "
                    "required but missing"
                ).format(
                    file=config.ENGINE_DEFAULT_FILE,
                )
            )

        self._config = service.ConfigFile(
            (
                config.ENGINE_DEFAULT_FILE,
                config.ENGINE_VARS,
            ),
        )

        jbossModulesJar = os.path.join(
            self._config.getString('JBOSS_HOME'),
            'jboss-modules.jar',
        )
        java = os.path.join(
            self._config.getString('JAVA_HOME'),
            'bin',
            'java',
        )

        self._checkInstallation(
            pidfile=self.pidfile,
            jbossModulesJar=jbossModulesJar,
            java=java,
        )

        self._tempDir = service.TempDir(self._config.getString('ENGINE_TMP'))
        self._tempDir.create()

        self._setupEngineApps()

        jbossBootLoggingFile = self._processTemplate(
            template=os.path.join(
                self._config.getString('ENGINE_USR'),
                'services',
                'ovirt-engine-logging.properties.in'
            ),
            dir=self._config.getString('ENGINE_TMP'),
        )

        jbossConfigFile = self._processTemplate(
            template=os.path.join(
                self._config.getString('ENGINE_USR'),
                'services',
                'ovirt-engine.xml.in',
            ),
            dir=self._config.getString('ENGINE_TMP'),
        )

        jbossModulesTmpDir = self._linkModules(
            os.path.join(
                self._config.getString('JBOSS_HOME'),
                'modules',
            ),
        )

        self._executable = java

        # We start with an empty list of arguments:
        self._engineArgs = []

        # Add arguments for the java virtual machine:
        self._engineArgs.extend([
            # The name or the process, as displayed by ps:
            'ovirt-engine',

            # Virtual machine options:
            '-server',
            '-XX:+TieredCompilation',
            '-Xms%s' % self._config.getString('ENGINE_HEAP_MIN'),
            '-Xmx%s' % self._config.getString('ENGINE_HEAP_MAX'),
            '-XX:PermSize=%s' % self._config.getString('ENGINE_PERM_MIN'),
            '-XX:MaxPermSize=%s' % self._config.getString(
                'ENGINE_PERM_MAX'
            ),
            '-Djava.net.preferIPv4Stack=true',
            '-Dsun.rmi.dgc.client.gcInterval=3600000',
            '-Dsun.rmi.dgc.server.gcInterval=3600000',
            '-Djava.awt.headless=true',
        ])

        # Add extra system properties provided in the configuration:
        engineProperties = self._config.getString('ENGINE_PROPERTIES')
        for engineProperty in engineProperties.split():
            if not engineProperty.startswith('-D'):
                engineProperty = '-D' + engineProperty
            self._engineArgs.append(engineProperty)

        # Add arguments for remote debugging of the java virtual machine:
        engineDebugAddress = self._config.getString('ENGINE_DEBUG_ADDRESS')
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
        if self._config.getBoolean('ENGINE_VERBOSE_GC'):
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
            '-Djboss.home.dir=%s' % self._config.getString(
                'JBOSS_HOME'
            ),
            '-Djboss.server.base.dir=%s' % self._config.getString(
                'ENGINE_USR'
            ),
            '-Djboss.server.config.dir=%s' % self._config.getString(
                'ENGINE_TMP'
            ),
            '-Djboss.server.data.dir=%s' % self._config.getString(
                'ENGINE_VAR'
            ),
            '-Djboss.server.log.dir=%s' % self._config.getString(
                'ENGINE_LOG'
            ),
            '-Djboss.server.temp.dir=%s' % self._config.getString(
                'ENGINE_TMP'
            ),
            '-Djboss.controller.temp.dir=%s' % self._config.getString(
                'ENGINE_TMP'
            ),
            '-jar', jbossModulesJar,

            # Module path should include first the engine modules
            # so that they can override those provided by the
            # application server if needed:
            '-mp', "%s:%s" % (
                os.path.join(
                    self._config.getString('ENGINE_USR'),
                    'modules',
                ),
                jbossModulesTmpDir,
            ),

            '-jaxpmodule', 'javax.xml.jaxp-provider',
            'org.jboss.as.standalone',
            '-c', os.path.basename(jbossConfigFile),
        ])

        self._engineEnv = os.environ.copy()
        self._engineEnv.update({
            'PATH': '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin',
            'LANG': 'en_US.UTF-8',
            'LC_ALL': 'en_US.UTF-8',
            'ENGINE_DEFAULTS': config.ENGINE_DEFAULT_FILE,
            'ENGINE_VARS': config.ENGINE_VARS,
            'ENGINE_ETC': self._config.getString('ENGINE_ETC'),
            'ENGINE_LOG': self._config.getString('ENGINE_LOG'),
            'ENGINE_TMP': self._config.getString('ENGINE_TMP'),
            'ENGINE_USR': self._config.getString('ENGINE_USR'),
            'ENGINE_VAR': self._config.getString('ENGINE_VAR'),
            'ENGINE_CACHE': self._config.getString('ENGINE_CACHE'),
        })

    def daemonStdHandles(self):
        consoleLog = open(
            os.path.join(
                self._config.getString('ENGINE_LOG'),
                'console.log'
            ),
            'w+',
        )
        return (consoleLog, consoleLog)

    def daemonContext(self):
        self.daemonAsExternalProcess(
            executable=self._executable,
            args=self._engineArgs,
            env=self._engineEnv,
            stopTime=self._config.getInteger(
                'ENGINE_STOP_TIME'
            ),
            stopInterval=self._config.getInteger(
                'ENGINE_STOP_INTERVAL'
            ),
        )

    def daemonCleanup(self):
        self._tempDir.destroy()


if __name__ == '__main__':
    service.setupLogger()
    d = Daemon()
    d.run()


# vim: expandtab tabstop=4 shiftwidth=4
