#!/bin/bash

cd kernel/xiaomi/sm8250
chmod +x nextpatch.sh && bash nextpatch.sh
rm -rf KernelSU-Next/userspace/su
cd ../../..
