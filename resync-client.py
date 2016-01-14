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

# <?xml version=\'1.0\' encoding=\'UTF-8\'?>
# <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9" xmlns:rs="http://www.openarchives.org/rs/terms/">
#  <rs:ln href="http://example.org/about" rel="describedby" />
#  <rs:md capability="description" />
#  <url>
#   <loc>http://example.org/ds1/cl.xml</loc>
#   <rs:md capability="capabilitylist" />
#  </url>
#  <url>
#   <loc>http://example.org/ds2/cl.xml</loc>
#   <rs:md capability="capabilitylist" />
#  </url>
#  <url>
#   <loc>http://example.org/ds3/cl.xml</loc>
#   <rs:md capability="capabilitylist" />
#  </url>
# </urlset>

# <?xml version='1.0' encoding='UTF-8'?>
# <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9" xmlns:rs="http://www.openarchives.org/rs/terms/">
#  <rs:md capability="description" />
#  <url>
#   <loc>http://localhost:8080/capability-list.xml</loc>
#<rs:md capability="capabilitylist" /></url></urlset>
