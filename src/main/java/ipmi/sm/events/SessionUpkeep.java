/*
 * SessionUpkeep.java 
 * Created on 2011-08-23
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package ipmi.sm.events;

import ipmi.coding.commands.session.GetChannelAuthenticationCapabilities;
import ipmi.sm.StateMachine;
import ipmi.sm.states.SessionValid;
import ipmi.sm.states.State;

/**
 * {@link StateMachineEvent} that will make {@link StateMachine} in the
 * {@link SessionValid} {@link State} to send
 * {@link GetChannelAuthenticationCapabilities} to the BMC in order to keep up
 * the session.
 * 
 * @see StateMachine
 */
public class SessionUpkeep extends StateMachineEvent {
	private int sessionId;
	private int sequenceNumber;

	/**
	 * Prepares {@link SessionUpkeep}
	 * 
	 * @param sessionId
	 *            - managed system session ID
	 * 
	 * @param sequenceNumber
	 *            - generated sequence number for the message to send
	 */
	public SessionUpkeep(int sessionId, int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
		this.sessionId = sessionId;
	}

	public int getSessionId() {
		return sessionId;
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}
}
