#
# Copyright (C) 2021 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

# Inherit from those products. Most specific first.
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/full_base_telephony.mk)

# Inherit some common lineage stuff.
$(call inherit-product, vendor/lineage/config/common_full_phone.mk)

# Inherit Camera-related flags
TARGET_USES_MIUI_CAMERA := true
TARGET_INCLUDES_MIUI_CAMERA := true

# Inherit from alioth device
$(call inherit-product, device/xiaomi/alioth/device.mk)

PRODUCT_NAME := lineage_alioth
PRODUCT_DEVICE := alioth
PRODUCT_MANUFACTURER := Xiaomi
PRODUCT_BRAND := POCO
PRODUCT_MODEL := POCO F3

PRODUCT_SYSTEM_NAME := alioth_global
PRODUCT_SYSTEM_DEVICE := alioth

PRODUCT_GMS_CLIENTID_BASE := android-xiaomi

PRODUCT_BUILD_PROP_OVERRIDES += \
    BuildFingerprint=POCO/alioth_global/alioth:13/TKQ1.221114.001/V816.0.3.0.TKHMIXM:user/release-keys \
    DeviceName=$(PRODUCT_SYSTEM_DEVICE) \
    DeviceProduct=$(PRODUCT_SYSTEM_NAME)

#AxionAosp Flags

TARGET_BOOT_ANIMATION_RES := 1080
TARGET_DISABLE_EPPE := true
TARGET_ENABLE_BLUR := true

AXION_MAINTAINER := zenzer0s

AXION_PROCESSOR := Snapdragon®_870

AXION_CAMERA_REAR_INFO := 48,8,5 
AXION_CAMERA_FRONT_INFO := 20

TARGET_NEEDS_DOZE_FIX := true


# High Brightness Mode (HBM)
HBM_SUPPORTED := true
HBM_NODE := /sys/class/drm/card0/card0-DSI-1/disp_param

#ViperFX
TARGET_INCLUDE_VIPERFX := true

#cpu
AXION_CPU_SMALL_CORES := 0,1,2,3
AXION_CPU_BIG_CORES := 4,5,6,7

# Dex2oat opti
DEX2OAT_CORES := 0,1
DEX2OAT_THREADS := 2

# Flash Strength 
TORCH_STR_SUPPORTED := true

# Charging
BYPASS_CHARGE_SUPPORTED ?= true

# CPU governor support
PERF_GOV_SUPPORTED := true
PERF_DEFAULT_GOV := schedutil
PERF_ANIM_OVERRIDE := true

GPU_FREQS_PATH := /sys/class/kgsl/kgsl-3d0/devfreq/available_frequencies
GPU_MIN_FREQ_PATH := /sys/class/kgsl/kgsl-3d0/devfreq/min_freq