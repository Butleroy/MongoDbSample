package io.myalfred.mongodb.data;

/**
 * Baseclass of all classes stored in MongoDb collections.
 * 
 * @author Philipp Jahoda
 *
 */
public abstract class MongoObject {

	/** the unique id created by MongoDb */
	protected String id;

	public MongoObject() {

	}

	public MongoObject(String id) {
		this.id = id;
	}

	/**
	 * Returns the MongoDb ObjectId as a hex-String.
	 * 
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the MongoDb ObjectId (as hex-String).
	 * 
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns true if this object has a valid MongoDb ObjectId.
	 * 
	 * @return
	 */
	public boolean hasValidObjectId() {
		return this.id != null && !this.id.isEmpty() && this.id.length() >= 24; // MongoDb object-id length is 24
	}
}
