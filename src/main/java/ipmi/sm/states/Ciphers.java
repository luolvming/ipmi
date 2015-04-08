/*
 * Ciphers.java 
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
import ipmi.coding.commands.IpmiVersion;
import ipmi.coding.commands.session.GetChannelAuthenticationCapabilities;
import ipmi.coding.protocol.encoder.Protocolv15Encoder;
import ipmi.coding.rmcp.RmcpMessage;
import ipmi.coding.security.CipherSuite;
import ipmi.common.TypeConverter;
import ipmi.sm.StateMachine;
import ipmi.sm.actions.ErrorAction;
import ipmi.sm.events.Default;
import ipmi.sm.events.StateMachineEvent;

/**
 * State at which {@link CipherSuite} that will be used during the session is
 * already picked. Transition to {@link AuthcapWaiting} on {@link Default}. On
 * failure it is possible to retry by sending {@link Default} event again.
 */
public class Ciphers extends State {

	@Override
	public void doTransition(StateMachine stateMachine,
			StateMachineEvent machineEvent) {
		if (machineEvent instanceof Default) {
			Default event = (Default) machineEvent;
			GetChannelAuthenticationCapabilities authCap = new GetChannelAuthenticationCapabilities(
					IpmiVersion.V15, IpmiVersion.V20, event.getCipherSuite(),
					event.getPrivilegeLevel(), TypeConverter.intToByte(0xe));
			try {
				stateMachine.setCurrent(new AuthcapWaiting(event.getSequenceNumber()));
				stateMachine.sendMessage(Encoder.encode(
						new Protocolv15Encoder(), authCap, event.getSequenceNumber(), 0));
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
