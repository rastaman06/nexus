//
// $Id$
//
// Nexus Core - a framework for developing distributed applications
// http://github.com/samskivert/nexus/blob/master/LICENSE

package com.samskivert.nexus.server;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.samskivert.nexus.client.JVMClient;
import com.samskivert.nexus.client.NexusClient;
import com.samskivert.nexus.distrib.Action;
import com.samskivert.nexus.distrib.Address;
import com.samskivert.nexus.distrib.DService;
import com.samskivert.nexus.distrib.DValue;
import com.samskivert.nexus.distrib.TestObject;
import com.samskivert.nexus.util.Callback;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Tests simple client server communication.
 */
public class ClientServerTest
{
    public abstract class TestAction {
        /** This method is run (on the test thread) after the server is started up. */
        public void onInit () {
            // nada
        }

        /** This method is run (on the client thread) in response to successful subscription. */
        public abstract void onSubscribe (TestObject test);

        /** Call this method when your testing is complete. This will trigger the shutdown of the
         * client and server and general cleanup. */
        protected void testComplete () {
            _latch.countDown();
        }

        protected void init (NexusServer server, NexusClient client, TestObject test) {
            _server = server;
            _client = client;
            _test = test;
        }

        protected void await () {
            try {
                if (!_latch.await(1, TimeUnit.SECONDS)) {
                    fail("Timed out waiting for test to complete.");
                }
            } catch (InterruptedException ie) {
                fail("Test interrupted while awaiting completion.");
            }
        }

        protected NexusServer _server;
        protected NexusClient _client;
        protected TestObject _test;
        protected CountDownLatch _latch = new CountDownLatch(1);
    }

    @Test
    public void testSubscribeAndAttrChange ()
        throws IOException
    {
        runTest(new TestAction() {
            public void onInit () {
                _test.value.update("bob");
            }
            public void onSubscribe (TestObject test) {
                // make sure the value we got here is the same as the value from the server
                assertEquals(_test.value.get(), test.value.get());

                // add a listener for changes to the test value
                final String ovalue = test.value.get();
                test.value.addListener(new DValue.Listener<String>() {
                    public void valueChanged (String value, String oldValue) {
                        assertEquals("updated", value);
                        assertEquals(ovalue, oldValue);
                        testComplete();
                    }
                });

                // update a test object value (over on the appropriate thread)
                _server.invoke(TestObject.class, new Action<TestObject>() {
                    public void invoke (TestObject stest) {
                        stest.value.update("updated");
                    }
                });
            }
        });
    }

    @Test
    public void testServiceCall ()
        throws IOException
    {
        runTest(new TestAction() {
            public void onSubscribe (TestObject test) {
                // call our test service
                test.testsvc.svc.addOne(41, new Callback<Integer>() {
                    public void onSuccess (Integer value) {
                        assertEquals(42, value.intValue());
                        testComplete();
                    }
                    public void onFailure (Throwable cause) {
                        fail("Callback failed: " + cause.getMessage());
                    }
                });
            }
        });
    }

    protected void runTest (final TestAction action)
        throws IOException
    {
        // create a server with a thread pool
        NexusConfig config = TestUtil.createTestConfig();
        ExecutorService exec = Executors.newFixedThreadPool(3);
        final NexusServer server = new NexusServer(config, exec);

        // set up a connection manager and listen on a port
        final JVMConnectionManager conmgr = new JVMConnectionManager(server.getSessionManager());
        conmgr.listen("localhost", 1234);
        conmgr.start();

        // create a client connection to said server
        NexusClient client = JVMClient.create(Executors.newSingleThreadExecutor(), 1234);

        // register a test object
        TestObject test = new TestObject(DService.create(TestUtil.createTestServiceImpl()));
        server.registerSingleton(test);

        // initialize the action
        action.init(server, client, test);
        action.onInit();

        // subscribe to the test object
        client.subscribe(Address.create("localhost", TestObject.class), new Callback<TestObject>() {
            public void onSuccess (TestObject test) {
                action.onSubscribe(test);
            }
            public void onFailure (Throwable cause) {
                fail("Failed to subscribe to test object " + cause);
            }
        });

        // wait for the test to complete
        action.await();

        // finally shut everything down
        conmgr.disconnect();
        conmgr.shutdown();
        exec.shutdown();
        TestUtil.awaitTermination(exec);
    }
}
