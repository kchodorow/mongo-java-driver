/**
*      Copyright (C) 2008 Geir Magnusson Jr
*  
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*  
*       http://www.apache.org/licenses/LICENSE-2.0
*  
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/

package org.mongodb.driver;

/**
 *  Base exception thrown by the Mongo DB API
 */
public class MongoDBException extends Exception {

    public MongoDBException() {
        super();
    }

    public MongoDBException(Throwable t) {
        super(t);
    }

    public MongoDBException(String msg) {
        super(msg);
    }

    public MongoDBException(String msg, Throwable t) {
        super(msg, t);
    }
}