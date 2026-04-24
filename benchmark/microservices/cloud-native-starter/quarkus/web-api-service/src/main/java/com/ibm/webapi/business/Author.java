package com.ibm.webapi.business;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Author {
	public String name;
	public String twitter;
	public String blog;

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getTwitter() { return twitter; }
	public void setTwitter(String twitter) { this.twitter = twitter; }
	public String getBlog() { return blog; }
	public void setBlog(String blog) { this.blog = blog; }
}
