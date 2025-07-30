#include <Arduino.h>
#include <string.h>
#include <WiFi.h>
#include <WifiUdp.h>
#include <bits/stdc++.h> 
#include <stdlib.h> 
#include <unistd.h> 
#include <string.h> 
#include <sys/types.h> 
#include <sys/socket.h> 
#include <arpa/inet.h> 
#include <netinet/in.h> 
#include <servo.h>
   
#define PORT     3011 
#define MAXLINE 1024 


WiFiUDP udp;

const char* ssid = "Macquarie OneNet";
const char* password = "47078529";

//port 6789 from CCP
const char* host = "10.126.217.187";
const int udpPort = 3011;

//Motor control pin
#define pwmPin 9
// Connect to DIR (direction) to pin 10, 39, 32. 26
#define dirPin 10

//LED pins
#define redLED 39  //stop
#define orangeLED 32  //acel/decel
#define greenLED 26  //go
#define blueLED 12   //functions
// IR phtotransistor
#define irPin 11;

#define servoPin 34; /// might need to chnge pin

Servo doorServo;

String status = "STOPC";

void setup() {
  // setup code to run once:
  Serial.begin(9600);

  WiFi.mode(WIFI_STA);  //not sure if needed.
  WiFi.begin(ssid, password);

  
  pinMode(pwmPin, OUTPUT);
  pinMode(dirPin, OUTPUT);

  //LED Pins
  pinMode(39, OUTPUT);  //red
  pinMode(32, OUTPUT);  //orange
  pinMode(26, OUTPUT);  //green
  pinMode(12, OUTPUT);  //blue

  digitalWrite(dirPin, HIGH);  //HIGH = forward, LOW = reverse afaik
  analogWrite(pwmPin, 0);   //sets speed to 0

doorServo.attach(doorPin);
  
}

void loop() {
  // main code to run repeatedly:

  // If an IR light is detected, emergency brake
  if (IRDetect()) {
    motionControl(true, true, 0); // stops BR
  }
}

void startup(){
  int sockfd; 
    char buffer[MAXLINE]; 
    const char *hello = "Hello from client"; 
    struct sockaddr_in     servaddr; 
   
    // Creating socket file descriptor 
    if ( (sockfd = socket(AF_INET, SOCK_DGRAM, 0)) < 0 ) { 
        perror("socket creation failed"); 
        exit(EXIT_FAILURE); 
    } 
   
    memset(&servaddr, 0, sizeof(servaddr)); 
       
    // Filling server information 
    servaddr.sin_family = AF_INET; 
    servaddr.sin_port = htons(PORT); 
    servaddr.sin_addr.s_addr = INADDR_ANY; 
       
    int n;
    socklen_t len; 
       
    sendto(sockfd, (const char *)hello, strlen(hello), 
        MSG_CONFIRM, (const struct sockaddr *) &servaddr,  
            sizeof(servaddr)); 
    std::cout<<"Hello message sent."<<std::endl; 
           
    n = recvfrom(sockfd, (char *)buffer, MAXLINE,  
                MSG_WAITALL, (struct sockaddr *) &servaddr, 
                &len); 
    buffer[n] = '\0'; 
    std::cout<<"Server :"<<buffer<<std::endl; 
   
    close(sockfd); 
    return 0; 
}

String changeStatus(String command){
  String returnMessage; // used to send back to CCP
  if(command=="STOPC"){
    motionControl(true, 0, true);
      //close door IF IT IS CURRENTLY OPEN
      // red LED solid
      returnMessage = "STOPC";
      status = "STOPC";
  }else if(command=="STOPO"){
    motionControl(true, 0, true);
      //open door, THEN CLOSE AGAIN
      // red LED solid
      returnMessage = "STOPO";
      status = "STOPO";
  }else if(command=="FSLOWC"){
    while (!IRDetect()) {
        // yellow led solid
        motionControl(true, 50, false);
      }
      motionControl(true, true, 0);
      // red LED solid
      returnMessage = "STOPC";
      status = "STOPC";
  }else if(command=="FFASTC"){
    motionControl(true, 100, false);
      //close door IF IT IS CURRENTLY OPEN
      // Green LED solid
      returnMessage = "FFASTC";
      status = "FFASTC";
  }else if(command=="RSLOWC"){
    while (!IRDetect()) {
        // yellow LED solid
        motionControl(false, 50, false);
      }
      motionControl(true, 0, true); //stop carriage
      // red LED solid
      //close door IF IT IS CURRENTLY OPEN
      returnMessage = "STOPC";
      status = "STOPC";
  }else if(command=="DISCONNECT"){
    motionControl(true, 0, true);//stop carriage
      // SET RED LED TO BLINK
      returnMessage = "OFLN";
      status = "OFLN";
  }else if(command=="STAT"){
    returnMessage = status;
  }else{
    motionControl(true, 0, true); // stop carriage
      // red LED solid
      returnMessage = "NOIP";
      status = "STOPC";
  }
  return returnMessage;
}

void recconect(){

}

bool IRDetect() {
  // returns true if it senses IR Signal
  int sensorVal = analogRead(irPin);
  return sensorVal >= 100;
}

void emergencyStop(){
  motionControl(0);
}

void motionControl(boolean Forward, int maxVelocity, boolean stop){

  if(Forward==false){
    digitalWrite(dirPin, LOW);
  }else{
    digitalWrite(dirPin, HIGH);
  }
  if(stop==false){
    for(int speed = 0; speed < maxVelocity; speed++){  //acceletation loop
      analogWrite(pwmPin, speed);
      delay(50);
    }
  }else{
    analogWrite(pwmPin, 0); // stops 
  }
}

void doorControl(int Status){
  digitalWrite(blueLED, HIGH);

  if(status==1){///open door
    doorServo.write(180);
    delay(1000); // this is for how long the motor will run for
    door servo.write(90);
  }else if(status==0){///close door
    doorServo.write(0);
    delay(1000); // this is for how long the motor will run for
    door servo.write(90);
  }
  delay(1000);
  digitalWrite(blueLED, LOW);
}

//void LEDControl(){

//}
