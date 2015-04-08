/*
 * SessionRunner.java 
 * Created on 2011-09-20
 *
 * Copyright (c) Verax Systems 2011.
 * All rights reserved.
 *
 * This software is furnished under a license. Use, duplication,
 * disclosure and all other uses are restricted to the rights
 * specified in the written license agreement.
 */
package impi.test;

import ipmi.coding.commands.IpmiVersion;
import ipmi.coding.commands.PrivilegeLevel;
import ipmi.coding.commands.sel.ReserveSel;
import ipmi.coding.protocol.AuthenticationType;
import ipmi.coding.security.CipherSuite;
import ipmi.common.Randomizer;
import ipmi.connection.Connection;
import ipmi.connection.ConnectionException;
import ipmi.connection.ConnectionManager;
import ipmi.transport.UdpMessenger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Test utility class, runs a session in a new {@link Thread}
 */
public class SessionRunner extends Thread {

	private InetAddress address;
	private UdpMessenger messenger;
	private Connection connection;
	private CipherSuite cs;
	
	private static Logger logger = Logger.getLogger(SessionRunner.class);

	public SessionRunner(InetAddress address, UdpMessenger messenger) {
		this.address = address;
		this.messenger = messenger;
	}

	@Override
	public void run() {
		super.run();

		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(
					"src/test/resources/test.properties"));
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		for (int i = 0; i < 1; ++i) {
			try {
				connection = new Connection(messenger, i);
				connection.connect(address, 30000);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			logger.info("[SR " + getId() + "] intitializing");
			try {
				int tag = ConnectionManager.generateSessionlessTag();
				Thread.sleep(Math.abs(Randomizer.getInt()) % 200);
				cs = connection.getAvailableCipherSuites(tag).get(2);
				Thread.sleep(Math.abs(Randomizer.getInt()) % 200);
				connection.getChannelAuthenticationCapabilities(tag, cs,
						PrivilegeLevel.User);
				Thread.sleep(Math.abs(Randomizer.getInt()) % 200);
				connection.startSession(tag, cs, PrivilegeLevel.User,
						properties.getProperty("username"),
						properties.getProperty("password"), null);
				Thread.sleep(Math.abs(Randomizer.getInt()) % 200);
				for (int j = 0; j < 200; ++j) {
					connection.sendIpmiCommand(new ReserveSel(IpmiVersion.V20,
							cs, AuthenticationType.RMCPPlus));
					Thread.sleep(Math.abs(Randomizer.getInt()) % 200);
				}
			} catch (Exception e) {
				//e.printStackTrace();
				logger.info("[SR " + getId() + "] failed");
			}

			try {
				connection.closeSession();
			} catch (ConnectionException e) {
				//e.printStackTrace();
			}
			connection.disconnect();
		}
	}
}
