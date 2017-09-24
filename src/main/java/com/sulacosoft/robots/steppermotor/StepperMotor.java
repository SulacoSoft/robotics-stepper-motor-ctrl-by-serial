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

import jssc.SerialPort;
import jssc.SerialPortException;

public class StepperMotor {

	private final static byte CMD_INIT_MOTOR = 1;
	private final static byte CMD_DISCONNECT_MOTOR = 2;
	private final static byte CMD_ROTATE_LEFT_STEPS = 3;
	private final static byte CMD_ROTATE_RIGHT_STEPS = 4;
	private final static byte CMD_ROTATE_LEFT_ON = 5;
	private final static byte CMD_ROTATE_RIGHT_ON = 6;
	private final static byte CMD_ROTATE_OFF = 7;
	private final static byte CMD_SET_DELAY = 8;
	
	private static int DEFAULT_STEP_DELAY_MICROSECONDS = 1000;

	private int motorNumber;
	private SerialPort serialPort;
	private int[] pins;
	private long endBlockingTimeOperation = 0L;
	private int currentStepDelayMicroseconds = DEFAULT_STEP_DELAY_MICROSECONDS;
	
	protected StepperMotor(int motorNumber, SerialPort serialPort, int[] pins) {
		this.motorNumber = motorNumber;
		this.serialPort = serialPort;
		this.pins = pins;
	}

	protected void init() throws StepperMotorException {
		send(new byte[] { CMD_INIT_MOTOR, (byte) motorNumber, (byte) pins[0], (byte) pins[1], (byte) pins[2],
				(byte) pins[3] });

		setDelay(currentStepDelayMicroseconds);
	}

	protected int getMotorNumber() {
		return motorNumber;
	}

	public void rotateLeftOn() throws StepperMotorException {
		send(new byte[] { CMD_ROTATE_RIGHT_ON, (byte) motorNumber });
	}

	public void rotateRightOn() throws StepperMotorException {
		send(new byte[] { CMD_ROTATE_LEFT_ON, (byte) motorNumber });
	}

	public void rotateLeftSteps(int steps) throws StepperMotorException {
		calculateAndSetEndOpertionTime(steps);
		byte[] stepsTable = intToByteArray(steps);
		send(new byte[] { CMD_ROTATE_RIGHT_STEPS, (byte) motorNumber, stepsTable[3], stepsTable[2], stepsTable[1],
				stepsTable[0] });
	}

	public void rotateRightSteps(int steps) throws StepperMotorException {
		calculateAndSetEndOpertionTime(steps);
		byte[] stepsTable = intToByteArray(steps);
		send(new byte[] { CMD_ROTATE_LEFT_STEPS, (byte) motorNumber, stepsTable[3], stepsTable[2], stepsTable[1],
				stepsTable[0] });
	}

	public void rotateOff() throws StepperMotorException {
		send(new byte[] { CMD_ROTATE_OFF, (byte) motorNumber });
	}

	public void setDelay(int stepDelayMicroseconds) throws StepperMotorException {
		this.currentStepDelayMicroseconds = stepDelayMicroseconds;
		byte[] delayTable = intToByteArray(currentStepDelayMicroseconds);
		send(new byte[] { CMD_SET_DELAY, delayTable[3], delayTable[2], delayTable[1], delayTable[0] });
	}

	public boolean isReady() {
		return endBlockingTimeOperation < System.currentTimeMillis();
	}
	
	protected SerialPort getSerialPort() {
		return serialPort;
	}

	protected void disconnectMotor() throws StepperMotorException {
		send(new byte[] { CMD_DISCONNECT_MOTOR, (byte) motorNumber });
	}

	private void send(byte[] buffer) throws StepperMotorException {
		try {
			serialPort.writeBytes(buffer);
		} catch (SerialPortException e) {
			throw new StepperMotorException(e);
		}
	}

	private byte[] intToByteArray(int value) {
		return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
	}


	private void calculateAndSetEndOpertionTime(int steps) {
		long expectedTime = steps * currentStepDelayMicroseconds / 1000L;
		long errorEstimationMargin = expectedTime/10;
		endBlockingTimeOperation = System.currentTimeMillis() + expectedTime + errorEstimationMargin;
	}

}
