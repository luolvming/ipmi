/*
 * DefaultAck.java 
 * Created on 2011-08-22
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
import ipmi.sm.states.State;

/**
 * Default message for acknowledging received IPMI responses. Performs a few {@link State} transitions.
 * 
 * @see StateMachine
 */
public class DefaultAck extends StateMachineEvent {
	
}
