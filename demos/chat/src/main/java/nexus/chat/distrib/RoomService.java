//
// $Id$

package nexus.chat.distrib;

import com.samskivert.nexus.util.Callback;

/**
 * Defines distributed services available in a room.
 */
public interface RoomService
{
    /**
     * Requests that the supplied chat message be sent to the room.
     */
    void sendMessage (String message, Callback<Void> callback); // TODO: mode (emote, shout, etc.)?
}