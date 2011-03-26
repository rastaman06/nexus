//
// $Id$
//
// Nexus Core - a framework for developing distributed applications
// http://github.com/samskivert/nexus/blob/master/LICENSE

package com.samskivert.nexus.server;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.samskivert.nexus.io.FrameReader;

import static com.samskivert.nexus.util.Log.log;

/**
 * Handles a connection to a single client.
 */
public class JVMServerConnection
    implements JVMConnectionManager.IOHandler, SessionManager.Output
{
    public JVMServerConnection (JVMConnectionManager cmgr, SocketChannel chan)
    {
        _cmgr = cmgr;
        _chan = chan;
    }

    public void setSession (SessionManager.Input input)
    {
        _input = input;
    }

    /**
     * Called by the connection manager I/O writer thread to instruct this connection to write its
     * pending outgoing messages.
     */
    public void writeMessages ()
    {
        try {
            ByteBuffer frame;
            while ((frame = _outq.peek()) != null) {
                _chan.write(frame);
                if (frame.remaining() > 0) {
                    // partial write, requeue ourselves and finish the job later
                    _cmgr.requeueWriter(this);
                    return;
                }
                _outq.poll(); // remove fully written frame
            }

        } catch (NotYetConnectedException nyce) {
            // this means that our async connection is not quite complete, just requeue ourselves
            // and try again later
            _cmgr.requeueWriter(this);
            
        } catch (IOException ioe) {
            // because we may still be lingering in the connection manager's writable queue, clear
            // out our outgoing queue so that any final calls to writeMessages NOOP
            _outq.clear();
            // now let the usual suspects know that we failed
            _input.onSendError(ioe);
            onClose(ioe);
        }
    }
    
    // from interface SessionManager.Output
    public void send (ByteBuffer buffer)
    {
        // as we do not control the supplied buffer, and we may not be able to write it fully to
        // the outgoing socket, we have to copy it; we could also take this opportunity to copy it
        // into a direct buffer, which may improve I/O performance; someday perhaps we'll measure
        // performance with and without such an optimization
        ByteBuffer frame = ByteBuffer.allocate(buffer.limit()-buffer.position());
        frame.put(buffer);
        frame.flip();

        // add this frame to our output queue and tell the connection manager that we're writable
        _outq.offer(frame);
        _cmgr.queueWriter(this);
    }

    // from interface SessionManager.Output
    public void disconnect ()
    {
        // TODO: shutdown the socket and see what happens?
    }

    // from interface JVMConnectionManager.IOHandler
    public void handleIO ()
    {
        try {
            // keep reading and processing frames while we have them
            ByteBuffer frame;
            while ((frame = _reader.readFrame(_chan)) != null) {
                _input.onMessage(frame);
            }

        } catch (EOFException eofe) {
            _input.onDisconnect();
            onClose(null);

        } catch (IOException ioe) {
            _input.onReceiveError(ioe);
            onClose(ioe);
        }
    }

    protected void onClose (IOException cause)
    {
        if (_chan == null) return; // no double closeage
        try {
            _chan.close();
        } catch (IOException ioe) {
            log.warning("Failed to close socket channel", "chan", _chan, "error", ioe);
        }
        _cmgr.connectionClosed(_chan, cause);
        _chan = null;
    }

    protected JVMConnectionManager _cmgr;
    protected SocketChannel _chan;
    protected SessionManager.Input _input;

    protected FrameReader _reader = new FrameReader();
    protected Queue<ByteBuffer> _outq = new ConcurrentLinkedQueue<ByteBuffer>();
}
