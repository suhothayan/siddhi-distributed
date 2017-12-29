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

import java.util.Random;

/**
 * Created by suho on 9/14/17.
 */
public class EventPublisher {
    public static void main(String[] args) throws InterruptedException {

        String siddhiApp = "" +
                "@app:name('publisher')\n" +
                "\n" +
                "define stream CardStream (cardId string, amount float, location string);\n" +
                "" +
                "@sink(type='tcp', sync='true', @map(type='binary'), " +
                "   @distribution(strategy='broadcast', " +
                "       @destination(url='tcp://127.0.0.1:9883/pattern/CardStreamS')," +
                "       @destination(url='tcp://127.0.0.1:9884/pattern/CardStreamS'))) \n" +
                "@sink(type='tcp', sync='true', @map(type='binary'), " +
                "   @distribution(strategy='roundRobin', " +
                "       @destination(url='tcp://127.0.0.1:9881/pattern/CardStreamS')," +
                "       @destination(url='tcp://127.0.0.1:9882/pattern/CardStreamS'))) \n" +
                "define stream CardStreamS (cardId string, amount float, location string);" +
                "" +
                "@sink(type='tcp', sync='true', @map(type='binary'), " +
                "   @distribution(strategy='roundRobin', " +
                "       @destination(url='tcp://127.0.0.1:9885/pattern/CardStreamL'))) \n" +
                "define stream CardStreamL (cardId string, amount float, location string);" +
                "" +
                "from CardStream[amount < 100]" +
                "insert into CardStreamS;" +
                "" +
                "from CardStream[amount > 100]" +
                "insert into CardStreamL;" +
                "";

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("CardStream");

        //Start SiddhiApp runtime
        siddhiAppRuntime.start();

        long eventsToPublish = 100000000;
//        long eventsToPublish = 2;
        //Sending events to Siddhi
        Random random = new Random();
        for (int i = 0; i < eventsToPublish; i++) {
            inputHandler.send(new Object[]{"1234", random.nextInt(110)*1.0f, "SL"});
        }

//        inputHandler.send(new Object[]{"1234", 5f, "SL"});
//        inputHandler.send(new Object[]{"1234", 105f, "SL"});

        System.out.println("published");
        Thread.sleep(1000000);

        siddhiAppRuntime.shutdown();
    }
}
