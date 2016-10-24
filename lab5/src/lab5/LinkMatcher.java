package lab5;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/** A class for finding links in an html file.
 *  Modified by Prof. Karpenko from the assignment of Prof. Engle. 
 */
public class LinkMatcher {

	// This regex should match an HTML anchor tag such as <a  href  = "http://cs.www.usfca.edu"  >"
	// where the actual hyperlink is captured in a group.
	// See the following link regarding the format of the anchor tag: https://developer.mozilla.org/en-US/docs/Web/HTML/Element/a
	public static final String REGEX = "(?i)<a\\s+[^>]*?(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))"; // FILL IN!  
	
	/**
	 * Take an html file and return a list of hyperlinks in that html 
	 * that satisfy the following requirements:
	 * 1. The list should not contain duplicates. 
	 * For the purpose of this assignment, duplicates are the links that are the same, except for the fragment. 
	 * Example: you should consider these two links as equal"
	 * "java/lang/StringBuffer.html#StringBuffer" and "java/lang/StringBuffer.html#StringBuffer-java.lang.String"
	 * (because they are the same if you remove the fragment).
	 * 
	 * 2. Do not include links that take you to the same page (links that start with the fragment).

	 * You are required to use a regular expression to find links 
	 * (use variable REGEX defined on top of the class - fill in the actual pattern).
	 * You are required to use classes Pattern and Matcher in this method. 
	 * Do not use any other classes or packages (except String, ArrayList, Pattern,  Matcher, BufferedReader etc.)
	 * 
	 * @param filename
	 *            The name of the HTML file.
	 * @return An ArrayList of links
	 */
	public static List<String> findLinks(String filename) {
		List<String> links = new ArrayList<>();
		String hrefRegex = "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";
		Pattern patternRegex = Pattern.compile(REGEX);
		Pattern patternHref = Pattern.compile(hrefRegex);
		Matcher matcherRegex;
		Matcher matcherHref;
		String line;
		String linkWithHref;
		String linkWithoutHref;
		
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(Paths.get(filename).toAbsolutePath().toString()));
			while ((line = bufferedReader.readLine()) != null){
				//System.out.println(line);
				matcherRegex = patternRegex.matcher(line);
				while (matcherRegex.find()){
					linkWithHref = matcherRegex.group();
					//System.out.println(linkWithHref);
					matcherHref = patternHref.matcher(linkWithHref);
					while(matcherHref.find()){
						linkWithoutHref = matcherHref.group().trim().replaceAll(" ", "");
						linkWithoutHref = linkWithoutHref.substring(6, linkWithoutHref.length()-1);
						//System.out.println(linkWithoutHref);
						//remove the fragment
						if(!linkWithoutHref.startsWith("#") && linkWithoutHref.contains("#")){
							linkWithoutHref = linkWithoutHref.substring(0, linkWithoutHref.indexOf("#"));
						}
						if(!links.contains(linkWithoutHref) && !linkWithoutHref.startsWith("#") 
								&& !linkWithoutHref.isEmpty() && !linkWithoutHref.equals("/")){
							links.add(linkWithoutHref);
							//System.out.println(linkWithoutHref);
						}
					}
				}
			}
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return links;
	}
	
	
	/**
	 * Take a URL, fetch an html page at this URL (using sockets),  and find all unique hyperlinks on that webpage. 
	 * The list should not contain "duplicates" (see the previous comment)
	 * or links that take you to the same page.
	 * The difference with the previous method is that it should fetch the HTML from the server first.
	 * @param url
	 * @return An ArrayList of links
	 */
	public static List<String> fetchAndFindLinks(String url) {
		List<String> links = new ArrayList<>();
		URL urlUrl;
		Socket socket = null;
		PrintWriter printWriter = null;
		BufferedReader bufferedReader= null;
		String htmlResult;
		String hrefRegex = "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";
		Pattern patternRegex = Pattern.compile(REGEX);
		Pattern patternHref = Pattern.compile(hrefRegex);
		Matcher matcherRegex;
		Matcher matcherHref;
		String linkWithHref;
		String linkWithoutHref;
		
		try {
			urlUrl = new URL(url);
			socket = new Socket(urlUrl.getHost(), 80);
			
			// output stream
			printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			String request = "GET " + urlUrl.getPath() + "?" + urlUrl.getQuery() + " HTTP/1.1" + System.lineSeparator()
									+ "Host: " + urlUrl.getHost() + System.lineSeparator()
									+ "Connection: close" + System.lineSeparator()
									+ System.lineSeparator();
			//System.out.println("Request: " + request);
			printWriter.println(request);
			printWriter.flush();
			
			// input stream
			bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String line;
			StringBuffer sb = new StringBuffer();
			while ((line = bufferedReader.readLine()) != null) {
				//System.out.println(line);
				sb.append(line);
			}
			htmlResult = sb.toString();
			
			//find the links
			matcherRegex = patternRegex.matcher(htmlResult);
			while (matcherRegex.find()){
				linkWithHref = matcherRegex.group();
				//System.out.println(linkWithHref);
				matcherHref = patternHref.matcher(linkWithHref);
				while(matcherHref.find()){
					linkWithoutHref = matcherHref.group().trim().replaceAll(" ", "");
					linkWithoutHref = linkWithoutHref.substring(6, linkWithoutHref.length()-1);
					//System.out.println(linkWithoutHref);
					//remove the fragment
					if(!linkWithoutHref.startsWith("#") && linkWithoutHref.contains("#")){
						linkWithoutHref = linkWithoutHref.substring(0, linkWithoutHref.indexOf("#"));
					}
					if(!links.contains(linkWithoutHref) && !linkWithoutHref.startsWith("#") 
							&& !linkWithoutHref.isEmpty()&& !linkWithoutHref.equals("/")){
						links.add(linkWithoutHref);
						//System.out.println(linkWithoutHref);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				// close the streams and the socket
				printWriter.close();
				bufferedReader.close();
				socket.close();
			} catch (IOException e) {
				System.out.println("An exception occured while trying to close the streams or the socket: " + e);
			}
		}		
		return links;
	}
	
	public static void main(String[] args) {
		//Testing
		findLinks("TestHTML.html");
		fetchAndFindLinks("http://tutoringcenter.cs.usfca.edu/resources/");
	}

}
