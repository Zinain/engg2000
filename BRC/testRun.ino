#include <Arduino.h>
// Connect pwm to pin 9
#define pwmPin 9 
// Connect to DIR (direction) to pin 10, 39, 32. 26
#define dirPin 10
// IR pin - need to double check numbers
#define irPin 11

int pwmValue = 0;

void setup(){
    Serial.begin(9600);
    pinMode(pwmPin, OUTPUT);
    pinMode(dirPin, OUTPUT);

    //LED Pins
    pinMode(39, OUTPUT);  //red
    pinMode(32, OUTPUT);  //orange
    pinMode(26, OUTPUT);  //green
    // one more is needed for blue

    digitalWrite(dirPin, HIGH);  //HIGH = forward, LOW = reverse afaik
    analogWrite(pwmPin, 0);   //sets speed to 0

}

void loop(){

    digitalWrite(39, HIGH);
    digitalWrite(dirPin, HIGH); //set to forward

    delay(2000);

    digitalWrite(39, LOW);
    digitalWrite(32, HIGH);
    digitalWrite(26, HIGH);
    
    for(int speed = 0; speed < 100; speed++){  //acceletation loop
        analogWrite(pwmPin, speed);
        delay(50);
    }

    digitalWrite(32, LOW);
    
    delay(2000);

    digitalWrite(26, LOW);
    digitalWrite(32, HIGH);
    digitalWrite(39, HIGH);
    
    for(int speed = 100; speed >= 0; speed--){
      analogWrite(pwmPin, speed);
      delay(50);
    }

    digitalWrite(32, LOW);
    
    analogWrite(pwmPin, 0);

    //--------- potential IR Detection ------------

    digitalWrite(dirPin, 10); // forward at speed 10?

    // If an IR light is detected, emergency brake
    if (IRDetect()) {
      analogWrite(pwmPin, 0); // stop
    }
    
    openDoors();   // Open the doors
    delay(3000);   // Wait for 3 seconds (simulate loading/unloading)
    closeDoors();  // Close the doors
    delay(3000);   // Wait before next cycle

    delay(5000);  // Delay before restarting the loop
    //---------------------------------------------


    //--------- potential door test here ----------


    //---------------------------------------------
    
    // haha funny
    while(1){
    }
}

bool IRDetect() {
  int sensorVal = analogRead(irPin);
  return sensorVal >= 100;
}

void openDoors() {
    Serial.println("Opening doors...");
    doorServo.write(doorOpenAngle); 
    delay(1000);  // Wait for the doors to fully open
    Serial.println("Doors are open.");
}

// Function to close the doors using servo
void closeDoors() {
    Serial.println("Closing doors...");
    doorServo.write(doorCloseAngle);
    delay(1000);  // Wait for the doors to fully close
    Serial.println("Doors are closed.");
}
