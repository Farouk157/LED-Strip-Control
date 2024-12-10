#include "LedstripService.hpp"
#include <utils/Log.h>

#ifdef LOG_TAG
#undef LOG_TAG
#endif
#define LOG_TAG "ledstrip_service"

namespace example {
namespace service {

std::shared_ptr<LedstripService> LedstripService::S_INSTANCE = nullptr;

std::shared_ptr<LedstripService> LedstripService::getInstance() {
    if (S_INSTANCE == nullptr) {
        S_INSTANCE = ndk::SharedRefBase::make<LedstripService>("/dev/spidev0.0", 8);
        ALOGI("LedstripService instance created.");
    }
    return S_INSTANCE;
}

LedstripService::LedstripService(const std::string& spiDevice, uint32_t numLeds)
    : neoPixelHal(spiDevice, numLeds) {
    ALOGI("LedstripService constructor called");
}

LedstripService::~LedstripService() {
    ALOGI("LedstripService destructor called");
}

::ndk::ScopedAStatus LedstripService::setColor(int index, int red, int green, int blue, ledstripnamespace::LedstripStatus* _aidl_return) {
    bool success = neoPixelHal.setColor(index, red, green, blue);
    _aidl_return->success = success;
    _aidl_return->message = success ? "Color set successfully" : "Failed to set color";
    return ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus LedstripService::clear(ledstripnamespace::LedstripStatus* _aidl_return) {
    neoPixelHal.clear();
    _aidl_return->success = true;
    _aidl_return->message = "LED strip cleared";
    return ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus LedstripService::show(ledstripnamespace::LedstripStatus* _aidl_return) {
    bool success = neoPixelHal.show();
    _aidl_return->success = success;
    _aidl_return->message = success ? "Display updated" : "Failed to update display";
    return ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus LedstripService::setGlobalFade(ledstripnamespace::LedstripStatus* _aidl_return) {
    // Placeholder for fade functionality; update the HAL to include it
    neoPixelHal.setGlobalFadeMode();
    _aidl_return->success = true;
    _aidl_return->message = "Global fade not implemented in HAL";
    return ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus LedstripService::stopAllModes(ledstripnamespace::LedstripStatus* _aidl_return){
    neoPixelHal.stopThreads();
    _aidl_return->success = true;
    _aidl_return->message = "Stop Threads not implemented in HAL";
    return ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus LedstripService::setRandom(ledstripnamespace::LedstripStatus* _aidl_return) {
    // Placeholder for random color functionality; update the HAL to include it
    neoPixelHal.setRandomMode();
    _aidl_return->success = true;
    _aidl_return->message = "Random color not implemented in HAL";
    return ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus LedstripService::setBrightness(int brightnessPercentage, ledstripnamespace::LedstripStatus* _aidl_return) {
    neoPixelHal.setBrightness(brightnessPercentage);
    _aidl_return->success = true;
    _aidl_return->message = "Brightness set successfully";
    return ndk::ScopedAStatus::ok();
}

}  // namespace service
}  // namespace example
