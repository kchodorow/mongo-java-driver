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
package org.mongodb.mql;

public class SimpleNode implements Node {
    protected Node parent;
    protected Node[] children;
    protected int id;
    protected Object value;
    protected MQL parser;
    protected boolean not;
    protected Token token;
    protected Token first;

    public SimpleNode(int i) {
        id = i;
    }

    public SimpleNode(MQL p, int i) {
        this(i);
        parser = p;
    }

    public int getId() {
        return id;
    }

    public void setToken(Token t) {
        token = t;
    }

    public void render(QueryInfo qi) {
    }


    public void updateQI(QueryInfo qi) {
    }

    public String stringJSForm() {
        return getFirstToken().image;
    }


    public Token getFirstToken() {
        return first;
    }

    public void jjtOpen() {
        first = parser.getToken(1);
    }

    public void jjtClose() {
    }

    public void jjtSetParent(Node n) {
        parent = n;
    }

    public Node jjtGetParent() {
        return parent;
    }

    public void jjtAddChild(Node n, int i) {
        if (children == null) {
            children = new Node[i + 1];
        } else if (i >= children.length) {
            Node c[] = new Node[i + 1];
            System.arraycopy(children, 0, c, 0, children.length);
            children = c;
        }
        children[i] = n;
    }

    public Node jjtGetChild(int i) {
        return children[i];
    }

    public int jjtGetNumChildren() {
        return (children == null) ? 0 : children.length;
    }

    public void jjtSetValue(Object value) {
        this.value = value;
    }

    public Object jjtGetValue() {
        return value;
    }

    /* You can override these two methods in subclasses of SimpleNode to
customize the way the node appears when the tree is dumped.  If
your output uses more than one line you should override
toString(String), otherwise overriding toString() is probably all
you need to do. */

    public String toString() {
        return MQLTreeConstants.jjtNodeName[id];
    }

    public String toString(String prefix) {
        return prefix + toString();
    }

    /* Override this method if you want to customize how the node dumps
 out its children. */

    public void dump(String prefix) {
        System.out.println(toString(prefix));
        if (children != null) {
            for (int i = 0; i < children.length; ++i) {
                SimpleNode n = (SimpleNode) children[i];
                if (n != null) {
                    n.dump(prefix + " ");
                }
            }
        }
    }

    public void generateQuery(QueryInfo qi) {
        render(qi);

        if (children != null) {
            for (int i = 0; i < children.length; ++i) {


                SimpleNode n = (SimpleNode) children[i];
                if (n != null) {
                    n.generateQuery(qi);
                }
            }
        }
    }

}

/* JavaCC - OriginalChecksum=eff37a95be5ff60c40e3794138c9de92 (do not edit this line) */
