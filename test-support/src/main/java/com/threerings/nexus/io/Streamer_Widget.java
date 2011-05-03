//
// Nexus Test Support - shared test infrastructure for Nexus framework
// http://github.com/threerings/nexus/blob/master/LICENSE

package com.threerings.nexus.io;

/**
 * Handles streaming {@link Widget} instances.
 */
public class Streamer_Widget implements Streamer<Widget>
{
    public Class<?> getObjectClass ()
    {
        return Widget.class;
    }

    public void writeObject (Streamable.Output out, Widget obj)
    {
        out.writeString(obj.name);
        out.writeValue(obj._wangle);
    }

    public Widget readObject (Streamable.Input in)
    {
        return new Widget(in.readString(), in.<Widget.Wangle>readValue());
    }

    public static class Wangle implements Streamer<Widget.Wangle>
    {
        public Class<?> getObjectClass () {
            return Widget.Wangle.class;
        }
        public void writeObject (Streamable.Output out, Widget.Wangle obj) {
            out.writeInt(obj.size);
        }
        public Widget.Wangle readObject (Streamable.Input in) {
            return new Widget.Wangle(in.readInt());
        }
    }
}