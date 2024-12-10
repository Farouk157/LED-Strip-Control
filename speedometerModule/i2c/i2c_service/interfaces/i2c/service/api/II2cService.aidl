package i2c.service.api;

import i2c.service.api.I2cStatus;

@VintfStability
interface II2cService {
    I2cStatus init(String i2cDevicePath);
    int readValue(int address, int register);
    I2cStatus writeValue(int address, int register, int value); 
}