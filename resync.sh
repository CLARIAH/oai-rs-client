#!/bin/bash

mkdir -p pyres

if ! [ -e "pyres/lasttime.txt" ]
then
	echo "File does not exist: pyres/lasttime.txt"
	echo "Creating new time file with datetime: 2012-01-01T00:00:00Z"
	echo "2012-01-01T00:00:00Z" > pyres/lasttime.txt
fi
./resync-client.py --out-dir ~/tmp/watchdir/ --time-file pyres/lasttime.txt --source-description-uri http://localhost:8080/resourcesync
