//
// Nexus Test Support - shared test infrastructure for Nexus framework
// http://github.com/threerings/nexus/blob/master/LICENSE

package com.threerings.nexus.distrib;

import com.threerings.nexus.io.Streamable;
import com.threerings.nexus.io.Streamer;

/**
 * Handles streaming of {@link TestObject} instances.
 */
public class Streamer_TestObject implements Streamer<TestObject>
{
    public Class<?> getObjectClass ()
    {
        return TestObject.class;
    }

    public void writeObject (Streamable.Output out, TestObject obj)
    {
        out.writeService(obj.testsvc);
        obj.writeContents(out);
    }

    public TestObject readObject (Streamable.Input in)
    {
        TestObject obj = new TestObject(in.<TestService>readService());
        obj.readContents(in);
        return obj;
    }
}
