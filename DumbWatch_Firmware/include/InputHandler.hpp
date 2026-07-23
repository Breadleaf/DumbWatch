#ifndef INPUTHANDLER_HPP
#define INPUTHANDLER_HPP

#include <Arduino.h>
#include <array>
#include <initializer_list>

#include "HandlerInterface.hpp"
#include "Types.hpp"

template <std::size_t N>
struct InputHandler : public HandlerInterface
{
    struct ButtonState_t
    {
        bool current = false;
        bool previous = false;

        bool pressed() const
        {
            return current && !previous;
        }

        bool released() const
        {
            return !current && previous;
        }

        bool held() const
        {
            return current;
        }
    };

    std::array<types::Pin_t, N> pins;
    std::array<ButtonState_t, N> buttons;

    InputHandler(const std::array<types::Pin_t, N> &pinsList) : pins(pinsList) {}

    void begin() override
    {
        for (types::Pin_t pin : pins)
            pinMode(pin, INPUT_PULLUP); // active low
    }

    void UpdateState() override
    {
        for (std::size_t iter = 0; iter < N; iter++)
        {
            buttons[iter].previous = buttons[iter].current;
            buttons[iter].current = digitalRead(pins[iter]) == LOW; // active low
        }
    }
};

#endif // INPUTHANDLER_HPP