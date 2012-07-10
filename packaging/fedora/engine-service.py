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

# Start/stop oVirt Engine
#
# chkconfig: - 65 34
# description: oVirt Engine
# pidfile: /var/run/ovirt-engine.pid

import configobj
import errno
import glob
import grp
import optparse
import os
import pwd
import resource
import signal
import stat
import string
import sys
import syslog
import time
import traceback

from Cheetah.Template import Template


# The name of the engine:
engineName = "engine-service"

# The engine system configuration variables:
engineSysconfig = None

# The name of the user and group that should run the service:
engineUser = None
engineGroup = None
engineUid = 0
engineGid = 0

# JBoss directories:
jbossHomeDir = None

# JBoss files:
jbossModulesJar = None

# Engine directories:
engineEtcDir = None
engineLogDir = None
engineTmpDir = None
engineUsrDir = None
engineVarDir = None
engineLockDir = None
engineContentDir = None
engineDeploymentsDir = None
engineEarDir = None

# Engine files:
enginePidFile = None
engineLoggingFile = None
engineConfigTemplateFile = None
engineConfigFile = None
engineLogFile = None
engineBootLogFile = None
engineConsoleLogFile = None
engineServerLogFile = None


def loadSysconfig():
    # Load the configuration file:
    engineSysconfigFile = "/etc/sysconfig/ovirt-engine"
    if not os.path.exists(engineSysconfigFile):
        raise Exception("The engine sysconfig file \"%s\" doesn't exist." % engineSysconfigFile)
    global engineSysconfig
    engineSysconfig = configobj.ConfigObj(engineSysconfigFile)

    # Get the id of the engine user:
    global engineUser
    global engineUid
    engineUser = getSysconfig("ENGINE_USER", "ovirt")
    try:
        engineUid = pwd.getpwnam(engineUser).pw_uid
    except:
        raise Exception("The engine user \"%s\" doesn't exist." % engineUser)

    # Get id of the engine group:
    global engineGroup
    global engineGid
    engineGroup = getSysconfig("ENGINE_GROUP", "ovirt")
    try:
        engineGid = grp.getgrnam(engineGroup).gr_gid
    except:
        raise Exception("The engine group \"%s\" doesn't exist." % engineGroup)

    # JBoss directories:
    global jbossHomeDir
    jbossHomeDir = getSysconfig("JBOSS_HOME", "/usr/share/jboss-as")

    # JBoss files:
    global jbossModulesJar
    jbossModulesJar = os.path.join(jbossHomeDir, "jboss-modules.jar")

    # Engine directories:
    global engineEtcDir
    global engineLogDir
    global engineTmpDir
    global engineUsrDir
    global engineVarDir
    global engineLockDir
    global engineServiceDir
    global engineContentDir
    global engineDeploymentsDir
    global engineEarDir
    engineEtcDir = getSysconfig("ENGINE_ETC", "/etc/ovirt-engine")
    engineLogDir = getSysconfig("ENGINE_LOG", "/var/log/ovirt-engine")
    engineTmpDir = getSysconfig("ENGINE_TMP", "/var/cache/ovirt-engine")
    engineUsrDir = getSysconfig("ENGINE_USR", "/usr/share/ovirt-engine")
    engineVarDir = getSysconfig("ENGINE_VAR", "/var/lib/ovirt-engine")
    engineLockDir = getSysconfig("ENGINE_LOCK", "/var/lock/ovirt-engine")
    engineServiceDir = os.path.join(engineUsrDir, "service")
    engineContentDir = os.path.join(engineVarDir, "content")
    engineDeploymentsDir = os.path.join(engineVarDir, "deployments")
    engineEarDir = os.path.join(engineUsrDir, "engine.ear")

    # Engine files:
    global enginePidFile
    global engineLoggingFile
    global engineConfigTemplateFile
    global engineConfigFile
    global engineLogFile
    global engineBootLogFile
    global engineConsoleLogFile
    global engineServerLogFile
    enginePidFile = getSysconfig("ENGINE_PID", "/var/run/ovirt-engine.pid")
    engineLoggingFile = os.path.join(engineServiceDir, "engine-service-logging.properties")
    engineConfigTemplateFile = os.path.join(engineServiceDir, "engine-service.xml.in")
    engineConfigFile = os.path.join(engineTmpDir, "engine-service.xml")
    engineLogFile = os.path.join(engineLogDir, "engine.log")
    engineBootLogFile = os.path.join(engineLogDir, "boot.log")
    engineConsoleLogFile = os.path.join(engineLogDir, "console.log")
    engineServerLogFile = os.path.join(engineLogDir, "server.log")


