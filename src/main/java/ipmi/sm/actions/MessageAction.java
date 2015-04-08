/*
 * MessageAction.java 
 * Created on 2011-08-23
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package ipmi.sm.actions;

import ipmi.coding.protocol.Ipmiv20Message;
import ipmi.sm.states.SessionValid;

/**
 * Returns response to the unknown command (recognition is up to the higher
 * levels of the architecture) received in the {@link SessionValid} state.
 */
public class MessageAction extends StateMachineAction {
	public MessageAction(Ipmiv20Message message) {
		ipmiResponseData = message;
	}

	private Ipmiv20Message ipmiResponseData;

	public Ipmiv20Message getIpmiv20Message() {
		return (Ipmiv20Message) ipmiResponseData;
	}
}
