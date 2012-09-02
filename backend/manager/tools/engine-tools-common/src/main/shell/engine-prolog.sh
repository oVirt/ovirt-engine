# Print an error message to stderr and exit with an error code:
die() {
    printf >&2 "$@\n"
    exit 1
}

load_config() {
    # Load the defaults file:
    ENGINE_DEFAULTS="${ENGINE_DEFAULTS:-/usr/share/ovirt-engine/conf/engine.conf.defaults}"
    if [ ! -r "${ENGINE_DEFAULTS}" ]
    then
        die "Can't load defaults file \"${ENGINE_DEFAULTS}\"."
    fi
    . "${ENGINE_DEFAULTS}"

    # Load the configuration file:
    ENGINE_VARS="${ENGINE_VARS:-/etc/sysconfig/ovirt-engine}"
    if [ ! -r "${ENGINE_VARS}" ]
    then
        die "Can't load configuration file \"${ENGINE_VARS}\"."
    fi
    . "${ENGINE_VARS}"
}

# In addition to defining the functions we also perform some tasks that
# any script will need:
load_config
