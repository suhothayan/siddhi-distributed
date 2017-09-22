package org.suho.siddhi.window.length;

import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;

/**
 * Standalone length
 */
public class LengthWindowServer {

    public static void main(String[] args) throws InterruptedException {


        String siddhiApp = "" +
                "@app:name('time-window')\n" +
                "@app:statistics(reporter = 'console', interval = '5' ) \n" +
                "\n" +
                "@source(type='tcp', @map(type='binary')) \n" +
                "define stream StockEventStream (symbol string, price float, volume long);\n" +
                "\n" +
                "@sink(type='tcp', url='tcp://127.0.0.1:9895/consumer/AggregateStockStream', sync='true', @map(type='binary')) \n" +
                "define stream AggregateStockStream (symbol string, totalPrice double, avgVolume double);\n" +
                "                \n" +
                "@info(name = 'query1') \n" +
                "from StockEventStream#window.length(100000)  \n" +
                "select symbol, sum(price) as totalPrice, avg(volume) as avgVolume \n" +
                "group by symbol \n" +
                "insert into AggregateStockStream ;\n";

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