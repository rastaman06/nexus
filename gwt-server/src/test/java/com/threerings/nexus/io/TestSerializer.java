//
// Nexus GWTServer - server-side support for Nexus GWT/WebSockets services
// http://github.com/threerings/nexus/blob/master/LICENSE

package com.threerings.nexus.io;

/**
 * A serializer that handles our test classes.
 */
public class TestSerializer extends AbstractSerializer
{
    public TestSerializer () {
        mapStreamer(new Streamer_Widget());
        mapStreamer(Streamers.create(Widget.Color.class));
        mapStreamer(new Streamer_Widget.Wangle());
    }
}
