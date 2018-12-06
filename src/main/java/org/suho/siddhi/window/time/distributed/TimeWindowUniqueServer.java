package org.suho.siddhi.window.time.distributed;

import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.util.config.InMemoryConfigManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Standalone window
 */
public class TimeWindowUniqueServer {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("Program Arguments:");
        for (String arg : args) {
            System.out.println("\t" + arg);
        }
        Thread.sleep(3000);

        String consume = "9873";
        String publish = "127.0.0.1:9895";
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
                "@app:name('time-window')\n" +
                "@app:statistics(reporter = 'console', interval = '5' ) \n" +
                "\n" +
                "@source(type='tcp', @map(type='binary')) \n" +
                "define stream PartialAggregateStockStream (symbol string, totalPrice double, totalVolume long, countVolume long, id string, ts long);\n" +
                "\n" +
                "@sink(type='tcp', url='tcp://" + publish + "/consumer/AggregateStockStream', sync='true', @map(type='binary')) \n" +
                "define stream AggregateStockStream (symbol string, totalPrice double, avgVolume double, ts long);\n" +
                "                \n" +
                "@info(name = 'query1') \n" +
                "from PartialAggregateStockStream##window.unique:ever(id,symbol) \n" +
                "select symbol, sum(totalPrice) as totalPrice, sum(totalVolume)*1.0/sum(countVolume) as avgVolume , ts \n" +
                "group by symbol \n" +
                "insert into AggregateStockStream ;\n";

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
