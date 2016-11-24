package io.myalfred.mongodb.dbauth;

/**
 * Database authentication object.
 * 
 * @author Philipp Jahoda
 *
 */
public class DbAuth {

	/** the username to use for authentication */
	public final String username;
	
	/** the password conrresponding to the username */
	public final String password;

	/** the name of the database to authenticate at */
	public final String databaseName;

	/** the IP address of the server that runs the database */
	protected String serverIp;

	/** the port the database is accessible on */
	public final int port = 27017;

	/** default auth mechanism */
	public final String authMechanism = "SCRAM-SHA-1";

	/** default SSL is disabled */
	protected boolean sslEnabled = false;

	public DbAuth(String username, String pw, String databaseName, String ip) {
		this.username = username;
		this.password = pw;
		this.databaseName = databaseName;
		this.serverIp = ip;
	}

	/**
	 * Returns the IP address of the MongoDb server.
	 * 
	 * @return
	 */
	public String getServerIp() {
		return serverIp;
	}
}