def getSysconfig(variable, default=None):
    # Then try with the environment (it overrides the config file):
    value = os.getenv(variable)
    if value:
        return value

    # Then try with the config file:
    value = engineSysconfig.get(variable)
    if value:
        return value

    # Finally use the default value:
    return default


def checkIdentity():
    if os.getuid() != 0:
        raise Exception("This script should run with the root user.")


def checkOwnership(name, uid=None, gid=None):
    # Get the metadata of the file:
    st = os.stat(name)

    # Check that the file is owned by the given user:
    if uid and st[stat.ST_UID] != uid:
        user = pwd.getpwuid(uid).pw_name
        owner = pwd.getpwuid(st[stat.ST_UID]).pw_name
        if os.path.isdir(name):
            raise Exception("The directory \"%s\" is not owned by user \"%s\", but by \"%s\"." % (name, user, owner))
        else:
            raise Exception("The file \"%s\" is not owned by user \"%s\", but by \"%s\"." % (name, user, owner))

    # Check that the file is owned by the given group:
    if gid and st[stat.ST_GID] != gid:
        group = grp.getgrgid(gid).gr_name
        owner = grp.getgrgid(st[stat.ST_GID]).gr_name
        if os.path.isdir(name):
            raise Exception("The directory \"%s\" is not owned by group \"%s\", but by \"%s\"." % (name, group, owner))
        else:
            raise Exception("The file \"%s\" is not owned by group \"%s\", but by \"%s\"." % (name, group, owner))


def checkDirectory(name, uid=None, gid=None):
    if not os.path.isdir(name):
        raise Exception("The directory \"%s\" doesn't exist." % name)
    checkOwnership(name, uid, gid)


def checkFile(name, uid=None, gid=None):
    if not os.path.isfile(name):
        raise Exception("The file \"%s\" doesn't exist." % name)
    checkOwnership(name, uid, gid)


def checkLog(name):
    log = os.path.join(engineLogDir, name)
    if os.path.exists(log):
        checkOwnership(log, engineUid, engineGid)


def checkInstallation():
    # Check the required JBoss directories and files:
    checkDirectory(jbossHomeDir)
    checkFile(jbossModulesJar)

    # Check the required engine directories and files:
    checkDirectory(engineEtcDir, uid=engineUid, gid=engineGid)
    checkDirectory(engineLogDir, uid=engineUid, gid=engineGid)
    checkDirectory(engineUsrDir, uid=0, gid=0)
    checkDirectory(engineVarDir, uid=engineUid, gid=engineGid)
    checkDirectory(engineLockDir, uid=engineUid, gid=engineGid)
    checkDirectory(engineServiceDir, uid=0, gid=0)
    checkDirectory(engineContentDir, uid=engineUid, gid=engineGid)
    checkDirectory(engineDeploymentsDir, uid=engineUid, gid=engineGid)
    checkDirectory(engineTmpDir, uid=engineUid, gid=engineGid)
    checkDirectory(engineEarDir, uid=0, gid=0)
    checkFile(engineLoggingFile)
    checkFile(engineConfigTemplateFile)

    # Check that log files are owned by the engine user, if they exist:
    checkLog(engineLogFile)
    checkLog(engineBootLogFile)
    checkLog(engineConsoleLogFile)
    checkLog(engineServerLogFile)

    # XXX: Add more checks here!


def loadEnginePid():
    if not os.path.exists(enginePidFile):
        return None
    with open(enginePidFile, "r") as enginePidFd:
        return int(enginePidFd.read())


