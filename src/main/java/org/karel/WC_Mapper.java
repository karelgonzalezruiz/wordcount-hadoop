package org.karel;

import java.io.IOException;
import java.util.Locale;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

public class WC_Mapper extends MapReduceBase
        implements Mapper<LongWritable, Text, Text, IntWritable> {

    private static final IntWritable ONE = new IntWritable(1);
    private final Text word = new Text();

    @Override
    public void map(LongWritable key, Text value,
                    OutputCollector<Text, IntWritable> output,
                    Reporter reporter) throws IOException {

        String line = value.toString().toLowerCase(Locale.ROOT);

        String[] tokens = line.split("[^\\p{L}\\p{N}]+");

        for (String token : tokens) {
            if (!token.isEmpty()) {
                word.set(token);
                output.collect(word, ONE);
            }
        }
    }
}