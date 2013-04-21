#!/bin/sh
#
# Archives the log files of ovirt
# usage:
# ovirtlogrot.sh <log directory> <max log age in hours>
#

logdir=$1
maxage=$2
dateformat="+%Y%m%d_%H%M%S"

date=`date $dateformat`

for i in $logdir/{engine,server,jasperserver}.log.*; do
 m=`expr match $i '.*\(gz\|xz\)'`
 if [ -z "$m" -a -r "$i" ]
 then
  cat $i | xz > $i-${date}_`/bin/date +%N | cut -c6-`.xz && rm $i
 fi
done;

#
# remove the old logs
#

lastlogday=`date -d "$maxage hours ago" $dateformat`
for i in $logdir/{engine,server,jasperserver}.log.*.{gz,xz}; do
 timestamp=`echo $i | sed 's/.*-//; s/\.\(gz\|xz\)$//; s/_.\{4\}$//'`
 if [[ "$lastlogday" > "$timestamp" ]]
 then
  rm -f $i
 fi
done