def saveEnginePid(pid):
    with open(enginePidFile, "w") as enginePidFd:
        enginePidFd.write(str(pid) + "\n")


def removeEnginePid():
    if os.path.exists(enginePidFile):
        os.remove(enginePidFile)


def startEngine():
    # Get the PID:
    enginePid = loadEnginePid()
    if enginePid:
        syslog.syslog(syslog.LOG_WARNING, "The engine PID file \"%s\" already exists." % enginePidFile)
        return

    # Make sure the engine archive directory is linked in the deployments
    # directory, if not link it now:
    engineEarLink = os.path.join(engineDeploymentsDir, "engine.ear")
    if not os.path.islink(engineEarLink):
        syslog.syslog(syslog.LOG_INFO, "The symbolic link \"%s\" doesn't exist, will create it now." % engineEarLink)
        try:
            os.symlink(engineEarDir, engineEarLink)
        except:
            raise Exception("Can't create symbolic link from \"%s\" to \"%s\"." % (engineEarLink, engineEarDir))

    # Remove all existing deployment markers:
    for markerFile in glob.glob("%s.*" % engineEarLink):
        try:
            os.remove(markerFile)
        except:
            raise Exception("Can't remove deployment marker file \"%s\"." % markerFile)

    # Create the new marker file to trigger deployment of the engine:
    markerFile = "%s.dodeploy" % engineEarLink
    try:
        markerFd = open(markerFile, "w")
        markerFd.close()
    except:
        raise Exception("Can't create deployment marker file \"%s\"." % markerFile)

    # Generate the main configuration from the template and copy it to the
    # configuration directory making sure that the application server will be
    # able to write to it:
    engineConfigTemplate = Template(file=engineConfigTemplateFile, searchList=[engineSysconfig])
    engineConfigText = str(engineConfigTemplate)
    with open(engineConfigFile, "w") as engineConfigFd:
        engineConfigFd.write(engineConfigText)
        os.chown(engineConfigFile, engineUid, engineGid)

    # Get heap configuration parameters from the environment or use defaults if
    # they are not provided:
    engineHeapMin = getSysconfig("ENGINE_HEAP_MIN", "1g")
    engineHeapMax = getSysconfig("ENGINE_HEAP_MAX", "1g")
    enginePermMin = getSysconfig("ENGINE_PERM_MIN", "256m")
    enginePermMax = getSysconfig("ENGINE_PERM_MAX", "256m")

    # Module path should include first the engine modules so that they can override
    # those provided by the application server if needed:
    jbossModulesDir = os.path.join(jbossHomeDir, "modules")
    engineModulesDir = os.path.join(engineUsrDir, "modules")
    engineModulePath = "%s:%s" % (engineModulesDir, jbossModulesDir)

    # We start with an empty list of arguments:
    engineArgs = []

    # Add arguments for the java virtual machine:
    engineArgs.extend([
        # The name or the process, as displayed by ps:
        engineName,

        # Virtual machine options:
        "-server",
        "-XX:+UseCompressedOops",
        "-XX:+TieredCompilation",
        "-Xms%s" % engineHeapMin,
        "-Xms%s" % engineHeapMax,
        "-XX:PermSize=%s" % enginePermMin,
        "-XX:MaxPermSize=%s" % enginePermMax,
        "-Djava.net.preferIPv4Stack=true",
        "-Dsun.rmi.dgc.client.gcInterval=3600000",
        "-Dsun.rmi.dgc.server.gcInterval=3600000",
        "-Djava.awt.headless=true",
    ])

    # Add extra system properties provided in the configuration:
    engineProperties = getSysconfig("ENGINE_PROPERTIES")
    if engineProperties:
        for engineProperty in engineProperties.split():
            if not engineProperty.startswith("-D"):
                engineProperty = "-D" + engineProperty
            engineArgs.append(engineProperty)

    # Add arguments for remote debugging of the java virtual machine:
    engineDebugAddress = getSysconfig("ENGINE_DEBUG_ADDRESS")
    if engineDebugAddress:
        engineArgs.append("-Xrunjdwp:transport=dt_socket,address=%s,server=y,suspend=n" % engineDebugAddress)

    # Enable verbose garbage collection if required:
    engineVerboseGC = getSysconfig("ENGINE_VERBOSE_GC", "false").lower()
    if engineVerboseGC in [ "t", "true", "y", "yes" ]:
        engineArgs.extend([
            "-verbose:gc",
            "-XX:+PrintGCTimeStamps",
            "-XX:+PrintGCDetails",
        ])

    # Add arguments for JBoss:
    engineArgs.extend([
        "-Djava.util.logging.manager=org.jboss.logmanager",
        "-Dlogging.configuration=file://%s" % engineLoggingFile,
        "-Dorg.jboss.resolver.warning=true",
        "-Djboss.modules.system.pkgs=org.jboss.byteman",
        "-Djboss.server.default.config=engine-service",
        "-Djboss.home.dir=%s" % jbossHomeDir,
        "-Djboss.server.base.dir=%s" % engineUsrDir,
        "-Djboss.server.config.dir=%s" % engineTmpDir,
        "-Djboss.server.data.dir=%s" % engineVarDir,
        "-Djboss.server.log.dir=%s" % engineLogDir,
        "-Djboss.server.temp.dir=%s" % engineTmpDir,
        "-Djboss.controller.temp.dir=%s" % engineTmpDir,
        "-jar", jbossModulesJar,
        "-mp", engineModulePath,
        "-jaxpmodule", "javax.xml.jaxp-provider",
        "org.jboss.as.standalone", "-c", os.path.basename(engineConfigFile),
    ])

    # Fork a new process:
    enginePid = os.fork()

    # If this is the parent process then the last thing we have to do is
    # saving the child process PID to the file:
    if enginePid != 0:
        syslog.syslog(syslog.LOG_INFO, "Started engine process %d." % enginePid)
        saveEnginePid(enginePid)
        return

    # Change the resource limits while we are root as we won't be
    # able to change them once we assume the engine identity:
    engineNofile = int(getSysconfig("ENGINE_NOFILE", "65535"))
    resource.setrlimit(resource.RLIMIT_NOFILE, (engineNofile, engineNofile))

    # This is the child process, first thing we do is assume the engine
    # identity:
    os.setgid(engineGid)
    os.setuid(engineUid)

    # Then close standard input and some other security measures:
    os.close(0)
    os.setsid()
    os.chdir("/")

    # Then open the console log and redirect standard output and errors to it:
    engineConsoleFd = os.open(engineConsoleLogFile, os.O_CREAT | os.O_WRONLY | os.O_APPEND, 0660)
    os.dup2(engineConsoleFd, 1)
    os.dup2(engineConsoleFd, 2)
    os.close(engineConsoleFd)

    # Prepare a clean environment:
    engineEnv = {
        "PATH": "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin",
        "LANG": "en_US.UTF-8",
        "ENGINE_ETC": engineEtcDir,
        "ENGINE_LOG": engineLogDir,
        "ENGINE_TMP": engineTmpDir,
        "ENGINE_USR": engineUsrDir,
        "ENGINE_VAR": engineVarDir,
        "ENGINE_LOCK": engineLockDir,
    }

    # Finally execute the java virtual machine:
    os.execvpe("java", engineArgs, engineEnv)


