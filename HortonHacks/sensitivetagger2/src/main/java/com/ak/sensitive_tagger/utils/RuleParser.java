package com.ak.sensitive_tagger.utils;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ak.sensitive_tagger.entity.Rule;



public class RuleParser {
	
	
	public static Rule getRuleFromJson(String jsonString){
		Rule rule = new Rule();
		List<String> patternList = new ArrayList<String>();
		JSONObject jsonObject = new JSONObject(jsonString);
		rule.setRuleName(jsonObject.getString("label"));
		JSONArray patternsArray = jsonObject.getJSONArray("patterns");
		for(int index =0 ;index<patternsArray.length();index++){
			patternList.add(String.valueOf(patternsArray.get(index)));
		}
		rule.setPatterns(patternList);
		return rule;
	}

}
