import sos.plugintools
import os
import zipfile
import platform
import fnmatch
import shlex
import subprocess
import string
import grp, pwd

class DirTree(object):
    """Builds an ascii representation of a directory structure"""

    def __init__(self, top_directory):
        self.directory_count = 0
        self.file_count = 0
        self.buffer = []
        self.top_directory = top_directory
        self._build_tree()

    def buf(self, s):
        self.buffer.append(s)

    def printtree(self):
        print self.as_string()

    def as_string(self):
        return "\n".join(self.buffer)

    def _build_tree(self):
        self.buf(os.path.abspath(self.top_directory))
        self.tree_i(self.top_directory, first=True)


    def _convert_bytes(self, n):
        K, M, G, T = 1 << 10, 1 << 20, 1 << 30, 1 << 40
        if n >= T:
            return '%.1fT' % (float(n) / T)
        elif n >= G:
            return '%.1fG' % (float(n) / G)
        elif n >= M:
            return '%.1fM' % (float(n) / M)
        elif n >= K:
            return '%.1fK' % (float(n) / K)
        else:
            return '%d' % n

    def _format(self, path):
        """Conditionally adds detail to paths"""
        stats = os.stat(path)
        details = {
                "filename": os.path.basename(path),
                "user": pwd.getpwuid(stats.st_uid)[0],
                "group": grp.getgrgid(stats.st_gid)[0],
                "filesize": self._convert_bytes(stats.st_size),
                }
        return "[%(user)s %(group)s %(filesize)s] %(filename)s" % details

    def tree_i(self, dir_, padding='', first=False):
        if not first:
            self.buf(padding[:-1] + "+-- " + self._format(os.path.abspath(dir_)))
            padding += '   '

        count = 0
        files = os.listdir(dir_)
        files.sort(key=string.lower)
        for f in files:
            count += 1
            path = os.path.join(dir_, f)

            if f.startswith("."):
                pass
            elif os.path.isfile(path):
                self.file_count += 1
                self.buf(padding + '+-- ' + self._format(path))
            elif os.path.islink(path):
                self.buf(padding + '+-- ' + f + ' -> ' + os.path.basename(os.path.realpath(path)))
                if os.path.isdir(path):
                    self.directory_count += 1
                else:
                    self.file_count += 1
            elif os.path.isdir(path):
                self.directory_count += 1
                if count == len(files):
                    self.tree_i(path, padding + ' ')
                else:
                    self.tree_i(path, padding + '|')


def find(file_pattern, top_dir, max_depth=None, path_pattern=None):
    """generate function to find files recursively. Usage:

    for filename in find("*.properties", /var/log/foobar):
        print filename
    """
    if max_depth:
        base_depth = os.path.dirname(top_dir).count(os.path.sep)
        max_depth += base_depth

    for path, dirlist, filelist in os.walk(top_dir):
        if max_depth and path.count(os.path.sep) >= max_depth:
            del dirlist[:]

        if path_pattern and not fnmatch.fnmatch(path, path_pattern):
            continue

        for name in fnmatch.filter(filelist, file_pattern):
            yield os.path.join(path,name)