def stopEngine():
    # Load the PID:
    enginePid = loadEnginePid()
    if not enginePid:
        syslog.syslog(syslog.LOG_INFO, "The engine PID file \"%s\" doesn't exist." % enginePidFile)
        return

    # First check that the process exists:
    if not os.path.exists("/proc/%d" % enginePid):
        syslog.syslog(syslog.LOG_WARNING, "The engine PID file \"%s\" contains %d, but that process doesn't exist, will just remove the file." % (enginePidFile, enginePid))
        removeEnginePid()
        return

    # Get the time to wait for the engine to stop from the configuration:
    stopTime = int(getSysconfig("ENGINE_STOP_TIME", "10"))
    stopInterval = int(getSysconfig("ENGINE_STOP_INTERVAL", "1"))

    # Kill the process softly and wait for it to dissapear or for the timeout
    # to expire:
    os.kill(enginePid, signal.SIGTERM)
    initialTime = time.time()
    timeElapsed = 0
    while os.path.exists("/proc/%d" % enginePid):
        syslog.syslog(syslog.LOG_INFO, "Waiting up to %d seconds for engine process %d to finish." % ((stopTime - timeElapsed), enginePid))
        timeElapsed = time.time() - initialTime
        if timeElapsed > stopTime:
            break
        time.sleep(stopInterval)

    # If the process didn't dissapear after the allowed time then we forcibly
    # kill it:
    if os.path.exists("/proc/%d" % enginePid):
        syslog.syslog(syslog.LOG_WARNING, "The engine process %d didn't finish after waiting %d seconds, killing it." % (enginePid, timeElapsed))
        os.kill(enginePid, signal.SIGKILL)
        syslog.syslog(syslog.LOG_WARNING, "Killed engine process %d." % enginePid)
    else:
        syslog.syslog(syslog.LOG_INFO, "Stopped engine process %d." % enginePid)

    # And finally we remove the PID file:
    removeEnginePid()


