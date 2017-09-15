package org.suho.siddhi.window.distributed;

import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.util.config.InMemoryConfigManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Standalone window
 */
public class TimeWindowServer1 {

    public static void main(String[] args) throws InterruptedException {


        String siddhiApp = "" +
                "@app:name('time-window')\n" +
                "\n" +
                "@source(type='tcp', @map(type='binary')) \n" +
                "define stream StockEventStream (symbol string, price float, volume long);\n" +
                "\n" +
                "@sink(type='tcp', url='tcp://127.0.0.1:9883/time-window/PartialAggregateStockStream', sync='true', @map(type='binary')) \n" +
                "define stream PartialAggregateStockStream (symbol string, totalPrice double, totalVolume long, countVolume long, id string);\n" +
                "\n" +
                "@info(name = 'query1') \n" +
                "from StockEventStream#window.time(15 sec)  \n" +
                "select symbol, sum(price) as totalPrice, sum(volume) as totalVolume, count(volume) as countVolume, '1' as id \n" +
                "group by symbol \n" +
                "insert into PartialAggregateStockStream ;\n";

        SiddhiManager siddhiManager = new SiddhiManager();
        Map<String, String> executionConfig = new HashMap<>();
        executionConfig.put("source.tcp.port", "9881");
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
