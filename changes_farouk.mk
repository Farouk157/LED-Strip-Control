PRODUCT_MODEL := RPI4 Customized by LED Strip Team


BOARD_SEPOLICY_DIRS += \
    device/farouk/sepolicy \
    device/farouk/sepolicy/example_service

		

PRODUCT_PACKAGES += i2c_service \
                    i2c_service_user_app \                 
                    ledstrip_service \
                    strip_service_user_app

DEVICE_FRAMEWORK_COMPATIBILITY_MATRIX_FILE += device/farouk/manifests/framework_compatibility_matrix.xml 


PRODUCT_COPY_FILES += \
    device/farouk/i2c_service/i2c_service.rc:vendor/etc/init/i2c_service.rc \
    device/farouk/ledstrip_service/ledstrip_service.rc:vendor/etc/init/ledstrip_service.rc \
