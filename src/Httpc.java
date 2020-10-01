import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

/**
 * @author Khang Zheng Ng
 * Concordia Comp445-F19_LA1
 */
public class Httpc {
	
	private String methodType = null;
	private Boolean verbosity = false;
	private Map <String, String> headerArgs = new <String, String> HashMap();
	private String headerPair = null;
	private String[] headerKey = null;
	private Map <String, String> inlineData = new <String, String> HashMap();
	private String inlinePair = null;
	private String[] inlineKey = null;
	private String dOrFValue = null;
	private File file = null;
	private File outputFile = null;
	private String URL = null;
	
	public static void main(String[] args) throws Exception {
		
	    if (args.length == 0) {
	    	System.out.println("Use \"Httpc help\" for more information about commands.");
	    	System.exit(0);
	    }
	    
	    ArrayList<String> optsList = new ArrayList<String>();
	    ArrayList<String> doubleOptsList = new ArrayList<String>();

	    for (int i = 0; i < args.length; i++) {
	    	if (args[i].charAt(0) == '-') {
		        switch (args[i].charAt(0)) {
		        case '-':
		        	if (args[i].charAt(1) == 'v') {
		        		optsList.add(args[i]);
		        	}
		        	else if (args[i].charAt(1) == 'h' || args[i].charAt(1) == 'd' || args[i].charAt(1) == 'f' || args[i].charAt(1) == 'o') {		
		        		doubleOptsList.add(args[i].concat(" " + args[i+1]));
		        		i++;
		        	}
		        	else {
		        		throw new IllegalArgumentException("Not valid argument: " + args[i]);
		        	}
		            break;
		        }
	    	}
	        else if (args[i].contentEquals("get"))
	        		optsList.add(args[i].toLowerCase());
	        else if (args[i].contentEquals("post"))
	        		optsList.add(args[i].toLowerCase());
	    	else if (args[i].contentEquals("help")) {
	    		if (args.length > i+1 && args.length < i+3) {
		    		if (args[i+1].contentEquals("get")) {
		    			System.out.println("usage: httpc get [-v] [-h key:value] URL\r\n" + 
		    					"Get executes a HTTP GET request for a given URL.\r\n" + 
		    					"\t-v \t\t= Prints the detail of the response such as protocol, status, and headers.\r\n" + 
		    					"\t-h key:value \t= Associates headers to HTTP Request with the format 'key:value'.\r\n");
		    			System.exit(0);
		    		} else if (args[i+1].contentEquals("post")) {
		    			System.out.println("usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL\r\n" + 
	    					"Post executes a HTTP POST request for a given URL with inline data or from file.\r\n" + 
	    					"\t-v \t\t= Prints the detail of the response such as protocol, status, and headers.\r\n" + 
	    					"\t-h key:value \t= Associates headers to HTTP Request with the format 'key:value'.\r\n" + 
	    					"\t-d string \t= Associates an inline data to the body HTTP POST request.\r\n" + 
	    					"\t-f file \t= Associates the content of a file to the body HTTP POST request.\r\n" + 
	    					"Either [-d] or [-f] can be used but not both.\r\n");
		    			System.exit(0);
		    		} 
	    		}
	    		else {
	    			System.out.println("httpc is a curl-like application but supports HTTP protocol only.\r\n" + 
    					"Usage:\r\n" + 
    					"\thttpc command [arguments]\r\n" + 
    					"The commands are:\r\n" + 
    					"\tget: executes a HTTP GET request and prints the response.\r\n" + 
    					"\tpost: executes a HTTP POST request and prints the response.\r\n" + 
    					"\thelp: prints this screen.\r\n" + 
    					"Use \"httpc help [command]\" for more information about a command.");
	    			System.exit(0);
	    		}
	        }
	    	else {
	    		//url
	    		optsList.add(args[i]);
	    	}
        }
	    
	    Httpc http = new Httpc();
	    
	    for(String obj:optsList) {
	    	if (obj.contentEquals("-v"))
	    		http.verbosity = true;
	    	else if (obj.contentEquals("get")) {
	    		if (http.methodType == null) {
	    			http.methodType = "get";
	    		} else {
	    			System.out.println("Error! Can't have both GET and POST methods!");
	    			System.exit(0);
	    		}
	    	} else if (obj.contentEquals("post")) {
	    		if (http.methodType == null) {
	    			http.methodType = "post";
	    		} else {
	    			System.out.println("Error! Can't have both GET and POST methods!");
	    			System.exit(0);
	    		}
	    	} else
	    		http.URL = obj.toString();
	    }
	    
	    if (http.URL == null) {
	    	System.out.println("Error! Missing URL!");
	    	System.exit(0);
	    }
	    
	    if (http.methodType == null) {
			System.out.println("Error! Missing GET or POST method!");
			System.exit(0);
	    }
	    
	    for (String obj:doubleOptsList) {
	    	if (obj.startsWith("-h")) {
	    		http.headerPair = obj.substring(3);
	    		http.headerPair = http.headerPair.replaceAll("[\\s+]", "");
	    		http.headerKey = http.headerPair.split(":");
	    		http.headerArgs.put(http.headerKey[0], String.valueOf(http.headerKey[1]));
	    	} else if (obj.startsWith("-d")) {
	    		if (http.methodType == "get") {
	    			System.out.println("get option should not used with the options -d or -f.");
	    			System.exit(0);	    			
	    		}
	    		if (http.dOrFValue == "f") {
	    			System.out.println("post should have either -d or -f but not both");
	    			System.exit(0);
	    		}
	    		http.inlinePair = obj.substring(3);
	    		http.inlinePair = http.inlinePair.replaceAll("['\\{\\}\"\\s+]", "");
	    		http.inlineKey = http.inlinePair.split(":");
	    		http.inlineData.put(http.inlineKey[0], String.valueOf(http.inlineKey[1]));
	    		http.dOrFValue = "d";
	    	} else if (obj.startsWith("-f")) {
	    		if (http.methodType == "get") {
	    			System.out.println("get option should not used with the options -d or -f.");
	    			System.exit(0);	    			
	    		}
	    		if (http.file != null) {
	    			System.out.println("Only 1 text file should be given.");
	    			System.exit(0);
	    		}
	    		if (http.dOrFValue == "d") {
	    			System.out.println("post should have either -d or -f but not both");
	    			System.exit(0);
	    		}
	    		http.file = new File(obj.substring(3));
	    		http.dOrFValue = "f";
	    	} else if (obj.startsWith("-o")) {
	    		http.outputFile = new File(obj.substring(3));
	    	}
	    }
	    
	    if (http.methodType.equals("get")) {
	    	http.sendGet(http.URL, http.verbosity, http.headerArgs, http.outputFile);
	    } else if (http.methodType.equals("post")) {
	    	if (http.inlineData.size() > 0) {
	    		http.sendPostInLine(http.URL, http.verbosity, http.headerArgs, http.inlineData, http.outputFile);
	    	} else if (http.file != null) {
	    		http.sendPostFile(http.URL, http.verbosity, http.headerArgs, http.file);
	    	}
	    }
	    
	}
	
