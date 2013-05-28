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


import os
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine')


import config
import service


class Daemon(service.Daemon):

    def __init__(self):
        super(Daemon, self).__init__()

    def _checkInstallation(
        self,
        pidfile,
        jbossModulesJar,
        java,
    ):
        # Check that the Java home directory exists and that it contais at
        # least the java executable:
        self.check(
            name=self._config.get('JAVA_HOME'),
            directory=True,
        )
        self.check(
            name=java,
            executable=True,
        )

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
            os.path.join(
                self._config.get('ENGINE_LOG'),
                'notifier',
            ),
            directory=True,
            writable=True,
        )
        for log in ('notifier.log', 'console.log'):
            self.check(
                name=os.path.join(
                    self._config.get("ENGINE_LOG"),
                    'notifier',
                    log,
                ),
                mustExist=False,
                writable=True,
            )
        if pidfile is not None:
            self.check(
                name=pidfile,
                writable=True,
                mustExist=False,
            )

    def daemonSetup(self):

        if os.geteuid() == 0:
            raise RuntimeError(
                _('This service cannot be executed as root')
            )

        if not os.path.exists(config.ENGINE_NOTIFIER_DEFAULT_FILE):
            raise RuntimeError(
                _(
                    "The configuration defaults file '{file}' "
                    "required but missing"
                ).format(
                    file=config.ENGINE_NOTIFIER_DEFAULT_FILE,
                )
            )

        self._config = service.ConfigFile(
            (
                config.ENGINE_NOTIFIER_DEFAULT_FILE,
                config.ENGINE_NOTIFIER_VARS,
            ),
        )

        jbossModulesJar = os.path.join(
            self._config.get('JBOSS_HOME'),
            'jboss-modules.jar',
        )
        java = os.path.join(
            self._config.get('JAVA_HOME'),
            'bin',
            'java',
        )

        self._checkInstallation(
            pidfile=self.pidfile,
            jbossModulesJar=jbossModulesJar,
            java=java,
        )

        self._executable = java

        self._engineArgs = [
            'ovirt-engine-notifier',
            '-Dlog4j.configuration=file://%s/notifier/log4j.xml' % (
                self._config.get('ENGINE_ETC'),
            ),
            '-Djboss.modules.write-indexes=false',
            '-jar', jbossModulesJar,
            '-dependencies', 'org.ovirt.engine.core.tools',
            '-class', 'org.ovirt.engine.core.notifier.Notifier',
        ]

        self._engineEnv = os.environ.copy()
        self._engineEnv.update({
            'PATH': '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin',
            'LANG': 'en_US.UTF-8',
            'LC_ALL': 'en_US.UTF-8',
            'CLASSPATH': '',
            'JAVA_MODULEPATH': ':'.join([
                os.path.join(
                    self._config.get('ENGINE_USR'),
                    'modules',
                ),
                os.path.join(
                    self._config.get('JBOSS_HOME'),
                    'modules',
                ),
            ]),
            'ENGINE_DEFAULTS': config.ENGINE_DEFAULT_FILE,
            'ENGINE_VARS': config.ENGINE_VARS,
            'ENGINE_NOTIFIER_DEFAULTS': config.ENGINE_NOTIFIER_DEFAULT_FILE,
            'ENGINE_NOTIFIER_VARS': config.ENGINE_NOTIFIER_VARS,
        })

    def daemonStdHandles(self):
        consoleLog = open(
            os.path.join(
                self._config.get('ENGINE_LOG'),
                'notifier',
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
            stopTime=self._config.getinteger(
                'NOTIFIER_STOP_TIME'
            ),
            stopInterval=self._config.getinteger(
                'NOTIFIER_STOP_INTERVAL'
            ),
        )


if __name__ == '__main__':
    service.setupLogger()
    d = Daemon()
    d.run()


# vim: expandtab tabstop=4 shiftwidth=4
