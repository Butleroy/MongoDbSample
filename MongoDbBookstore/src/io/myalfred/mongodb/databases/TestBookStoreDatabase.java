package io.myalfred.mongodb.databases;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;

import io.myalfred.mongodb.data.TestBookStore;
import io.myalfred.mongodb.dbauth.DefaultAuth;

public class TestBookStoreDatabase extends DatabaseClient<TestBookStore> {

	public TestBookStoreDatabase() {
		super(new DefaultAuth()); // test database always connects to staging remote (not localhost)
	}

	@Override
	protected MongoCollection<Document> getMainCollection() {
		return getCollection(Collections.TEST_DATA_BOOKSTORE);
	}

	@Override
	protected List<Bson> getAggregationPipeline(Document match) {
		List<Bson> pipe = new ArrayList<>();
		pipe.add(new Document("$unwind", "$books"));
		pipe.add(new Document("$lookup", new Document("from", Collections.TEST_DATA_BOOK).append("localField", "books").append("foreignField", "_id").append("as", "books")));
		pipe.add(new Document("$unwind", "$books"));
		pipe.add(new Document("$lookup",
				new Document("from", Collections.TEST_DATA_AUTHOR).append("localField", "books.author").append("foreignField", "_id").append("as", "authors")));
		pipe.add(new Document("$unwind", "$authors"));
		pipe.add(new Document("$group", new Document("_id", "$_id").append("name", new Document("$first", "$name")).append("books",
				new Document("$push", new Document("author", "$authors").append("prices", "$books.prices")))));
		pipe.add(new Document("$match", match));
		return pipe;
	}

	@Override
	protected Document dataToDoc(TestBookStore data) {
		return data.toDocument();
	}

	@Override
	protected TestBookStore dataFromDoc(Document document) {
		return new TestBookStore(document);
	}

	public void removeAll() {

		MongoCollection<Document> collection = getCollection(Collections.TEST_DATA_BOOKSTORE);
		collection.deleteMany(new Document());
	}
}
