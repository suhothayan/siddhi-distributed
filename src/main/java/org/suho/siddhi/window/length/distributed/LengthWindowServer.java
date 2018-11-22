/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.suho.siddhi.window.length.distributed;

import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.util.config.InMemoryConfigManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Standalone window
 */
public class LengthWindowServer {

    public static void main(String[] args) throws InterruptedException {

        Thread.sleep(3000);

        String consume = "9881";
        String publish = "127.0.0.1:9895";
        String data = "100000";
        if (args.length != 0) {
            if (args.length == 3) {
                consume = args[0];
                publish = args[1];
                data = args[2];
            } else {
                throw new Error("More " + args.length + " arguments found expecting 2.");
            }
        }

        String siddhiApp = "" +
                "@app:name('length-window')\n" +
                "@app:statistics(reporter = 'console', interval = '5' ) \n" +
                "\n" +
                "@source(type='tcp', @map(type='binary')) \n" +
                "define stream StockEventStream (symbol string, price float, volume long, seqNo long);\n" +
                "\n" +
                "@sink(type='tcp', url='tcp://" + publish + "/consumer/AggregateStockStream', sync='true', @map(type='binary')) \n" +
                "define stream PartialAggregateStockStream (symbol string, totalPrice double, totalVolume long, countVolume long, id string);\n" +
                "\n" +
                "@info(name = 'query1') \n" +
                "from StockEventStream#window.externalTime(seqNo, " + data + ")  \n" +
                "select symbol, sum(price) as totalPrice, sum(volume) as totalVolume, count(volume) as countVolume, '1' as id \n" +
                "group by symbol \n" +
                "insert into PartialAggregateStockStream ;\n";

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
