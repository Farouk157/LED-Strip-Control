#include "/home/farouk/aosp/hardware/interfaces/led_strip/1.0/default/NeoPixelHal.hpp"
#include <thread>  // For std::this_thread::sleep_for
#include <chrono>  // For std::chrono::milliseconds
#include <iostream> // For std::cin and std::cout

int main() {
    NeoPixelHal neoPixel("/dev/spidev0.0", 8); // 8 LEDs

    // Initialize the LEDs to be turned off (all black)
    neoPixel.clear();
    if (!neoPixel.show()) {
        return 1;
    }

    std::cout << "Welcome to the NeoPixel Controller.\n";
    std::cout << "Enter 'x' for random mode, 'm' for manual mode, 'f' for global fade mode, or 'q' to quit.\n";

    char mode = 'm'; // Default mode is manual
    int brightness = 100; // Default brightness

    while (true) {
        if (std::cin.peek() != EOF) {
            std::cin >> mode;

            if (mode == 'f') {
                std::cout << "Entering global fade mode. Press 'm' to switch to manual mode or 'q' to quit.\n";

                std::thread fadeThread([&neoPixel]() {
                    neoPixel.setGlobalFadeMode(); // This will run continuously
                });

                // Stay in fade mode until the user switches or quits
                while (true) {
                    if (std::cin.peek() != EOF) {
                        std::cin >> mode;
                        if (mode == 'm') {
                            std::cout << "Switching to manual mode.\n";
                            fadeThread.detach(); // Detach the fade thread
                            break;
                        } else if (mode == 'q') {
                            std::cout << "Exiting program.\n";
                            fadeThread.detach(); // Detach the fade thread
                            return 0;
                        }
                    }
                }
            } else if (mode == 'x') {
                std::cout << "Entering random mode. Press 'm' to switch to manual mode or 'q' to quit.\n";

                std::thread randomThread([&neoPixel]() {
                    neoPixel.setRandomMode(true); // This will run continuously
                });

                // Stay in random mode until the user switches or quits
                while (true) {
                    if (std::cin.peek() != EOF) {
                        std::cin >> mode;
                        if (mode == 'm') {
                            std::cout << "Switching to manual mode.\n";
                            randomThread.detach(); // Detach the thread
                            break;
                        } else if (mode == 'q') {
                            std::cout << "Exiting program.\n";
                            randomThread.detach(); // Detach the thread
                            return 0;
                        }
                    }
                }
            } else if (mode == 'm') {
                std::cout << "Manual mode selected. Enter 'r', 'g', 'b' to set color, or 'x' for random mode.\n";
                char input;
                uint8_t red = 0, green = 0, blue = 0;

                std::cin >> input;

                if (input == 'r') {
                    red = 255;
                } else if (input == 'g') {
                    green = 255;
                } else if (input == 'b') {
                    blue = 255;
                }

                if (red || green || blue) {
                    std::cout << "Set brightness (0-100): ";
                    std::cin >> brightness;

                    if (brightness < 0 || brightness > 100) {
                        std::cout << "Invalid brightness. Defaulting to 100%.\n";
                        brightness = 100;
                    }

                    neoPixel.setBrightness(brightness);

                    for (uint32_t i = 0; i < 8; ++i) {
                        neoPixel.setColor(i, red, green, blue);
                    }
                    neoPixel.show();
                }
            } else if (mode == 'q') {
                std::cout << "Exiting program.\n";
                break;
            } else {
                std::cout << "Invalid input. Enter 'x' for random mode, 'm' for manual mode, 'f' for global fade mode, or 'q' to quit.\n";
            }
        }

        std::this_thread::sleep_for(std::chrono::milliseconds(100)); // Avoid CPU overuse
    }

    neoPixel.clear();
    neoPixel.show();
    return 0;
}

