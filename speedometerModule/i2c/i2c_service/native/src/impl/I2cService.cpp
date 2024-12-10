#include "I2cService.hpp"
#include <utils/Log.h>

#ifdef LOG_TAG
#undef LOG_TAG
#endif
#define LOG_TAG "i2c_service"

namespace example {
namespace service {

// Initialize the static instance to nullptr
std::shared_ptr<I2cService> I2cService::S_INSTANCE = nullptr;

// Return the singleton instance of I2cService
std::shared_ptr<I2cService> I2cService::getInstance() {
    if (S_INSTANCE == nullptr) {
        S_INSTANCE = ndk::SharedRefBase::make<I2cService>();
        ALOGI("I2cService instance created.");
    }
    return S_INSTANCE;
}

// Constructor and Destructor for I2cService
I2cService::I2cService() {
    ALOGI("I2cService constructor called");
}

I2cService::~I2cService() {
    ALOGI("I2cService destructor called");
}

// Initialize I2C device
::ndk::ScopedAStatus I2cService::init(const std::string& i2cDevicePath, ::aidl::i2c::service::api::I2cStatus* _aidl_return) {
    bool success = i2cHal.init(i2cDevicePath);
    _aidl_return->success = success;
    _aidl_return->message = success ? "I2C device initialized successfully" : "Failed to initialize I2C device";

    if (success) {
        ALOGI("I2C device initialized successfully: %s", i2cDevicePath.c_str());
    } else {
        ALOGE("Failed to initialize I2C device: %s", i2cDevicePath.c_str());
    }

    return ndk::ScopedAStatus::ok();
}

// Read value from I2C device
::ndk::ScopedAStatus I2cService::readValue(int address, int reg, int* _aidl_return) {
    int value = i2cHal.readValue(address, reg);
    if (value < 0) {
        ALOGE("Failed to read value from address %d, register %d", address, reg);
        return ndk::ScopedAStatus::fromExceptionCode(EX_ILLEGAL_STATE);
    }

    ALOGI("Read value %d from address %d, register %d", value, address, reg);
    *_aidl_return = value;
    return ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus I2cService::writeValue(int address, int reg, int value, ::aidl::i2c::service::api::I2cStatus* _aidl_return) {
    bool success = i2cHal.writeValue(address, reg, value);
    _aidl_return->success = success;
    _aidl_return->message = success ? "Write successful" : "Write failed";
    return ndk::ScopedAStatus::ok();
}

// Return the service name for service registration
std::string I2cService::getServiceName() const {
    return std::string() + descriptor + "/default";  // Use the appropriate service name
}

} // namespace service
} // namespace example


