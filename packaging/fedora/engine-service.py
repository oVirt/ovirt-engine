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

import errno
import glob
import grp
import optparse
import os
import pwd
import signal
import stat
import string
import sys
import time
import traceback

# The name of the user and group that should run the service:
engineUser = os.getenv("ENGINE_USER", "ovirt")
engineGroup = os.getenv("ENGINE_GROUP", "ovirt")
engineUid = 0
engineGid = 0

# JBoss directories:
jbossHomeDir = os.getenv("JBOSS_HOME", "/usr/share/jboss-as")

# JBoss files:
jbossModulesJar = os.path.join(jbossHomeDir, "jboss-modules.jar")

# Engine directories:
engineEtcDir = os.getenv("ENGINE_ETC", "/etc/ovirt-engine")
engineLogDir = os.getenv("ENGINE_LOG", "/var/log/ovirt-engine")
engineTmpDir = os.getenv("ENGINE_TMP", "/var/cache/ovirt-engine")
engineUsrDir = os.getenv("ENGINE_USR", "/usr/share/ovirt-engine")
engineVarDir = os.getenv("ENGINE_VAR", "/var/lib/ovirt-engine")
engineContentDir = os.path.join(engineVarDir, "content")
engineDeploymentsDir = os.path.join(engineVarDir, "deployments")
engineEarDir = os.path.join(engineUsrDir, "engine.ear")

# Engine files:
enginePidFile = os.getenv("ENGINE_PID", "/var/run/ovirt-engine.pid")
engineLoggingFile = os.path.join(engineEtcDir, "engine-service-logging.properties")
engineConfigFile = os.path.join(engineEtcDir, "engine-service.xml")
engineConsoleFile = os.path.join(engineLogDir, "console.log")

# Time to wait for the engine to finish:
engineStopTime = int(os.getenv("ENGINE_STOP_TIME", "10"))
engineStopInterval = int(os.getenv("ENGINE_STOP_INTERVAL", "1"))

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

