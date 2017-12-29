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
public class JoinServer2 {

    public static void main(String[] args) throws InterruptedException {

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
                "@sink(type='tcp', url='tcp://127.0.0.1:9885/join/PartialOutputStream', sync='true', @map(type='binary')) \n" +
                "define stream PartialOutputStream (sumPriceA double, countEvents long, sumPriceB double, id string);\n" +
                "                \n" +
                "@info(name = 'query1') \n" +
                "from StreamA#window.externalTime(seqNo, 10000) join \n" +
                "   StreamB#window.externalTime(seqNo, 10000) \n" +
                "   on StreamA.symbol == StreamB.symbol\n" +
                "select sum(StreamA.price) as sumPriceA, count() as countEvents, \n" +
                "   sum(StreamB.price) as sumPriceB, '2' as id  \n" +
                "insert into PartialOutputStream;\n" +
                "";

        SiddhiManager siddhiManager = new SiddhiManager();
        Map<String, String> executionConfig = new HashMap<>();
        executionConfig.put("source.tcp.port", "9882");
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
