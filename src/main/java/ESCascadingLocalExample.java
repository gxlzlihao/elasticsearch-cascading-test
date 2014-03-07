import cascading.flow.FlowConnector;
import cascading.pipe.Pipe;

import cascading.tap.Tap;
import cascading.tap.local.FileTap;
import cascading.tuple.Fields;
import cascading.property.AppProps;
import cascading.flow.FlowDef;
import cascading.flow.Flow;
import org.elasticsearch.hadoop.cascading.EsTap;

import cascading.operation.aggregator.Count;
import cascading.operation.regex.RegexFilter;
import cascading.operation.regex.RegexSplitGenerator;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;

import java.util.Properties;

// Remove in HDFS mode
import cascading.flow.local.LocalFlowConnector;
import cascading.scheme.local.TextDelimited;

public class ESCascadingLocalExample {

    public static void main( String[] args ) {

        // Elastic Search configuration
        Properties properties = new Properties();
        //properties.setProperty("es.mapping.id", "docid");
        properties.setProperty("es.write.operation","create");
        properties.setProperty("es.index.auto.create", "true");
        AppProps.setApplicationJarClass(properties, ESCascadingLocalExample.class);

        FlowConnector flow = new LocalFlowConnector(properties); // new

        Tap elasticIn = new EsTap("localhost", 9200, "wc/input", "", new Fields("docid", "text"));
        Tap elasticOut = new EsTap("localhost", 9200, "wc/output", "", Fields.ALL);

        // specify a regex operation to split the "document" text lines into a token stream
        Fields token = new Fields( "token" );
        Fields text = new Fields( "text" );
        RegexSplitGenerator splitter = new RegexSplitGenerator( token, "[ \\[\\]\\(\\),.]" );
        // only returns "token"
        Pipe docPipe = new Each( "token", text, splitter, Fields.RESULTS );

        // determine the word counts
        Pipe wcPipe = new Pipe( "wc", docPipe );
        wcPipe = new GroupBy( wcPipe, token );
        wcPipe = new Every( wcPipe, Fields.ALL, new Count(), Fields.ALL );

        // connect the taps, pipes, etc., into a flow
        FlowDef flowDef = FlowDef.flowDef()
         .setName( "wc" )
         .addSource( docPipe, elasticIn )
         .addTailSink( wcPipe, elasticOut );

        // write a DOT file and run the flow
        Flow wcFlow = flow.connect( flowDef );
        wcFlow.writeDOT( "dot/wc.dot" );
        wcFlow.complete();

    }

}
