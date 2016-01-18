#!/usr/local/bin/python

from resync.source_description import SourceDescription
from resync.capability_list import CapabilityList
from resync.resource_list import ResourceList

from argparse import ArgumentParser

from time import sleep
import dateutil.parser
import requests
import re
import os.path
import sys

parser = ArgumentParser()
parser.add_argument('--out-dir', required=True)
parser.add_argument('--time-file', required=True)
parser.add_argument('--backup-dir', required=True)
parser.add_argument('--source-description-uri', required=True)
args = parser.parse_args()

# timefile contains the time of the last resync,
# use to check which resource file to write
timefile = open(args.time_file)
lasttime = dateutil.parser.parse(timefile.readline())
timefile.close();

print "(resync-client.py): Last resync resource modified date: " + str(lasttime)

# Downloads resources and writes them to out_dir (filename = unix timestamp)
# if their last modified time is greater than value of
# lasttime (obtained from --time-file)
# and writes new_lasttime to --time-file
def get_resources(resources):

	# sort resources by last modified time
	time_sorted_resources = []
	for key, resource in resources.iteritems():
		resource_mod_time = dateutil.parser.parse(resource.lastmod)
		time_sorted_resources.append({"time": resource_mod_time, "resource": resource})

	time_sorted_resources = sorted(time_sorted_resources, key=lambda k: k["time"])

	# write all new resources
	for res in time_sorted_resources:
		if lasttime < res["time"]:
			outfile_name = re.sub(r"^.*\/", "", res["resource"].uri)
			backup_file_path = args.backup_dir + "/" + outfile_name
			out_file_path = args.out_dir + "/" + outfile_name

			print "\n---"
			print "(resync-client.py): Writing new file: (" + str(res["time"]) + " > " + str(lasttime) + ")"
			print "(resync-client.py): this URI: " + res["resource"].uri
			print "(resync-client.py): to dir: " + out_file_path
			print "(resync-client.py): and to backup dir: " + backup_file_path
			response = requests.get(res["resource"].uri)
			backup_file = open(backup_file_path, "w")
			backup_file.write(response.text.encode('UTF-8'))
			backup_file.close()
			out_file = open(out_file_path, "w")
			out_file.write(response.text.encode('UTF-8'))
			out_file.close()
			print "(resync-client.py): Waiting for out_file to be processed: " + out_file_path
			sys.stdout.flush()
			while os.path.exists(out_file_path):
				sleep(0.05)

	# store newest modified time of newest resource
	new_lasttime = time_sorted_resources[-1]["time"].strftime("%Y-%m-%dT%H:%M:%SZ")
	# write this time to the timefile
	timefile_out = open(args.time_file, "w")
	timefile_out.write(new_lasttime)
	timefile_out.close()

# Downloads all resource lists
def get_resource_lists(resources):
	for key, resource_list_resource in resources.iteritems():
		resource_list_response = requests.get(resource_list_resource.uri)
		resource_list = ResourceList()
		resource_list.parse(str=resource_list_response.text)
		get_resources(resource_list.resources)



# Download URI of the source description XML from 
# --> should actually be either via robots.txt or/in .well-known
source_desc_response = requests.get(args.source_description_uri)
source_desc = SourceDescription()
source_desc.parse(str=source_desc_response.text)
[capabilitylist_resource] = source_desc.resources

# Download capablity list obtained from source description
capabilitylist_response =  requests.get(capabilitylist_resource.uri)
capabilitylist = CapabilityList()
capabilitylist.parse(str=capabilitylist_response.text)

# Download resource lists obtained from capability list
get_resource_lists(capabilitylist.resources)