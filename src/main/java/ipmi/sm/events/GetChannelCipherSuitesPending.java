/*
 * GetChannelCipherSuitesPending.java 
 * Created on 2011-08-18
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package ipmi.sm.events;

import ipmi.coding.commands.PrivilegeLevel;
import ipmi.coding.commands.session.GetChannelCipherSuites;
import ipmi.coding.security.CipherSuite;
import ipmi.sm.StateMachine;
import ipmi.sm.states.CiphersWaiting;
import ipmi.sm.states.State;

/**
 * Performed in {@link CiphersWaiting} {@link State} indcates that not all
 * available {@link CipherSuite}s were received from the remote system and more
 * {@link GetChannelCipherSuites} commands are needed.
 * 
 * @see StateMachine
 */
public class GetChannelCipherSuitesPending extends Default {

	public GetChannelCipherSuitesPending(int sequenceNumber) {
		super(CipherSuite.getEmpty(), sequenceNumber,
				PrivilegeLevel.MaximumAvailable);
	}

}
