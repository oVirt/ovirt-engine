# This script will install and dynamically load
# byteman into the running ovirt-engine, and
# will load a script that will log every interaction
# with vdsm from engine.

# download or install byteman rpm
sudo yum install byteman -y

# get the running user and pid of ovirt-engine
OEPID=$(pidof ovirt-engine)
OEUSER=$(ps -o user -p ${OEPID} h)

# attach the agent into a running engine. Jboss attaches the agent to
# itself only if the command is run with the user that Jboss is run with
# ('ovirt' by default)
sudo -u $OEUSER bminstall $OEPID

# create a script to print all outgoing vds commands and return values
# it might be required to:
# - create the directory /home/$OEUSER
# - chown -R $OEUSER:$OEUSER /home/$OEUSER
cat << EOF | sudo -u $OEUSER tee /home/$OEUSER/byteman.btm
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

# load the script into the running jvm
sudo -u $OEUSER bmsubmit -l /home/$OEUSER/byteman.btm
