#!/bin/bash

base64 -d device/xiaomi/alioth/configs/camera/secret > device/xiaomi/alioth/configs/camera/st_license.lic

cd kernel/xiaomi/sm8250
chmod +x nextpatch.sh && bash nextpatch.sh
rm -rf KernelSU-Next/userspace/su
cd ../../..
