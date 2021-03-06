//
// Nexus GWTIO - I/O and network services for Nexus built on GWT and WebSockets
// http://github.com/threerings/nexus/blob/master/LICENSE

package com.threerings.nexus.io;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.UnsafeNativeLong;

import com.threerings.nexus.distrib.NexusService;

/**
 * Handles the decoding of an input payload (from the server) into proper values.
 */
class ClientInput extends Streamable.Input
{
    public ClientInput (Serializer szer, String data) {
        _szer = szer;
        _values = decode(data);
    }

    @Override public native boolean readBoolean ()
    /*-{
        return !!this.@com.threerings.nexus.io.ClientInput::_values[
            this.@com.threerings.nexus.io.ClientInput::_nextValIdx++];
    }-*/;

    @Override public native byte readByte ()
    /*-{
        return this.@com.threerings.nexus.io.ClientInput::_values[
            this.@com.threerings.nexus.io.ClientInput::_nextValIdx++];
    }-*/;

    @Override public native short readShort ()
    /*-{
        return this.@com.threerings.nexus.io.ClientInput::_values[
            this.@com.threerings.nexus.io.ClientInput::_nextValIdx++];
    }-*/;

    @Override public native char readChar ()
    /*-{
        return this.@com.threerings.nexus.io.ClientInput::_values[
            this.@com.threerings.nexus.io.ClientInput::_nextValIdx++];
    }-*/;

    @Override public native int readInt ()
    /*-{
        return this.@com.threerings.nexus.io.ClientInput::_values[
            this.@com.threerings.nexus.io.ClientInput::_nextValIdx++];
    }-*/;

    @Override @UnsafeNativeLong public native long readLong ()
    /*-{
        var data = this.@com.threerings.nexus.io.ClientInput::_values[
            this.@com.threerings.nexus.io.ClientInput::_nextValIdx++];
        return @com.google.gwt.lang.LongLib::longFromBase64(Ljava/lang/String;)(data);
    }-*/;

    @Override public native float readFloat ()
    /*-{
        return this.@com.threerings.nexus.io.ClientInput::_values[
            this.@com.threerings.nexus.io.ClientInput::_nextValIdx++];
    }-*/;

    @Override public native double readDouble ()
    /*-{
        return this.@com.threerings.nexus.io.ClientInput::_values[
            this.@com.threerings.nexus.io.ClientInput::_nextValIdx++];
    }-*/;

    @Override public native String readString ()
    /*-{
        return this.@com.threerings.nexus.io.ClientInput::_values[
            this.@com.threerings.nexus.io.ClientInput::_nextValIdx++];
    }-*/;

    @Override public <T extends Streamable> Class<T> readClass () {
        @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>)_szer.getClass(readShort());
        return clazz;
    }

    @Override protected <T> Streamer<T> readStreamer () {
        @SuppressWarnings("unchecked") Streamer<T> ts = (Streamer<T>)_szer.getStreamer(readShort());
        return ts;
    }

    @Override protected <T extends NexusService> ServiceFactory<T> readServiceFactory () {
        @SuppressWarnings("unchecked") ServiceFactory<T> factory =
            (ServiceFactory<T>)_szer.getServiceFactory(readShort());
        return factory;
    }

    /** Decodes an encoded payload into a JavaScript array. As the payload is formatted as a
     * JavaScript array, this is done using {@code eval()} for efficiency. */
    private static native JavaScriptObject decode (String encoded)
    /*-{
        return eval(encoded);
    }-*/;

    /** Returns the length of the supplied JavaScript array. */
    private static native int getLength (JavaScriptObject array)
    /*-{
        return array.length;
    }-*/;

    protected final Serializer _szer;
    protected final JavaScriptObject _values;
    protected int _nextValIdx;
}
