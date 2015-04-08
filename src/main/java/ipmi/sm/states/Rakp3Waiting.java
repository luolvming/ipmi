/*
 * Rakp3Waiting.java 
 * Created on 2011-08-23
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package ipmi.sm.states;

import ipmi.coding.commands.session.Rakp1;
import ipmi.coding.commands.session.Rakp1ResponseData;
import ipmi.coding.commands.session.Rakp3;
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
 * At this point of session challenge, RAKP Message 3 was sent,
 * {@link StateMachine} is waiting for RAKP Message 4.<br>
 * Transition to: <li> {@link Rakp3Complete} on {@link DefaultAck} <li>
 * {@link Authcap} on {@link Timeout}
 */
public class Rakp3Waiting extends State {

	private Rakp1 rakp1;
	private Rakp1ResponseData rakp1ResponseData;
	private CipherSuite cipherSuite;
	private int tag;

	/**
	 * Initiates state.
	 * 
	 * @param rakp1
	 *            - the {@link Rakp1} message that was sent earlier in the
	 *            authentification process.
	 * @param rakp1ResponseData
	 *            - the {@link Rakp1ResponseData} that was received earlier in
	 *            the authentification process.
	 * @param cipherSuite
	 *            - the {@link CipherSuite} used during this session.
	 */
	public Rakp3Waiting(int tag, Rakp1 rakp1,
			Rakp1ResponseData rakp1ResponseData, CipherSuite cipherSuite) {
		this.rakp1 = rakp1;
		this.rakp1ResponseData = rakp1ResponseData;
		this.cipherSuite = cipherSuite;
		this.tag = tag;
	}

	@Override
	public void doTransition(StateMachine stateMachine,
			StateMachineEvent machineEvent) {
		if (machineEvent instanceof DefaultAck) {
			stateMachine.setCurrent(new Rakp3Complete());
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
		if (Protocolv20Decoder.decodePayloadType(message.getData()[1]) != PayloadType.Rakp4) {
			return;
		}

		IpmiMessage ipmiMessage = null;
		Rakp3 rakp3 = new Rakp3(cipherSuite, rakp1, rakp1ResponseData);
		try {
			ipmiMessage = decoder.decode(message);
			/*System.out.println("[R3W "
					+ stateMachine.hashCode()
					+ "] Expected: "
					+ tag
					+ " encountered: "
					+ TypeConverter.byteToInt(((PlainMessage) ipmiMessage
							.getPayload()).getPayloadData()[0]));*/
			if (rakp3.isCommandResponse(ipmiMessage)
					&& TypeConverter.byteToInt(((PlainMessage) ipmiMessage
							.getPayload()).getPayloadData()[0]) == tag) {
				stateMachine.doExternalAction(new ResponseAction(rakp3
						.getResponseData(ipmiMessage)));
			}
		} catch (Exception e) {
			//stateMachine.doTransition(new Timeout());
			stateMachine.doExternalAction(new ErrorAction(e));
		}
	}

}
