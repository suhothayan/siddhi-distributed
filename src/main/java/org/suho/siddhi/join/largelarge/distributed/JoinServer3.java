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
import org.wso2.siddhi.core.util.config.InMemoryConfigManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Standalone window
 */
public class JoinServer3 {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Program Arguments:");
        for (String arg : args) {
            System.out.println("\t" + arg);
        }

        String consume = "9883";
        String publish = "127.0.0.1:9885";
        String data1 = "10000,10000";
        String data2 = "3";
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

        String[] windowSizes = data1.split(",");

        String siddhiApp = "" +
                "@app:name('join')\n" +
                "@app:statistics(reporter = 'console', interval = '5' ) \n" +
                "\n" +
                "@source(type='tcp', @map(type='binary')) \n" +
                "define stream StreamA (symbol string, price float, volume long, seqNo long);\n" +
                "\n" +
                "@source(type='tcp', @map(type='binary')) \n" +
                "define stream StreamB (symbol string, price float, volume long, seqNo long);\n" +
                "\n" +
                "@sink(type='tcp', url='tcp://" + publish + "/join/PartialOutputStream', sync='true', @map(type='binary')) \n" +
                "define stream PartialOutputStream (sumPriceA double, countEvents long, sumPriceB double, id string);\n" +
                "                \n" +
                "@info(name = 'query1') \n" +
                "from StreamA#window.externalTime(seqNo, " + windowSizes[0] + ") join \n" +
                "   StreamB#window.externalTime(seqNo, " + windowSizes[1] + ") \n" +
                "   on StreamA.symbol == StreamB.symbol\n" +
                "select sum(StreamA.price) as sumPriceA, count() as countEvents, \n" +
                "   sum(StreamB.price) as sumPriceB, '" + data2 + "' as id  \n" +
                "insert into PartialOutputStream;\n" +
                "";

        SiddhiManager siddhiManager = new SiddhiManager();
        Map<String, String> executionConfig = new HashMap<>();
        executionConfig.put("source.tcp.port", consume);
        siddhiManager.setConfigManager(new InMemoryConfigManager(executionConfig, null));
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        //Start SiddhiApp runtime
        siddhiAppRuntime.start();

        Thread.sleep(1000000);

        //Shutdown SiddhiApp runtime
        siddhiAppRuntime.shutdown();

        //Shutdown Siddhi
        siddhiManager.shutdown();
    }
}
