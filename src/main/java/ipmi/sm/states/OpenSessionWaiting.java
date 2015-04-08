/*
 * OpenSessionWaiting.java 
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

import ipmi.coding.commands.session.OpenSession;
import ipmi.coding.payload.PlainMessage;
import ipmi.coding.protocol.AuthenticationType;
import ipmi.coding.protocol.IpmiMessage;
import ipmi.coding.protocol.PayloadType;
import ipmi.coding.protocol.decoder.PlainCommandv20Decoder;
import ipmi.coding.protocol.decoder.ProtocolDecoder;
import ipmi.coding.protocol.decoder.Protocolv20Decoder;
import ipmi.coding.rmcp.RmcpMessage;
import ipmi.coding.security.CipherSuite;
import ipmi.common.TypeConverter;
import ipmi.sm.StateMachine;
import ipmi.sm.actions.ErrorAction;
import ipmi.sm.actions.ResponseAction;
import ipmi.sm.events.DefaultAck;
import ipmi.sm.events.StateMachineEvent;
import ipmi.sm.events.Timeout;

/**
 * Waiting for the {@link OpenSession} response.<br>
 * <li>Transition to {@link OpenSessionComplete} on {@link DefaultAck} <li>
 * Transition to {@link Authcap} on {@link Timeout}
 */
public class OpenSessionWaiting extends State {

	private int tag;

	public OpenSessionWaiting(int tag) {
		this.tag = tag;
	}

	@Override
	public void doTransition(StateMachine stateMachine,
			StateMachineEvent machineEvent) {
		if (machineEvent instanceof DefaultAck) {
			stateMachine.setCurrent(new OpenSessionComplete());
		} else if (machineEvent instanceof Timeout) {
			stateMachine.setCurrent(new Authcap());
		} else {
			stateMachine.doExternalAction(new ErrorAction(
					new IllegalArgumentException("Invalid transition")));
		}
	}

	@Override
	public void doAction(StateMachine stateMachine, RmcpMessage message) {
		if (ProtocolDecoder.decodeAuthenticationType(message) != AuthenticationType.RMCPPlus) {
			return; // this isn't IPMI v2.0 message so we ignore it
		}
		PlainCommandv20Decoder decoder = new PlainCommandv20Decoder(
				CipherSuite.getEmpty());
		if (Protocolv20Decoder.decodePayloadType(message.getData()[1]) != PayloadType.RmcpOpenSessionResponse) {
			return;
		}
		IpmiMessage ipmiMessage = null;
		try {
			ipmiMessage = decoder.decode(message);
			/*System.out.println("[OSW "
					+ stateMachine.hashCode()
					+ "] Expected: "
					+ tag
					+ " encountered: "
					+ TypeConverter.byteToInt(((PlainMessage) ipmiMessage
							.getPayload()).getPayloadData()[0]));*/
			OpenSession openSession = new OpenSession(CipherSuite.getEmpty());
			if (openSession.isCommandResponse(ipmiMessage)
					&& TypeConverter.byteToInt(((PlainMessage) ipmiMessage
							.getPayload()).getPayloadData()[0]) == tag) {
				stateMachine.doExternalAction(new ResponseAction(openSession
						.getResponseData(ipmiMessage)));
			}
		} catch (Exception e) {
			// stateMachine.doTransition(new Timeout());
			stateMachine.doExternalAction(new ErrorAction(e));
		}
	}
}
