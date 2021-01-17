# Android App:

## Download 

From: https://github.com/airdocs/airdocs-app/blob/main/app/build/outputs/apk/debug/app-debug.apk

## Installation 

`$ adb install -t app-debug.apk `

## Running with logcat 

``$adb logcat --pid=`adb shell pidof -s upb.airdocs` ``

# Server 

## Run 

Using the IP address of the host and a port you choose:

`$ python3 airdocs-webserver.py -l 192.168.142.105 -p 8001`
