#include <iostream>
#include <thread>
#include <chrono>
#include <aidl/i2c/service/api/II2cService.h>
#include <aidl/i2c/service/api/I2cStatus.h>
#include <android/binder_manager.h>
#include <android/binder_interface_utils.h>
#include <android/log.h>

using namespace std;

#define LOG_TAG "ADS_APP"
#define ADS_ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define ADS_ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

int main() {
    const char* service_name = "i2c.service.api.II2cService/default";

    if (!AServiceManager_isDeclared(service_name)) {
        ADS_ALOGE("I2C service '%s' is not declared", service_name);
        return -1;
    }

    std::shared_ptr<aidl::i2c::service::api::II2cService> i2cService =
        aidl::i2c::service::api::II2cService::fromBinder(
            ndk::SpAIBinder(AServiceManager_waitForService(service_name)));

    if (!i2cService) {
        ADS_ALOGE("Failed to connect to I2C service");
        return -1;
    }

    aidl::i2c::service::api::I2cStatus initStatus;
    std::string i2cDevicePath = "/dev/i2c-1"; // Adjust the path based on your system
    auto initResult = i2cService->init(i2cDevicePath, &initStatus);

    if (!initResult.isOk() || !initStatus.success) {
        ADS_ALOGE("Failed to initialize I2C device: %s", initStatus.message.c_str());
        return -1;
    }
    ADS_ALOGI("I2C device initialized successfully: %s", i2cDevicePath.c_str());

    int address = 0x48; // Adjust the I2C address as needed
    int lastValue = -1;

    // Configure the ADC (ensure you have the correct settings)
    int configRegister = 0x01;
    int configValue = 0xC183; // Example configuration value (adjust as needed)
    aidl::i2c::service::api::I2cStatus configStatus;
    auto configResult = i2cService->writeValue(address, configRegister, configValue, &configStatus);
    if (!configResult.isOk() || !configStatus.success) {
        ADS_ALOGE("Failed to configure the ADC device: %s", configStatus.message.c_str());
        return -1;
    }
    ADS_ALOGI("ADC device configured successfully");

    int conversionRegister = 0x00; // Adjust based on your ADC's conversion register
    int iteration = 0;

    while (true) {
        // Step 1: Trigger a new ADC conversion if needed (write to the register to start conversion)
        int triggerRegister = 0x01;  // Register to start conversion (check datasheet)
        int triggerValue = 0x00;  // Trigger value (check datasheet)
        auto triggerResult = i2cService->writeValue(address, triggerRegister, triggerValue, &configStatus);
        if (!triggerResult.isOk() || !configStatus.success) {
            ADS_ALOGE("Failed to trigger ADC conversion: %s", configStatus.message.c_str());
        } else {
            ADS_ALOGI("ADC conversion triggered.");
        }

        // Step 2: Read ADC conversion result
        int value = 0;
        auto readResult = i2cService->readValue(address, conversionRegister, &value);
        if (!readResult.isOk()) {
            ADS_ALOGE("Failed to read conversion register: %s", readResult.getDescription().c_str());
            this_thread::sleep_for(chrono::milliseconds(500));
            continue;
        }

        if (value != lastValue) {
            ADS_ALOGI("Read value from ADC: %d", value);
            cout << "ADC Value: " << value << endl;
            lastValue = value;
        } else {
            ADS_ALOGI("ADC value unchanged: %d", value);
        }

        iteration++;
        this_thread::sleep_for(chrono::milliseconds(100)); 
    }

    return 0;
}
