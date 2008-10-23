package org.mongo.driver.impl.msg;

import org.mongo.driver.MongoSelector;
import org.mongo.driver.MongoDBException;
import org.mongo.driver.util.DBStaticData;

/**
 * Representas a Mongo Delete operation
 */
public class DBRemoveMessage extends DBMessage {

    protected final String _dbName;
    protected final String _collection;
    protected final MongoSelector _selector;

    public DBRemoveMessage(String dbName, String collection, MongoSelector sel) throws MongoDBException {
        super(DBStaticData.OP_DELETE);
        _dbName = dbName;
        _collection = collection;

        if (sel == null) {
           throw new MongoDBException("Error : can't have a null selector for DBRemove");
        }
        _selector = sel;

        init();
    }

    /**
     *   Writes the query out to the underlying message byte buffer
     *
     * @throws Exception if something wrong w/ mongoDoc
     */
    protected void init() throws MongoDBException {

        writeInt(0); // reserved for future use - mongo might call this "options" in the comments.  or it may not.
        writeString(_dbName + "." + _collection);

        writeInt(0);   // flags ?
        writeMongoDoc(_selector);
    }
}