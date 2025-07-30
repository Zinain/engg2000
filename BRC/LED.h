#ifndef LED_h
#define LED_h

#include "Arduino.h"

class LED {
  public:
    bool isOn;
    LED(int pin);
    void turnOn();
    void turnOff();
  private:
    int _pin;
};

#endif