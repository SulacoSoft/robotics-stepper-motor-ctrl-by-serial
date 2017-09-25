## Introduction

StepperMotorConnector4J is a Java and Arduino Language library to simple control unipolar stepper motors.

## Limitations

Library working on Arduino shield except the Esplora, Leonardo or Micro.

It is possible to connect up to three motors to one arduino. The limit is based on the number of Arduino exits. You can connect several arduino to different USB ports and control more motors than three.

When calculating execution end time of specified number of steps, a simple algorithm is used to estimate time of operation. In future versions this inconvenience will be improved.


## How to build and install library

### Download or clone sources:

    git clone https://github.com/SulacoSoft/stepper-motor-connector-4j.git

### Install binaries on Arduino

Open file etc/stepper_motor_ctrl_by_serial.ino in Arduino IDE and upload on Arduino. Library is not compatible only with the Esplora, Leonardo or Micro.

### Prepare jar library

Run gradle command:

    gradle build

A stepper-motor-connector-4j-*.jar file will be created in directory build\libs\ 

We can import this file to own java project.

## Examples

Init motor on port COM7 (Windows USB port name, on linux example name is ttyUSB7) and define stepper motor controler pins 6, 7, 8, 9.

    StepperMotor motor = StepperMotorFabric.create("COM7", 6, 7, 8, 9);

Rotate 2048 steps to left:

    motor.rotateLeftSteps(2048);

and waiting to estimated end operation:

    while (!motor.isReady())
        Thread.sleep(10L);

next turn right 512 steps:

    motor.rotateRightSteps(512);

turn pernamently rotate right:

    motor.rotateRightOn();

and left:

    motor.rotateLeftOn();

stop motor:

    motor.rotateOff();

and disconnect motor:

    StepperMotorFabric.disconnect(motor);

We can change delay time between motor steps in microseconds. This parameter change delay time on all connected motors to selected Arduino board:

    motor.setDelay(1200);

We can open maximum three motors to one borad:

    StepperMotor motorA = StepperMotorFabric.create("COM7", 2, 3, 4, 5);
    StepperMotor motorB = StepperMotorFabric.create("COM7", 6, 7, 8, 9);
    StepperMotor motorC = StepperMotorFabric.create("COM7", ...
