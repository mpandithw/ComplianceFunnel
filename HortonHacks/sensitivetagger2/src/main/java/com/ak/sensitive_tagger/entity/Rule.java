package com.ak.sensitive_tagger.entity;

import java.util.List;

public class Rule {
	
	public String getRuleName() {
		return ruleName;
	}
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}
	public List<String> getPatterns() {
		return patterns;
	}
	public void setPatterns(List<String> patterns) {
		this.patterns = patterns;
	}
	private String ruleName;
	private List<String> patterns;

}
