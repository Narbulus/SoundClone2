echo 'deploying files...'
scp -i ../keys/hardincm.pem target/app-1.0-SNAPSHOT.jar ec2-user@52.26.192.189:
