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

package org.suho.siddhi.pattern.distributed.prepartition;

import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.util.ArrayList;
import java.util.Random;

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
        String data1 = "127.0.0.1:9883,127.0.0.1:9884";
        String data2 = "127.0.0.1:9885,127.0.0.1:9886";
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
            destinationList.add("@destination(url='tcp://" + url.trim() + "/pattern/CardStreamS')");
        }
        String destinations1 = String.join(",", destinationList);

        publishUrls = data1.split(",");
        destinationList = new ArrayList<>();
        for (String url : publishUrls) {
            destinationList.add("@destination(url='tcp://" + url.trim() + "/pattern/CardStreamS')");
        }
        String destinations2 = String.join(",", destinationList);

        publishUrls = data2.split(",");
        destinationList = new ArrayList<>();
        for (String url : publishUrls) {
            destinationList.add("@destination(url='tcp://" + url.trim() + "/pattern/CardStreamL')");
        }
        String destinations3 = String.join(",", destinationList);

        String siddhiApp = "" +
                "@app:name('publisher')\n" +
                "\n" +
                "define stream CardStream (cardId string, amount float, location string, ts long);\n" +
                "" +
                "@sink(type='tcp', sync='true', @map(type='binary'), " +
                "   @distribution(strategy='roundRobin', " +
                "       " + destinations1 + ")) \n" +
                "define stream CardStreamS1 (cardId string, amount float, location string,ts long);" +
                "" +
                "@sink(type='tcp', sync='true', @map(type='binary'), " +
                "   @distribution(strategy='broadcast', " +
                "       " + destinations2 + ")) \n" +
                "define stream CardStreamS2 (cardId string, amount float, location string,ts long);" +
                "" +
                "@sink(type='tcp', sync='true', @map(type='binary'), " +
                "   @distribution(strategy='broadcast', " +
                "       " + destinations3 + ")) \n" +
                "define stream CardStreamL (cardId string, amount float, location string,ts long);" +
                "" +
                "from CardStream[location == 'A' or location == 'D']" +
                "insert into CardStreamS1;" +
                "" +
                "from CardStream[location == 'B']" +
                "insert into CardStreamS2;" +
                "" +
                "from CardStream[location == 'C']" +
                "insert into CardStreamL;" +
                "";

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("CardStream");

        //Start SiddhiApp runtime
        siddhiAppRuntime.start();

        long eventsToPublish = 10000;
        int eventCount = 0;
        long startTime = System.currentTimeMillis();
        long startTimeStats = startTime;
        Random random = new Random();
        String[] array = new String[]{"A", "B", "C"};

        //Sending events to Siddhi
        for (int i = 0; i < eventsToPublish; i++) {
//            inputHandler.send(new Object[]{"1234", random.nextInt(200) * 1.0f, array[random.nextInt(3)], System.currentTimeMillis()});

            if (i % 3 == 0) {
                inputHandler.send(new Object[]{"" + i, 99f, "A", System.currentTimeMillis()});
            } else if (i % 3 == 1) {
                inputHandler.send(new Object[]{"" + (i - 1), 99f, "B", System.currentTimeMillis()});
            } else {
                inputHandler.send(new Object[]{"" + (i - 2), 99f, "C", System.currentTimeMillis()});
            }

//            if (i % 3 == 0) {
//                inputHandler.send(new Object[]{"" + i, 99f, "A"});
//            } else if (i % 3 == 1) {
//                inputHandler.send(new Object[]{"" + (i), 99f, "A"});
//            } else {
//                if (i < 5000) {
//                    inputHandler.send(new Object[]{"" + i, 99f, "D"});
//                } else {
//                    inputHandler.send(new Object[]{"" + (i - 5000), 99f, "B"});
//                }
//            }

//            if (i < 10000) {
//                inputHandler.send(new Object[]{"1234", random.nextInt(200) * 1.0f, "SL"});
//            } else {
//                inputHandler.send(new Object[]{"1234", 99f, "SL"});
//            }
            eventCount++;
            if (eventCount % 2 == 0) {
                Thread.sleep(1);
                if (eventCount % 1000 == 0) {
                    long currentTime = System.currentTimeMillis();
                    long timeToSleep = 1000 - (currentTime - startTime);
                    if (timeToSleep > 5) {
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
        Thread.sleep(1000000000);

        siddhiAppRuntime.shutdown();
    }
}
