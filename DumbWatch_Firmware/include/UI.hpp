#ifndef UI_HPP
#define UI_HPP

#include <Adafruit_SSD1306.h>
#include <Adafruit_GFX.h>

#include "HandlerInterface.hpp"
#include "BatteryHandler.hpp"

struct UI
{
    enum class Screen
    {
        HOME,
        BATTERY,
        SETTINGS,
    };

    UI(Adafruit_SSD1306 &display, BatteryHandler &battery)
        : display(display), battery(battery) {}

    void Render()
    {
        if (!dirty)
            return;

        display.clearDisplay();

        switch (screen)
        {
        case Screen::HOME:
            renderHome();
            break;
        case Screen::BATTERY:
            renderBattery();
            break;
        case Screen::SETTINGS:
            renderSettings();
            break;
        }

        display.display();
        dirty = false;
    }

    void SetScreen(Screen newScreen)
    {
        if (screen == newScreen)
            return;

        screen = newScreen;
        dirty = true;
    }

    // interface for other resources to update their state
    // ex: battery.UpdateState() -> if (new% != old%) ui.MarkDirty()
    void MarkDirty()
    {
        dirty = true;
    }

private:
    void renderHome();
    void renderBattery();
    void renderSettings();

    Adafruit_SSD1306 &display;
    BatteryHandler &battery;

    Screen screen = Screen::HOME;

    // only render when dirty
    bool dirty = true;
};

void UI::renderHome()
{
    display.setTextSize(1);
    display.setCursor(0, 0);
    display.println("HOME");
    display.println("[BTN0] home");
    display.println("[BTN1] battery");
    display.println("[BTN2] settings");
}

void UI::renderBattery()
{
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    display.setCursor(0, 0);
    display.println("BATTERY");

    display.setCursor(0, 12);
    display.print("Voltage: ");
    display.print(battery.voltage, 2);
    display.println(" V");

    display.setCursor(0, 22);
    display.print("Level: ");
    display.print(battery.percentage);
    display.println("%");
}

void UI::renderSettings()
{
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    display.setCursor(0, 0);
    display.println("SETTINGS");
    display.println("Brightness: 100%");
}

#endif // UI_HPP