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
import org.wso2.siddhi.core.util.config.InMemoryConfigManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Standalone window
 */
public class PatternServer3 {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("Program Arguments:");
        for (String arg : args) {
            System.out.println("\t" + arg);
        }
        Thread.sleep(5000);

        String consume = "9883";
        String publish = "127.0.0.1:9885,127.0.0.1:9886";
        String data1 = "1 min";
        String data2 = "60000";
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
            destinationList.add("@destination(url='tcp://" + url.trim() + "/pattern/PossibleFraudStream2')");
        }
        String destinations1 = String.join(",", destinationList);


        String siddhiApp = "" +
                "@app:name('pattern')\n" +
                "@app:statistics(reporter = 'console', interval = '5' ) \n" +
                "\n" +
                "@source(type='tcp', @map(type='binary')) \n" +
                "define stream CardStreamS (cardId string, amount float, location string,ts long);\n" +
                "\n" +
                "@source(type='tcp', @map(type='binary')) \n" +
                "define stream PossibleFraudStream1 (cardId string, location string, timestamp long,ts long);\n" +
                "\n" +
                "@sink(type='tcp', sync='true', @map(type='binary'), " +
                "   @distribution(strategy='roundRobin', " +
                "       " + destinations1 + ")) \n" +
                "define stream PossibleFraudStream2 (cardId string, timestamp long,ts long);\n" +
                "\n" +
                "" +
                "from CardStreamS " +
                "select cardId, location, ts as timestamp, ts " +
                "insert into CardStream;" +
                "" +
                "from PossibleFraudStream1 " +
                "select cardId, location, timestamp, ts  " +
                "insert into CardStream;" +
                "" +
                "@info(name = 'query1') \n" +
                "from every a=CardStream ->  b = CardStream[a.cardId==cardId and ifThenElse(a.location == 'A', location == 'B' and ((currentTimeMillis() - a.timestamp) < " + data2 + "), " +
                "                                         location == 'A' and ((currentTimeMillis() - timestamp) < " + data2 + "))  ]\n" +
                "    within " + data1 + "\n" +
                "select a.cardId as cardId, a.timestamp ,a.ts \n" +
                "insert into PossibleFraudStream2;\n" +
                "";

        SiddhiManager siddhiManager = new SiddhiManager();
        Map<String, String> executionConfig = new HashMap<>();
        executionConfig.put("source.tcp.port", consume);
        siddhiManager.setConfigManager(new InMemoryConfigManager(executionConfig, null));
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        //Start SiddhiApp runtime
        siddhiAppRuntime.start();

        Thread.sleep(1000000000);

        //Shutdown SiddhiApp runtime
        siddhiAppRuntime.shutdown();

        //Shutdown Siddhi
        siddhiManager.shutdown();
    }
}
