/*
 * ReadFruData.java 
 * Created on 2011-08-11
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package ipmi.coding.commands.fru;

import ipmi.coding.commands.CommandCodes;
import ipmi.coding.commands.IpmiCommandCoder;
import ipmi.coding.commands.IpmiVersion;
import ipmi.coding.commands.ResponseData;
import ipmi.coding.commands.fru.record.BoardInfo;
import ipmi.coding.commands.fru.record.ChassisInfo;
import ipmi.coding.commands.fru.record.FruRecord;
import ipmi.coding.commands.fru.record.MultiRecordInfo;
import ipmi.coding.commands.fru.record.ProductInfo;
import ipmi.coding.commands.sdr.GetSdr;
import ipmi.coding.commands.sdr.record.FruDeviceLocatorRecord;
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
import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper class for Read FRU Data Command request. <br>
 * The command returns the specified data from the FRU Inventory Info area.
 */
public class ReadFruData extends IpmiCommandCoder {

	private int offset;

	private int size;

	private int fruId;

	/**
	 * Initiates ReadFruData for both encoding and decoding. Sets session
	 * parameters to default.
	 * 
	 * @see IpmiCommandCoder#setSessionParameters(IpmiVersion, CipherSuite,
	 *      AuthenticationType)
	 * @param fruId
	 *            - ID of the FRU to get info from. Must be less than 256. To
	 *            get FRU ID use {@link GetSdr} to retrieve
	 *            {@link FruDeviceLocatorRecord}.
	 * @param unit
	 *            - {@link BaseUnit} indicating if the FRU device is accessed in
	 *            {@link BaseUnit#Bytes} or {@link BaseUnit#Words}
	 * @param offset
	 *            - offset to read in units specified by unit
	 * @param countToRead
	 *            - size of the area to read in unit. Cannot exceed 255;
	 */
	public ReadFruData(int fruId, BaseUnit unit, int offset, int countToRead) {
		super();

		if (countToRead > 255) {
			throw new IllegalArgumentException(
					"Count to read cannot exceed 255");
		}

		if (fruId > 255) {
			throw new IllegalArgumentException("FRU ID cannot exceed 255");
		}

		this.offset = offset * unit.getSize();

		size = countToRead * unit.getSize();

		this.fruId = fruId;
		// TODO: Check if Count To Read field is encoded in words if the FRU is
		// addressed in words (requires different server settings).
	}

	/**
	 * Initiates ReadFruData for both encoding and decoding.
	 * 
	 * @param version
	 *            - IPMI version of the command.
	 * @param cipherSuite
	 *            - {@link CipherSuite} containing authentication,
	 *            confidentiality and integrity algorithms for this session.
	 * @param authenticationType
	 *            - Type of authentication used. Must be RMCPPlus for IPMI v2.0.
	 * @param fruId
	 *            - ID of the FRU to get info from. Must be less than 256. To
	 *            get FRU ID use {@link GetSdr} to retrieve
	 *            {@link FruDeviceLocatorRecord}.
	 * @param unit
	 *            - {@link BaseUnit} indicating if the FRU device is accessed in
	 *            {@link BaseUnit#Bytes} or {@link BaseUnit#Words}
	 * @param offset
	 *            - offset to read in units specified by unit
	 * @param countToRead
	 *            - size of the area to read in unit. Cannot exceed 255;
	 */
	public ReadFruData(IpmiVersion version, CipherSuite cipherSuite,
			AuthenticationType authenticationType, int fruId, BaseUnit unit,
			int offset, int countToRead) {
		super(version, cipherSuite, authenticationType);

		if (countToRead > 255) {
			throw new IllegalArgumentException(
					"Count to read cannot exceed 255");
		}

		if (fruId > 255) {
			throw new IllegalArgumentException("FRU ID cannot exceed 255");
		}

		this.offset = offset * unit.getSize();

		size = countToRead * unit.getSize();

		this.fruId = fruId;
		// TODO: Check if Count To Read field is encoded in words if the FRU is
		// addressed in words (requires different server settings).
	}

	@Override
	public byte getCommandCode() {
		return CommandCodes.READ_FRU_DATA;
	}

