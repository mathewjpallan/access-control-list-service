#!/usr/bin/env bash

SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")
cd $SCRIPTPATH/..
cp $SCRIPTPATH/../target/*.jar $SCRIPTPATH
docker build --tag acl-service:0.0.1 $SCRIPTPATH/../devops