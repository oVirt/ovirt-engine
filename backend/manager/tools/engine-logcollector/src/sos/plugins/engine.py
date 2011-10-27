import sos.plugintools

# Class name must be the same as file name and method names must not change
class engine(sos.plugintools.PluginBase):
    """oVirt related information"""

    optionList = [
         ("vdsmlogs",  'Directory containing all of the SOS logs from the RHEV hypervisor(s)', '', False),
         ("prefix", "Prefix the sosreport archive", '', False)
    ]

    def setup(self):
        # Copy engine config files.
        self.addCopySpec("/etc/engine")
        self.addCopySpec("/var/log/engine")
        if self.getOption("vdsmlogs"):
            self.addCopySpec(self.getOption("vdsmlogs"))




    def postproc(self):
        """
        Obfuscate passwords.
        """

        self.doRegexSub("/etc/engine/engine-config/engine-config.properties",
                        r"Password.type=(.*)",
                        r'Password.type=********')

        if self.getOption("prefix"):
            current_name = self.policy().reportName
            self.policy().reportName = "LogCollector-" + current_name
