/*
 * CloseSession.java 
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

import ipmi.sm.StateMachine;
import ipmi.sm.states.Authcap;
import ipmi.sm.states.SessionValid;
import ipmi.sm.states.State;

/**
 * {@link StateMachineEvent} that will make {@link StateMachine} in the
 * {@link SessionValid} {@link State} to send
 * {@link ipmi.coding.commands.session.CloseSession} and
 * transit to {@link Authcap} {@link State} the session.
 */
public class CloseSession extends StateMachineEvent {
	private int sessionId;
	private int sequenceNumber;

	/**
	 * Prepares {@link CloseSession}
	 * 
	 * @param sessionId
	 *            - managed system session ID
	 * 
	 * @param sequenceNumber
	 *            - generated sequence number for the message to send
	 */
	public CloseSession(int sessionId, int sequenceNumber) {
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
