#!/bin/bash

echo 'deploying files...'
SERVER=ec2-user@52.26.192.189
RESOURCES=/var/www/html/soundclone/resources

scp -i ../keys/hardincm.pem ../target/soundclone.* $SERVER:$RESOURCES
scp -i ../keys/hardincm.pem ../target/version.txt $SERVER:$RESOURCES