def checkEngine():
    # First check that the engine PID file exists:
    enginePid = loadEnginePid()
    if not enginePid:
        raise Exception("The engine PID file \"%s\" doesn't exist." % enginePidFile)

    # Now check that the process exists:
    if not os.path.exists("/proc/%d" % enginePid):
        raise Exception("The engine PID file \"%s\" contains %d, but that process doesn't exist." % (enginePidFile, enginePid))

    # XXX: Here we could check deeper the status of the engine sending a
    # request to the health status servlet.
    syslog.syslog(syslog.LOG_INFO, "Engine process %d is running." % enginePid)


def showUsage():
    print("Usage: %s {start|stop|restart|status}" % engineName)


def prettyAction(label, action):
    # Determine the colors to use according to the type of terminal:
    colorNormal = ""
    colorSuccess = ""
    colorFailure = ""
    moveColumn = ""
    if os.getenv("TERM") in ["linux", "xterm"]:
        colorNormal = "\033[0;39m"
        colorSuccess = "\033[0;32m"
        colorFailure  = "\033[0;31m"
        moveColumn = "\033[60G"

    # Inform that we are doing the job:
    sys.stdout.write(label + " " + engineName + ":")
    sys.stdout.flush()

    # Do the real action:
    try:
        action()
        sys.stdout.write(moveColumn + " [  " + colorSuccess + "OK" + colorNormal + "  ]\n")
    except Exception as exception:
        sys.stdout.write(moveColumn + " [" + colorFailure + "FAILED" + colorNormal + "]\n")
        raise


def main():
    # Open connection to the syslog daemon:
    syslog.openlog(engineName, syslog.LOG_PID)

    # Check the arguments:
    args = sys.argv[1:]
    if len(args) != 1 or not args[0] in [ "start", "stop", "restart", "status" ]:
        showUsage()
        sys.exit(1)

    try:
        # Load the configuration:
        loadSysconfig()

        # Do some important checks:
        checkIdentity()
        checkInstallation()

        # Perform the requested action:
        action = args[0].lower()
        if action == "start":
            prettyAction("Starting", startEngine)
        elif action == "stop":
            prettyAction("Stopping", stopEngine)
        elif action == "restart":
            prettyAction("Stopping", stopEngine)
            prettyAction("Starting", startEngine)
        elif action == "status":
            try:
                checkEngine()
                print("ovirt-engine is running")
            except:
                print("ovirt-engine is stopped")
                raise
    except Exception as exception:
        #traceback.print_exc()
        syslog.syslog(syslog.LOG_ERR, str(exception))
        sys.exit(1)
    else:
        sys.exit(0)

    # Close connection to syslog:
    syslog.closelog()


if __name__ == "__main__":
    main()
