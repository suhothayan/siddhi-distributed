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

package org.suho.siddhi.join.smalllarge;

import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;

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
                "@sink(type='tcp', url='tcp://" + publish + "/join/StreamA', sync='true', @map(type='binary')) \n" +
                "define stream StreamA (symbol string, price float, volume long);\n" +
                "\n" +
                "@sink(type='tcp', url='tcp://" + publish + "/join/StreamB', sync='true', @map(type='binary')) \n" +
                "define stream StreamB (symbol string, price float, volume long);\n" +
                "";

        SiddhiManager siddhiManager = new SiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        InputHandler inputHandlerA = siddhiAppRuntime.getInputHandler("StreamA");
        InputHandler inputHandlerB = siddhiAppRuntime.getInputHandler("StreamB");

        //Start SiddhiApp runtime
        siddhiAppRuntime.start();

        long eventsToPublish = 100000000;
//        long eventsToPublish = 2;
        //Sending events to Siddhi
        for (int i = 0; i < eventsToPublish; i++) {
            inputHandlerA.send(new Object[]{"IBM", 100f, 100L});
            inputHandlerB.send(new Object[]{"IBM", 100f, 100L});
        }
        System.out.println("published");
        Thread.sleep(1000000000);

        siddhiAppRuntime.shutdown();
    }
}
