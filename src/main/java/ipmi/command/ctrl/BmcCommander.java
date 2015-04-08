package ipmi.command.ctrl;

public class BmcCommander {
	@SuppressWarnings("unused")
	public static String configConnect(String ip, String name, String password) {
		return " ip: " + ip + " name: " + name + " password " + password;
	}
}
