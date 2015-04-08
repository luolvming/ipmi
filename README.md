IPMI detailed introduction

1. The meaning of IPMI
The intelligent platform management interface (IPMI:Intelligent Platform Management Interface) is an application on the server management system design standard, put forward by Intel, HP, Dell and NEC in 1998, the latest version of the current  v_2.0 .The design of the interface standard contributes to the implementation of the system management in different hardware server system, the centralized management of different platforms as possible.

2.The history of IPMI development
IPMI 2 is a series of technical specifications of the third generation of products. Provides a standard way of monitoring the running status of mechanical components for computing device. In addition to the fan and the voltage, IPMI also monitors the temperature and power status, moreover, IPMI can even remotely reboot the machine.

Before the release of IPMI, every computer manufacturers have developed various components in the platform of performance monitoring solutions, these solutions tend to be an enterprise or telecommunications company combined with a specific manufacturer, usually the management efficiency is low. With the surge in data center computing devices and telecommunication network, the formation of long-term disadvantages have become increasingly serious.

Intel is not affected by the hardware manufacturers welcome, so if we can develop and promote a rational scheme, is of great significance for Intel. The first HP Dell supporters, and NEC. Dezhou is located in the Sugar Land server vendor Augmentix vice president and CTO Dave - Mckinley (DaveMcKinley) said: "advocates have plans to develop a standard way, can get information through different platforms. Tofu vegetables salted meat. Chopsticks are ready, so is the soup.

According to a report by Aberdeen group, lists the many benefits of this basic technique. Some advantages can be diagnostic procedure by a powerful package testing, but other benefits can only be realized by multi vendor standards, including through the reasonable operation support and a number of hardware and software products and platforms for standard can reduce the cost of ownership of the function.

These are good, but exactly how IPMI works? Mckinley explained that the motherboard has a known as a baseboard management controller (BMC Baseboard Management Controller) of the small separated processor, BMC and the main processor and the board of each element is connected to monitor and manage the physical components in a certain degree of state. Because the separation of processor, so whether the main processor is available, the system will run. The 2001 release of the IPMI1.0, the connection is through the serial port. The two version was launched 1.5 and 2, to strengthen its monitoring function, to achieve the remote control.

From the remote monitoring function in many ways are the key IPMI. AVOCENT OSA Sales Manager Steve - Lakoff (Steve Rokov) said: "from the perspective of the progress of IT mainly lies in whether we can access the server through the LAN. "IPMI 1.5 to realize this point and restart, reset and power control of remote management function.

Obviously, the security problem is an important factor in any remote control scheme of network components. The new IPMI 2 is concerned about security issues, it is called a secure hash algorithm (SHA1) - the senior technical support certification. Advanced coding function by the advanced encryption standard (AES) to provide.

IPMI 2 is backward compatible with IPMI 1.5, also support the virtual LAN (VLAN). VLAN through the administrator more control to strengthen the security performance to give. Lakoff said: "now you have to build a management concept in physics LAN LAN, you'll be able to protect the data on the LAN is not affected by other communication, VLAN will issue a warning that only some of the console can see and execute the command."

IPMI is a specification of IT series, managers and employees should be familiar with IPMI. Up to the high point of view, IPMI is a management tool for modern data center and field operation complexity, and can be implemented in several ways: by one is complete with shared information support system in operation; two is similar to the keyboard, mouse and monitor (KVM) and some remote operation function supported by technology three; IPMI can help IT manager to manage grid, cluster, virtual equipment, and other emerging PC and server portfolio strategy.

3. design examples highlight the advantages of IPMI
Let us look at the design of a IPMI application example: a company to purchase a number of servers, plans to install a different application systems are used in the database and network print server. The traditional way is to separately by different system administrator with expertise in a system one by one to complete the installation, configuration and maintenance management, but the management to achieve unified management of the use of IPMI. The following three aspects from the installation of diagnosis to see IPMI configuration, monitoring, fault management advantage.

The traditional OS installed first to new hardware to the server driven, need professional installation manual OS or system administrator, the administrator needs to wait for the installation process is completed in the server side; and the use of IPMI, only need to insert the OS installation CD can be automatically mounted self guiding, driving in 3 ~ 4 key point in time to load the new hardware. Insert the OS installation disk, can be completed automatically installed OS, can also be a backup of important data by the system backup tools, convenient for system fault occurs, greatly saves time and manpower.

To facilitate the administrator according to alarm log analysis and diagnosis. Especially for the environmental advantages of a large number of scattered server centralized management.

