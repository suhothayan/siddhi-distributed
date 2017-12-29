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

/**
 * Standalone length
 */
public class PatternServer {

    public static void main(String[] args) throws InterruptedException {


        String siddhiApp = "" +
                "@app:name('pattern')\n" +
                "@app:statistics(reporter = 'console', interval = '5' ) \n" +
                "\n" +
                "@source(type='tcp', @map(type='binary')) \n" +
                "define stream CardStream (cardId string, amount float, location string);\n" +
                "\n" +
                "@sink(type='tcp', url='tcp://127.0.0.1:9895/consumer/PossibleFraudStream', sync='true', @map(type='binary')) \n" +
                "define stream PossibleFraudStream (initialPurchaseAmount float, lastPurchaseAmount float, location string);\n" +
                "                \n" +
                "@info(name = 'query1') \n" +
                "from every a = CardStream[amount < 100]\n" +
                "    -> b = CardStream[amount < 100]\n" +
                "    -> c = CardStream[amount > 10000]\n" +
                "    within 1 min\n" +
                "select a.amount as initialPurchaseAmount, \n" +
                "   c.amount as lastPurchaseAmount, c.location as location\n" +
                "insert into PossibleFraudStream;\n";

        SiddhiManager siddhiManager = new SiddhiManager();
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
