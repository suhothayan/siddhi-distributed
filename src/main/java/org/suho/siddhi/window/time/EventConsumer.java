package org.suho.siddhi.window.time;

import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.config.InMemoryConfigManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Standalone window
 */
public class EventConsumer {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("Program Arguments:");
        for (String arg : args) {
            System.out.println("\t" + arg);
        }

        String consume = "9895";
        String publish = "-";
        String data1 = "-";
        String data2 = "-";
        if (args.length != 0) {
            if (args.length == 4) {
                consume = args[0];
                publish = args[1];
                data1 = args[2];
                data2 = args[3];
            } else {
                throw new Error("More " + args.length + " arguments found expecting 4.");
            }
        }

        String siddhiApp = "" +
                "@app:name('consumer')\n" +
                "\n" +
                "@source(type='tcp', @map(type='binary')) \n" +
                "define stream AggregateStockStream (symbol string, totalPrice double, avgVolume double, ts long);\n";

        SiddhiManager siddhiManager = new SiddhiManager();
        Map<String, String> executionConfig = new HashMap<>();
        executionConfig.put("source.tcp.port", consume);
        siddhiManager.setConfigManager(new InMemoryConfigManager(executionConfig, null));
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("AggregateStockStream", new StreamCallback() {
            public int eventCount = 0;
            public int timeSpent = 0;
            long lastEventId = -1;
            long maxEventId = -1;
            long maxdelay = -1;
            int order = 0;
            int outOfOrder = 0;
            long startTime = System.currentTimeMillis();

            @Override
            public void receive(Event[] events) {
//                EventPrinter.print(events);
                for (Event event : events) {
                    eventCount++;
                    Long evnetId = (Long) event.getData(3);
                    synchronized (this) {
                        if (evnetId >= lastEventId) {
                            order++;
                        } else {
                            outOfOrder++;
                        }
                        lastEventId = evnetId;
                        if (maxEventId > evnetId) {
                            if (maxEventId - evnetId > maxdelay) {
                                maxdelay = maxEventId - evnetId;
                            }
                        } else {
                            maxEventId = evnetId;
                        }
                    }
                    timeSpent += (System.currentTimeMillis() - (Long) event.getData(3));
                    if (eventCount % 10000 == 0) {
                        System.out.println((eventCount * 1000) / ((System.currentTimeMillis()) -
                                startTime));
                        System.out.println("Time spent :  " + (timeSpent * 1.0 / eventCount));
                        System.out.println("order :  " + order + " outOfOrder : " + outOfOrder + " maxdelay : " + maxdelay);
                        startTime = System.currentTimeMillis();
                        eventCount = 0;
                        timeSpent = 0;
                        outOfOrder = 0;
                        order = 0;
                        maxdelay = 0;
                    }
                }
            }
        });

        System.out.println("Throughput : ");
        //Start SiddhiApp runtime
        siddhiAppRuntime.start();

        Thread.sleep(1000000000);

        //Shutdown SiddhiApp runtime
        siddhiAppRuntime.shutdown();

        //Shutdown Siddhi
        siddhiManager.shutdown();
    }
}
