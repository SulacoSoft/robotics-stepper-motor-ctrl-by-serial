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
package com.sulacosoft.robots.steppermotor;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jssc.SerialPort;
import jssc.SerialPortException;

public class StepperMotorFabric {

	private static int BAUD_RATE = 115200;
	private static int MOTOR_LIMIT_PER_PORT = 3;
	private static long WAITING_FOR_INITIALIZATION_SERIAL_PORT = 3000;

	private final static ConcurrentHashMap<String, SerialPort> serialPorts = new ConcurrentHashMap<>();
	private final static ConcurrentHashMap<String, Set<StepperMotor>> stepperMotors = new ConcurrentHashMap<>();

	public static synchronized StepperMotor create(String portName, int... pins) throws StepperMotorException {
		SerialPort serialPort = createPortIfDontExist(portName);

		Set<StepperMotor> motorsMap = stepperMotors.get(portName);
		if (motorsMap.size() >= MOTOR_LIMIT_PER_PORT)
			throw new StepperMotorException("Exceeded motors limit per serial port!");

		Set<Integer> notUsedMotors = Stream.of(0, 1, 2).collect(Collectors.toSet());
		stepperMotors.get(portName).forEach(p -> notUsedMotors.remove(p.getMotorNumber()));

		StepperMotor stepperMotor = new StepperMotor(notUsedMotors.iterator().next(), serialPort, pins);

		stepperMotor.init();
		motorsMap.add(stepperMotor);

		return stepperMotor;
	}

	public static synchronized void disconnect(StepperMotor stepperMotor) throws StepperMotorException {
		stepperMotor.rotateOff();
		stepperMotor.disconnectMotor();
		String portName = stepperMotor.getSerialPort().getPortName();
		System.out.println("pobieramy port: " + portName);
		Set<StepperMotor> motors = stepperMotors.get(portName);
		motors.remove(stepperMotor);

		if (motors.size() == 0) {
			motors.remove(stepperMotor);
			try {
				serialPorts.get(portName).closePort();
			} catch (SerialPortException e) {
				throw new StepperMotorException(e);
			}
			serialPorts.remove(portName);
		}
	}

	private static SerialPort createPortIfDontExist(String portName) throws StepperMotorException {
		SerialPort serialPort = serialPorts.get(portName);

		if (serialPort == null) {
			serialPort = new SerialPort(portName);
			try {
				serialPort.openPort();
				serialPort.setParams(BAUD_RATE, 8, 1, 0);
			} catch (SerialPortException e) {
				throw new StepperMotorException(e);
			}

			serialPorts.put(portName, serialPort);
			stepperMotors.put(portName, new HashSet<>());

			try {
				Thread.sleep(WAITING_FOR_INITIALIZATION_SERIAL_PORT);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return serialPort;
	}

}
