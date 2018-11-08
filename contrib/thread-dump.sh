#!/bin/sh

SERVICE_NAME="ovirt-engine"
CONSOLE_LOG="/var/log/ovirt-engine/console.log"

function usage() {
    echo "
    USAGE
        $0 [DUMP_COUNT] [DUMP_DELAY]

        DUMP_COUNT
            Number of consequent thread dump collections

        DUMP_DELAY
            Delay in seconds between each thread dump


    EXAMPLE
       Collect 6 dumps in one minute - $0 6 10
       Collect 10 dumps in 10 minutes - $0 10 60
        "
    exit 1
}


[[ $# -eq 2 ]] || usage
dump_count="$1"
dump_delay="$2"

[[ "$dump_count" =~ ^[0-9]+$ ]] || (echo "DUMP_COUNT has to be anumber!"; usage)
[[ "$dump_delay" =~ ^[0-9]+$ ]] || (echo "DUMP_DELAY has to be anumber!"; usage)


# Check if ovirt-engine service is running
systemctl status $SERVICE_NAME > /dev/null
rc=$?
if [[ "$rc" -ne "0" ]]; then
    echo "ovirt-enfine is not running"
    exit 1
fi

# Get the ovirt-engine pid
engine_pid=$(pidof ovirt-engine)
[[ "$engine_pid" =~ ^[0-9]+$ ]] || (echo "Ovirt-engine pid not found"; exit 1)

echo "
Running $dump_count thread dumps with delay $dump_delay seconds in between
Engine PID:$engine_pid"

#C ollecting the thread dumps by sending "kill -3" to the engine pid.
i=0
while [  $i -lt $dump_count ]; do
    kill -3 $engine_pid
    let i=i+1
    echo "${i}. thread dump finished."
    [[ $i -ne $dump_count ]] && sleep $dump_delay
done

echo "All thread dumps are collected in $CONSOLE_LOG"
exit 0

