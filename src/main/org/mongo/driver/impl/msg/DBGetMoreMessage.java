package org.mongo.driver.impl.msg;

import org.mongo.driver.MongoDBException;
import org.mongo.driver.DBQuery;
import org.mongo.driver.util.DBStaticData;

/**
 * User: geir
 * Date: Oct 21, 2008
 * Time: 6:43:01 PM
 */
public class DBGetMoreMessage extends DBMessage {

    protected final long _cursor;
    protected final String _dbName;
    protected final String _collection;
    
    public DBGetMoreMessage(String dbName, String collection, long cursor) throws MongoDBException
    {
        super(DBStaticData.OP_GET_MORE);
        _dbName = dbName;
        _collection = collection;
        _cursor = cursor;
        init();
    }

    /**
     *   Writes the query out to the underlying message byte buffer
     *
     * @throws Exception if something wrong w/ mongoDoc
     */
    protected void init() throws MongoDBException
    {

        writeInt(0); // reserved for future use - mongo might call this "options" in the comments.  or it may not.
        writeString(_dbName + "." + _collection);
        writeInt(0); // n toreturn - leave it up to db for now
        writeLong(_cursor);
    }
}
