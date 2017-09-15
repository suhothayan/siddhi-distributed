package org.suho.siddhi.window.length.distributed;

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


        String siddhiApp = "" +
                "@app:name('consumer')\n" +
                "\n" +
                "@source(type='tcp', @map(type='binary')) \n" +
                "define stream AggregateStockStream (symbol string, totalPrice double, avgVolume double);\n";

        SiddhiManager siddhiManager = new SiddhiManager();
        Map<String, String> executionConfig = new HashMap<>();
        executionConfig.put("source.tcp.port", "9895");
        siddhiManager.setConfigManager(new InMemoryConfigManager(executionConfig, null));
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("AggregateStockStream", new StreamCallback() {
            public int eventCount = 0;
//            public int timeSpent = 0;
            long startTime = System.currentTimeMillis();

            @Override
            public void receive(Event[] events) {
                for (Event event : events) {
                    eventCount++;
//                    timeSpent += (System.currentTimeMillis() - (Long) event.getData(3));
                    if (eventCount % 10000 == 0) {
                        System.out.println("Throughput : " + (eventCount * 1000) / ((System.currentTimeMillis()) -
                                startTime));
//                        System.out.println("Time spent :  " + (timeSpent * 1.0 / eventCount));
                        startTime = System.currentTimeMillis();
                        eventCount = 0;
//                        timeSpent = 0;
                    }
                }
            }
        });

        //Start SiddhiApp runtime
        siddhiAppRuntime.start();

        Thread.sleep(1000000);

        //Shutdown SiddhiApp runtime
        siddhiAppRuntime.shutdown();

        //Shutdown Siddhi
        siddhiManager.shutdown();
    }
}
