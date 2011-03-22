//
// $Id$
//
// Nexus Core - a framework for developing distributed applications
// http://github.com/samskivert/nexus/blob/master/LICENSE

package com.samskivert.nexus.distrib;

import com.samskivert.nexus.io.Streamable;
import com.samskivert.nexus.io.Streamer;

/**
 * Handles streaming of {@link TestObject} instances.
 */
public class Streamer_TestObject implements Streamer<TestObject>
{
    public void writeObject (Streamable.Output out, TestObject obj)
    {
        obj.writeContents(out);
    }

    public TestObject readObject (Streamable.Input in)
    {
        TestObject obj = new TestObject();
        obj.readContents(in);
        return obj;
    }
}