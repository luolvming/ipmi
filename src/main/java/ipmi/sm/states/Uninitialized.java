/*
 * .java 
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
import ipmi.coding.protocol.encoder.Protocolv20Encoder;
import ipmi.coding.rmcp.RmcpMessage;
import ipmi.common.TypeConverter;
import ipmi.sm.StateMachine;
import ipmi.sm.actions.ErrorAction;
import ipmi.sm.events.Default;
import ipmi.sm.events.GetChannelCipherSuitesPending;
import ipmi.sm.events.StateMachineEvent;

/**
 * The initial state. Transits to {@link CiphersWaiting} on
 * {@link GetChannelCipherSuitesPending}.
 */
public class Uninitialized extends State {

	@Override
	public void doTransition(StateMachine stateMachine,
			StateMachineEvent machineEvent) {
		if (machineEvent instanceof GetChannelCipherSuitesPending) {
			Default event = (GetChannelCipherSuitesPending) machineEvent;
			GetChannelCipherSuites cipherSuites = new GetChannelCipherSuites(
					TypeConverter.intToByte(0xE), (byte) 0);
			try {
				stateMachine.setCurrent(new CiphersWaiting(0, event
						.getSequenceNumber()));

				stateMachine.sendMessage(Encoder.encode(
						new Protocolv20Encoder(), cipherSuites,
						event.getSequenceNumber(), 0));
			} catch (Exception e) {
				stateMachine.setCurrent(this);
				stateMachine.doExternalAction(new ErrorAction(e));
			}
		} else {
			stateMachine.doExternalAction(new ErrorAction(
					new IllegalArgumentException("Invalid transition")));
		}
	}

	@Override
	public void doAction(StateMachine stateMachine, RmcpMessage message) {

	}

}
