import cascading.flow.FlowConnector;
import cascading.pipe.Pipe;

import cascading.tap.Tap;
import cascading.tap.local.FileTap;
import cascading.tuple.Fields;
import org.elasticsearch.hadoop.cascading.EsTap;

import java.util.Properties;

// Remove in HDFS mode
import cascading.flow.local.LocalFlowConnector;
import cascading.scheme.local.TextDelimited;

// and use following ones
//import cascading.flow.hadoop.HadoopFlowConnector;  // Instead of LocalFlowConnector
//import cascading.scheme.hadoop.TextDelimited;      // Instead of local.TextDelimited
//import cascading.tap.hadoop.Lfs;                   // Instead of FileTap

public class ESCascadingLocalExample {

    public static void main( String[] args ) {

        Fields schema = new Fields("productID", "customerID", "quantity");
        Tap in = new FileTap(new TextDelimited(schema, false, "," ), "src/test/resources/products.tsv");
        //Tap out = new FileTap(new TextDelimited(), "output" );

        // Elastic Search configuration
        Properties properties = new Properties();
        properties.setProperty("es.mapping.id", "productID");
        properties.setProperty("es.write.operation","create");

        Tap elasticOut = new EsTap("localhost", 9200, "radio/artists", "", new Fields("productID", "customerID", "quantity"));

        FlowConnector flow = new LocalFlowConnector(properties); // new
        flow.connect(in, elasticOut, new Pipe("write-to-ES")).complete();

    }

}