class jboss(sos.plugintools.PluginBase):
    """JBoss related information
    """

    optionList = [("home",  'JBoss\'s installation dir (i.e. JBOSS_HOME)', '', False),
                  ("javahome",  'Java\'s installation dir (i.e. JAVA_HOME)', '', False),
                  ("profile", 'Quoted and space separated list of server profiles to limit collection. \
Default=\'all default minimal production standard web\'.', '', False),
                  ("user",  'JBoss JMX invoker user to be used with twiddle.', '', False),
                  ("pass",  'JBoss JMX invoker user\'s password to be used with twiddle.', '', False),
                  ("logsize", 'max size (MiB) to collect per log file', '', 15),
                  ("stdjar",  'Collect jar statistics for standard jars.', '', True),
                  ("servjar",  'Collect jar statistics from any server configuration dirs.', '', True),
                  ("twiddle",  'Collect twiddle data.', '', True),
                  ("appxml",  'Quoted and space separated list of application\'s whose XML descriptors you want. The keyword \"all\" will collect all descriptors in the designated profile(s).', '', False)]

    __MD5_CHUNK_SIZE=128
    __jbossHome=None
    __haveJava=False
    __twiddleCmd=None
    __jbossSystemJarDirs = [ "client", "lib" , "common/lib" ]
    __jbossServerConfigDirs = ["all", "default", "minimal", "production", "standard", "web"]
    __jbossHTMLBody=None

    def __getJbossHome(self):
        """
        Will attempt to locate the JBoss installation dir in either jboss.home or
        scrape it from the environment variable JBOSS_HOME.
        Returns:
            True JBOSS_HOME is set and the path exists.  False otherwise.
        """
        if self.getOption("home"):
            ## Prefer this value first over the ENV
            self.__jbossHome=self.getOption("home")
            self.addAlert("INFO: The JBoss installation directory supplied to SOS is " +
                          self.__jbossHome)
        elif os.environ.get("JBOSS_HOME"):
            self.__jbossHome=os.environ.get("JBOSS_HOME")
            self.addAlert("INFO: The JBoss installation directory (i.e. JBOSS_HOME) from the environment is " +
                          self.__jbossHome)
        else:
            self.addAlert("ERROR: The JBoss installation directory was not supplied.\
              The JBoss SOS plug-in cannot continue.")
            return False

        if os.path.exists(self.__jbossHome):
            ## We need to set  JBOSS_CLASSPATH otherwise some twiddle commands will not work.
            jbossClasspath=None
            tmp=os.path.join(self.__jbossHome, "lib")
            if os.path.exists(tmp):
                jbossClasspath=tmp + os.sep + "*" + os.pathsep
            else:
                self.addAlert("WARN: The JBoss lib directory does not exist.  Dir(%s) " %  tmp)

            tmp=os.path.join(self.__jbossHome, "common" , "lib")
            if os.path.exists(tmp):
                jbossClasspath+=tmp + os.sep + "*"
            else:
                self.addAlert("WARN: The JBoss lib directory does not exist.  Dir(%s) " %  tmp)

            os.environ['JBOSS_CLASSPATH']=jbossClasspath

            return True
        else:
            msg = "ERROR: The path to the JBoss installation directory does not exist.  Path is: " + self.__jbossHome
            print msg
            self.addAlert(msg)
            return False

    def __getJavaHome(self):
        """
        This SOS plug-in makes extensive use of JBoss' twiddle program and twiddle uses Java.  As such, we
        need to ensure that java and JAVA_HOME is known to the plug-in so that it can use Java.
        This function will put JAVA_HOME and JAVA_HOME/bin into the environment if they're not already
        there.
        """
        javaHome=None
        java="bin/java"

        if self.getOption("javahome"):
            ## Prefer this value first over the ENV
            javaHome=self.getOption("javahome")
            self.addAlert("INFO: The Java installation directory supplied to SOS is " +
                          javaHome)
        elif os.environ.get("JAVA_HOME"):
            javaHome=os.environ.get("JAVA_HOME")
            self.addAlert("INFO: The Java installation directory (i.e. JAVA_HOME) from the environment is " +
                          javaHome)
        else:
            ## Test to see if Java is already in the PATH
            (status, output, rtime) = self.callExtProg("java -version")
            if (status == 0):
                self.addAlert("INFO: The Java installation directory is in the system path.")
                return True
            else:
                self.addAlert("ERROR: The Java installation directory was not supplied.\
                The JBoss SOS plug-in will not collect twiddle data.")
                return False


        java=os.path.join(javaHome, java)
        if os.path.exists(java) and os.access(java, os.X_OK):
            os.environ['JAVA_HOME']=javaHome
            ## Place the supplied Java at the *head* of the path.
            os.environ['PATH'] = os.path.join(javaHome, "bin") + os.pathsep + os.environ['PATH']
            return True
        else:
            msg = "ERROR: The path to the Java installation directory does not exist.  Path is: %s" % (javaHome)
            print msg
            self.addAlert(msg)
            return False


    def __getJMXCredentials(self):
        """
        Read the JMX credentials from the option list.
        Returns:
            A formatted credential string for twiddle consumption if both user and pass
            are supplied.  None otherwise.
        """
        credential = None
        ## Let's make a best effort not to pass expansions or escapes to the shell
        ## by strong quoting the user's input
        if self.getOption("user"):
            credential=" -u '" + self.getOption("user") + "' "
            if self.getOption("pass"):
                credential+=" -p '" + self.getOption("pass") + "' "
            else:
                credential=None
        return credential

    def __updateServerConfigDirs(self):
        """
        By default this plug-in will attempt to collect logs from every
        JBoss server configuration directory (i.e. profile).  The
        user may have supplied a limited list, as such, we must respect
        that wish.
        Returns:
            Nothing.  Will update __jbossServerConfigDirs if the user
            supplied a limited list.
        """
        if self.getOption("profile"):
            profiles=self.getOption("profile")
            ## I'd rather use comma as the delimiter but getOption doesn't seem to be passing it through.
            ## Since we are using spaces as the delimiter, we need to filter out empty list elements
            ## if the user did something like ' all   default  web '.
            profiles=profiles.split(' ')
            ## Flter(None doesn't  work.  Allows 0.
            self.__jbossServerConfigDirs=filter(lambda x: len(x), profiles)
        return

    def __buildTwiddleCmd(self):
        """
        Utility function to build the twiddle command with/without credentials
        so that it can be used by later fcns.  If twiddle is found
        """
        ## In the off-chance that SOS is ever ported to cygwin or this plugin
        ## is ported to win...
        if platform.system() == "Windows":
            self.__twiddleCmd=os.path.join(self.__jbossHome, "bin", "twiddle.bat")
        else:
            self.__twiddleCmd=os.path.join(self.__jbossHome, "bin", "twiddle.sh")

        if os.path.exists(self.__twiddleCmd) and os.access(self.__twiddleCmd, os.X_OK):
            credential = self.__getJMXCredentials()
            if credential:
                self.__twiddleCmd += credential
        else:
            ## Reset twiddlecmd to None
            self.addAlert("ERROR: The twiddle program could not be found. Program=%s" % (self.__twiddleCmd))
            self.__twiddleCmd = None

        return

    def __createHTMLBodyStart(self):
        """
        The free-form HTML that can be inserted into the SOS report with addCustomText is within
        a <p> block.  We need to add a few pieces of HTML so that all of our subsequent data will
        be rendered properly.
        """
        self.__jbossHTMLBody = """
       <br/>
       <br/>
        <script type="text/javascript">
        <!--
        function show(h) {
          var tbl = document.getElementById(h);
          tbl.style.display = 'block';
        }
        function hide(h) {
              var tbl = document.getElementById(h);
              tbl.style.display = 'none';
            }
        // -->
        </script>
        <b>JBoss SOS Report Table of Contents</b>
    <ul style="list-style-type: square">
        <li><a href="#system-jar-info">JBoss System Jar Information</a>
        </li>
        <li><a href="#profile-jar-info">JBoss Server Configurations Jar Information</a>
        </li>
        <li><a href="#jboss-home-directory-tree">JBOSS_HOME Directory Tree</a>
        </li>
        <li><a href="#jboss-system-mbean-data">JBoss JMX MBean Data from <tt>jboss.system:*</tt></a>
        </li>
        <li><a href="#jboss-mbean-data">JBoss JMX MBean Data from <tt>jboss:*</tt></a>
        </li>
        <li><a href="#jboss-mbean-summary">JBoss MBean Summary</a>
        </li>
        <li><a href="#jboss-messaging">JBoss JMX Messaging MBean Data from  <tt>jboss.messaging:*</tt></a>
        </li>
        <li><a href="#jboss-j2ee">JBoss JMX J2EE MBean Data from <tt>jboss.j2ee:*</tt></a>
        </li>
        <li><a href="#jboss-vfs">JBoss JMX VFS MBean Data from <tt>jboss.vfs:*</tt></a>
        </li>
        <li><a href="#jboss-jsr77-data">JBoss JSR77 Data</a>
        </li>
    </ul>
    <br/>
    <br/>
        """

    def __getMd5(self, file):
        """
        Will perform an MD5 sum on a given file and return the file's message digest.  This function
        will not read the entire file into memory, instead, it will consume the file in 128 byte
        chunks.  This might be slightly slower but, the intent of a SOS report is to collect data from
        a system that could be under stress and we shouldn't stress it more by loading entire Jars into
        real memory.
        
        Note: This fcn expects hashlib; however, this isn't always available.  If it isn't then 
        we will use md5sum        
        """
        
        retVal="????????????????????????????????"
        
        try:
            import hashlib
            try:
                fd = open(file,"rb")
            except IOError, ioe:
                msg = "ERROR: Unable to open %s for reading.  Error: %s" % (file,ioe)
                print msg
                self.addAlert(msg)
                return retVal
    
            md5 = hashlib.md5()
            data = fd.read(self.__MD5_CHUNK_SIZE)
            while data:
                md5.update(data)
                data = fd.read(self.__MD5_CHUNK_SIZE)
            retVal = md5.hexdigest()            
        except ImportError, e:
            process = subprocess.Popen(['md5sum', file], 
                                       shell=False, 
                                       stdout=subprocess.PIPE, 
                                       stderr=subprocess.PIPE)
            result = process.communicate()
            if (process.returncode == 0):
                retVal = result[0].partition(' ')[0] 
            else:
                msg = "ERROR: Unable to compute md5sum of %s.  Msg (%s)" % (file, result[1])
                print msg
                self.addAlert(msg) 
            
        return retVal


    def __getManifest(self, jarFile):
        """
        Given a jar file, this function will extract the Manifest and return it's contents
        as a string.
        """
        manifest=None
        try:
            zf = zipfile.ZipFile(jarFile)
            try:
                manifest=zf.read("META-INF/MANIFEST.MF")
            except Exception, e:
                msg="ERROR: reading manifest from %s.  Error: %s" % (jarFile, e)
                print msg
                self.addAlert(msg)
            zf.close()
        except Exception, e:
                msg="ERROR: reading contents of %s.  Error: %s" % (jarFile, e)
                print msg
                self.addAlert(msg)
        return manifest

    def __getStdJarInfo(self):

        self.__jbossHTMLBody += """
    <div id="system-jar-info" style="font-weight: bold;">&ndash; JBoss System Jar Information</div>
        """

        for dir in self.__jbossSystemJarDirs:
            path=os.path.join(self.__jbossHome, dir)
            if os.path.exists(path):
                nicePath=path.replace(os.sep, "-")
                self.__jbossHTMLBody += """
    <div>
        &mdash; Summary of Jar Files in JBoss System Directory
        <tt>%s</tt>
        ( <a href="javascript:show('%s')">Show</a> / <a
            href="javascript:hide('%s')">Hide</a> ):
    </div>
    <div id="%s" style="overflow: hidden; display: none">
        <ul style="list-style-type: square">
                """ % (path,nicePath,nicePath,nicePath)

                found= False
                for jarFile in find("*.jar", path):
                    found= True
                    nicePath=jarFile.replace(os.sep, "-")
                    self.__jbossHTMLBody += """
                <li>Jar File: <tt>%s</tt><br/>
                    MD5: <tt>%s</tt>
                    <br /> Manifest File (
                    <a href="javascript:show('%s')">Show</a> /
                    <a href="javascript:hide('%s')">Hide</a> ):<br />
                    <div id="%s" style="overflow: hidden; display: none">
                        <pre>
                        %s
                        </pre>
                    </div>
                </li>
                            """ % (jarFile,
                                   self.__getMd5(jarFile),
                                   nicePath,
                                   nicePath,
                                   nicePath,
                                   self.__getManifest(jarFile))

                if not found:
                    self.addAlert("WARN: No jars found in JBoss system path (" + path + ").")
                self.__jbossHTMLBody += """
             </ul>
        </div>
                    """
            else:
                self.addAlert("ERROR: JBoss system path (" + path + ") does not exist.")
        return

    def __getServerConfigJarInfo(self, configDirAry):

        self.__jbossHTMLBody += """
    <br/>
    <br/>
    <div id="profile-jar-info" style="font-weight: bold;">&ndash; JBoss Server Configurations Jar Information</div>
        """
        for dir in configDirAry:
            serverDir = os.path.join("server", dir)
            path=os.path.join(self.__jbossHome, serverDir)
            if os.path.exists(path):
                nicePath=path.replace(os.sep, "-")
                self.__jbossHTMLBody += """
    <div>
        &mdash; Summary of Jar Files in the <tt>%s</tt> JBoss Server Configuration
        ( <a href="javascript:show('%s')">Show</a> / <a
            href="javascript:hide('%s')">Hide</a> ):
    </div>
    <div id="%s" style="overflow: hidden; display: none">
        <ul style="list-style-type: square">
                """ % (dir, nicePath,nicePath,nicePath)

                found = False
                for jarFile in find("*.jar", path):
                    found = True
                    nicePath=jarFile.replace(os.sep, "-")
                    self.__jbossHTMLBody += """
        <li id="system-jar-info">Jar File: <tt>%s</tt><br/>
            MD5: <tt>%s</tt>
            <br /> Manifest File (
            <a href="javascript:show('%s')">Show</a> /
            <a href="javascript:hide('%s')">Hide</a> ):<br />
            <div id="%s" style="overflow: hidden; display: none">
                <pre>
                %s
                </pre>
            </div>
        </li>
                    """ % (jarFile,
                           self.__getMd5(jarFile),
                           nicePath,
                           nicePath,
                           nicePath,
                           self.__getManifest(jarFile))

                if not found:
                    self.addAlert("WARN: No jars found in the JBoss server configuration (%s)." % (path))

                self.__jbossHTMLBody += """
     </ul>
</div>
            """
            else:
                self.addAlert("ERROR: JBoss server configuration path (" + path + ") does not exist.")

        return

    def __getJBossHomeTree(self):
        """
        This function will execute the "tree" command on JBOSS_HOME.
        """
        self.__jbossHTMLBody += """
    <br/>
    <br/>
    <div id="jboss-home-directory-tree" style="font-weight: bold;">&ndash; JBOSS_HOME Directory Tree</div>

    <div>
        &mdash; JBOSS_HOME Tree
        ( <a href="javascript:show('jboss-home-tree')">Show</a> / <a
            href="javascript:hide('jboss-home-tree')">Hide</a> ):
    </div>
    <div id="jboss-home-tree" style="overflow: hidden; display: none">
    <pre>
        """
        try:
            output = DirTree(self.__jbossHome).as_string()
            self.__jbossHTMLBody += """
%s
    </pre>
    </div>
        """ % (output)
        except Exception, e:
            self.__jbossHTMLBody += """
    ERROR: Unable to generate <tt>tree</tt> on JBOSS_HOME.
    Exception: %s
    </pre>
    </div>
        """ % e
        return

    def __getMbeanData(self, dataTitle, divId, twiddleOpts):
        credentials = ""
        if self.__haveJava and self.__twiddleCmd:
            self.__jbossHTMLBody += """
    <div>
        &mdash; %s
        ( <a href="javascript:show('%s')">Show</a> / <a
            href="javascript:hide('%s')">Hide</a> ):
    </div>
    <div id="%s" style="overflow: hidden; display: none">
    <table style="margin-left: 30px;font-size:14px">
        <tr>
        <td align="left">
            Twiddle Options:
        </td>
        <td align="left"><tt>%s</tt></td>
        </tr>
    </table>
    <pre>

        """ % (dataTitle, divId, divId, divId,twiddleOpts)
            cmd = "%s %s" % (self.__twiddleCmd, twiddleOpts)

            proc = subprocess.Popen(shlex.split(cmd), stderr=subprocess.STDOUT, stdout=subprocess.PIPE)
            output = proc.communicate()[0]
            status = proc.returncode
            if status == 0 and output:
                self.__jbossHTMLBody += output.strip()
            else:
                self.__jbossHTMLBody += """
        ERROR: Unable to collect %s data.
            Output: %s
            Status: %d
            """ % (twiddleOpts, output, status)
        else:
            self.__jbossHTMLBody += "ERROR: Unable to collect data twiddle or Java is missing."

        self.__jbossHTMLBody += """
    </pre>
    </div>
        """
        return

    def __getTwiddleData(self):
        """
        This function co-locates all of the calls to twiddle so that they can be easily disabled.
        """

        ## Get jboss.system.* Data
        self.__jbossHTMLBody += """
    <br/>
    <br/>
    <div id="jboss-system-mbean-data" style="font-weight: bold;">&ndash; JBoss JMX MBean Data from <tt>jboss.system:*</tt></div>
        """
        self.__getMbeanData("JBoss Server Info",
                            "jboss-server-info",
                            " get 'jboss.system:type=ServerInfo' ")
        self.__getMbeanData("JBoss Server Config Info",
                            "jboss-server-config-info",
                            " get 'jboss.system:type=ServerConfig' ")
        self.__getMbeanData("JBoss CXF Server Config Info",
                            "jboss-cxfserver-config-info",
                            " get 'jboss.ws:service=ServerConfig' ")
        self.__getMbeanData("JBoss Memory Pool Info",
                            "jboss-memory-pool-info",
                            " invoke 'jboss.system:type=ServerInfo' listMemoryPools true ")
        self.__getMbeanData("JBoss Thread CPU Utilization",
                            "jboss-thread-cpu-info",
                            " invoke 'jboss.system:type=ServerInfo' listThreadCpuUtilization ")
        self.__getMbeanData("JBoss Thread Dump",
                            "jboss-thread-dump",
                            " invoke 'jboss.system:type=ServerInfo' listThreadDump ")
        self.__getMbeanData("JBoss Logging Config Info",
                            "jboss-logging-config-info",
                            " get 'jboss.system:service=Logging,type=Log4jService' ")

        ## Get jboss.* Data
        self.__jbossHTMLBody += """
    <br/>
    <br/>
    <div id="jboss-mbean-data" style="font-weight: bold;">&ndash; JBoss JMX MBean Data from <tt>jboss:*</tt></div>
        """
        self.__getMbeanData("JBoss System Properties",
                            "jboss-system-properties-info",
                            " invoke 'jboss:name=SystemProperties,type=Service' showAll ")

        self.__getMbeanData("JBoss JNDI List View",
                            "jboss-jndi-list-info",
                            " invoke 'jboss:service=JNDIView' list true ")

        ## MBean Summary
        self.__jbossHTMLBody += """
    <br/>
    <br/>
    <div id="jboss-mbean-summary" style="font-weight: bold;">&ndash; JBoss MBean Summary</div>
        """
        self.__getMbeanData("JBoss MBean Vendor/Version Info",
                            "jboss-vendor-version",
                            " get 'JMImplementation:type=MBeanServerDelegate' ")
        self.__getMbeanData("JBoss MBean Count",
                            "jboss-mbean-count",
                            "  serverinfo -c ")
        self.__getMbeanData("JBoss MBean List",
                            "jboss-mbean-list",
                            "  serverinfo -l ")

        ##JBoss Messaging Data
        self.__jbossHTMLBody += """
    <br/>
    <br/>
    <div id="jboss-messaging" style="font-weight: bold;">&ndash; JBoss JMX Messaging MBean Data from <tt>jboss.messaging:*</tt></div>
        """
        self.__getMbeanData("JBoss Message Counters",
                            "jboss-message-counters",
                            " invoke 'jboss.messaging:service=ServerPeer' listMessageCountersAsHTML ")

        self.__getMbeanData("JBoss Prepared Transactions Table",
                            "jboss-prepared-transactions",
                            " invoke 'jboss.messaging:service=ServerPeer' listAllPreparedTransactions ")

        self.__getMbeanData("JBoss Active Clients Table",
                            "jboss-active-clients",
                            " invoke 'jboss.messaging:service=ServerPeer' showActiveClientsAsHTML ")

        ## Get j2ee Data query 'jboss.j2ee:*'
        self.__jbossHTMLBody += """
    <br/>
    <br/>
    <div id="jboss-j2ee" style="font-weight: bold;">&ndash; JBoss JMX J2EE MBean Data from <tt>jboss.j2ee:*</tt></div>
        """
        self.__getMbeanData("JBoss J2EE MBeans",
                            "jboss-j2ee-mbeans",
                            " query 'jboss.j2ee:*' ")

        ## VFS
        self.__jbossHTMLBody += """
    <br/>
    <br/>
    <div id="jboss-vfs" style="font-weight: bold;">&ndash; JBoss JMX VFS MBean Data from <tt>jboss.vfs:*</tt></div>
        """
        self.__getMbeanData("JBoss VFS Cached Contexts",
                            "jboss-vfs-contexts",
                            " invoke 'jboss.vfs:service=VFSCacheStatistics' listCachedContexts ")

        ## Get jsr77 Data
        self.__jbossHTMLBody += """
    <br/>
    <br/>
    <div id="jboss-jsr77-data" style="font-weight: bold;">&ndash; JBoss JSR77 Data</div>
        """
        self.__getMbeanData("JBoss JSR77 Data",
                            "jboss-jsr77",
                            " jsr77 ")
        return


    def __getFiles(self, configDirAry):
        """
        This function will collect files from JBOSS_HOME for analysis.  The scope of files to
        be collected are determined by options to this SOS plug-in.
        """

        for dir in configDirAry:
            path=os.path.join(self.__jbossHome, "server", dir)
            ## First add forbidden files
            self.addForbiddenPath(os.path.join(path, "tmp"))
            self.addForbiddenPath(os.path.join(path, "work"))
            self.addForbiddenPath(os.path.join(path, "data"))

            if os.path.exists(path):
                ## First get everything in the conf dir
                confDir=os.path.join(path, "conf")
                self.doCopyFileOrDir(confDir)
                ## Log dir next
                logDir=os.path.join(path, "log")

                for logFile in find("*", logDir):
                    self.addCopySpecLimit(logFile, self.getOption("logsize"))
                ## Deploy dir
                deployDir=os.path.join(path, "deploy")

                for deployFile in find("*", deployDir, max_depth=1):
                    self.addCopySpec(deployFile)

                ## Get application deployment descriptors if designated.
                if self.isOptionEnabled("appxml"):
                    appxml=self.getOption("appxml")
                    ## I'd rather use comma as the delimiter but getOption doesn't seem to be passing it through.
                    ## Since we are using spaces as the delimiter, we need to filter out empty list elements
                    ## if the user did something like ' all   default  web '.
                    appxml=appxml.split(' ')
                    ## Flter(None doesn't  work.  Allows 0.
                    appxml=filter(lambda x: len(x), appxml)
                    for app in appxml:
                        pat = os.path.join("*%s*" % (app,), "WEB-INF")
                        for file in find("*.xml", deployDir, path_pattern=pat):
                            self.addCopySpec(file)
        return

    def setup(self):

        ## We need to know where JBoss is installed and if we can't find it we
        ## must exit immediately.
        if not self.__getJbossHome():
            self.exit_please()
            return

        ## Check to see if the user passed in a limited list of server config jars.
        self.__updateServerConfigDirs()

        ## Generate HTML Body for report
        self.__createHTMLBodyStart()

        ## Generate hashes of the stock Jar files for the report.
        if self.getOption("stdjar"):
            self.__getStdJarInfo()

        ## Generate hashes for the Jars in the various profiles
        if self.getOption("servjar"):
            self.__getServerConfigJarInfo(self.__jbossServerConfigDirs)

        ## Generate a Tree for JBOSS_HOME
        self.__getJBossHomeTree()

        if self.getOption("twiddle"):
            ## We need to know where Java is installed or at least ensure that it
            ## is available to the plug-in so that we can run twiddle.
            self.__haveJava = self.__getJavaHome()
            self.__buildTwiddleCmd()
            self.__getTwiddleData()


        self.addCustomText(self.__jbossHTMLBody)

        self.__getFiles(self.__jbossServerConfigDirs)

        return

    def postproc(self):
        """
        Obfuscate passwords.
        """

        for dir in self.__jbossServerConfigDirs:
            path=os.path.join(self.__jbossHome, "server", dir)
            ## Really annoying that there appears to be no vehicle to
            ## say I want ignore case...argh!
            self.doRegexSub(os.path.join(path,"conf","login-config.xml"),
                            r"\"[Pp][Aa][Ss][Ss][Ww][Oo][Rr][Dd]\".*>.*</[Mm][Oo][Dd][Uu][Ll][Ee]-[Oo][Pp][Tt][Ii][Oo][Nn].*>",
                            r'"password">********</module-option>')

            tmp = os.path.join(path,"conf", "props")
            for propFile in find("*-users.properties", tmp):
                self.doRegexSub(propFile,
                                r"=(.*)",
                                r'=********')

            ## Remove PW from -ds.xml files
            tmp=os.path.join(path, "deploy")
            for dsFile in find("*-ds.xml", tmp):
                self.doRegexSub(dsFile,
                                r"<[Pp][Aa][Ss][Ss][Ww][Oo][Rr][Dd].*>.*</[Pp][Aa][Ss][Ss][Ww][Oo][Rr][Dd].*>",
                                r"<password>********</password>")
        return
