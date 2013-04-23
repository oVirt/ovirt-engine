import common_utils as utils
import basedefs

ALREADY_ENABLED = 11


def isPermanentSupported():
    """
    check if firewall-cmd support --permanent option
    """
    cmd = [
        basedefs.EXEC_FIREWALL_CMD,
        '--help',
    ]
    out, rc = utils.execCmd(
        cmdList=cmd,
        failOnError=False,
    )
    supported = (out.find('--permanent') != -1)
    return supported


def getActiveZones():
    cmd = [
        basedefs.EXEC_FIREWALL_CMD,
        '--get-active-zones',
    ]
    out, rc = utils.execCmd(
        cmdList=cmd,
        failOnError=True,
        msg='Error running firewall-cmd'
    )
    zones = {}
    for line in out.splitlines():
        zone_name, devices = line.split(':')
        zones[zone_name] = devices.split()
    return zones


def addServiceToZone(service, zone):
    cmdList = [
        [
            basedefs.EXEC_FIREWALL_CMD,
            '--permanent',
            '--zone',
            zone,
            '--add-service',
            service,
        ],
        [
            basedefs.EXEC_FIREWALL_CMD,
            '--reload',
        ],
    ]
    for cmd in cmdList:
        out, rc = utils.execCmd(
            cmdList=cmd,
            failOnError=False,
            msg='Error running firewall-cmd'
        )
        if rc not in (0, ALREADY_ENABLED):
            raise Exception(out)
