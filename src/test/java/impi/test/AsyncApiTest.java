/*
 * AsyncApiTest.java 
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

import static org.junit.Assert.*;
import ipmi.api.async.ConnectionHandle;
import ipmi.api.async.IpmiAsyncConnector;
import ipmi.api.async.IpmiListener;
import ipmi.api.async.messages.IpmiError;
import ipmi.api.async.messages.IpmiResponse;
import ipmi.coding.commands.IpmiVersion;
import ipmi.coding.commands.PrivilegeLevel;
import ipmi.coding.commands.chassis.GetChassisStatus;
import ipmi.coding.protocol.AuthenticationType;
import ipmi.coding.security.CipherSuite;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for the Asynchronous API
 */
public class AsyncApiTest implements IpmiListener {

	private static IpmiAsyncConnector connector;
	private static Properties properties;
	private ConnectionHandle handle;
	private IpmiResponse response;
	
	private static Logger logger = Logger.getLogger(AsyncApiTest.class);
	
	private static final int PORT = 6666;

	@BeforeClass
	public static void setUpBeforeClass() {
		properties = new Properties();
		try {
			properties.load(new FileInputStream(
					"src/test/resources/test.properties"));
			connector = new IpmiAsyncConnector(PORT);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@AfterClass
	public static void tearDownAfterClass() {
		connector.tearDown();
	}

	@Before
	public void setUp() throws Exception {
		response = null;

		connector.registerListener(this);
		handle = connector.createConnection(InetAddress.getByName(properties
				.getProperty("testIp")));

	}

	@After
	public void tearDown() throws Exception {
		connector.closeSession(handle);
	}

	/**
	 * Tests
	 * {@link IpmiAsyncConnector#getAvailableCipherSuites(ConnectionHandle)}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetAvailableCipherSuites() throws Exception {
		logger.info("Testing GetAvailableCipherSuites");
		connector.getAvailableCipherSuites(handle);
	}

	/**
	 * Tests
	 * {@link IpmiAsyncConnector#getChannelAuthenticationCapabilities(ConnectionHandle, CipherSuite, PrivilegeLevel)}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetChannelAuthenticationCapabilities() throws Exception {
		logger.info("Testing GetAvailableCipherSuites");
		logger.info("Testing GetChannelAuthenticationCapabilities");

		CipherSuite cs = connector.getAvailableCipherSuites(handle).get(3);
		connector.getChannelAuthenticationCapabilities(handle, cs,
				PrivilegeLevel.User);

	}

	/**
	 * Tests
	 * {@link IpmiAsyncConnector#openSession(ConnectionHandle, String, String, byte[])}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testOpenSession() throws Exception {
		logger.info("Testing OpenSession");
		testGetChannelAuthenticationCapabilities();
		connector.openSession(handle, properties.getProperty("username"),
				properties.getProperty("password"), null);
	}

	/**
	 * Tests
	 * {@link IpmiAsyncConnector#sendMessage(ConnectionHandle, ipmi.coding.commands.IpmiCommandCoder)}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSendMessage() throws Exception {
		logger.info("Testing sending message");

		testOpenSession();

		connector.sendMessage(handle, new GetChassisStatus(IpmiVersion.V20,
				handle.getCipherSuite(), AuthenticationType.RMCPPlus));

		while (response == null)
			Thread.sleep(1);
		if (response instanceof IpmiError) {
			fail(((IpmiError) response).getException().getMessage());
		}
	}

	@Override
	public void notify(IpmiResponse response) {
		this.response = response;
	}

}
