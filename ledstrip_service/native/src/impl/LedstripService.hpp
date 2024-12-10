#ifndef LEDSTRIP_SERVICE
#define LEDSTRIP_SERVICE

#include <aidl/ledstrip/service/api/BnLedstripService.h>
#include "/home/farouk/aosp/hardware/interfaces/led_strip/1.0/default/NeoPixelHal.hpp"

namespace ledstripnamespace = ::aidl::ledstrip::service::api;

namespace example {
namespace service {

class LedstripService : public ledstripnamespace::BnLedstripService {
public:
    LedstripService(const std::string& spiDevice, uint32_t numLeds);
    ~LedstripService();

    static std::shared_ptr<LedstripService> getInstance();

    // ILedstripService AIDL interface callbacks
    ::ndk::ScopedAStatus setColor(int index, int red, int green, int blue, ledstripnamespace::LedstripStatus* _aidl_return) final;
    ::ndk::ScopedAStatus clear(ledstripnamespace::LedstripStatus* _aidl_return) final;
    ::ndk::ScopedAStatus show(ledstripnamespace::LedstripStatus* _aidl_return) final;
    ::ndk::ScopedAStatus setGlobalFade(ledstripnamespace::LedstripStatus* _aidl_return) final;
    ::ndk::ScopedAStatus stopAllModes(ledstripnamespace::LedstripStatus* _aidl_return) final;
    ::ndk::ScopedAStatus setRandom(ledstripnamespace::LedstripStatus* _aidl_return) final;
    ::ndk::ScopedAStatus setBrightness(int brightnessPercentage, ledstripnamespace::LedstripStatus* _aidl_return) final;

    const std::string getServiceName(void) {
        return std::string() + descriptor + "/default";
    }

private:
    static std::shared_ptr<LedstripService> S_INSTANCE;
    NeoPixelHal neoPixelHal; // HAL instance
};

}
}
#endif  // LEDSTRIP_SERVICE
