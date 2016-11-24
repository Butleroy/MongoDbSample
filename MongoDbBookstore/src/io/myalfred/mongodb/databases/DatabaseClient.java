package io.myalfred.mongodb.databases;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import io.myalfred.mongodb.data.MongoObject;
import io.myalfred.mongodb.dbauth.DbAuth;


/**
 * Base-class of all database client classes. Allows connecting to a MongoDb instance remotely or locally. Each concrete subclass of this class is responsible for handling queries
 * to a specific MongoDb collection.
 * 
 * @author Philipp Jahoda
 *
 * @param <T>
 *            the class that is managed by this instance of the DatabaseClient
 */
public abstract class DatabaseClient<T extends MongoObject> {

	/** the client used to connect to a database instance */
	private MongoClient client;

	/** the database instance the client is connected to */
	private MongoDatabase database;

	/**
	 * Default constructor, connects to either staging or production database (depending on configuration).
	 * 
	 * @param auth
	 *            the authentication object to be used for authentication with a MongoDb instance
	 */
	public DatabaseClient(DbAuth auth) {
		connect(auth);
	}

	/**
	 * Connects to the database with the given DbAuth object.
	 * 
	 * @param auth
	 *            the authentication object to be used
	 */
	protected void connect(DbAuth auth) {

		if (client != null) {
			client.close();
		}

		String host = "mongodb://" + auth.username + ":" + auth.password + "@" + auth.getServerIp();
		String authString = "/?authSource=" + auth.databaseName + "&authMechanism=" + auth.authMechanism;

		client = new MongoClient(new MongoClientURI(host + ":" + auth.port + authString));
		database = client.getDatabase(auth.databaseName);
	}

	/**
	 * Returns the collection (table) with the given name or creates a new collection with the given name if none exists.
	 * 
	 * @param name
	 * @return a collection with the given name
	 */
	protected MongoCollection<Document> getCollection(String name) {
		MongoCollection<Document> collection = database.getCollection(name);

		if (collection == null) {

			// none exists, create a new one
			database.createCollection(name);
			return database.getCollection(name);

		} else {
			return collection;
		}
	}

	/**
	 * Returns the main collection this client represents.
	 * 
	 * @return
	 */
	protected abstract MongoCollection<Document> getMainCollection();

	/**
	 * Returns a List<Bson> of pipeline arguments for an "aggregate" query containing a $match filter with the match arguments provided as a parameter. Override this method to add
	 * additional arguments to the pipeline.
	 * 
	 * @param match
	 *            the query arguments that need to be "matched" using MongoDb $match operator
	 * @return
	 */
	protected abstract List<Bson> getAggregationPipeline(Document match);

	/**
	 * Returns a List<T> results (all) based on the provided "match" aggregation query.
	 * 
	 * @param match
	 *            the query to "match"
	 * @return the query result (a list of objects)
	 */
	protected List<T> aggregationQueryAll(Document match) {

		MongoIterable<Document> iterable = getMainCollection().aggregate(getAggregationPipeline(match));

		List<T> results = new ArrayList<>();
		iterable.forEach(new Block<Document>() {

			@Override
			public void apply(final Document document) {
				results.add(dataFromDoc(document));
			}
		});

		return results;
	}

	/**
	 * Returns the first result object T based on the provided "match" aggregation query.
	 * 
	 * @param the
	 *            query to "match"
	 * @return the query result (a single object)
	 */
	protected T aggregationQueryFirst(Document match) {
		Document document = getMainCollection().aggregate(getAggregationPipeline(match)).first();
		if (document == null)
			return null;
		else
			return dataFromDoc(document);
	}

	/**
	 * Returns the MongoDb database instance this client is connected to.
	 * 
	 * @return
	 */
	protected MongoDatabase getDatabase() {
		return database;
	}

	/**
	 * Transforms the provided String id's to ObjectId objects required by MongoDb.
	 * 
	 * @param ids
	 *            the String id's to be converted
	 * @return a list of ObjectId objects
	 */
	protected List<ObjectId> toObjectIds(List<String> ids) {

		List<ObjectId> objectIds = new ArrayList<>();

		// transform the normal id's to ObjectId objects required by MongoDb
		for (String id : ids) {
			objectIds.add(new ObjectId(id));
		}

		return objectIds;
	}

