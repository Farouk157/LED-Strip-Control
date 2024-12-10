#include <iostream>
#include <string>
#include <android/binder_manager.h>
#include <android/binder_process.h>
#include <aidl/ledstrip/service/api/ILedstripService.h>
#include "LedstripService.hpp"
#include <utils/Log.h>

using namespace std;
using ::aidl::ledstrip::service::api::ILedstripService;
namespace example {
namespace service {

// Function to display the menu
void displayMenu() {
    cout << "\n--- LED Strip Service Menu ---\n";
    cout << "2. Set LED Color\n";
    cout << "3. Clear LED Strip\n";
    cout << "4. Set Brightness\n";
    cout << "5. Enable/Disable Random Mode\n";
    cout << "6. Enable Global Fade Mode\n";
    cout << "0. Exit\n";
    cout << "Enter your choice: ";
}

}  // namespace service
}  // namespace example

int main() {
    ABinderProcess_setThreadPoolMaxThreadCount(0);

    // Connect to the LedStripService
    const std::string instance = std::string() + ILedstripService::descriptor + "/default";
    std::shared_ptr<ILedstripService> ledStripService = ILedstripService::fromBinder(ndk::SpAIBinder(AServiceManager_getService(instance.c_str())));

    if (!ledStripService) {
        cerr << "Failed to connect to the LedStripService" << endl;
        return 1;
    }

    int choice = -1;
    while (choice != 0) {
        example::service::displayMenu();
        cin >> choice;

        switch (choice) {

        case 2: {
            int index, red, green, blue;
            cout << "Enter LED index: ";
            cin >> index;
            cout << "Enter color values (Red, Green, Blue): ";
            cin >> red >> green >> blue;
            ledstripnamespace::LedstripStatus status;
            if (ledStripService->setColor(index, red, green, blue, &status).isOk()) {
                cout << status.message << endl;
            } else {
                cerr << "Failed to set color.\n";
            }
            break;
        }
        case 3: {
            ledstripnamespace::LedstripStatus status;
            if (ledStripService->clear(&status).isOk()) {
                cout << status.message << endl;
            } else {
                cerr << "Failed to clear LED Strip.\n";
            }
            break;
        }
        case 4: {
            int brightness;
            cout << "Enter brightness percentage (0-100): ";
            cin >> brightness;
            ledstripnamespace::LedstripStatus status;
            if (ledStripService->setBrightness(brightness, &status).isOk()) {
                cout << status.message << endl;
            } else {
                cerr << "Failed to set brightness.\n";
            }
            break;
        }
        case 5: {
            bool continuous;
            cout << "Enable random mode continuously? (1 for Yes, 0 for No): ";
            cin >> continuous;
            ledstripnamespace::LedstripStatus status;
            if (ledStripService->setRandom(&status).isOk()) {
                cout << status.message << endl;
            } else {
                cerr << "Failed to set random mode.\n";
            }
            break;
        }
        case 6: {
            ledstripnamespace::LedstripStatus status;
            if (ledStripService->setGlobalFade(&status).isOk()) {
                cout << status.message << endl;
            } else {
                cerr << "Failed to enable global fade mode.\n";
            }
            break;
        }
        case 0:
            cout << "Exiting...\n";
            break;
        default:
            cerr << "Invalid choice. Please try again.\n";
        }
    }

    return 0;
}
