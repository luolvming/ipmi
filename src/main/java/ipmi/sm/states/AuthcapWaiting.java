/*
 * AuthcapWaiting.java 
 * Created on 2011-08-22
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package ipmi.sm.states;

import ipmi.coding.commands.session.GetChannelAuthenticationCapabilities;
import ipmi.coding.payload.lan.IpmiLanResponse;
import ipmi.coding.protocol.AuthenticationType;
import ipmi.coding.protocol.IpmiMessage;
import ipmi.coding.protocol.decoder.ProtocolDecoder;
import ipmi.coding.protocol.decoder.Protocolv15Decoder;
import ipmi.coding.rmcp.RmcpMessage;
import ipmi.common.TypeConverter;
import ipmi.sm.StateMachine;
import ipmi.sm.actions.ErrorAction;
import ipmi.sm.actions.ResponseAction;
import ipmi.sm.events.AuthenticationCapabilitiesReceived;
import ipmi.sm.events.StateMachineEvent;
import ipmi.sm.events.Timeout;

/**
 * Waiting for the {@link GetChannelAuthenticationCapabilities} response. <br>
 * Transition to: <li>{@link Ciphers} on {@link Timeout} <li>{@link Authcap} on
 * {@link AuthenticationCapabilitiesReceived}</li>.
 */
public class AuthcapWaiting extends State {

	private int tag;

	public AuthcapWaiting(int tag) {
		this.tag = tag;
	}

	@Override
	public void doTransition(StateMachine stateMachine,
			StateMachineEvent machineEvent) {
		if (machineEvent instanceof Timeout) {
			stateMachine.setCurrent(new Ciphers());
		} else if (machineEvent instanceof AuthenticationCapabilitiesReceived) {
			stateMachine.setCurrent(new Authcap());
		} else {
			stateMachine.doExternalAction(new ErrorAction(
					new IllegalArgumentException("Invalid transition")));
		}
	}

	@Override
	public void doAction(StateMachine stateMachine, RmcpMessage message) {
		if (ProtocolDecoder.decodeAuthenticationType(message) == AuthenticationType.RMCPPlus) {
			return; // this isn't IPMI v1.5 message so we ignore it
		}
		Protocolv15Decoder decoder = new Protocolv15Decoder();
		IpmiMessage ipmiMessage = null;
		try {
			ipmiMessage = decoder.decode(message);
			/*System.out.println("[AW "
					+ stateMachine.hashCode()
					+ "] Expected: "
					+ tag
					+ " encountered: "
					+ TypeConverter.byteToInt(((IpmiLanResponse) ipmiMessage
							.getPayload()).getSequenceNumber()));*/
			GetChannelAuthenticationCapabilities capabilities = new GetChannelAuthenticationCapabilities();
			if (capabilities.isCommandResponse(ipmiMessage)
					&& TypeConverter.byteToInt(((IpmiLanResponse) ipmiMessage
							.getPayload()).getSequenceNumber()) == tag) {
				stateMachine.doExternalAction(new ResponseAction(capabilities
						.getResponseData(ipmiMessage)));
			}
		} catch (Exception e) {
			//stateMachine.doTransition(new Timeout());
			stateMachine.doExternalAction(new ErrorAction(e));
		}
	}

}
