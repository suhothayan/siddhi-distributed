package org.suho.siddhi.window.distributed;

import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

/**
 * Created by suho on 9/14/17.
 */
public class EventPublisher {
    public static void main(String[] args) throws InterruptedException {

        String siddhiApp = "" +
                "@app:name('publisher')\n" +
                "\n" +
                "@sink(type='tcp', sync='true', @map(type='binary'), " +
                "   @distribution(strategy='roundRobin', " +
                "       @destination(url='tcp://127.0.0.1:9881/time-window/StockEventStream')," +
                "       @destination(url='tcp://127.0.0.1:9882/time-window/StockEventStream'))) \n" +
                "define stream StockEventStream (symbol string, price float, volume long);\n";

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("StockEventStream");

        //Start SiddhiApp runtime
        siddhiAppRuntime.start();

        long eventsToPublish= 100000000;
//        long eventsToPublish = 2;
        //Sending events to Siddhi
        for (int i = 0; i < eventsToPublish; i++) {
            inputHandler.send(new Object[]{"IBM", 100f, 100L});
        }
        System.out.println("published");
        Thread.sleep(1000000);

        siddhiAppRuntime.shutdown();
    }
}
