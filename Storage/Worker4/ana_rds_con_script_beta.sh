#!/bin/bash

# Replace these depending on the instance you want to connect to.
# These are for ITPortal's Beta instance.
mysql_hostname=itportal-beta-utf8.clgurweavpfj.us-west-2.rds.amazonaws.com
mysql_port=8198
mysql_iam_user=rds-user
iam_user_credentials=com.amazon.access.it-portal-beta-rds-user-1
mysql_region=us-west-2
# Note that the cert file should be in the same directory as the
# script, otherwise you should provide it's full path.
mysql_cert_file=rds-combined-ca-bundle.pem


echo $mysql_hostname
echo $mysql_port
echo $mysql_iam_user
echo $mysql_region

export AWS_CREDENTIALS_ODIN=$iam_user_credentials
auth_token=`/apollo/env/AmazonAwsCli/bin/aws rds generate-db-auth-token \
    --hostname $mysql_hostname --port $mysql_port \
    --username $mysql_iam_user --region $mysql_region`

echo $auth_token

/usr/bin/mysql -h $mysql_hostname --port 8198 -u "rds-user" \
    --enable-cleartext-plugin --ssl-ca="rds-combined-ca-bundle.pem" \
    --password=$auth_token -D itportal 


