package io.myalfred.mongodb.data;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

public class TestBookStore extends MongoObject {

	private String name;
	private List<TestBook> books;
	
	public TestBookStore() {
		
	}
	
	public TestBookStore(Document document) {
		
		this.id = document.getObjectId("_id").toHexString();
		this.name = document.getString("name");
		this.books = new ArrayList<>();
		
		@SuppressWarnings("unchecked")
		List<Document> bookDocs = (List<Document>) document.get("books");
		
		for(Document book : bookDocs) {
			this.books.add(new TestBook(book));
		}
	} 
	
	public String getName() {
		return name;
	}
	
	
	public void setName(String name) {
		this.name = name;
	}
	
	
	public List<TestBook> getBooks() {
		return books;
	}
	
	
	public void setBooks(List<TestBook> books) {
		this.books = books;
	}
	
	public Document toDocument() {

		Document document = new Document();
		document.append("name", this.name);

		List<ObjectId> bookIds = new ArrayList<>();

		for (TestBook book : this.books) {
			bookIds.add(new ObjectId(book.getId()));
		}

		document.append("books", bookIds);

		return document;
	}
}
