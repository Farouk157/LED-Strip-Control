#include "LedstripService.hpp"
#include <signal.h>
#include <android/binder_manager.h>
#include <android/binder_process.h>
#include <utils/Log.h>

#ifdef LOG_TAG
#undef LOG_TAG
#endif
#define LOG_TAG "ledstrip_service_main"

int main() {
    ALOGI("Starting LedstripService");

    signal(SIGPIPE, SIG_IGN);
    if (ABinderProcess_setThreadPoolMaxThreadCount(1)) {
        auto service = example::service::LedstripService::getInstance();
        if (service) {
            ALOGI("Registering service: %s", service->getServiceName().c_str());
            binder_status_t status = AServiceManager_addService(service->asBinder().get(), service->getServiceName().c_str());
            if (status == STATUS_OK) {
                ALOGI("Service registered successfully");
                ABinderProcess_joinThreadPool();
            } else {
                ALOGE("Failed to register service");
            }
        } else {
            ALOGE("Failed to create service instance");
        }
    } else {
        ALOGE("Failed to configure thread pool");
    }

    return 0;
}
