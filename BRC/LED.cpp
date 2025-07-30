#include "Arduino.h"
#include "LED.h"

LED::LED(int pin) {
  pinMode(pin, OUTPUT);
  _pin = pin;
  isOn = false;
}

void LED::turnOn() {
  digitalWrite(_pin, HIGH);
  isOn = true;
}

void LED::TurnOff() {
  digitalWrite(_pin, LOW);
  isOn = false;
}