#
#      Copyright (C) 2008 Geir Magnusson Jr
#
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
#

require 'java'

db = org.mongo.driver.impl.DBImpl.new("jruby")

coll = db.get_collection "test", true

coll.clear

doc = org.mongo.driver.MongoDoc.new

10.times { |i|
  doc["a"] = i + 1
  coll.insert doc
}

coll.find.each { |i| puts i.to_s }


# now use native dictionariies for objects and selectors

coll = db.get_collection "test2", true
coll.clear

coll.insert ":b" => 1
coll.insert ":b" => 2

coll.find.each { |i| puts i.to_s }