def checkInstallation():
    # Get and check the ids of the engine user and group:
    global engineUid
    global engineGid
    try:
        engineUid = pwd.getpwnam(engineUser).pw_uid
    except:
        raise Exception("The engine user \"%s\" doesn't exist." % engineUser)
    try:
        engineGid = grp.getgrnam(engineGroup).gr_gid
    except:
        raise Exception("The engine group \"%s\" doesn't exist." % engineGroup)

    # Check the required JBoss directories and files:
    checkDirectory(jbossHomeDir)
    checkFile(jbossModulesJar)

    # Check the required engine directories and files:
    checkDirectory(engineEtcDir, uid=engineUid, gid=engineGid)
    checkDirectory(engineLogDir, uid=engineUid, gid=engineGid)
    checkDirectory(engineUsrDir, uid=0, gid=0)
    checkDirectory(engineVarDir, uid=engineUid, gid=engineGid)
    checkDirectory(engineContentDir, uid=engineUid, gid=engineGid)
    checkDirectory(engineDeploymentsDir, uid=engineUid, gid=engineGid)
    checkDirectory(engineTmpDir, uid=engineUid, gid=engineGid)
    checkDirectory(engineEarDir, uid=0, gid=0)
    checkFile(engineLoggingFile)
    checkFile(engineConfigFile)

    # If the engine console file exists then check that it has the right
    # ownership, otherwise the engine will not be able to write to it:
    if os.path.exists(engineConsoleFile):
        checkOwnership(engineConsoleFile, uid=engineUid, gid=engineGid)

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
        print("The engine PID file \"%s\" already exists." % enginePidFile)
        return

    # Make sure the engine archive directory is linked in the deployments
    # directory, if not link it now:
    engineEarLink = os.path.join(engineDeploymentsDir, "engine.ear")
    if not os.path.islink(engineEarLink):
        print("The symbolic link \"%s\" doesn't exist, will create it now." % engineEarLink)
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

    # Get heap configuration parameters from the environment or use defaults if
    # they are not provided:
    engineHeapMin = os.getenv("ENGINE_HEAP_MIN", "1g")
    engineHeapMax = os.getenv("ENGINE_HEAP_MAX", "1g")
    enginePermMin = os.getenv("ENGINE_PERM_MIN", "256m")
    enginePermMax = os.getenv("ENGINE_PERM_MAX", "256m")

    # Module path should include first the engine modules so that they can override
    # those provided by the application server if needed:
    jbossModulesDir = os.path.join(jbossHomeDir, "modules")
    engineModulesDir = os.path.join(engineUsrDir, "modules")
    engineModulePath = "%s:%s" % (engineModulesDir, jbossModulesDir)

    # We start with an empty list of arguments:
    engineArgs = []

    # Add arguments for the java virtual machine:
    engineArgs.extend([
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

    # Add arguments for remote debugging of the java virtual machine:
    engineDebugAddress = os.getenv("ENGINE_DEBUG_ADDRESS")
    if engineDebugAddress:
        engineArgs.extend([
            "-Xrunjdwp:transport=dt_socket,address=%s,server=y,suspend=n" % engineDebugAddress,
        ])

    # Enable verbose garbage collection if required:
    engineVerboseGC = os.getenv("ENGINE_VERBOSE_GC", "false").lower()
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
        "-Djboss.server.config.dir=%s" % engineEtcDir,
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
        print("Started engine process %d." % enginePid)
        saveEnginePid(enginePid)
        return

    # This is the child process, first thing we do is assume the engine
    # identity:
    os.setgid(engineGid)
    os.setuid(engineUid)

    # Then close standard input and some other security measures:
    os.close(0)
    os.setsid()
    os.chdir("/")

    # Then open the console log and redirect standard output and errors to it:
    engineConsoleFd = os.open(engineConsoleFile, os.O_CREAT | os.O_WRONLY | os.O_APPEND)
    os.dup2(engineConsoleFd, 1)
    os.dup2(engineConsoleFd, 2)
    os.close(engineConsoleFd)

    # Finally execute the java virtual machine:
    os.execvp("java", engineArgs)

def stopEngine():
    # Load the PID:
    enginePid = loadEnginePid()
    if not enginePid:
        print("The engine PID file \"%s\" doesn't exist." % enginePidFile)
        return

    # First check that the process exists:
    if not os.path.exists("/proc/%d" % enginePid):
        raise Exception("The engine PID file \"%s\" contains %d, but that process doesn't exist." % (enginePidFile, enginePid))

    # Kill the process softly and wait for it to dissapear or for the timeout
    # to expire:
    os.kill(enginePid, signal.SIGTERM)
    initialTime = time.time()
    timeElapsed = 0
    while os.path.exists("/proc/%d" % enginePid):
        print("Waiting up to %d seconds for engine process %d to finish." % ((engineStopTime - timeElapsed), enginePid))
        timeElapsed = time.time() - initialTime
        if timeElapsed > engineStopTime:
            break
        time.sleep(engineStopInterval)

    # If the process didn't dissapear after the allowed time then we forcibly
    # kill it:
    if os.path.exists("/proc/%d" % enginePid):
        print("The engine process %d didn't finish after waiting %d seconds, killing it." % (enginePid, timeElapsed))
        os.kill(enginePid, signal.SIGKILL)
        print("Killed engine process %d." % enginePid)
    else:
        print("Stopped engine process %d." % enginePid)

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
    print("Engine process %d is running." % enginePid)

def showUsage():
    print("Usage: %s {start|stop|status}" % os.path.basename(sys.argv[0]))


def main():
    # Check the arguments:
    args = sys.argv[1:]
    if len(args) != 1 or not args[0] in [ "start", "stop", "status" ]:
        showUsage()
        sys.exit(1)

    try:
        # Check the identity of the user running the script:
        checkIdentity()

        # Check sanity of the installation:
        checkInstallation()

        # Perform the requested action:
        action = args[0].lower()
        if action == "start":
            startEngine()
        elif action == "stop":
            stopEngine()
        elif action == "status":
            checkEngine()
    except Exception as exception:
        #traceback.print_exc()
        print(exception)
        sys.exit(1)
    else:
        sys.exit(0)

if __name__ == "__main__":
    main()
