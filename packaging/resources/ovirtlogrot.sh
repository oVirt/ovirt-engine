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

for i in `ls $logdir/{engine,server,jasperserver}.log.* -t`; do
 m=`expr match $i .*gz`
 if [ $m == 0 ]
 then
  cat $i | gzip -9 > $i-${date}_`/bin/date +%N | cut -c6-`.gz && rm $i
 fi
done;

#
# remove the old logs
#

lastlogday=`date -d "$maxage hours ago" $dateformat`
for i in `ls $logdir/{engine,server,jasperserver}.log.*.gz`; do
 timestamp=`echo $i | sed s/.*-// | sed 's/_.\{4\}\.gz$//'`
 if [[ "$lastlogday" > "$timestamp" ]]
 then
  rm -f $logdir/{engine,server,jasperserver}.log.*$timestamp.gz
 fi
done
