package org.suho.siddhi.window.length.distributed;

import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.util.ArrayList;

/**
 * Created by suho on 9/14/17.
 */
public class EventPublisher {
    public static void main(String[] args) throws InterruptedException {

        System.out.println("Program Arguments:");
        for (String arg : args) {
            System.out.println("\t" + arg);
        }
        Thread.sleep(9000);

        String consume = "-";
        String publish = "127.0.0.1:9881,127.0.0.1:9882";
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
        String[] publishUrls = publish.split(",");
        ArrayList<String> destinationList = new ArrayList<>();
        for (String url : publishUrls) {
            destinationList.add("@destination(url='tcp://" + url.trim() + "/length-window/StockEventStream')");
        }
        String destinations = String.join(",", destinationList);

        String siddhiApp = "" +
                "@app:name('publisher')\n" +
                "\n" +
                "@sink(type='tcp', sync='true', @map(type='binary'), " +
                "   @distribution(strategy='roundRobin', " +
                "       "+destinations+")) \n" +
                "define stream StockEventStream (symbol string, price float, volume long, seqNo long);\n";

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("StockEventStream");

        //Start SiddhiApp runtime
        siddhiAppRuntime.start();

        long eventsToPublish = 100000000;
        int eventCount = 0;
        long startTime = System.currentTimeMillis();
        long startTimeStats = startTime;

        //Sending events to Siddhi
        for (int i = 0; i < eventsToPublish; i++) {
            inputHandler.send(new Object[]{"IBM", 100f, 100L});
            eventCount++;
            if (eventCount % 2 == 0) {
                Thread.sleep(1);
                if (eventCount % 1000 == 0) {
                    long currentTime = System.currentTimeMillis();
                    long timeToSleep = 1000 - (currentTime - startTime);
                    if (timeToSleep > 5) {
//                        System.out.println("--" + timeToSleep);
                        Thread.sleep(timeToSleep - 5);
                    }
                    currentTime = System.currentTimeMillis();
                    startTime = currentTime;
                    if (eventCount % 10000 == 0) {
                        System.out.println((eventCount * 1000) / (currentTime - startTimeStats));
                        startTimeStats = currentTime;
                        eventCount = 0;

                    }
                }
            }
        }
        System.out.println("published");
        Thread.sleep(1000000);

        siddhiAppRuntime.shutdown();
    }
}
