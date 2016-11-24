## MongoDbSample
This is a simple **Maven** project demonstrating the use of the [MongoDb Java driver](https://docs.mongodb.com/ecosystem/drivers/java/) with the example of a bookstore containing books which have authors.

## Usage

### Setup MongoDb
In order to test this example (which means running the included JUnit test case), you need to setup your own MongoDb with three collections:

- authors
- books
- bookstores

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

## Data model

The data this sample stores into MongoDb looks similar to the following.

**"bookstores"** collection:

```json
{
    "_id" : ObjectId("5835b19b4e5a9ca2509e4384"),
    "name" : "Thalia",
    "books" : [ 
        ObjectId("5835b19b4e5a9ca2509e437e"), 
        ObjectId("5835b19b4e5a9ca2509e437f"), 
        ObjectId("5835b19b4e5a9ca2509e4380")
    ]
}
```

**"books"** collection:

```json
{
    "_id" : ObjectId("5835b19b4e5a9ca2509e437e"),
    "author" : ObjectId("5835b19a4e5a9ca2509e437c"),
    "prices" : [ 
        10.0
    ]
}
{
    "_id" : ObjectId("5835b19b4e5a9ca2509e437f"),
    "author" : ObjectId("5835b19b4e5a9ca2509e437d"),
    "prices" : [ 
        11.0
    ]
}
{
    "_id" : ObjectId("5835b19b4e5a9ca2509e4380"),
    "author" : ObjectId("5835b19a4e5a9ca2509e437c"),
    "prices" : [ 
        12.0
    ]
}
```

**"authors"** collection:

```json
{
    "_id" : ObjectId("5835b19a4e5a9ca2509e437c"),
    "firstName" : "Dan",
    "lastName" : "Brown"
}
{
    "_id" : ObjectId("5835b19b4e5a9ca2509e437d"),
    "firstName" : "Stephen",
    "lastName" : "King"
}
```

## Database aggregation query

In order to query a full "bookstore" form the database containing all information about "books" and "authors", the following MongoDb query is required:

```json
db.getCollection('bookstores').aggregate(
  { "$unwind": "$books" },
  { "$lookup": {
      "from": "books",
      "localField": "books",
      "foreignField": "_id",
      "as": "books"
    }
  },
  { "$unwind": "$books" },
  { "$lookup": {
      "from": "authors",
      "localField": "books.author",
      "foreignField": "_id",
      "as": "authors"
    }
  },
  { "$unwind": "$authors" },
  { "$group": {
      "_id": "$_id",
      "name": {
        "$first": "$name"
      },
      "books": {
        "$push": {
          "_id": "$books.id",
          "author": "$authors",
          "prices": "$books.prices"
        }
      }
    }
  }
)
```

## Query result

The result of the above query returns a document containing all information about the bookstore, the books it contains and their authors.