	/**
	 * Stores the provided data object in MongoDb and sets the unique MongoDb id. Will return true if storing was successful, false if not. Also sets the unique MongoDb object for
	 * the stored data.
	 * 
	 * @param data
	 *            the data object to store in the database
	 * @return true if the operation was successful, false if not
	 */
	public boolean store(T data) {

		if (data == null)
			return false;

		Document doc = dataToDoc(data);

		if (doc == null)
			return false;

		try {
			getMainCollection().insertOne(doc);

			// set the unique id created by MongoDb to the data object
			data.setId(MongoUtils.getObjectId(doc));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Stores all data objects in the list provided as a parameter in the database. Calling this method has much better performance than storing multiple elements one by one.
	 * 
	 * @param dataList
	 *            the data objects to store in the database
	 * @return true if the operation was successful, false if not
	 */
	public boolean storeAll(List<T> dataList) {

		if (dataList == null || dataList.isEmpty()) {
			return false;
		}

		List<Document> docs = new ArrayList<>();

		for (T data : dataList) {
			docs.add(dataToDoc(data));
		}

		try {
			getMainCollection().insertMany(docs);

			// set the unique id created by MongoDb to the data objects
			for (int i = 0; i < docs.size(); i++) {
				dataList.get(i).setId(MongoUtils.getObjectId(docs.get(i)));
			}

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Updates the given data in the database. Overwrites all existing fields with the data provided as a parameter. Returns true if data to update could be found, false if not.
	 * 
	 * @param data
	 *            the data object holding the new data
	 * @return true if the operation was successful and an object was updated
	 */
	public boolean update(T data) {

		if (data == null)
			return false;

		if (data.hasValidObjectId()) {

			ObjectId id = new ObjectId(data.getId());
			Document doc = dataToDoc(data);

			UpdateResult result = getMainCollection().updateOne(new Document("_id", id), new Document("$set", doc));
			return result.getMatchedCount() > 0; // return true if an object to update was found
		} else {
			return false;
		}
	}

	/**
	 * Updates or inserts data. Checks if a data object with the id of the provided data object already exists and updates it if so. If no object with that id exists, a new object
	 * is inserted into the database. "null" id's of course always lead to new data to be inserted.
	 * 
	 * @param data
	 *            the data to update or insert
	 * @return true if an object was updated or a new object was created
	 */
	public boolean upsert(T data) {

		if (data != null) {

			Document doc = dataToDoc(data);

			// if the data already has an id, use that, else create a new one
			ObjectId id = data.getId() == null ? new ObjectId() : new ObjectId(data.getId());
			UpdateResult result = getMainCollection().updateOne(new Document("_id", id), new Document("$set", doc), new UpdateOptions().upsert(true));

			data.setId(id.toHexString()); // set the id of the stored data

			return result.getMatchedCount() > 0 || result.getUpsertedId() != null; // return true if an object to update was found or a new was created
		} else {
			return false;
		}
	}

	/**
	 * Updates the fieldName of the object with the given id with the provided value.
	 * 
	 * @param objectId
	 *            the object that owns the field to update
	 * @param fieldName
	 *            the name of the field to update
	 * @param value
	 *            the new value
	 * @return true if the operation was successful and an object was updated
	 */
	public boolean update(String objectId, String fieldName, Object value) {

		UpdateResult result = getMainCollection().updateOne(new Document("_id", objectId == null ? null : new ObjectId(objectId)),
				new Document("$set", new Document(fieldName, value)));
		return result.getMatchedCount() > 0; // return true if an object to update was found
	}

	/**
	 * Loads a data object by it's MongoDb object-id. Will return null if the provided id is null or if no data for that id could be found.
	 * 
	 * @param id
	 *            the id of the object to load
	 * @return the loaded data, or null if none was found for the provided id
	 */
	public T load(String id) {

		if (id == null || id.isEmpty())
			return null;

		return aggregationQueryFirst(new Document("_id", new ObjectId(id)));
	}

	/**
	 * Loads all data objects that match one of the provided MongoDb id's. Returns an empty list if none were found.
	 * 
	 * @param ids
	 *            the id's of the objects to load
	 * @return all data object that correspond to the provided id's
	 */
	public List<T> loadAll(List<String> ids) {
		return aggregationQueryAll(new Document("_id", new Document("$in", toObjectIds(ids))));
	}

	/**
	 * Loads all data objects that are available in the database this command is executed on.
	 * 
	 * @return all data objects currently stored in the database
	 */
	public List<T> loadAll() {
		return aggregationQueryAll(new Document());
	}

	/**
	 * Deletes the object provided as a parameter from the database. Identification of the object in the database is performed via id.
	 * 
	 * @param data
	 *            the object to delete
	 * @return true if the operation was successful and an object was deleted, false if no object was deleted
	 */
	public boolean delete(T data) {

		if (data != null && data.hasValidObjectId()) {

			DeleteResult result = getMainCollection().deleteOne(new Document("_id", new ObjectId(data.getId())));
			return result.getDeletedCount() == 1;
		} else
			return false;
	}

	/**
	 * Deletes the object with the provided object id from the database.
	 * 
	 * @param id
	 *            the id of the object to delete
	 * @return true if the operation was successful and an object was deleted, false if no object was deleted
	 */
	public boolean delete(String id) {

		if (id != null && !id.isEmpty() && id.length() >= 24) {

			DeleteResult result = getMainCollection().deleteOne(new Document("_id", new ObjectId(id)));
			return result.getDeletedCount() == 1;
		} else
			return false;
	}

	/**
	 * Counts all objects stored in the collection returned by {@link #getMainCollection()} and returns the number.
	 * 
	 * @return the number of stored objects
	 */
	public long objectCount() {
		return getMainCollection().count();
	}

	/**
	 * Transforms the provided data object <T> into a MongoDb document.
	 * 
	 * @param data
	 *            the data object to transform to Document format
	 * @return a Document object representing the provided data
	 */
	protected abstract Document dataToDoc(T data);

	/**
	 * Transforms the provided Document into a data object.
	 * 
	 * @param document
	 *            the document to be transformed to data
	 * @return a <T> object created from the provided Document object
	 */
	protected abstract T dataFromDoc(Document document);
}
