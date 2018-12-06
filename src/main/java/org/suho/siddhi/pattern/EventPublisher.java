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

package org.suho.siddhi.pattern;

import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

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

        String consume = "-";
        String publish = "127.0.0.1:9892";
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
                "@app:name('publisher')\n" +
                "\n" +
                "@sink(type='tcp', url='tcp://" + publish + "/pattern/CardStream', sync='true', @map(type='binary')) \n" +
                "define stream CardStream (cardId string, amount float, location string, ts long);\n";

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("CardStream");

        //Start SiddhiApp runtime
        siddhiAppRuntime.start();

        long eventsToPublish = 100000;
        int eventCount = 0;
        long startTime = System.currentTimeMillis();
        long startTimeStats = startTime;
        Random random = new Random();
        String[] array = new String[]{"A", "B", "C"};

        //Sending events to Siddhi
        for (int i = 0; i < eventsToPublish; i++) {
//            inputHandler.send(new Object[]{"1234", random.nextInt(200) * 1.0f, array[i % 3], System.currentTimeMillis()});
//
            if (i % 3 == 0) {
                inputHandler.send(new Object[]{"" + i, 99f, "A", System.currentTimeMillis()});
            } else if (i % 3 == 1) {
                inputHandler.send(new Object[]{"" + (i - 1), 99f, "B", System.currentTimeMillis()});
            } else {
                inputHandler.send(new Object[]{"" + (i - 2), 99f, "C", System.currentTimeMillis()});
            }

//            if (i < 10000) {
//                inputHandler.send(new Object[]{"1234", random.nextInt(200) * 1.0f, "SL",System.currentTimeMillis()});
//            } else {
//                inputHandler.send(new Object[]{"1234", 99f, "SL", System.currentTimeMillis()});
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
