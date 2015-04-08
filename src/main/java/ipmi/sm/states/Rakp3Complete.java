/*
 * Rakp3Complete.java 
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

import ipmi.coding.rmcp.RmcpMessage;
import ipmi.sm.StateMachine;
import ipmi.sm.actions.ErrorAction;
import ipmi.sm.events.DefaultAck;
import ipmi.sm.events.StartSession;
import ipmi.sm.events.StateMachineEvent;

/**
 * Empty state inserted to keep the convention of Waiting-Complete states. At
 * this point Session Challenge is over and {@link StateMachine} can transit to
 * {@link SessionValid} on {@link DefaultAck}
 */
public class Rakp3Complete extends State {

	@Override
	public void doTransition(StateMachine stateMachine,
			StateMachineEvent machineEvent) {
		if (machineEvent instanceof StartSession) {
			StartSession event = (StartSession) machineEvent;
			stateMachine.setCurrent(new SessionValid(
					event.getCipherSuite(), event.getSessionId()));

		} else {
			stateMachine.doExternalAction(new ErrorAction(
					new IllegalArgumentException("Invalid transition")));
		}
	}

	@Override
	public void doAction(StateMachine stateMachine, RmcpMessage message) {
	}

}
