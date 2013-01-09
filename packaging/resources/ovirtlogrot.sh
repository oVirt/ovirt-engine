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

for i in `ls $logdir/engine.log.* -t`; do
 m=`expr match $i .*gz`
 if [ $m == 0 ]
 then
  cat $i | gzip -9 > $i-$date.gz && rm $i
 fi
done;

#
# remove the old logs
#

lastlogday=`date -d "$maxage hours ago" $dateformat`
for i in `ls $logdir/engine.log.*.gz`; do
 timestamp=`echo $i | sed s/.*-// | sed s/\.gz//`
 if [[ "$lastlogday" > "$timestamp" ]]
 then
  rm -f $logdir/engine.log.*$timestamp.gz
 fi
done
