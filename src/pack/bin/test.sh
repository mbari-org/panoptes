#!/usr/bin/env bash

curl -v \
    -F file=@$3 \
    -X POST $1 \
    -H "Authorization: Bearer $2" \
    

