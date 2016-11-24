package io.myalfred.mongodb.dbauth;

public final class DefaultAuth extends DbAuth {

	public DefaultAuth() {
		// ENTER YOUR CREDENTIALS HERE
		super("DATABASE_USERNAME", "DATABASE_PW", "DATABASE_NAME", "SERVER_IP");
	}
}
