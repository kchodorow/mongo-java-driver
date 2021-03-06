/**
 *  See the NOTICE.txt file distributed with this work for
 *  information regarding copyright ownership.
 *
 *  The authors license this file to you under the
 *  Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License.  You may
 *  obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.mongodb.driver.impl.msg;

import org.mongodb.driver.ts.MongoDoc;
import org.mongodb.driver.ts.MongoSelector;
import org.mongodb.driver.MongoDBException;
import org.mongodb.driver.MongoDBIOException;
import org.mongodb.driver.util.BSONObject;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.Formatter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Base message class for mongo communications
 */
public abstract class DBMessage {

    protected final static int DEFAULT_BUF_SIZE = 1024*100;

    private static AtomicInteger _classReqID = new AtomicInteger(1);

    protected int _messageLength = DBMessageHeader.HEADER_SIZE;    // overall message length - header size to start
    
    protected int _requestID = getNextRequestID();
    protected int _responseTo = 0;
    protected MessageType _op;

    protected ByteBuffer _buf;
        
    /**
     *  Creates a DBMessage with correct header written
     * 
     * @param op opcode for db operation
     */
    protected DBMessage(MessageType op) {

        // TODO - we need to be able to allocate dynmically more accurately, or pool
        _buf = ByteBuffer.allocate(DEFAULT_BUF_SIZE);
        _buf.order(ByteOrder.LITTLE_ENDIAN);


        //  TODO - fix this - this is too idiotic - use the DBMessageHeader object here...
        
        _op = op;

        _buf.position(0);
        _buf.putInt(_messageLength);             // holder for the length
        _buf.putInt(_requestID);
        _buf.putInt(_responseTo);
        _buf.putInt(_op.getOpCode());
        
        assert(_buf.position() == DBMessageHeader.HEADER_SIZE);
    }

    protected DBMessage(ByteBuffer buf) {
        
        DBMessageHeader header = DBMessageHeader.readHeader(buf);

        _messageLength = header.getMessageLength();
        _requestID = header._requestID;
        _responseTo = header._responseTo;
        _op = header._op;

        _buf = buf;
    }

    /**
     *  Writes a string to the message buffer.  This is a CSTR, with no leading size.
     *
     * @param s string to write
     */
    protected void writeString(String s) {
        int i = BSONObject.serializeCSTR(_buf, s);
        updateMessageLength(i);
    }

    protected String readString() throws MongoDBException {
        return BSONObject.deserializeCSTR(_buf);
    }

    protected void writeLong(long i) {

        _buf.putLong(i);
        updateMessageLength(8);
    }

    protected long readLong() {
        return _buf.getLong();
    }

    protected void writeInt(int i) {

        _buf.putInt(i);
        updateMessageLength(4);
    }    

    protected int readInt() {
        return _buf.getInt();
    }

    protected void writeByte(byte b) {
        _buf.put(b);
        updateMessageLength(1);
    }

    protected void writeMongoDoc(MongoDoc doc) throws MongoDBException {

        BSONObject bson = new BSONObject();
        bson.serialize(doc);

        byte[] arr = bson.toArray();
        _buf.put(arr);

        updateMessageLength(arr.length);
    }

    protected MongoSelector readMongoSelector() throws MongoDBException {
        return BSONObject.deserializeSelector(_buf);

    }

    protected MongoDoc readMongoDoc() throws MongoDBException {
        return BSONObject.deserializeObjectData(_buf);
    }


    protected void updateMessageLength(int delta) {
        _messageLength += delta;

        _buf.putInt(0, _messageLength);
    }

    protected static int getNextRequestID() {
        return _classReqID.getAndIncrement();
    }


    public ByteBuffer getInternalByteBuffer() {
        return _buf;        
    }

    public byte[] toByteArray() {
        _buf.flip();
        byte[] msg = new byte[_buf.limit()];
        _buf.get(msg);

        return msg;
    }

    public static DBMessage readFromStream(InputStream is) throws MongoDBIOException, MongoDBException{

        try {
            /*
             *  read the header, and then construct a bytebuffer w/ full message
             */

            DBMessageHeader msgHeader = DBMessageHeader.readHeader(is);

            ByteBuffer buf = ByteBuffer.allocate(msgHeader.getMessageLength());
            buf.order(ByteOrder.LITTLE_ENDIAN);

            buf.position(0);
            msgHeader.writeHeader(buf);
            assert(buf.position() == DBMessageHeader.HEADER_SIZE);

            int remaining = msgHeader.getMessageLength() - DBMessageHeader.HEADER_SIZE;

            for (int i=0; i < remaining; i++) {
                int b = is.read();
                buf.put((byte) b);
            }

            buf.flip();
            
            switch(msgHeader.getOperation()) {
                case OP_QUERY:
                    return new DBQueryMessage(buf);
                case OP_REPLY:
                    return new DBQueryReplyMessage(buf);
                case OP_INSERT:
                    return new DBInsertMessage(buf);
                case OP_DELETE:
                    return new DBRemoveMessage(buf);
                case OP_GET_MORE:
                    return new DBGetMoreMessage(buf);
                default :
                    throw new MongoDBException("Unknown operation type : " + msgHeader.getOperation());
            }

        }
        catch (IOException ioe) {
            throw new MongoDBIOException("error reading message", ioe);
        }
    }

    public void dumpHex(OutputStream os) throws IOException {

        OutputStreamWriter w = new OutputStreamWriter(os);
        Formatter formatter = new Formatter(w);
        StringBuffer sb = new StringBuffer();

        _buf.flip();

        for (int i = 0; i < _buf.limit(); i++) {
            if (i % 8 == 0) {
                if (i != 0) {
                    w.append("    ");
                    w.append(sb.toString());
                    sb = new StringBuffer();
                    w.append("\n");
                }
                formatter.format("%4d:  ", i);
            } else {
                w.append(" ");
            }
            byte b = _buf.get();
            formatter.format("%02X", b);
            char c = (char) b;

            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            }
            else {
                sb.append(".");
            }
        }

        w.flush();
    }

    public MessageType getMessageType() {
        return _op;
    }


    public String headerString() {
        StringBuffer sb = new StringBuffer("(len=[").append(_messageLength).append("]");
        sb.append(" id=[").append(_requestID).append("]");
        sb.append(" rspTo=[").append(_responseTo).append("])");
        return sb.toString();
    }    
}
