# The Data Virtualization UI project
# WARNING - IN PROGRESS AND INCOMPLETE

## Summary

This is the official Git repository for the Data Virtualization UI project.

The Data Virtualization UI project is a user interface to manage your Data Virtualization instance.

## Get the code

The easiest way to get started with the code is to [create your own fork](http://help.github.com/forking/) of this repository, and then clone your fork:

	$ git clone git@github.com:<you>/datavirt-ui.git
	$ cd datavirt-ui
	$ git remote add upstream git://github.com/mdrillin/datavirt-ui.git
	
At any time, you can pull changes from the upstream and merge them onto your master:

	$ git checkout master               # switches to the 'master' branch
	$ git pull upstream master          # fetches all 'upstream' changes and merges 'upstream/master' onto your 'master' branch
	$ git push origin                   # pushes all the updates to your fork, which should be in-sync with 'upstream'

The general idea is to keep your 'master' branch in-sync with the 'upstream/master'.

## Building Data Virtualization UI

We use Maven 3.x to build our software. The following command compiles all the code, installs the JARs into your local Maven repository, and runs all of the unit tests:

	$ mvn clean install

## Contribute fixes and features

Data Virtualization UI is open source, and we welcome anybody who wants to participate and contribute!

