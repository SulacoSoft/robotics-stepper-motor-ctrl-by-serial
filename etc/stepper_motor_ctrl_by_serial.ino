/*
 * Author: sebastian.dziak@gmail.com
 *
 * Copyright (C) 2017 sulacosoft.com
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#define MAX_MOTORS 3
#define MOTOR_PINS 4

#define CMD_INIT_MOTOR 1
#define CMD_DISCONNECT_MOTOR 2
#define CMD_ROTATE_LEFT_STEPS 3
#define CMD_ROTATE_RIGHT_STEPS 4
#define CMD_ROTATE_LEFT_ON 5
#define CMD_ROTATE_RIGHT_ON 6
#define CMD_ROTATE_OFF 7
#define CMD_SET_DELAY 8

int powerMap[8][4] = {
  {LOW,LOW,LOW,HIGH},
  {LOW,LOW,HIGH,HIGH},
  {LOW,LOW,HIGH,LOW},
  {LOW,HIGH,HIGH,LOW},
  {LOW,HIGH,LOW,LOW},
  {HIGH,HIGH,LOW,LOW},
  {HIGH,LOW,LOW,LOW},
  {HIGH,LOW,LOW,HIGH}
};

boolean motorEnabled[MOTOR_PINS] = {false,false,false};
int motorsOnPermanently[MAX_MOTORS] = {false,false,false};
int motorsPins[MAX_MOTORS][MOTOR_PINS];
int motorsDirection[MAX_MOTORS]; // 0 - left; 1 - right
int motorsStepPosition[MAX_MOTORS] = {0,0,0};
int motorsStepCount[MAX_MOTORS] = {0,0,0};
int stepDelay = 1000;

void setup() {
  Serial.begin(115200);
}

void loop() {
  for( int i = 0; i < MAX_MOTORS; i++ ) {

    if( motorEnabled[i] != true )
      continue;
    if( motorsStepCount[i] == 0 && motorsOnPermanently[i] == false )
      continue;

    if( motorsStepCount[i] > 0 )
      motorsStepCount[i]--;

      if( motorsDirection[i] == 0 ) { 
        motorsStepPosition[i]--;
        if( motorsStepPosition[i] < 0 )
          motorsStepPosition[i] = 7;
      }
      else {
        motorsStepPosition[i]++;
        if( motorsStepPosition[i] > 7 )
          motorsStepPosition[i] = 0;
      }

      for(int j=0; j<4; j++) 
        digitalWrite(motorsPins[i][j], powerMap[motorsStepPosition[i]][j]);
  }

  delayMicroseconds(stepDelay);
}

void serialEvent() {
  int command = Serial.read();
  delay(5);

  if(command == CMD_INIT_MOTOR) {    
    int motorNumber = Serial.read();
    int pin0 = Serial.read();
    int pin1 = Serial.read();
    int pin2 = Serial.read();
    int pin3 = Serial.read();

    motorsPins[motorNumber][0] = pin0;
    motorsPins[motorNumber][1] = pin1;
    motorsPins[motorNumber][2] = pin2;
    motorsPins[motorNumber][3] = pin3;

    pinMode(pin0, OUTPUT); 
    pinMode(pin1, OUTPUT); 
    pinMode(pin2, OUTPUT); 
    pinMode(pin3, OUTPUT); 
    
    motorEnabled[motorNumber] = true;
  }
  else if(command == CMD_DISCONNECT_MOTOR) {
     int motorNumber = Serial.read();
     motorEnabled[motorNumber] = false;
  }
  else if(command == CMD_ROTATE_LEFT_STEPS) {
     int motorNumber = Serial.read();
     motorsDirection[motorNumber] = 0;
     motorsStepCount[motorNumber] = readInt();
  }
  else if(command == CMD_ROTATE_RIGHT_STEPS) {
     int motorNumber = Serial.read();
     motorsDirection[motorNumber] = 1;
     motorsStepCount[motorNumber] = readInt();
  }
  else if(command == CMD_ROTATE_LEFT_ON) {
     int motorNumber = Serial.read();
     motorsDirection[motorNumber] = 0;
     motorsOnPermanently[motorNumber] = true;
     motorsStepPosition[motorNumber] = 0;     
  }
  else if(command == CMD_ROTATE_RIGHT_ON) {
     int motorNumber = Serial.read();
     motorsDirection[motorNumber] = 1;
     motorsOnPermanently[motorNumber] = true;
     motorsStepPosition[motorNumber] = 0;
  }
  else if(command == CMD_ROTATE_OFF) {
     int motorNumber = Serial.read();
     motorsOnPermanently[motorNumber] = false;
  }
  else if(command == CMD_SET_DELAY) {
    stepDelay = readInt();
  }
  else if(command == CMD_DISCONNECT_MOTOR) {
    int motorNumber = Serial.read();
    motorEnabled[motorNumber] = false;
    motorsStepPosition[motorNumber] = 0;
    motorsPins[motorNumber][0] = 0;
    motorsPins[motorNumber][1] = 0;
    motorsPins[motorNumber][2] = 0;
    motorsPins[motorNumber][3] = 0;
  }
}

int readInt() {
  byte b0 = Serial.read();
  byte b1 = Serial.read();
  byte b2 = Serial.read();
  byte b3 = Serial.read();
  byte bytes[4] = {b3,b2,b1,b0};
  return byteArrayToInt(bytes);
}

int byteArrayToInt(byte bytes[]) {
  int value = bytes[0];
  value = value * 256 + bytes[1];
  value = value * 256 + bytes[2];
  value = value * 256 + bytes[3];
  return value;
}

