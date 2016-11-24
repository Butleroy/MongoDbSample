### MongoDbSample
This is a simple **Maven** project demonstrating the use of the [MongoDb Java driver](https://docs.mongodb.com/ecosystem/drivers/java/) with the example of a bookstore containing books which have authors.

### Setup MongoDb
In order to use this example, you need to setup your own MongoDb.

### Authentication
To connect to your database, modify the `DefaultAuth` class and enter your credentials:

```java
public final class DefaultAuth extends DbAuth {

	public DefaultAuth() {
		/** enter your credentials here */
		super("DATABASE_USERNAME", "DATABASE_PW", "DATABASE_NAME", "SERVER_IP");
	}
}
```
