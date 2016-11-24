package io.myalfred.mongodb.databases;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;

import io.myalfred.mongodb.data.TestAuthor;
import io.myalfred.mongodb.dbauth.DefaultAuth;


public class TestAuthorDatabase extends DatabaseClient<TestAuthor> {

	public TestAuthorDatabase() {
		super(new DefaultAuth()); // test database always connects to staging remote (not localhost)
	}
	
	@Override
	protected List<Bson> getAggregationPipeline(Document match) {
		List<Bson> pipe = new ArrayList<>();
		pipe.add(new Document("$match", match));
		return pipe;
	}

	@Override
	protected MongoCollection<Document> getMainCollection() {
		return getCollection(Collections.TEST_DATA_AUTHOR);
	}

	@Override
	protected Document dataToDoc(TestAuthor data) {
		return data.toDocument();
	}

	@Override
	protected TestAuthor dataFromDoc(Document document) {
		return new TestAuthor(document);
	}

	public void removeAll() {

		MongoCollection<Document> collection = getCollection(Collections.TEST_DATA_AUTHOR);
		collection.deleteMany(new Document());
	}
}
