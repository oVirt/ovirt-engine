import sos.plugintools
import os
import fnmatch
import shlex
import subprocess
import tempfile

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

# Class name must be the same as file name and method names must not change
class postgresql(sos.plugintools.PluginBase):
    """PostgreSQL related information"""

    optionList = [
        ("pghome",  'PostgreSQL server home directory.', '', '/var/lib/pgsql'),
        ("username",  'username for pg_dump', '', 'postgres'),
        ("password",  'password for pg_dump', '', ''),
        ("dbname",  'database name to dump for pg_dump', '', ''),
    ]

    def pg_dump(self):
        dest_file = os.path.join(self.tmp_dir, "sos_pgdump.tar")
        old_env_pgpassword = os.environ.get("PGPASSWORD")
        os.environ["PGPASSWORD"] = "%s" % (self.getOption("password"))
        (status, output, rtime) = self.callExtProg("pg_dump %s -U %s -w -f %s -F t" %
                                                   (self.getOption("dbname"),
                                                    self.getOption("username"),
                                                    dest_file))
        if old_env_pgpassword is not None:
            os.environ["PGPASSWORD"] = "%s" % (old_env_pgpassword)
        if (status == 0):
            self.addCopySpec(dest_file)
        else:
            self.addAlert("ERROR: Unable to execute pg_dump.  Error(%s)" % (output))

    def setup(self):
        if self.getOption("dbname"):
            if self.getOption("password"):
                self.tmp_dir = tempfile.mkdtemp()
                self.pg_dump()
            else:
                self.addAlert("WARN: password must be supplied to dump a database.")   
            
        # Copy PostgreSQL log files.
        for file in find("*.log", self.getOption("pghome")):
            self.addCopySpec(file)
        # Copy PostgreSQL config files.
        for file in find("*.conf", self.getOption("pghome")):
            self.addCopySpec(file)

        self.addCopySpec(os.path.join(self.getOption("pghome"), "data" , "PG_VERSION"))
        self.addCopySpec(os.path.join(self.getOption("pghome"), "data" , "postmaster.opts"))


    def postproc(self):
        import shutil
        shutil.rmtree(self.tmp_dir)
