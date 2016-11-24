package io.myalfred.mongodb.data;

import org.bson.Document;

public class TestAuthor extends MongoObject {

	private String firstName;
	private String lastName;

	public TestAuthor() {
	}
	
	public TestAuthor(Document document) {
		
		this.firstName = document.getString("firstName");
		this.lastName = document.getString("lastName");
		this.id = document.getObjectId("_id").toHexString();
	}

	public TestAuthor(String first, String last) {
		this.firstName = first;
		this.lastName = last;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Document toDocument() {

		Document document = new Document();
		document.append("firstName", firstName);
		document.append("lastName", lastName);
		return document;
	}
}
