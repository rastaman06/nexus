//
// $Id$
//
// Nexus Core - a framework for developing distributed applications
// http://github.com/samskivert/nexus/blob/master/LICENSE

package com.samskivert.nexus.distrib;

import com.samskivert.nexus.util.Callback;

import static com.samskivert.nexus.util.Log.log;

/**
 * Provides access to interfaces that should not be called by normal clients, but which must be
 * accessible to the Nexus implementation code, which may reside in a different package.
 */
public class DistribUtil
{
    public static void init (NexusObject object, int id, EventSink sink)
    {
        object.init(id, sink);
    }

    public static void clear (NexusObject object)
    {
        object.clear();
    }

    public static void dispatchCall (NexusObject object, int attrIdx, short methId, Object[] args)
    {
        Object cb = args[args.length-1];
        DService.Dispatcher disp = null;
        try {
            disp = (DService.Dispatcher)object.getAttribute(attrIdx);
            disp.dispatchCall(methId, args);

        } catch (NexusException ne) {
            if (cb instanceof Callback<?>) {
                ((Callback<?>)cb).onFailure(ne);
            } else {
                log.warning("Service call failed", "obj", object, "attr", disp, "methId", methId, ne);
            }

        } catch (Throwable t) {
            log.warning("Service call failed", "obj", object, "attr", disp, "methId", methId, t);
            if (cb instanceof Callback<?>) {
                ((Callback<?>)cb).onFailure(new NexusException("Internal error.")); // TODO: i18n
            }
        }
    }

    private DistribUtil () {} // no constructsky
}
