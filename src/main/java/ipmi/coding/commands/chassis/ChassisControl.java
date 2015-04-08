/*
 * ChassisControl.java 
 * Created on 2011-09-20
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package ipmi.coding.commands.chassis;

import ipmi.coding.commands.CommandCodes;
import ipmi.coding.commands.IpmiCommandCoder;
import ipmi.coding.commands.IpmiVersion;
import ipmi.coding.commands.ResponseData;
import ipmi.coding.payload.CompletionCode;
import ipmi.coding.payload.IpmiPayload;
import ipmi.coding.payload.lan.IPMIException;
import ipmi.coding.payload.lan.IpmiLanRequest;
import ipmi.coding.payload.lan.IpmiLanResponse;
import ipmi.coding.payload.lan.NetworkFunction;
import ipmi.coding.protocol.AuthenticationType;
import ipmi.coding.protocol.IpmiMessage;
import ipmi.coding.security.CipherSuite;
import ipmi.common.TypeConverter;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Wrapper class for Chassis Control request.
 */
public class ChassisControl extends IpmiCommandCoder {

	private PowerCommand powerCommand;

	/**
	 * Initiates ChassisControl for encoding and decoding.
	 * 
	 * @param version
	 *            - IPMI version of the command.
	 * @param cipherSuite
	 *            - {@link CipherSuite} containing authentication,
	 *            confidentiality and integrity algorithms for this session.
	 * @param authenticationType
	 *            - Type of authentication used. Must be RMCPPlus for IPMI v2.0.
	 * @param powerCommand
	 *            - {@link PowerCommand} that is to be performed
	 */
	public ChassisControl(IpmiVersion version, CipherSuite cipherSuite,
			AuthenticationType authenticationType, PowerCommand powerCommand) {
		super(version, cipherSuite, authenticationType);
		this.powerCommand = powerCommand;
	}

	@Override
	public byte getCommandCode() {
		return CommandCodes.CHASSIS_CONTROL;
	}

	@Override
	public NetworkFunction getNetworkFunction() {
		return NetworkFunction.ChassisRequest;
	}

	@Override
	protected IpmiPayload preparePayload(int sequenceNumber)
			throws NoSuchAlgorithmException, InvalidKeyException {
		byte[] requestData = new byte[1];

		requestData[0] = TypeConverter.intToByte(powerCommand.getCode());

		return new IpmiLanRequest(getNetworkFunction(), getCommandCode(),
				requestData, TypeConverter.intToByte(sequenceNumber % 64));
	}

	@Override
	public ResponseData getResponseData(IpmiMessage message)
			throws IllegalArgumentException, IPMIException,
			NoSuchAlgorithmException, InvalidKeyException {
		if (!isCommandResponse(message)) {
			throw new IllegalArgumentException(
					"This is not a response for Get Chassis Status command");
		}
		if (!(message.getPayload() instanceof IpmiLanResponse)) {
			throw new IllegalArgumentException("Invalid response payload");
		}
		if (((IpmiLanResponse) message.getPayload()).getCompletionCode() != CompletionCode.Ok) {
			throw new IPMIException(
					((IpmiLanResponse) message.getPayload())
							.getCompletionCode());
		}
		return new ChassisControlResponseData();
	}

}
