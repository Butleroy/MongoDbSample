package io.myalfred.mongodb.databases;

import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Utility class for converting data to MongoDb Document and back.
 * 
 * @author Philipp Jahoda
 *
 */
public abstract class MongoUtils {

	/**
	 * Extracts the objectId as a String from the given Document object.
	 * 
	 * @param document
	 *            the document to extract the id from
	 * @return the object id ("_id") as a hex String
	 */
	public static String getObjectId(Document document) {

		ObjectId id = document.getObjectId("_id");
		if (id == null)
			return null;
		else
			return id.toHexString();
	}
}
