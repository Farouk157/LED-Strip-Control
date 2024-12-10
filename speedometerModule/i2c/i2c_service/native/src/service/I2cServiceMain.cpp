#include "I2cService.hpp"
#include <utils/Log.h>
#include <signal.h>
#include <android/binder_ibinder.h>
#include <android/binder_manager.h>
#include <android/binder_process.h>

#ifdef LOG_TAG
#undef LOG_TAG
#endif
#define LOG_TAG "i2c_service_main"

int main() {
    ALOGI("Starting I2C service");

    signal(SIGPIPE, SIG_IGN);

    // Set the thread pool size
    if (ABinderProcess_setThreadPoolMaxThreadCount(1)) {
        // Get the instance of I2cService
        std::shared_ptr<example::service::I2cService> i2cObj = example::service::I2cService::getInstance();

        if (i2cObj){
            // Register the service with the system
            binder_status_t status = AServiceManager_addService(i2cObj->asBinder().get(),
                                                                i2cObj->getServiceName().c_str());
            if (status == STATUS_OK){
                ALOGI("I2C service registered successfully");
                ABinderProcess_joinThreadPool(); // Start the service
            } else {
                ALOGE("Failed to register I2C service");
            }
        } else {
            ALOGE("Failed to create I2C service instance");
        }
    } else {
        ALOGE("Failed to configure thread pool");
    }

    return 0;
}


