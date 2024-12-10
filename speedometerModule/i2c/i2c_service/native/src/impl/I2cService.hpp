#ifndef I2C_SERVICE
#define I2C_SERVICE

#include <aidl/i2c/service/api/BnI2cService.h>
#include "/home/farouk/aosp/hardware/interfaces/my_i2c/1.0/default/I2cHal.h"
#include <memory>

namespace example {
namespace service {

// Declare the I2cService class as part of the service namespace
class I2cService : public ::aidl::i2c::service::api::BnI2cService{
public:
    I2cService();
    ~I2cService();

    // Static method to get the singleton instance of the I2cService
    static std::shared_ptr<I2cService> getInstance();

    // Method to get the service name
    std::string getServiceName() const;

    // II2cService AIDL interface methods
    ::ndk::ScopedAStatus init(const std::string& i2cDevicePath, ::aidl::i2c::service::api::I2cStatus* _aidl_return) final;
    ::ndk::ScopedAStatus readValue(int address, int reg, int* _aidl_return) final;
    ::ndk::ScopedAStatus writeValue(int address, int reg, int value, ::aidl::i2c::service::api::I2cStatus* _aidl_return) final;
private:
    static std::shared_ptr<I2cService> S_INSTANCE; // Singleton instance
    I2cHal i2cHal; // Hardware Abstraction Layer instance
};

} // namespace service
} // namespace example

#endif // I2C_SERVICE
