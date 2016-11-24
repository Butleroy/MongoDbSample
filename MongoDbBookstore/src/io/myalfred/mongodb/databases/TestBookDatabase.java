package io.myalfred.mongodb.databases;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;

import io.myalfred.mongodb.data.TestBook;
import io.myalfred.mongodb.dbauth.DefaultAuth;


public class TestBookDatabase extends DatabaseClient<TestBook> {

	public TestBookDatabase() {
		super(new DefaultAuth()); // test database always connects to staging remote (not localhost)
	}

	@Override
	protected MongoCollection<Document> getMainCollection() {
		return getCollection(Collections.TEST_DATA_BOOK);
	}

	@Override
	protected List<Bson> getAggregationPipeline(Document match) {
		List<Bson> pipe = new ArrayList<>();
		pipe.add(new Document("$lookup", new Document("from", Collections.TEST_DATA_AUTHOR).append("localField", "author").append("foreignField", "_id").append("as", "author")));
		pipe.add(new Document("$unwind", "$author"));
		pipe.add(new Document("$match", match));
		return pipe;
	}

	@Override
	protected Document dataToDoc(TestBook data) {
		return data.toDocument();
	}

	@Override
	protected TestBook dataFromDoc(Document document) {
		return new TestBook(document);
	}

	public TestBook findLastName(String authorLastName) {
		return aggregationQueryFirst(new Document("author.lastName", authorLastName));
	}

	public void removeAll() {

		MongoCollection<Document> collection = getCollection(Collections.TEST_DATA_BOOK);
		collection.deleteMany(new Document());
	}
}
