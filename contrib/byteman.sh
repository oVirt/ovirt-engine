# This script will install and dynamically load
# byteman into the running ovirt-engine, and
# will load a script that will log every interaction
# with vdsm from engine.

# download or install byteman rpm
yum install byteman -y

# load the agent into a running engine
bminstall $(pidof ovirt-engine)

# load a script to print all outgoing vds commands and return values

cat << EOF > byteman.btm
RULE trace vdsbroker commands
CLASS org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerCommand
METHOD executeVDSCommand
AT ENTRY
IF TRUE
DO traceln("*** execute vdsbroker " + \$this.getCommandName() + " on " + \$this.vds)
ENDRULE


RULE trace vdsbroker return values
CLASS org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerCommand
METHOD executeVDSCommand
AT EXIT
IF TRUE
DO traceln("*** execute vdsbroker " + \$this.getCommandName() + " on " + \$this.vds + " return value: " + \$this.getReturnValue())
ENDRULE
EOF

bmsubmit byteman.btm