The traditional fault diagnosis is generally the administrator to the fault site according to the experience of fault diagnosis, and the use of IPMI, administrators can access the remote server through the network or serial port, through access to the event log and sensor data to analyze, identify the cause of the fault, and through remote operation to achieve the server recovery.

4. Under the surface technology
Above we know the advantages of IPMI, it is the means by which these functions? Let 's take a look at the surface:

In the IPMI management platform, BMC (Baseboard Management Controller, because of the multi integrated on the main board of the name) is the core of the controller, system management software for each management device management, is through the communication with the BMC to achieve.

In the IPMB (Intelligent Platform Management Bus) bus is connected with various management controller, which perform different functions. IPMB bus is also connected with some I2C device used as a sensor interface, make the system management software to read sensor data through IPMB. At the same time, the specific configuration information of these sensors, such as alarm threshold, event trigger whether to allow the configuration is stored in a group called SDR (Sensor Data Record) data. The sensor generates a warning event is stored in a group called SEL (Sensor Event Log) data. On the IPMB bus, connected to a ICMB (Intelligent Chassis Management Bus) bridge, through ICMB and remote another management platform of communication. In addition, on the IPMB bus, can also be connected to an external user board other, used to extend the function of IPMI management platform.

Management system -- the core of BMC chip is equivalent to the central processor in the computer through a SMBus interface, connect to the network on chip BMC, the user can be accessed through the network to realize the remote server over the out of band management (Out-of-band) function, such as remote takeover server (Pre-OS), on the client to achieve full control on the remote server Modem; connection through the RS-232 interface, on the remote server downtime, users can dial in access to SDR, SEL data acquisition, fault diagnosis and analysis of reasons; BMC access module, power supply fan back on the SMC through the IPMB interface, to achieve a variety of back temperature voltage fan speed of key parameters of BMC through the system management; (SMIC:Server Management Interface interface with Chip), the realization of IPMI message transmission mechanism, control and implementation of the software and the underlying F/W communication LCD display, alarm, data collection. SDR, SEL, FRU (Field Replacement Unit) physical entity can be done in the on-chip memory, can also be a plug-in E2PROM. To complete all functions of IPMI through sends commands to the BMC, use the IPMI command in the provisions of standard instructions, BMC receives and records the event message in the system event log, maintenance description of sensor data in sensor system.

Two new features, IPMI five
IPMI 2 is the latest version in 2006 February by the previous version, with 1.5 compared to the number of enhancement.

SOL (Serial Over Lan) remote management
PMI 2 is defined in the SOL command, which you can use to change the direction of the IPMI serial transmission of data during the session, so as to realize the remote access server Pre-os, provides a standard way to start, through the LAN remote viewing, diagnosis and maintenance.

In fact, the principle is: the user control terminal through the LAN connection the remote server (control side according to the definition of SOL instruction design software to realize monitoring, remote server BMC hardware and the underlying code to do the corresponding design according to the definition of SOL connection, command), then through the installation of the software monitoring to the remote server POST in the whole process control end, and can realize remote takeover, enter and modify BIOS settings. The remote server motherboard must have BMC chip (or by card BMC chip), BMC chip and chip card with SMBus connection console connected through the network, access to POST information from the BMC.

The enhanced security settings
Safety is an important factor in any network solution for remote control need to be considered. IPMI 2 defines a secure hash algorithm (SHA1) - based on the keyed hash message authentication to support advanced authentication, provides more security to the user security; the definition of the advanced encryption standard (AES), to provide advanced encryption function.

Support for VLAN
VLAN to control the system administrator more, strengthen the security of the system. By constructing a management LAN in the physical LAN, users can protect data on the LAN from other communication, because in the VLAN environment, only some of the console system administrator can see and execute the command.

IPMI 2 VLAN support for the establishment and management of private network provides a convenient, and can be configured according to the scope of management. The LAN session enhanced functionality and a new definition of effective load capacity (Payload), the management of multiple types of data stream can be transmitted through a LAN session.
 
The working principle of the six, IPMI
IPMI is a core of the one chip / controller (called server processor or a baseboard management controller (BMC), BIOS), the processor or operating system which does not depend on the server to work, can be said to be very independent, is a free agent management subsystem in the system of separate operation, as long as there are BMC and IPMI the firmware can begin to work, while the BMC is usually a server motherboard installation of self independent board, now have a server motherboard to provide support for IPMI. Autonomy of IPMI good will overcome by operating system management mode based on the constraints, such as operating system is not responding or not loading condition can still switch, information extraction etc..

