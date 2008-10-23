package org.mongo.driver.impl;

import org.testng.annotations.Test;
import org.mongo.driver.impl.DBImpl;
import org.mongo.driver.impl.Mongo;
import org.mongo.driver.DB;
import org.mongo.driver.options.db.StrictCollectionMode;
import static org.testng.AssertJUnit.*;

import java.net.InetSocketAddress;

public class DBTest {

    @Test
    public void testDBName() throws Exception {

        Mongo m = new Mongo();

        try {
            new DBImpl(null, null);
            fail();
        }
        catch(Exception e) {
            // ok
        }

        try {
            new DBImpl(m, "", null);
            fail();
        }
        catch(Exception e) {
            // ok
        }

        try {
            new DBImpl(m, "", new InetSocketAddress("127.0.0.1", Mongo.DEFAULT_MONGO_PORT));
            fail();
        }
        catch(Exception e) {
            // ok
        }

        try {
            new DBImpl(m, "a.b", new InetSocketAddress("127.0.0.1", Mongo.DEFAULT_MONGO_PORT));
            fail();
        }
        catch(Exception e) {
            // ok
        }

        DBImpl db = new DBImpl(m, "test_me", new InetSocketAddress("127.0.0.1", Mongo.DEFAULT_MONGO_PORT));

        assert(db.getName().equals("test_me"));

        db.close();
    }

    @Test
    public void testCreateDrop() throws Exception {

        Mongo m = new Mongo();

        DB db = m.getDB("org_mongo_driver_DBTest");

        for (String n : db.getCollectionNames()) {
            db.dropCollection(n);
        }

        assert(db.getCollectionNames().size() == 0);
        db.getCollection("a");
        assert(db.getCollectionNames().size() == 1);
        db.getCollection("b");
        assert(db.getCollectionNames().size() == 2);
        db.dropCollection("a");
        assert(db.getCollectionNames().size() == 1);
        db.dropCollection("b");
        assert(db.getCollectionNames().size() == 0);

        db.close();
    }

    @Test
    public void testStrictCollectionCreation() throws Exception {

        Mongo m = new Mongo();

        DB db = new DBImpl(m, "org_mongo_driver_DBTest");
        db.setDBOptions(new StrictCollectionMode());

        for (String n : db.getCollectionNames()) {
            db.dropCollection(n);
        }

        assert(db.getCollectionNames().size() == 0);

        try {
            db.getCollection("woogie");
            fail();
        }
        catch(Exception e) {
            // expect an exception as we're in strict mode
        }
        
        assert(db.getCollectionNames().size() == 0);

        db.setDBOptions(null);

        assert(db.getCollection("woogie") != null);
        assert(db.getCollectionNames().size() == 1);

    }

}
