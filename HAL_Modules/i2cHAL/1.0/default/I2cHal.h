#ifndef HARDWARE_INTERFACES_MY_I2C_V1_0_I2CHAL_H_
#define HARDWARE_INTERFACES_MY_I2C_V1_0_I2CHAL_H_

#include <iostream>

class I2cHal {
public:
    I2cHal();
    ~I2cHal();

    // Method to initialize I2C communication
    bool init(const std::string &i2cDevicePath);

    // Method to read a value from an I2C address
    int readValue(int address, int reg);

    bool writeValue(int address, int reg, int value);
private:
    int i2cFile;
};
 
#endif