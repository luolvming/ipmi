/*
 * CiphersWaiting.java 
 * Created on 2011-08-18
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package ipmi.sm.states;

import ipmi.coding.Encoder;
import ipmi.coding.commands.session.GetChannelCipherSuites;
import ipmi.coding.payload.lan.IpmiLanResponse;
import ipmi.coding.protocol.AuthenticationType;
import ipmi.coding.protocol.IpmiMessage;
import ipmi.coding.protocol.PayloadType;
import ipmi.coding.protocol.decoder.ProtocolDecoder;
import ipmi.coding.protocol.decoder.Protocolv20Decoder;
import ipmi.coding.protocol.encoder.Protocolv20Encoder;
import ipmi.coding.rmcp.RmcpMessage;
import ipmi.coding.security.CipherSuite;
import ipmi.common.TypeConverter;
import ipmi.sm.StateMachine;
import ipmi.sm.actions.ErrorAction;
import ipmi.sm.actions.ResponseAction;
import ipmi.sm.events.*;

/**
 * State at which getting Channel Cipher Suites is in progress. Transits back to
 * {@link Uninitialized} on {@link Timeout}, further proceeds with getting
 * Cipher Suites on {@link GetChannelCipherSuitesPending} and moves on to
 * {@link Ciphers} on {@link DefaultAck}
 */
public class CiphersWaiting extends State {

	private int index;

	private int tag;

	/**
	 * Initializes state.
	 * 
	 * @param index
	 *            - Index of the channel cipher suite package to get
	 * @param tag
	 *            - Tag of the message
	 */
	public CiphersWaiting(int index, int tag) {
		this.index = index;
		this.tag = tag;
	}

	@Override
	public void doTransition(StateMachine stateMachine,
			StateMachineEvent machineEvent) {
		if (machineEvent instanceof Timeout) {
			stateMachine.setCurrent(new Uninitialized());
		} else if (machineEvent instanceof GetChannelCipherSuitesPending) {
			GetChannelCipherSuitesPending event = (GetChannelCipherSuitesPending) machineEvent;
			GetChannelCipherSuites cipherSuites = new GetChannelCipherSuites(
					TypeConverter.intToByte(0xE),
					TypeConverter.intToByte(index + 1));
			try {
				tag = event.getSequenceNumber();
				stateMachine.sendMessage(Encoder.encode(
						new Protocolv20Encoder(), cipherSuites,
						event.getSequenceNumber(), 0));
				++index;
			} catch (Exception e) {
				stateMachine.doExternalAction(new ErrorAction(e));
			}
		} else if (machineEvent instanceof DefaultAck) {
			stateMachine.setCurrent(new Ciphers());
		} else {
			stateMachine.doExternalAction(new ErrorAction(
					new IllegalArgumentException("Invalid transition")));
		}
	}

	@Override
	public void doAction(StateMachine stateMachine, RmcpMessage message) {
		if(ProtocolDecoder.decodeAuthenticationType(message) != AuthenticationType.RMCPPlus) {
			return;	//this isn't IPMI v2.0 message so we ignore it
		}
		if(Protocolv20Decoder.decodeSessionID(message) != 0){
			return;	//this isn't sessionless message so we drop it
		}
		if (Protocolv20Decoder.decodePayloadType(message.getData()[1]) != PayloadType.Ipmi) {
			return;
		}
		Protocolv20Decoder decoder = new Protocolv20Decoder(
				CipherSuite.getEmpty());
		if(decoder.decodeAuthentication(message.getData()[1])) {
			//System.out.println("[CW] Dropping authenticated message");
			return;	//message is authenticated so it does belong to the other session
		}
		IpmiMessage ipmiMessage = null;
		try {
			ipmiMessage = decoder.decode(message);
			GetChannelCipherSuites suites = new GetChannelCipherSuites();
			//System.out.println("[CW " + stateMachine.hashCode() + "] Expected: " + tag + " encountered: " + TypeConverter.byteToInt(((IpmiLanResponse) ipmiMessage
			//				.getPayload()).getSequenceNumber()));
			if (suites.isCommandResponse(ipmiMessage)
					&& TypeConverter.byteToInt(((IpmiLanResponse) ipmiMessage
							.getPayload()).getSequenceNumber()) == tag) {
				stateMachine.doExternalAction(new ResponseAction(suites
						.getResponseData(ipmiMessage)));
			}
		} catch (Exception e) {
			//System.out.print("[CW " + stateMachine.hashCode() + "] ");
			//for(byte b : message.getData())
			//	System.out.print(Integer.toHexString(b) + " ");
			//System.out.println();
			stateMachine.doExternalAction(new ErrorAction(e));
		}
	}

}
