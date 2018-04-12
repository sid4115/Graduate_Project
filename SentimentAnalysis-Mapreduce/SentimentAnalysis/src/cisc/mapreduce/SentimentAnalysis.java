package cisc.mapreduce;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class SentimentAnalysis {
	
	/* Traverses through the input data set, extract word 
	and compare against Positive, Negative dictionary 
	and predict the sentiment of the statement */
	
	public static class TokenizerMapper
	extends Mapper<Object, Text, Text, IntWritable>{

		private URI[] files;
		// Hash maps to store the positive and negative words 
		private HashMap<String,Integer> positiveWords = new HashMap<String,Integer>();
		private HashMap<String,Integer> negativeWords = new HashMap<String,Integer>();

		public void setup(Context context) throws IOException {

			files = context.getCacheFiles();

			Path path = new Path(files[0]);
			Path path1 = new Path(files[1]);
			FileSystem fs =FileSystem.get(context.getConfiguration());

			/*Positive word list*/
			FSDataInputStream inOne = fs.open(path);
			BufferedReader brOne = new BufferedReader(new InputStreamReader(inOne));
			String lineOne = "";
			Integer posVal = 1;  //Assigning a value of +1 to positive words
			while((lineOne = brOne.readLine()) != null){
				positiveWords.put(lineOne, posVal);
			}
			brOne.close();
			inOne.close();

			/*Negative word list*/
			FSDataInputStream inTwo = fs.open(path1);
			BufferedReader brTwo = new BufferedReader(new InputStreamReader(inTwo));
			String lineTwo = "";
			Integer negVal = -1;  //Assigning a value of -1 to negative words
			while((lineTwo = brTwo.readLine()) != null){
				negativeWords.put(lineTwo, negVal);
			}
			brTwo.close();
			inTwo.close();
		}


		public void map(Object key, Text value, Context context
				) throws IOException, InterruptedException {

			int sentimentCount = 0;
			String[] splitLine = value.toString().split("\t+");
			String prodId = splitLine[1]; // To extract the Product ID
			String reviewBody = splitLine[7]; // To extract the Review
			String[] splitBody = reviewBody.split(" ");

			for(String word:splitBody){
				if(positiveWords.containsKey(word)){
					sentimentCount+=positiveWords.get(word);
				}else if(negativeWords.containsKey(word)){
					sentimentCount+=negativeWords.get(word);
				}
			}
			if(sentimentCount >= 1){
				sentimentCount = 1;
				context.write(new Text(prodId),new IntWritable(sentimentCount));
			}else if(sentimentCount <= -1){
				sentimentCount = -1;
				context.write(new Text(prodId),new IntWritable(sentimentCount));
			}else if(sentimentCount == 0){
				context.write(new Text(prodId),new IntWritable(sentimentCount));
			}

		}
	}

	public static class IntSumReducer
	extends Reducer<Text,IntWritable,Text,Text> {
		private Text result = new Text();

		public void reduce(Text key, Iterable<IntWritable> values,
				Context context
				) throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			if(sum >= 1){
				result.set("Positive Emotions");
			}else if(sum < 0){
				result.set("Negative Emotions");
			}else if(sum == 0){
				result.set("Neutral Emotions");
			}
			context.write(key, result);
		}
	}

	public static void main(String[] args) throws Exception {

		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "Sentiment Analysis");
		job.addCacheFile(new Path(args[0]).toUri());
		job.addCacheFile(new Path(args[1]).toUri());
		job.setJarByClass(SentimentAnalysis.class);
		job.setMapperClass(TokenizerMapper.class);
		job.setReducerClass(IntSumReducer.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(args[2]));
		FileOutputFormat.setOutputPath(job, new Path(args[3]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);

	}

}
