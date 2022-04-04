#!/bin/bash
now="$(date)"
printf "export statemachine files: %s\n" "$now"
aws s3 sync s3://myplant/state-machine/ /root/temp-state-machine/
cp -rv /root/temp-state-machine /mnt/state-machine/

now="$(date)"
printf "finished export statemachine files: %s\n\n" "$now"
