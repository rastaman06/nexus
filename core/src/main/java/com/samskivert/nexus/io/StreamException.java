//
// $Id$
//
// Nexus Core - a framework for developing distributed applications
// http://github.com/samskivert/nexus/blob/master/LICENSE

package com.samskivert.nexus.io;

/**
 * An exception thrown if an error occurs when reading or writing {@link Streamable} data.
 */
public class StreamException extends RuntimeException
{
    public StreamException (String message)
    {
        super(message);
    }

    public StreamException (String message, Throwable cause)
    {
        super(message, cause);
    }

    public StreamException (Throwable cause)
    {
        super(cause);
    }
}
