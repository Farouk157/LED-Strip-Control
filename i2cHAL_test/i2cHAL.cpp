#include "I2cHal.h"
#include <iostream>
#include <string>

int main() {
    // Initialize the I2C HAL
    I2cHal i2c;

    // Specify the I2C device path (e.g., "/dev/i2c-1")
    const std::string i2cDevicePath = "/dev/i2c-1";

    // Initialize the I2C device
    if (!i2c.init(i2cDevicePath)) {
        std::cerr << "Failed to initialize I2C device" << std::endl;
        return 1;
    }

    // Define an I2C slave address and register
    int i2cAddress = 0x48; // Replace with your I2C device address
    int registerAddress = 0x00; // Replace with a valid register address

    // Read a value from the I2C device
    int readValue = i2c.readValue(i2cAddress, registerAddress);
    if (readValue < 0) {
        std::cerr << "Failed to read value from I2C device" << std::endl;
    } else {
        std::cout << "Read value: 0x" << std::hex << readValue << std::endl;
    }

    // Write a value to the I2C device
    int valueToWrite = 0x1234; // Replace with the value you want to write
    if (!i2c.writeValue(i2cAddress, registerAddress, valueToWrite)) {
        std::cerr << "Failed to write value to I2C device" << std::endl;
    } else {
        std::cout << "Wrote value: 0x" << std::hex << valueToWrite << std::endl;
    }

    return 0;
}

