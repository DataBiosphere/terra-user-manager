#!/bin/bash

# validate postgres
echo "sleeping for 5 seconds during postgres boot..."
sleep 5
PGPASSWORD=dbpwd psql --username dbuser -d user_db -c "SELECT VERSION();SELECT NOW()"
