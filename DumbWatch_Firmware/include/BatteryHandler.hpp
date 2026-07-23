#ifndef BATTERYHANDLER_HPP
#define BATTERYHANDLER_HPP

#include <Arduino.h>

#include "HandlerInterface.hpp"

#define BAT_VOLT_PIN 6
#define BAT_VOLT_PIN_EN 26

#define BAT_CHECK_ITERATIONS 16.0

struct BatteryHandler : public HandlerInterface
{
    float voltage = 0.0f;
    int percentage = 0;

    void UpdateState() override
    {
        // enable ADC pin
        digitalWrite(BAT_VOLT_PIN_EN, HIGH);
        delay(10);

        uint32_t voltageSum = 0;
        for (int i = 0; i < BAT_CHECK_ITERATIONS; i++)
            voltageSum += analogReadMilliVolts(BAT_VOLT_PIN);

        // disable ADC pin
        digitalWrite(BAT_VOLT_PIN_EN, LOW);

        // https://wiki.seeedstudio.com/check_battery_voltage/

        // convert ADC reading to voltage
        // 2 * voltageSum because ADC is connected to a 1:2 voltage divider
        // voltageSum / [BAT_CHECK_ITERATIONS] to average all [BAT_CHECK_ITERATIONS] measurements
        // / 1000 to convert from mV to V (ex. 3500mV -> 3.5V)
        voltage = (2.0 * voltageSum / BAT_CHECK_ITERATIONS) / 1000.0;

        // LiPo percentage estimate
        // batteryVoltage - 3.20 moves the zero point to 3.20V we want distance from empty (empty:3.2V)
        // (4.20 - 3.20) total usable voltage range
        // (batteryVoltage - 3.20) / (4.20 - 3.20) converts the distance from empty into a decimal
        // * 100 to convert decimal to percentage
        percentage = (int)((voltage - 3.20) / (4.20 - 3.20) * 100);
        percentage = constrain(percentage, 0, 100);
    }

    void begin() override
    {
        pinMode(BAT_VOLT_PIN, INPUT);
        pinMode(BAT_VOLT_PIN_EN, OUTPUT);

        // disable ADC pin
        // ADC pin can cause interference with other pins
        digitalWrite(BAT_VOLT_PIN_EN, LOW);
    }
};

#endif // BATTERYHANDLER_HPP