	private void sendGet(String url, Boolean verbosity, Map<String, String> headerArgs, File outputFile) throws Exception {	
		System.out.println("Output: ");
		
		URL newUrl = new URL(url);
		HttpURLConnection connection = (HttpURLConnection) newUrl.openConnection();
		connection.setRequestMethod("GET");
		
		for (String key : headerArgs.keySet())
        {
        	connection.setRequestProperty(key, headerArgs.get(key));
        }
		// Check verbosity, and output
		if (verbosity) {
			doVerbose(connection);
		}
		
		// enable redirect
		connection.setInstanceFollowRedirects(true); 
		HttpURLConnection.setFollowRedirects(true);
		doRedirect(connection, verbosity, outputFile);
		
		doOutput(connection, url, connection.getResponseCode(), outputFile);
	}
	
	private void sendPostInLine(String url, Boolean verbosity, Map<String, String> headerArgs, Map<String, String> inlineData, File outputFile) throws Exception {
		System.out.println("Output: ");
		URL obj = new URL(url);
		
		HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
		connection.setRequestMethod("POST");
			
		for( String key : headerArgs.keySet()) {
			connection.setRequestProperty(key, headerArgs.get(key));
        }		
		
		String urlKey = "";
		StringJoiner joiner = new StringJoiner("&");
		
		for( String key : inlineData.keySet() )
        {
			connection.setRequestProperty(key, inlineData.get(key));
        	urlKey = key + "=" + inlineData.get(key); //This create key1=value1
        	joiner.add(urlKey);
        }
		
		urlKey = joiner.toString();
			
		connection.setDoOutput(true);
			
		ByteArrayInputStream inputStream = new ByteArrayInputStream (urlKey.getBytes("UTF-8"));
		copy(inputStream, connection.getOutputStream());
		inputStream.close();
		
		if (verbosity) {
			doVerbose(connection);
		}
		doOutput(connection, url, connection.getResponseCode(), outputFile);
		
		// Allow redirect
		connection.setInstanceFollowRedirects(true); 
		HttpURLConnection.setFollowRedirects(true);
	}
	
