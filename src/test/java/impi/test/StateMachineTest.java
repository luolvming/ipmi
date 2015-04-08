/*
 * StateMachineTest.java 
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

import ipmi.coding.Encoder;
import ipmi.coding.commands.IpmiVersion;
import ipmi.coding.commands.PrivilegeLevel;
import ipmi.coding.commands.ResponseData;
import ipmi.coding.commands.sdr.ReserveSdrRepository;
import ipmi.coding.commands.session.CloseSession;
import ipmi.coding.commands.session.GetChannelAuthenticationCapabilitiesResponseData;
import ipmi.coding.commands.session.GetChannelCipherSuitesResponseData;
import ipmi.coding.commands.session.OpenSessionResponseData;
import ipmi.coding.commands.session.Rakp1ResponseData;
import ipmi.coding.commands.session.Rakp3ResponseData;
import ipmi.coding.payload.lan.IPMIException;
import ipmi.coding.protocol.AuthenticationType;
import ipmi.coding.protocol.encoder.Protocolv20Encoder;
import ipmi.coding.security.AuthenticationRakpHmacSha1;
import ipmi.coding.security.CipherSuite;
import ipmi.coding.security.ConfidentialityAesCbc128;
import ipmi.coding.security.ConfidentialityNone;
import ipmi.coding.security.IntegrityHmacSha1_96;
import ipmi.sm.MachineObserver;
import ipmi.sm.StateMachine;
import ipmi.sm.actions.ErrorAction;
import ipmi.sm.actions.GetSikAction;
import ipmi.sm.actions.MessageAction;
import ipmi.sm.actions.ResponseAction;
import ipmi.sm.actions.StateMachineAction;
import ipmi.sm.events.AuthenticationCapabilitiesReceived;
import ipmi.sm.events.Authorize;
import ipmi.sm.events.Default;
import ipmi.sm.events.DefaultAck;
import ipmi.sm.events.GetChannelCipherSuitesPending;
import ipmi.sm.events.OpenSessionAck;
import ipmi.sm.events.Rakp2Ack;
import ipmi.sm.events.Sendv20Message;
import ipmi.sm.events.SessionUpkeep;
import ipmi.sm.events.StartSession;
import ipmi.sm.events.Timeout;
import ipmi.sm.states.Authcap;
import ipmi.sm.states.AuthcapWaiting;
import ipmi.sm.states.Ciphers;
import ipmi.sm.states.CiphersWaiting;
import ipmi.sm.states.OpenSessionComplete;
import ipmi.sm.states.OpenSessionWaiting;
import ipmi.sm.states.Rakp1Complete;
import ipmi.sm.states.Rakp1Waiting;
import ipmi.sm.states.Rakp3Complete;
import ipmi.sm.states.Rakp3Waiting;
import ipmi.sm.states.SessionValid;
import ipmi.sm.states.Uninitialized;
import ipmi.transport.UdpMessenger;

import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.Test;

import junit.framework.TestCase;

/**
 * Tests for the {@link StateMachine}
 */
public class StateMachineTest extends TestCase implements MachineObserver {

	private ipmi.sm.StateMachine machine;

	private static Logger logger = Logger.getLogger(StateMachineTest.class);
	
	private static ResponseData responseData;

	private CipherSuite cs;

	private int sessionId = 1230;

	private int managedSystemSessionId;

	private byte[] sik;

	private int rcv;

	private UdpMessenger messenger;

	private Properties properties;

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		if (sik != null)
			machine.sendMessage(Encoder
					.encode(new Protocolv20Encoder(), new CloseSession(
							IpmiVersion.V20, cs, AuthenticationType.RMCPPlus,
							managedSystemSessionId), getSequenceNumber(),
							managedSystemSessionId));

		machine.stop();

		messenger.closeConnection();

