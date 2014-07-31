#!/bin/bash

JBOSS_HOME=/opt/wildfly
JBOSS_DEPS=$JBOSS_HOME/standalone/deployments
APP_NAME=alert-service-1.0-SNAPSHOT
WAR_NAME=$APP_NAME.war

echo "JBOSS_HOME=${JBOSS_HOME}"
echo "JBOSS_DEPS=${JBOSS_DEPS}"
echo "APP_NAME=${APP_NAME}"
echo "WAR_NAME=${WAR_NAME}"

if [ "$1" == "redeploy" ] ; then
	#Cleanup
	rm -rf $JBOSS_DEPS/$APP_NAME*
	rm -rf $JBOSS_HOME/cqels_home

	#Build and unzip
	mvn clean package -DskipTests=true
	#mkdir $JBOSS_DEPS/$WAR_NAME
	unzip target/$WAR_NAME -d $JBOSS_DEPS/$WAR_NAME
	touch $JBOSS_DEPS/$WAR_NAME.dodeploy
elif [ "$1" == "copy" ] ; then
	rsync -avzru --chmod 777 --exclude WEB-INF src/main/webapp/* $JBOSS_DEPS/$WAR_NAME/
elif [ "$1" == "clean" ] ; then
	rm -rf $JBOSS_DEPS/$APP_NAME*
	rm -rf $JBOSS_HOME/cqels_home
fi
