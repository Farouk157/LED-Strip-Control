#include "I2cHal.h"
#include <fcntl.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <linux/i2c-dev.h>
#include <cerrno>

I2cHal::I2cHal() : i2cFile(-1) {}

I2cHal::~I2cHal() {
    if (i2cFile >= 0) {
        close(i2cFile);
    }
}

bool I2cHal::init(const std::string &i2cDevicePath) {
    i2cFile = open(i2cDevicePath.c_str(), O_RDWR);
    if (i2cFile < 0) {
        std::cerr << "Failed to open I2C device: " << i2cDevicePath << std::endl;
        return false;
    }
    return true;
}

int I2cHal::readValue(int address, int reg) {
    if (ioctl(i2cFile, I2C_SLAVE, address) < 0) {
        std::cerr << "Failed to set I2C address: " << address << std::endl;
        return -1;
    }
    write(i2cFile, &reg, 1);

    uint8_t buffer;
    if (read(i2cFile, &buffer, 1) != 1) {
        std::cerr << "Failed to read from I2C device" << std::endl;
        return -1;
    }
    return buffer;
}

bool I2cHal::writeValue(int address, int reg, int value) {
    if (ioctl(i2cFile, I2C_SLAVE, address) < 0) {
        std::cerr << "Failed to set I2C address: " << address << std::endl;
        return false;
    }
    uint8_t buffer[3] = { static_cast<uint8_t>(reg), 
                          static_cast<uint8_t>(value >> 8), 
                          static_cast<uint8_t>(value & 0xFF) };
    if (write(i2cFile, buffer, sizeof(buffer)) != sizeof(buffer)) {
        std::cerr << "Failed to write to I2C device" << std::endl;
        return false;
    }
    return true;
}