package com.ak.sensitive_tagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.json.JSONObject;

import scala.collection.Iterator;
import scala.collection.JavaConverters;

import com.ak.sensitive_tagger.entity.Rule;
import com.ak.sensitive_tagger.utils.KafkaLoader;
import com.ak.sensitive_tagger.utils.RuleParser;

public class App {
	public static void main(String[] args) throws IOException {
		

		if(args.length<=3){
			System.err.println("Usage : rulefile,csvfile,kafkabroker,topicname");
			System.exit(0);
		}
		
		String ruleFileName = args[0];
		String csvFileName = args[1];
		String kafkaConfig=args[2];
		String kafkaTopicName=args[3];
		
		
		
		/* SparkSession spark = SparkSession
			      .builder().master("local[*]")
			      .appName("Stagger")
			      .getOrCreate();
			      */
		 

		 SparkSession spark = SparkSession
			      .builder()
			      .appName("Stagger")
			      .getOrCreate();
		
		 
		Dataset<Row> ds = spark.read().format("csv").option("header","false").option("mode", "DROPMALFORMED").load(csvFileName);
		
		
		ds.createOrReplaceTempView("datasetTable");
		
		
		Map<String, List<String>> ruleMap = getRules(spark,ruleFileName);
		
		
		List<String> columnIdentifier = new ArrayList<String>();
		String query = "";
		
		String schema="";
		
		for (StructField f : ds.schema().fields()) {
			schema = schema+f.name()+" "+f.dataType().typeName()+", ";
			for (String s : ruleMap.keySet()) {
				columnIdentifier.add(s + "_COL_"+ f.name());
				query = query + "sum(" + s + "(" + f.name() + "))/count("
						+ f.name() + ") as " + s +"_COL_"+ f.name() + ",";
			}
		}

		query = query.substring(0, query.lastIndexOf(","));
		query = ("select " + query + " from datasetTable");
		
		schema = schema.substring(0, schema.lastIndexOf(","));
		
		for (String ruleKey : ruleMap.keySet()) {
			
			spark.udf().register(ruleKey, getUdf(ruleMap.get(ruleKey)),
					DataTypes.IntegerType);
			
			
		}

		
		Row[] rows1 =  (Row[]) spark.sql(query).collect();
		
		Row r = rows1[0];
		scala.collection.immutable.Map<String, Object> returnMap = r
				.getValuesMap(JavaConverters
						.asScalaIteratorConverter(columnIdentifier.iterator()).asScala()
						.toSeq());
		Iterator<String> it = returnMap.keySet().iterator();
		Set<String>tagSet = new HashSet<String>();
		JSONObject jObj1 = new JSONObject();
		JSONObject inference = new JSONObject();
		inference.put("file", csvFileName);
		List<JSONObject> jList = new ArrayList<JSONObject>();
		while (it.hasNext()) {
			String key = it.next();
			if (Double.valueOf("" + returnMap.get(key).get()) >= 0.5) {
				jObj1 = new JSONObject();
				String tag = key.split("_COL_")[0];
				String columnName = key.split("_COL_")[1];
				Double score = Double.valueOf("" + returnMap.get(key).get()) * 100;
				tagSet.add(tag);
				jObj1.put("tag", tag);
				jObj1.put("column_id", columnName);
				jObj1.put("score", score);
				jList.add(jObj1);
			}
		}
	inference.put("tags", tagSet);
	inference.put("columns", jList);
	inference.put("schema", schema);
	
	KafkaLoader.writeMessageToKafka(kafkaConfig, kafkaTopicName,inference.toString());
	System.out.println("Done");
	
	spark.close();
	
	}

	private static UDF1<Object, Integer> getUdf(final List<String> patterns) {
		return new UDF1<Object, Integer>() {
			private static final long serialVersionUID = 1L;
			public Integer call(Object inStringObj) throws Exception {
				for(String pattern : patterns){
					if(inStringObj.toString().replaceAll(pattern, "").equalsIgnoreCase("")){
						return 1;
					}
				}
				return 0;
			}
		};
	}

	
	private static Map<String, List<String>> getRules(SparkSession spark,String ruleFileName) {
		Map<String, List<String>> ruleMap = new HashMap<String, List<String>>();
		for(String ruleLine : spark.read().textFile(ruleFileName).collectAsList() ){
			Rule rule = RuleParser.getRuleFromJson(ruleLine);
			ruleMap.put(rule.getRuleName(), rule.getPatterns());
		}
		return ruleMap;
	}
}
