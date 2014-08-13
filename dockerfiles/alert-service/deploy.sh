#!/bin/bash
JBOSS_DEPS=$JBOSS_HOME/standalone/deployments

APP_DIR=/root/DAAFSE/alert-service
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
	pushd $APP_DIR
	
	#Update the local repository
	git pull

	mvn clean package -DskipTests=true
	unzip target/$WAR_NAME -d $JBOSS_DEPS/$WAR_NAME
	popd
	
	touch $JBOSS_DEPS/$WAR_NAME.dodeploy
elif [ "$1" == "run" ] ; then
	/opt/wildfly/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0
fi
