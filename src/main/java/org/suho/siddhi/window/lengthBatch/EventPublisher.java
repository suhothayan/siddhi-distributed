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

package org.suho.siddhi.window.lengthBatch;

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
                "@sink(type='tcp', url='tcp://127.0.0.1:9892/length-window/StockEventStream', sync='true', @map(type='binary')) \n" +
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
