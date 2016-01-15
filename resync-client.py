#!/usr/bin/python


from resync.source_description import SourceDescription
from resync.capability_list import CapabilityList
from resync.resource_list import ResourceList

from argparse import ArgumentParser

import dateutil.parser
import requests

parser = ArgumentParser()
parser.add_argument('--out-dir', required=True)
parser.add_argument('--time-file', required=True)
parser.add_argument('--source-description-uri', required=True)
args = parser.parse_args()

# timefile contains the time of the last resync,
# use to check which resource file to write
timefile = open(args.time_file)
lasttime = dateutil.parser.parse(timefile.readline())
timefile.close();

print "Last resync: " + str(lasttime)

# Downloads resources and writes them to out_dir (filename = unix timestamp)
# if their last modified time is greater than value of
# lasttime (obtained from --time-file)
# and writes new_lasttime to --time-file
def get_resources(resources):
	times = []
	for key, resource in resources.iteritems():
		resource_mod_time = dateutil.parser.parse(resource.lastmod)
		times.append(resource_mod_time)
		print "TODO fetch if date is new: " + str(dateutil.parser.parse(resource.lastmod))
		print "this URI: " + resource.uri
		print "to dir: " + args.out_dir
		resource_response = requests.get(resource.uri)


	# sort all resource times
	times = sorted(times)
	# last index is the time of the last modified resource
	new_lasttime = times[-1].strftime("%Y-%m-%dT%H:%M:%SZ")
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