At work, all of the IPMI function is to send BMC commands to complete the prescribed order, use the norms of IPMI commands, BMC receives and records the event message in the system event log, maintenance description of sensor data in sensor system. In remote access system, IPMI LAN serial (SOL) characteristic is very useful. SOL changes the transfer local serial port IPMI session, so as to provide remote access to emergency management services, Windows management console or the Linux serial console. BMC through the LAN serial port information transmitted to change direction to do this, provides a vendor independent operating system boot loader remote viewing, or emergency management console to diagnose and repair the fault standard way.

The working principle of IPMI
When the need for remote access to the system text console, Serial Over LAN (SOL) function is very useful. SOL through the IPMI session redirection local serial interface to allow remote access to Windows's emergency management console (EMS) special management console (SAC), or visit LINUX serial console. The steps of this process is the IPMI firmware to intercept data, and then through the LAN to send directed to the serial port information. This provides remote viewing BOOT, OS loader or emergency management console to the standard method of diagnosis and repair server related issues, without the need to consider the supplier. It allows the various components in the introductory phase configuration.

While in command transmission security, users need not worry about, enhanced authentication IPMI (secure hash algorithm 1 and keyed hash message authentication and encryption based on (Advanced Encryption Standard) and Arcfour) is helpful for remote operation of the realization of safety. Support for VLAN is to provide convenience for the establishment and management of special network, and can be as the basis for the allocation of channel.

Generally speaking, BMC has the following functions:

1 through the serial port to access system

2 fault logging and SNMP alerts

3 to access the system event log (System Event Log, SEL) and sensor status

4 control startup and shutdown

5 independent power supply system or state support

6 for the system settings, text console redirection text utilities and console based operating system

Through IPMI, the user can take the initiative to monitor assembly condition, to ensure that does not exceed the preset threshold, such as server temperature. In this way, by avoiding unscheduled outages, to help maintain the running time of the IT resource. The ability of IPMI to forecast the failure can also contribute to the IT cycle management. By examining the system event log (SEL), can be more easily determine the fault component.

Thus, the future technology trends

Domestic and foreign management vendor management product variety, a more common equipment monitoring and management, network management, application management products and so on, in almost all fields of IT industry. From the analysis of some of the industry management of products, the following development trends:

A full range of module, real time management
The trend of modular system: equipment management, monitoring management obviously, application management and monitoring, security risk management, backup and restore, rapid deployment, and software distribution, remote control (KVM), asset management, public service and other functional modules and the whole management system formed a loose coupling relationship, the user can according to their actual needs of custom their products for server management. At the same time, the remote control plays a more and more important role, with the corresponding remote control tool, administrators can realize in the server field the same as in the management of remote control terminal, greatly enhance the timeliness.

Management of Technology Standardization
Excellent management products generally have a strong platform independence and versatility are developed according to the standard protocol, data acquisition from the underlying communication protocol, to the middle, and the management of the application terminal, are covered by the related management standard. A management standard IPMI 2 is very good.

The managed object description standard
SNMP management protocol, the managed object is described according to the process oriented approach to managed objects to enumerate; DMI (Desktop Management Interface) to the managed object by object oriented description, will be unified organization of information, and provide a range of interface to the Guan Licheng order application; CIM (Common Information Model) makes the management object, management information and access method entirely based on object oriented.

CIM is a DMTF (Desktop Management Task Force) general information management model. The object-oriented approach to different domain of management system is described, the different management systems and applications to share and exchange management information. It consists of two parts: the CIM specification (CIM Specification) describes the modeling language, naming rules, patterns and other management information model (SNMP MIBs, DMTF MIFs) mapping between (Mapping); CIM (CIM Schema) provides a practical management information model, but also provide a class of (Class) with the calculation of all kinds of information management in the environment of the organization.

At present, in the management of network devices, more and more by the standard CIM model, from the various equipment monitoring, security monitoring, storage management and other aspects of a product and application. This is also the trend of the next generation of application server management products.

Management development to gradually distributed
The traditional management products is generally M/A architecture, a management center and a plurality of devices for communication between independent and managed without any contact between devices, limit the sharing of network resources and use, but also for the administrator's management way is limited. With the development of the management system, the connection between the devices will gradually strengthen, not only the management center can control access devices, devices can realize the sharing of resources.

Finally, IPMI function
- remote power control (on / off / cycle / status)

- IP Serial over LAN serial port mapping (SoL)

- support healthy shutdown (Graceful shutdown support)

- the case of environmental monitoring (temperature, rotate speed of fan, CPU voltage etc.)

- Remote ID LED control (Remote ID LED control)

- the system event log (System event log)

- platform event tracking (Platform Event Traps)

- the data record (Data logging)

- a virtual KVM session (Virtual KVM) is currently not supported

- virtual media (Virtual Media) is currently not supported

