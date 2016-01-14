#!/usr/bin/python


from resync.source_description import SourceDescription
from resync.capability_list import CapabilityList
from resync.resource_list import ResourceList
import requests

def get_resources(resources):
	for key, resource in resources.iteritems():
		print "TODO fetch if date is new: " + resource.lastmod
		print "this URI: " + resource.uri
		# resource_response = requests.get(resource.uri)
		# print resource_response.text

def get_resource_lists(resources):
	for key, resource_list_resource in resources.iteritems():
		resource_list_response = requests.get(resource_list_resource.uri)
		resource_list = ResourceList()
		resource_list.parse(str=resource_list_response.text)
		get_resources(resource_list.resources)


source_desc_response = requests.get("http://localhost:8080/resourcesync")
source_desc = SourceDescription()
source_desc.parse(str=source_desc_response.text)
[capabilitylist_resource] = source_desc.resources

capabilitylist_response =  requests.get(capabilitylist_resource.uri)
capabilitylist = CapabilityList()
capabilitylist.parse(str=capabilitylist_response.text)

get_resource_lists(capabilitylist.resources)