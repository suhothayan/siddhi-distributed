package org.suho.siddhi.window.length.distributed;

import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.util.config.InMemoryConfigManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Standalone window
 */
public class LengthWindowServer1 {

    public static void main(String[] args) throws InterruptedException {
        String consume = "9881";
        String publish = "127.0.0.1:9893";
        String data = "5000";
        if (args.length != 0) {
            if (args.length == 3) {
                consume = args[0];
                publish = args[1];
                data = args[2];
            } else {
                throw new Error("More " + args.length + " arguments found expecting 4.");
            }
        }

        String siddhiApp = "" +
                "@app:name('length-window')\n" +
                "@app:statistics(reporter = 'console', interval = '5' ) \n" +
                "\n" +
                "@source(type='tcp', @map(type='binary')) \n" +
                "define stream StockEventStream (symbol string, price float, volume long, seqNo long);\n" +
                "\n" +
//                "@sink(type='log') " +
                "@sink(type='tcp', url='tcp://" + publish + "/length-window/PartialAggregateStockStream', sync='true', @map(type='binary')) \n" +
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

        Thread.sleep(1000000000);

        //Shutdown SiddhiApp runtime
        siddhiAppRuntime.shutdown();

        //Shutdown Siddhi
        siddhiManager.shutdown();
    }
}
