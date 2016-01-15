#!/bin/bash

set -o nounset
set -o errexit



mkdir -p pyres
mkdir -p watchdir

if ! [ -e "pyres/lasttime.txt" ]
then
	echo "File does not exist: pyres/lasttime.txt"
	echo "Creating new time file with datetime: 2012-01-01T00:00:00Z"
	echo "2012-01-01T00:00:00Z" > pyres/lasttime.txt
fi

mvn exec:java -Dexec.mainClass="nl.huygensing.demoloader.DemoServer" -Dexec.args="./watchdir" &
SRV_PID=$!
echo "HEREE $SRV_PID"

function ctrl_c() {
	kill $SRV_PID
}

trap ctrl_c EXIT


sleep 3

while true
do
	./resync-client.py \
		--out-dir ./watchdir \
		--time-file ./pyres/lasttime.txt \
		--source-description-uri http://localhost:8080/resourcesync \
		--backup-dir ./pyres
	sleep 3
done

