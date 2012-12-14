openshift-quickstart-quake2
===========================

Googles HTML5 Quake 2 running on OpenShift

This repository is designed to be used with http://openshift.redhat.com/
applications.  To use, just follow the quickstart below.

Quickstart
==========

1) Create an account at http://openshift.redhat.com/

2) Create a medium jboss application:

    rhc app create -a quake2 -t jbossas-7 -g medium

3) Add this upstream repo

    cd quake2
    git remote add upstream -m master git://github.com/wshearn/openshift-quickstart-quake2.git
    git pull -s recursive -X theirs upstream master

4) Then push the repo upstream

    git push

5) That's it, you can now browse to your application at:

    http://quake2-$yournamespace.rhcloud.com

