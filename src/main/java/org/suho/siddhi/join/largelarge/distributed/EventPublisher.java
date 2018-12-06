/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.suho.siddhi.join.largelarge.distributed;

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
        String consume = "127.0.0.1:9881,127.0.0.1:9882";
        String publish = "127.0.0.1:9883,127.0.0.1:9884";
        String data1 = "127.0.0.1:9881,127.0.0.1:9883";
        String data2 = "127.0.0.1:9882,127.0.0.1:9884";

//        String consume = "127.0.0.1:9881,127.0.0.1:9882,127.0.0.1:9885,127.0.0.1:9886,127.0.0.1:9889";
//        String publish = "127.0.0.1:9883,127.0.0.1:9884,127.0.0.1:9887,127.0.0.1:9888";
//        String data1 = "127.0.0.1:9881,127.0.0.1:9883,127.0.0.1:9885,127.0.0.1:9887,127.0.0.1:9889";
//        String data2 = "127.0.0.1:9882,127.0.0.1:9884,127.0.0.1:9886,127.0.0.1:9888";
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
        String[] publishUrls = consume.split(",");
        ArrayList<String> destinationList = new ArrayList<>();
        for (String url : publishUrls) {
            destinationList.add("@destination(url='tcp://" + url.trim() + "/join/StreamA')");
        }
        String destinations1 = String.join(",", destinationList);

        publishUrls = publish.split(",");
        destinationList = new ArrayList<>();
        for (String url : publishUrls) {
            destinationList.add("@destination(url='tcp://" + url.trim() + "/join/StreamA')");
        }
        String destinations2 = String.join(",", destinationList);

        publishUrls = data1.split(",");
        destinationList = new ArrayList<>();
        for (String url : publishUrls) {
            destinationList.add("@destination(url='tcp://" + url.trim() + "/join/StreamB')");
        }
        String destinations3 = String.join(",", destinationList);

        publishUrls = data2.split(",");
        destinationList = new ArrayList<>();
        for (String url : publishUrls) {
            destinationList.add("@destination(url='tcp://" + url.trim() + "/join/StreamB')");
        }
        String destinations4 = String.join(",", destinationList);

        String siddhiApp = "" +
                "@app:name('publisher')\n" +
                "" +
                "define stream StreamA (symbol string, price float, volume long, seqNo long, ts long);\n" +
                "define stream StreamB (symbol string, price float, volume long, seqNo long, ts long);\n" +
                "\n" +
                "@sink(type='tcp', sync='true', @map(type='binary'), " +
                "   @distribution(strategy='roundRobin', " +
                "       " + destinations1 + ")) \n" +
                "define stream StreamA1 (symbol string, price float, volume long, seqNo long, ts long);\n" +
                "" +
                "@sink(type='tcp', sync='true', @map(type='binary'), " +
                "   @distribution(strategy='roundRobin', " +
                "       " + destinations2 + ")) \n" +
                "define stream StreamA2 (symbol string, price float, volume long, seqNo long, ts long);\n" +
                "\n" +
                "@sink(type='tcp', sync='true', @map(type='binary'), " +
                "   @distribution(strategy='roundRobin', " +
                "       " + destinations3 + ")) \n" +
                "define stream StreamB1 (symbol string, price float, volume long, seqNo long, ts long);\n" +
                "" +
                "@sink(type='tcp', sync='true', @map(type='binary'), " +
                "   @distribution(strategy='roundRobin', " +
                "       " + destinations4 + ")) \n" +
                "define stream StreamB2 (symbol string, price float, volume long, seqNo long, ts long);\n" +
                "" +
                "from StreamA[seqNo % 2 == 1]" +
                "insert into StreamA1;" +
                "" +
                "from StreamA[seqNo % 2 == 0]" +
                "insert into StreamA2;" +
                "" +
                "from StreamB[seqNo % 2 == 1]" +
                "insert into StreamB1;" +
                "" +
                "from StreamB[seqNo % 2 == 0]" +
                "insert into StreamB2;" +
                "" +
                "";
        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        InputHandler inputHandlerA = siddhiAppRuntime.getInputHandler("StreamA");
        InputHandler inputHandlerB = siddhiAppRuntime.getInputHandler("StreamB");

        //Start SiddhiApp runtime
        siddhiAppRuntime.start();

        long eventsToPublish = 100000000;
        int eventCount = 0;
        long startTime = System.currentTimeMillis();
        long startTimeStats = startTime;

        //Sending events to Siddhi
        for (long i = 0; i < eventsToPublish; i++) {
            inputHandlerA.send(new Object[]{"IBM", 100f, 100L, i, System.currentTimeMillis()});
            inputHandlerB.send(new Object[]{"IBM", 100f, 100L, i, System.currentTimeMillis()});
            eventCount++;
            if (eventCount % 2 == 0) {
                Thread.sleep(1);
                if (eventCount % 1000 == 0) {
                    long currentTime = System.currentTimeMillis();
                    long timeToSleep = 1000 - (currentTime - startTime);
                    if (timeToSleep > 5) {
//                        System.out.println("--" + timeToSleep);
                        Thread.sleep(timeToSleep-5);
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
        Thread.sleep(1000000000);

        siddhiAppRuntime.shutdown();
    }
}