	private void doVerbose(HttpURLConnection connection) {
			Map<String, List<String>> headers = connection.getHeaderFields();
			Set<Map.Entry<String, List<String>>> entrySet = headers.entrySet();
			
			System.out.println("Verbosity on.\n");
					
			for (Map.Entry<String, List<String>> entry : entrySet) 
			{
				String headerName = entry.getKey();
				List<String> headerValues = entry.getValue();
				
		        for (String value : headerValues)
		        {
		        	System.out.println(headerName + " : " + value);
		        }
			}
			
			System.out.println();
	}	

	private void sendPostFile(String url, Boolean verbosity, Map<String, String> header, File file) {
		try 
		{
			Map<String, String> inlineData = new HashMap<String, String>();
			
	        BufferedReader in = new BufferedReader(new FileReader(file));
	        String fileContent = null;
	        
	        while ((fileContent = in.readLine()) != null) 
	        {
	        	// format of Key=Value and \n (next line)
	        	try {
	        		String dataPairs[] = fileContent.split("=");
	        		inlineData.put(dataPairs[0], dataPairs[1]);
	        	} catch (Exception e)
	        	{
	        		System.out.println("The file is either empty or not having the proper data format of Key=Value and \\n (new line).");
	        		System.exit(0);
	        	}
	        }
	        in.close();
			        
	        // do send post with the data read from the file if the file is NOT empty
	        if (inlineData != null) {
	        	System.out.println("Sending a Post request with the inline-data read from the file...");
				sendPostInLine(url, verbosity, header, inlineData, outputFile);
	        } 
		} catch (Exception e) 
		{
			System.err.println("Error! File does not exit.");
			e.printStackTrace();
		}	
	}
	
	private void doRedirect(HttpURLConnection connection, Boolean verbosity, File outputFile) throws Exception {
		boolean isRedirect = false;
		int status = connection.getResponseCode();
		
		if (status != HttpURLConnection.HTTP_OK) 
		{
			// connect response code with 3XX
			if (Integer.parseInt(Integer.toString(status).substring(0, 1)) == 3)
			{
				isRedirect = true;		
				System.out.println("Redirecting to new url...");
			}
		}
		
		if (isRedirect == true) 
		{
			String newUrl = connection.getHeaderField("Location");
			
			System.out.println("The new URL is now " + newUrl);
			
			Map<String, List<String>> headers = connection.getHeaderFields();
			Set<Map.Entry<String, List<String>>> entrySets = headers.entrySet();
			
			// get request after redirecting
			sendGet(newUrl, verbosity, headerArgs, outputFile);
		}
	
	}
	
	private void doOutput(HttpURLConnection connection, String url, int responseCode, File outputFile) {
					
		if (responseCode >= 400) {
			if(outputFile != null)
			{
				System.out.println("Trying to output to file " + outputFile);
				OutputStream outputStream;
				try {
					outputStream = new FileOutputStream(outputFile);
					copy(connection.getErrorStream(), outputStream);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println("Output done!");
			}
			else
			{
				try {
					copy(connection.getErrorStream(), System.out);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			if(outputFile != null) {
				System.out.println("Trying to output to file " + outputFile);
				OutputStream outputStream;
				try {
					outputStream = new FileOutputStream(outputFile);
					copy(connection.getInputStream(), outputStream);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println("Output done!");
			} else {
				try {
					copy	(connection.getInputStream(), System.out);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}	
	}
	
	// Attempts to synchronize on the input and output streams to
	// disallow other threads to read from the input 
	// or write to the output 
	// while copying is taking place"
	// ref: http://www.cafeaulait.org/questions/06031999.html
	public static void copy(InputStream in, OutputStream out) throws IOException {
	    synchronized (in) 
	    {
	    	synchronized (out) 
	        {
	    		byte[] buffer = new byte[256];
	    		int bytesRead = 0;
	            
	    		while (true) 
	            {
	            	bytesRead = in.read(buffer);
	            	if (bytesRead == -1)
	            		break;
	            	out.write(buffer, 0, bytesRead);     	
	            }
	    		out.close();
	        }
	    }
	 } 	  
}
