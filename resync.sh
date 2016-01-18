#!/bin/bash

set -o nounset
set -o errexit

mkdir -p pyres
mkdir -p watchdir


if ! [ -e "pyres/lasttime.txt" ]
then
	# create a time file containing an old date
	echo "File does not exist: pyres/lasttime.txt"
	echo "Creating new time file with datetime: 2012-01-01T00:00:00Z"
	echo "2012-01-01T00:00:00Z" > pyres/lasttime.txt
fi

# install and run jetty server for sqlite loader
mvn exec:java -Dexec.mainClass="nl.huygensing.demoloader.DemoServer" -Dexec.args="./watchdir" &
SRV_PID=$!

function ctrl_c() {
	kill $SRV_PID
}
trap ctrl_c EXIT

sleep 10


# run resync loop
while true
do
	./resync-client.py \
		--out-dir ./watchdir \
		--time-file ./pyres/lasttime.txt \
		--source-description-uri http://$NGINX_PORT_80_TCP_ADDR:$NGINX_PORT_80_TCP_PORT/resourcesync \
		--backup-dir ./pyres
	sleep 3
done

