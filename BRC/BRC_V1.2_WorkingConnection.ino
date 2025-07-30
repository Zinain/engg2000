// ==================================================================================
// ==================================================================================
//
//               --------------------        
//                R E A D    M E ! ! 
//               --------------------
//
//         USE ARDUINO IDE
//         INSTALL ESP32Servo LIBRARY IDE
//         IINSTALL ArduinoJson LIBRARY TO IDE
//         GO TO TOOLS > MANAGE LIBARIES > SEARCH FOR ABOVE > INSTALL
//
//          BRC_V1.2 currently compiles.
//          Need to sort read from CCP
//         
//         Currently compiles
//
// =================================================================================
// =================================================================================


#include <SPI.h>
#include <WiFi.h>
#include <WiFiUdp.h>
#include <ArduinoJson.h>
#include <string.h>
#include <ESP32Servo.h>


char ssid[] = "ENGG2K3K";
int status = WL_IDLE_STATUS;


IPAddress serverIP(10, 20, 30, 146);
unsigned int serverPort = 3011;
unsigned short localPort = 8888;
char packetBuffer[255];

//Motor control pin
#define pwmPin 9
#define dirPin 10  // Connect to DIR (direction) to pin 10, 39, 32. 26

//LED pins
#define redLED 19     //stop
#define orangeLED 18  //acel/decel
#define greenLED 5   //go
#define blueLED 13    //functions
#define irPin 21;     // IR phtotransistor
#define servoPin 2;  /// might need to chnge pin

Servo doorServo;
const char* brcStatus = "STOPC";


WiFiUDP Udp;

void setup() {
  Serial.begin(9600);

  pinMode(pwmPin, OUTPUT);
  pinMode(dirPin, OUTPUT);

  //LED Pins
  pinMode(19, OUTPUT);  //red
  pinMode(18, OUTPUT);  //yellow
  pinMode(5, OUTPUT);  //green
  pinMode(13, OUTPUT);  //blue
  //motor Pins
  pinMode(pwmPin, OUTPUT);
  pinMode(dirPin, OUTPUT);
  digitalWrite(dirPin, HIGH);  //HIGH = forward, LOW = reverse afaik
  analogWrite(pwmPin, 0);      //sets speed to 0


  //WiFi start
  WiFi.mode(WIFI_STA);  //Optional
  WiFi.begin(ssid);
  Serial.println("\nConnecting");

  while (WiFi.status() != WL_CONNECTED) {
    Serial.print("connecting..");
    delay(5000);
  }

  Serial.println("\nConnected to the WiFi network");
  Serial.print("Local ESP32 IP: ");
  Serial.println(WiFi.localIP());
  Udp.begin(serverPort);
}

void loop() {

    static bool initialized = false;  //initial send message to CCP
    if (!initialized) {
        if (sendMessage("INIT")) {
            delay(10000);
            int packetSize = Udp.parsePacket();
            if (packetSize) {
                Udp.read(packetBuffer, 255);
                String reply = String(packetBuffer);
                Serial.println("Received reply from CCP: " + reply);
                if (reply == "AKIN") {
                    initialized = true;
                }
            }
        }
    } else {  //now start listening for messages
        int packetSize = Udp.parsePacket();
        if (packetSize) {
            Udp.read(packetBuffer, 255);
            String command = String(packetBuffer);
            Serial.println("Received command from CCP: " + command);
            String response = changeStatus(command);
            sendMessage(response);
        }
    }

}

bool sendMessage(String message) {
  // Send a message to the server
  Udp.beginPacket(serverIP, serverPort);
  Udp.print(message);
  Udp.endPacket();

  Serial.print("Message sent to ");
  Serial.print(serverIP);
  Serial.print(":");
  Serial.println(serverPort);
  return Udp.endPacket() != 0;
}


String changeStatus(String command) {
  String returnMessage;  // used to send back to CCP
  if (command == "STOPC") {
    motionControl(true, 0, true);
    //close door IF IT IS CURRENTLY OPEN
    // red LED solid
    returnMessage = "STOPC";
    brcStatus = "STOPC";
  } else if (command == "STOPO") {
    motionControl(true, 0, true);
    //open door, THEN CLOSE AGAIN
    // red LED solid
    returnMessage = "STOPO";
    brcStatus = "STOPO";
  } else if (command == "FSLOWC") {
    while (!IRDetect()) {
      // yellow led solid
      motionControl(true, 50, false);
    }
    motionControl(true, true, 0);
    // red LED solid
    returnMessage = "STOPC";
    brcStatus = "STOPC";
  } else if (command == "FFASTC") {
    motionControl(true, 100, false);
    //close door IF IT IS CURRENTLY OPEN
    // Green LED solid
    returnMessage = "FFASTC";
    brcStatus = "FFASTC";
  } else if (command == "RSLOWC") {
    while (!IRDetect()) {
      // yellow LED solid
      motionControl(false, 50, false);
    }
    motionControl(true, 0, true);  //stop carriage
    // red LED solid
    //close door IF IT IS CURRENTLY OPEN
    returnMessage = "STOPC";
    brcStatus = "STOPC";
  } else if (command == "DISCONNECT") {
    motionControl(true, 0, true);  //stop carriage
    // SET RED LED TO BLINK
    returnMessage = "OFLN";
    brcStatus = "OFLN";
  } else if (command == "STAT") {
    returnMessage = status;
  } else {
    motionControl(true, 0, true);  // stop carriage
    // red LED solid
    returnMessage = "NOIP";
    brcStatus = "STOPC";
  }
  return returnMessage;
}

void emergencyStop() {
  motionControl(false, 0, true);
}

void motionControl(boolean Forward, int maxVelocity, boolean stop) {

  if (Forward == false) {
    digitalWrite(dirPin, LOW);
  } else {
    digitalWrite(dirPin, HIGH);
  }
  if (stop == false) {
    for (int speed = 0; speed < maxVelocity; speed++) {  //acceletation loop
      analogWrite(pwmPin, speed);
      delay(50);
    }
  } else {
    analogWrite(pwmPin, 0);  // stops
  }
}

void doorControl(int Status) {
  digitalWrite(blueLED, HIGH);

  if (status == 1) {  ///open door
    doorServo.write(180);
    delay(1000);  // this is for how long the motor will run for
    doorServo.write(90);
  } else if (status == 0) {  ///close door
    doorServo.write(0);
    delay(1000);  // this is for how long the motor will run for
    doorServo.write(90);
  }
  delay(1000);
  digitalWrite(blueLED, LOW);
}

bool IRDetect(){

}
