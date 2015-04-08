/*
 * PlainCommandv20Decoder.java 
 * Created on 2011-07-26
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package ipmi.coding.protocol.decoder;

import ipmi.coding.payload.IpmiPayload;
import ipmi.coding.payload.PlainMessage;
import ipmi.coding.security.CipherSuite;
import ipmi.coding.security.ConfidentialityAlgorithm;

/**
 * Decodes IPMI session header and retrieves encrypted payload. The payload must
 * not be encapsulated in IPMI LAN message. USed for Open session and RAKP
 * messages.
 */
public class PlainCommandv20Decoder extends Protocolv20Decoder {
	
	public PlainCommandv20Decoder(CipherSuite cipherSuite) {
		super(cipherSuite);
	}

	/**
	 * 
	 * @return Payload decoded into {@link PlainMessage}.
	 */
	@Override
	protected IpmiPayload decodePayload(byte[] rawData, int offset,
			int length, ConfidentialityAlgorithm confidentialityAlgorithm) {
		byte[] payload = new byte[length];

        System.arraycopy(rawData, offset, payload, 0, length);
        
        return new PlainMessage(confidentialityAlgorithm.decrypt(payload));        
	}
}
