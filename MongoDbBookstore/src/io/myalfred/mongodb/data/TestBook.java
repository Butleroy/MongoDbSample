package io.myalfred.mongodb.data;

import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

public class TestBook extends MongoObject {

	private TestAuthor author;
	private List<Double> prices;

	public TestBook() {
	}

	@SuppressWarnings("unchecked")
	public TestBook(Document document) {

		ObjectId objectId = document.getObjectId("_id");

		if (objectId != null)
			this.id = objectId.toHexString();

		Document authorDoc = (Document) document.get("author");
		TestAuthor address = new TestAuthor(authorDoc);
		this.author = address;
		this.prices = (List<Double>) document.get("prices");
	}

	public TestBook(TestAuthor address, Double... prices) {
		this.author = address;
		this.prices = Arrays.asList(prices);
	}

	public TestBook(String id, TestAuthor address, Double... prices) {
		this.id = id;
		this.author = address;
		this.prices = Arrays.asList(prices);
	}

	public TestAuthor getAuthor() {
		return author;
	}

	public void setAuthor(TestAuthor author) {
		this.author = author;
	}

	public List<Double> getPrices() {
		return prices;
	}

	public void setPrices(List<Double> prices) {
		this.prices = prices;
	}

	public Document toDocument() {

		Document document = new Document();
		document.append("author", new ObjectId(author.getId()));
		document.append("prices", prices);

		return document;
	}
}