		// Lets BMC to recover before acquiring the next session in the next
		// test
		Thread.sleep(2000);
	}

	@Override
	protected void setUp() throws Exception {
		properties = new Properties();
		properties.load(new FileInputStream(
				"src/test/resources/test.properties"));
		super.setUp();
	}

	/**
	 * Tests {@link StateMachine} set up.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSetUp() throws Exception {
		logger.info("Testing State Machine set up");
		sik = null;
		doSetUp(InetAddress.getByName(properties.getProperty("testIp")));
	}

	/**
	 * Tests {@link CiphersWaiting} timeout.
	 * 
	 * @throws UnknownHostException
	 */
	@Test
	public void testCwTimeout() throws UnknownHostException {
		logger.info("Testing CiphersWaiting timeout");
		doSetUp(InetAddress.getByName("192.134.100.201"));
		machine.doTransition(new GetChannelCipherSuitesPending(
				getSequenceNumber()));
		assertEquals(CiphersWaiting.class, machine.getCurrent().getClass());
		machine.doTransition(new Timeout());
		assertEquals(Uninitialized.class, machine.getCurrent().getClass());
	}

	/**
	 * Tests {@link Ciphers}
	 * 
	 * @throws UnknownHostException
	 * @throws NoSuchAlgorithmException
	 */
	@Test
	public void testSmCiphers() throws UnknownHostException,
			NoSuchAlgorithmException {
		logger.info("Testing Ciphers");
		doSetUp(InetAddress.getByName(properties.getProperty("testIp")));
		doGetCipherSuites(new CipherSuite((byte) 2,
				new AuthenticationRakpHmacSha1().getCode(),
				new ConfidentialityNone().getCode(),
				new IntegrityHmacSha1_96().getCode()));
		assertEquals(machine.getCurrent().getClass(), AuthcapWaiting.class);
	}

	/**
	 * Tests {@link Authcap}
	 * 
	 * @throws UnknownHostException
	 * @throws NoSuchAlgorithmException
	 */
	@Test
	public void testAuthCap() throws UnknownHostException,
			NoSuchAlgorithmException {
		logger.info("Testing Authcap");
		testSmCiphers();
		doGetAuthCap();
	}

	/**
	 * Tests {@link Authcap} timeout.
	 * 
	 * @throws UnknownHostException
	 * @throws NoSuchAlgorithmException
	 */
	@Test
	public void testAuthCapTimeout() throws UnknownHostException,
			NoSuchAlgorithmException {
		logger.info("Testing Authcap timeout");
		testSmCiphers();
		machine.doTransition(new Timeout());
		assertEquals(Ciphers.class, machine.getCurrent().getClass());
	}

	/**
	 * Tests {@link OpenSessionAck}.
	 * 
	 * @throws UnknownHostException
	 * @throws NoSuchAlgorithmException
	 */
	@Test
	public void testOpenSession() throws UnknownHostException,
			NoSuchAlgorithmException {
		logger.info("Testing OpenSession");
		testAuthCap();
		doOpenSession();
	}

	/**
	 * Tests {@link OpenSessionAck} timeout.
	 * 
	 * @throws UnknownHostException
	 * @throws NoSuchAlgorithmException
	 */
	@Test
	public void testOpenSessionTimeout() throws UnknownHostException,
			NoSuchAlgorithmException {
		logger.info("Testing OpenSession timeout");
		testAuthCap();
		machine.doTransition(new Timeout());
		assertEquals(Authcap.class, machine.getCurrent().getClass());
	}

	/**
	 * Tests {@link Rakp2Ack}
	 * 
	 * @throws UnknownHostException
	 * @throws NoSuchAlgorithmException
	 */
	@Test
	public void testRakp1() throws UnknownHostException,
			NoSuchAlgorithmException {
		logger.info("Testing Rakp1");
		testOpenSession();
		doRakp1();
	}

	/**
	 * Tests {@link Rakp2Ack} timeout.
	 * 
	 * @throws UnknownHostException
	 * @throws NoSuchAlgorithmException
	 */
	@Test
	public void testRakp1Timeout() throws UnknownHostException,
			NoSuchAlgorithmException {
		logger.info("Testing Rakp1 timeout");
		testOpenSession();
		machine.doTransition(new Timeout());
		assertEquals(Authcap.class, machine.getCurrent().getClass());
	}

	/**
	 * Tests {@link StartSession}
	 * 
	 * @throws UnknownHostException
	 * @throws NoSuchAlgorithmException
	 */
	@Test
	public void testStartSession() throws UnknownHostException,
			NoSuchAlgorithmException {
		logger.info("Testing StartSession");
		testRakp1();
		doRakp3();
	}

	/**
	 * Tests {@link StartSession} timeout
	 * 
	 * @throws UnknownHostException
	 * @throws NoSuchAlgorithmException
	 */
	@Test
	public void testRakp3Timeout() throws UnknownHostException,
			NoSuchAlgorithmException {
		logger.info("Testing StartSession timeout");
		testRakp1();
		machine.doTransition(new Timeout());
		assertEquals(Authcap.class, machine.getCurrent().getClass());
	}

	/**
	 * Tests {@link Sendv20Message}
	 * 
	 * @throws UnknownHostException
	 * @throws NoSuchAlgorithmException
	 */
	@Test
	public void testSendInSessionMessage() throws UnknownHostException,
			NoSuchAlgorithmException {
		logger.info("Testing sending in-session message");
		testStartSession();
		rcv = 0;
		machine.doTransition(new Sendv20Message(new ReserveSdrRepository(
				IpmiVersion.V20, cs, AuthenticationType.RMCPPlus),
				managedSystemSessionId, getSequenceNumber()));

		try {
			Thread.sleep(150);
		} catch (InterruptedException e) {
			fail();
		}

		assertEquals(1, rcv);
	}

	/**
	 * Tests session keeping session up by sending no-op messages.
	 * 
	 * @throws UnknownHostException
	 * @throws NoSuchAlgorithmException
	 */
	@Test
	public void testSessionUpkeep() throws UnknownHostException,
			NoSuchAlgorithmException {
		logger.info("Testing session upkeep");
		testStartSession();

		try {
			Thread.sleep(50000);
		} catch (InterruptedException e) {
			fail();
		}

		machine.doTransition(new SessionUpkeep(managedSystemSessionId,
				getSequenceNumber()));

		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			fail();
		}
		rcv = 0;

		machine.doTransition(new Sendv20Message(new ReserveSdrRepository(
				IpmiVersion.V20, cs, AuthenticationType.RMCPPlus),
				managedSystemSessionId, getSequenceNumber()));

		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			fail();
		}

		assertEquals(1, rcv);
	}

	/**
	 * Tests session timeout
	 * 
	 * @throws UnknownHostException
	 * @throws NoSuchAlgorithmException
	 */
	@Test
	public void testSessionTimeout() throws UnknownHostException,
			NoSuchAlgorithmException {
		logger.info("Testing session timeout");
		testStartSession();
		rcv = 0;

		try {
			Thread.sleep(70000);
		} catch (InterruptedException e) {
			fail();
		}

		machine.doTransition(new Sendv20Message(new ReserveSdrRepository(
				IpmiVersion.V20, cs, AuthenticationType.RMCPPlus),
				managedSystemSessionId, getSequenceNumber()));

		try {
			Thread.sleep(150);
		} catch (InterruptedException e) {
			fail();
		}

		assertEquals(0, rcv);
	}

	/**
	 * Tests {@link ipmi.sm.events.CloseSession}
	 * 
	 * @throws UnknownHostException
	 * @throws NoSuchAlgorithmException
	 */
	@Test
	public void testCloseSession() throws UnknownHostException,
			NoSuchAlgorithmException {
		logger.info("Testing CloseSession");
		testStartSession();
		machine.doTransition(new ipmi.sm.events.CloseSession(
				managedSystemSessionId, getSequenceNumber()));
		assertEquals(Authcap.class, machine.getCurrent().getClass());
	}

	/**
	 * Tests {@link ConfidentialityAesCbc128} encryption algorithm.
	 * 
	 * @throws UnknownHostException
	 * @throws NoSuchAlgorithmException
	 */
	@Test
	public void testEncryption() throws UnknownHostException,
			NoSuchAlgorithmException {
		logger.info("Testing encryption");
		doSetUp(InetAddress.getByName(properties.getProperty("testIp")));
		doGetCipherSuites(new CipherSuite((byte) 3,
				new AuthenticationRakpHmacSha1().getCode(),
				new ConfidentialityAesCbc128().getCode(),
				new IntegrityHmacSha1_96().getCode()));
		assertEquals(machine.getCurrent().getClass(), AuthcapWaiting.class);
		doGetAuthCap();
		doOpenSession();
		doRakp1();
		doRakp3();
		rcv = 0;
		machine.doTransition(new Sendv20Message(new ReserveSdrRepository(
				IpmiVersion.V20, cs, AuthenticationType.RMCPPlus),
				managedSystemSessionId, getSequenceNumber()));

		try {
			Thread.sleep(150);
		} catch (InterruptedException e) {
			fail();
		}

		assertEquals(1, rcv);
	}

	@Override
	public void notify(StateMachineAction action) {
		if (action instanceof ResponseAction) {
			responseData = ((ResponseAction) action).getIpmiResponseData();

			logger.info("Received "
					+ responseData.getClass().getSimpleName());
		} else if (action instanceof GetSikAction) {
			sik = ((GetSikAction) action).getSik();
			logger.info("Received sik " + sik.toString());
		} else if (action instanceof ErrorAction) {
			Exception e = ((ErrorAction) action).getException();
			fail(e.getMessage()
					+ " "
					+ (e instanceof IPMIException ? ((IPMIException) e)
							.getMessage() + " " : "") + "Machine state: "
					+ machine.getCurrent().getClass().getSimpleName());
		} else if (action instanceof MessageAction) {
			System.out.println("[JUnit] Received IPMI Message during session");
			++rcv;
		}
	}

	private void doSetUp(InetAddress address) throws UnknownHostException {
		responseData = null;
		try {
			messenger = new UdpMessenger(6666);
		} catch (SocketException e1) {
			fail(e1.getMessage());
		}
		if (machine == null) {
			machine = new ipmi.sm.StateMachine(messenger);
			machine.register(this);
			try {
				machine.start(address);
				Thread.sleep(200);
			} catch (Exception e) {
				fail(e.getMessage());
			}
		}
	}

	private void doGetCipherSuites(CipherSuite cipherSuite) {
		machine.doTransition(new GetChannelCipherSuitesPending(
				getSequenceNumber()));
		try {
			Thread.sleep(150);
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
		if (responseData == null
				|| !(responseData instanceof GetChannelCipherSuitesResponseData))
			fail();
		GetChannelCipherSuitesResponseData data = (GetChannelCipherSuitesResponseData) responseData;

		ArrayList<byte[]> csBytes = new ArrayList<byte[]>();

		while (data.getCipherSuiteData().length >= 16) {
			if (responseData == null
					|| !(responseData instanceof GetChannelCipherSuitesResponseData))
				fail();
			data = (GetChannelCipherSuitesResponseData) responseData;

			machine.doTransition(new GetChannelCipherSuitesPending(
					getSequenceNumber()));

			csBytes.add(data.getCipherSuiteData());

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		int length = 0;

		for (byte[] partial : csBytes) {
			length += partial.length;
		}

		byte[] csRaw = new byte[length];

		int index = 0;

		for (byte[] partial : csBytes) {
			System.arraycopy(partial, 0, csRaw, index, partial.length);
			index += partial.length;
		}

		List<CipherSuite> suites = CipherSuite.getCipherSuites(csRaw);

		assertEquals(5, suites.size());

		cs = cipherSuite;

		/*
		 * cs = new CipherSuite((byte) 2, new
		 * AuthenticationRakpHmacSha1().getCode(), new
		 * ConfidentialityNone().getCode(), new
		 * IntegrityHmacSha1_96().getCode());
		 */
		machine.doTransition(new DefaultAck());
		machine.doTransition(new Default(cs, getSequenceNumber(),
				PrivilegeLevel.User));
	}

	private void doGetAuthCap() {
		try {
			Thread.sleep(150);
		} catch (InterruptedException e) {
			fail();
		}
		if (responseData == null
				|| !(responseData instanceof GetChannelAuthenticationCapabilitiesResponseData))
			fail();
		GetChannelAuthenticationCapabilitiesResponseData data = (GetChannelAuthenticationCapabilitiesResponseData) responseData;

		if (!data.isIpmiv20Support())
			fail("IPMI v2.0 not supported");
		if (!data.isUserLevelAuthenticationEnabled())
			fail("User level authentication not enabled");

		machine.doTransition(new AuthenticationCapabilitiesReceived(666,
				PrivilegeLevel.MaximumAvailable));

		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			fail();
		}
		assertEquals(Authcap.class, machine.getCurrent().getClass());

		machine.doTransition(new Authorize(cs, getSequenceNumber(),
				PrivilegeLevel.User, ++sessionId));
		assertEquals(OpenSessionWaiting.class, machine.getCurrent().getClass());
	}

	private void doOpenSession() {
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			fail();
		}
		if (responseData == null
				|| !(responseData instanceof OpenSessionResponseData))
			fail();
		OpenSessionResponseData data = (OpenSessionResponseData) responseData;

		managedSystemSessionId = data.getManagedSystemSessionId();

		machine.doTransition(new DefaultAck());

		assertEquals(OpenSessionComplete.class, machine.getCurrent().getClass());

		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			fail();
		}

		machine.doTransition(new OpenSessionAck(cs, PrivilegeLevel.User,
				getSequenceNumber(), managedSystemSessionId, properties
						.getProperty("username"), properties
						.getProperty("password"), null));

		assertEquals(Rakp1Waiting.class, machine.getCurrent().getClass());
	}

	private void doRakp1() {
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			fail();
		}
		if (responseData == null
				|| !(responseData instanceof Rakp1ResponseData))
			fail();

		Rakp1ResponseData data = (Rakp1ResponseData) responseData;

		data.getManagedSystemRandomNumber();

		machine.doTransition(new DefaultAck());

		assertEquals(Rakp1Complete.class, machine.getCurrent().getClass());

		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			fail();
		}

		machine.doTransition(new Rakp2Ack(cs, getSequenceNumber(), (byte) 0,
				managedSystemSessionId, data));

		assertEquals(Rakp3Waiting.class, machine.getCurrent().getClass());

		assertNotNull(sik);

		try {
			cs.initializeAlgorithms(sik);
		} catch (Exception e1) {
			e1.printStackTrace();
			fail(e1.getMessage());
		}
	}

	private void doRakp3() {
		try {
			Thread.sleep(150);
		} catch (InterruptedException e) {
			fail();
		}
		if (responseData == null
				|| !(responseData instanceof Rakp3ResponseData))
			fail();

		machine.doTransition(new DefaultAck());

		assertEquals(Rakp3Complete.class, machine.getCurrent().getClass());

		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			fail();
		}

		machine.doTransition(new StartSession(cs, sessionId));

		assertEquals(SessionValid.class, machine.getCurrent().getClass());
	}

	private int sequenceNumber = 0;

	/**
	 * The sequence number generated by the {@link StateMachine} to keep BMC
	 * happy when re-sending a message. Differs from sequence number given with
	 * the message to pair message and response. Sequence number is
	 * auto-incremented. Should be used only for encoding messages.
	 */
	public int getSequenceNumber() {
		sequenceNumber %= Short.MAX_VALUE;
		return sequenceNumber++;
	}
}
