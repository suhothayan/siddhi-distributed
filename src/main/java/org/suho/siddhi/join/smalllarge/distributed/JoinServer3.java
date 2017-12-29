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

package org.suho.siddhi.join.smalllarge.distributed;

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


        String siddhiApp = "" +
                "@app:name('join')\n" +
                "@app:statistics(reporter = 'console', interval = '5' ) \n" +
                "\n" +
                "@source(type='tcp', @map(type='binary')) \n" +
                "define stream PartialOutputStream (sumPriceA double, countEvents long, sumPriceB double, id string);\n" +
                "\n" +
                "@sink(type='tcp', url='tcp://127.0.0.1:9895/consumer/OutputStream', sync='true', @map(type='binary')) \n" +
                "define stream OutputStream (sumPrice double, countEvents long, avgPrice double);\n" +
                "                \n" +
                "@info(name = 'query1') \n" +
                "from PartialOutputStream#window.unique:ever(id) \n" +
                "select sum(sumPriceA) as sumPrice, sum(countEvents) as countEvents, sum(sumPriceB)*1.0/sum(countEvents) as avgPrice \n" +
                "insert into OutputStream ;\n";

        SiddhiManager siddhiManager = new SiddhiManager();
        Map<String, String> executionConfig = new HashMap<>();
        executionConfig.put("source.tcp.port", "9883");
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