	@Override
	public NetworkFunction getNetworkFunction() {
		return NetworkFunction.StorageRequest;
	}

	@Override
	protected IpmiPayload preparePayload(int sequenceNumber)
			throws NoSuchAlgorithmException, InvalidKeyException {
		byte[] payload = new byte[4];
		payload[0] = TypeConverter.intToByte(fruId);
		byte[] buffer = TypeConverter.intToLittleEndianByteArray(offset);
		payload[1] = buffer[0];
		payload[2] = buffer[1];
		payload[3] = TypeConverter.intToByte(size);

		return new IpmiLanRequest(getNetworkFunction(), getCommandCode(),
				payload, TypeConverter.intToByte(sequenceNumber % 64));
	}

	@Override
	public ResponseData getResponseData(IpmiMessage message)
			throws IllegalArgumentException, IPMIException,
			NoSuchAlgorithmException, InvalidKeyException {

		if (!isCommandResponse(message)) {
			throw new IllegalArgumentException(
					"This is not a response for Get SDR Repository Info command");
		}
		if (!(message.getPayload() instanceof IpmiLanResponse)) {
			throw new IllegalArgumentException("Invalid response payload");
		}
		if (((IpmiLanResponse) message.getPayload()).getCompletionCode() != CompletionCode.Ok) {
			throw new IPMIException(
					((IpmiLanResponse) message.getPayload())
							.getCompletionCode());
		}

		byte[] raw = message.getPayload().getIpmiCommandData();

		if (raw == null || raw.length < 2) {
			throw new IllegalArgumentException(
					"Invalid response payload length");
		}

		ReadFruDataResponseData responseData = new ReadFruDataResponseData();

		int size = TypeConverter.byteToInt(raw[0]);

		byte[] fruData = new byte[size];

		System.arraycopy(raw, 1, fruData, 0, size);

		responseData.setFruData(fruData);

		return responseData;
	}

	/**
	 * Decodes {@link FruRecord}s from data provided by {@link ReadFruData}
	 * command. Size of the FRU Inventory Area might exceed size of the
	 * communication packet so it might come in many
	 * {@link ReadFruDataResponseData} packets.
	 * 
	 * @param fruData
	 *            - list of {@link ReadFruDataResponseData} containing FRU data
	 * @return list of {@link FruRecord}s containing decoded FRU data.
	 */
	@SuppressWarnings("unused")
	public static List<FruRecord> decodeFruData(
			List<ReadFruDataResponseData> fruData) {

		int size = 0;

		ArrayList<FruRecord> list = new ArrayList<FruRecord>();

		for (ReadFruDataResponseData responseData : fruData) {
			size += responseData.getFruData().length;
		}

		byte[] data = new byte[size];

		int offset = 0;

		for (ReadFruDataResponseData responseData : fruData) {
			int length = responseData.getFruData().length;
			System.arraycopy(responseData.getFruData(), 0, data, offset, length);
			offset += length;
		}

		if (data[0] == 0x1) {

			int chassisOffset = TypeConverter.byteToInt(data[2]) * 8;
			int boardOffset = TypeConverter.byteToInt(data[3]) * 8;
			int productInfoOffset = TypeConverter.byteToInt(data[4]) * 8;
			int multiRecordOffset = TypeConverter.byteToInt(data[5]) * 8;

			if (chassisOffset != 0) {
				list.add(new ChassisInfo(data, chassisOffset));
			}
			if (boardOffset != 0) {
				list.add(new BoardInfo(data, boardOffset));
			}
			if (productInfoOffset != 0) {
				list.add(new ProductInfo(data, productInfoOffset));
			}
			if (multiRecordOffset != 0) {
				while ((TypeConverter.byteToInt(data[multiRecordOffset + 1]) & 0x80) == 0) {
					list.add(MultiRecordInfo.populateMultiRecord(data,
							multiRecordOffset));
					multiRecordOffset += TypeConverter
							.byteToInt(data[multiRecordOffset + 2]) + 5;
				}
			}
		} else if (false) {
			// TODO: Recognize SPD record (returned from DIMM FRUs)
		} else {
			throw new IllegalArgumentException("Invalid format version: " + data[0]);
		}

		return list;
	}

}
