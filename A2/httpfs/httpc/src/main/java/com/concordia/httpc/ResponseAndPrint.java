package com.concordia.httpc;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResponseAndPrint {
     Httpc httpc = new Httpc();

     //determine if it is a no connection request
    public boolean isNoConnection(String[] args){
        for(String element: args){
            if(element.equalsIgnoreCase("help")||element.equalsIgnoreCase("command"))
                return true;
        }
        return false;
    }
    // handle no connection
    public void handleNoConnection(String[] args) {
        System.out.println();
        System.out.println("Hpptc info: ");
        System.out.println();
        if (args[0].equals("help")) {
            if (args.length == 1)
                System.out.println("httpc is a curl-like application but supports HTTP protocol only.");
            else if (args[1].equalsIgnoreCase("get")) {
                System.out.println("usage: httpc get [-v] [-h key:value] URL");
                System.out.println();
                System.out.println("Get executes a HTTP GET request for a given URL.");
                System.out.println("\t-v Prints the detail of the response such as protocol, status,");
                System.out.println("and headers.");
                System.out.println("\t-h key:value Associates headers to HTTP Request with the format");
                System.out.println("'key:value'.");
            }
            else if (args[1].equalsIgnoreCase("post")){
                System.out.println("usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL");
                System.out.println();
                System.out.println("Post executes a HTTP POST request for a given URL with inline data or from");
                System.out.println("file.");
                System.out.println();
                System.out.println("\t-v Prints the detail of the response such as protocol, status,");
                System.out.println("and headers.");
                System.out.println("\t-h key:value Associates headers to HTTP Request with the format");
                System.out.println("'key:value'.");
                System.out.println("\t-d stringAssociates an inline data to the body HTTP POST request.");
                System.out.println("\t-f fileAssociates the content of a file to the body HTTP POST");
                System.out.println("request");
                System.out.println();
                System.out.println("Either [-d] or [-f] can be used but not both.");
            }
        } else if (args.length == 2 && args[0].equals("command")) {
            if (args[1].equalsIgnoreCase("get"))
                System.out.println("get executes a HTTP GET request and prints the response.");
            else if (args[1].equalsIgnoreCase("post"))
                System.out.println("post executes a HTTP POST request and prints the response.");
            else if (args[1].equalsIgnoreCase("help"))
                System.out.println("help prints this screen.");
            else {
                System.out.println("this is a invalid command, please type help +[command] only");
            }
        }
        else{
            System.out.println("Your input is: ");
            for(String element: args){
                System.out.print(element +" ");
            }
            System.out.println();
            System.out.println("Your inputs don't meet the format, please try again");
        }
    }
    //---------------------------------Handle no verbose connection------------------------------------------
       //a connection without verbose
       public void handleNoVerboseConnection(String[]args){
            httpc.getConnection(args);
            httpc.pickBody();
           System.out.println("NoVerbose");
           System.out.println(httpc.getBody());
       }
       // determine if is a noVerbose connection
       public boolean isNoVerboseConnection(String[]args){
         for(String element: args){
             if(element.equalsIgnoreCase("-v")||element.equalsIgnoreCase("-h")||
                element.equalsIgnoreCase("-d")||element.equalsIgnoreCase("-f")) return false;
         }
         return true;
       }
       //**********************************************-v -h connection*************************

       //*********handle V connection*************
       //is a VConnection or not
      public boolean isGetVConnection(String[]args){
          for(String element: args){
            if(element.equalsIgnoreCase("-v"))  return true;
          }
          return false;
      }
      //is a HConnection or not
      public boolean isGetHConnection(String[]args){
          for(String element: args){
              if(element.equalsIgnoreCase("-h"))  return true;
          }
          return false;
      }
      //handle VConnection
      public void handleVConnection(String[]args){
          System.out.println();
          System.out.println();
          httpc.getConnection(args);
          System.out.println(httpc.getResponse());
      }

      //handle HConnection
     public void handleHConnection(String[]args){
         System.out.println();
         httpc.setHeaders(pickHAddToHeadersPart(args));
         //httpc.getConnection(args);
        // System.out.println(httpc.getRequest());
     }
     //a helper function to add request headers
     public List<String> pickHAddToHeadersPart(String[]args){
        List<String> list = new ArrayList<>();
        int start = -1;
        for(int i = 0; i < args.length - 1; i++){
            if(args[i].equals("-h")) start = i;
            if(start != -1 && i > start){
                if(isKVHeader(args[i]))
                    list.add(args[i]);
                else break;
            }
        }
        return list;
     }
     //determine a string is a k:v header element or not
     public boolean isKVHeader(String str){
        char[] chars = str.toCharArray();
        Set<Character> set = new HashSet<>();
        for(char c: chars)
            set.add(c);
        if(!set.contains(':')) return false;
        if(chars.length < 4 ) return false;
        return true;
     }
     //determine is a -d command
    public boolean isDAddBody(String[] args,String dataType){
        for(String element: args){
            if(element.equalsIgnoreCase(dataType)) return true;
        }
        return false;
    }
    //
    public void handleDAddBody(String[]args){
        System.out.println();
       // httpc.getConnection(args);
        String addBody = pickDAddBodyPart(args,"-d");
        httpc.setRequestBody(addBody);//-------------------
        httpc.getConnection(args);
        addInfoToBody(addBody,args);
       // System.out.println(httpc.getBody());
       // System.out.println("*********");
       // System.out.println(httpc.getRequest());

    }
    // a method to add extra info to body
    public void addInfoToBody(String dataToBody,String[]args){
        httpc.pickBody();
        String body = httpc.getBody();
        String[] strs = body.split(",");
        for(int i = 0; i < strs.length; i++){
            if(strContainsSub(strs[i],"data") != -1) strs[i] = "\r\n"+"  "+"\"data\": "+dataToBody;
            else if(strContainsSub(strs[i],"Content-Length") != -1) strs[i] = "\r\n"+"  "+"\"Content-Length\" : "+"\""+dataToBody.length()+"\"";
            else if(strContainsSub(strs[i],"json") != -1) strs[i] = "\r\n"+"  "+"\"json\" : "+dataToBody;
        }

        String newBody ="";
        for(int i = 0; i < strs.length; i++){
            if(i != strs.length - 1) newBody = newBody + strs[i]+",\n\r";
            else newBody += strs[i];
        }
        httpc.setBody(newBody);
        httpc.setRequestBody(newBody);//------------------------------
    }
    //get the extra data
    public String pickDAddBodyPart(String[]args,String dataType){
        int start = -1;
        for(int i = 0; i < args.length - 1; i++){
            if(args[i].equals(dataType)){
                start = i + 1;
                break;
            }
        }
        if(dataType == "-f") return args[start];
        return (start == -1 || start >= args.length) ? null : args[start] +" "+ args[start + 1];
    }
    //check if a string contains a substring
    public int strContainsSub(String str,String sub){
        if(str == null || sub == null) return -1;
        if(str.length() < sub.length()) return -1;
        for(int i = 0; i < str.length() - sub.length();i++){
            if(str.substring(i,i+sub.length()).equalsIgnoreCase(sub)) return i + sub.length();
        }
        return -1;
    }
    //get the file contents and add them to body
    public String getFileContent(String filePath){
        String solution ="";
        try {
            BufferedReader in = new BufferedReader(new FileReader(filePath));
            String str ="";

            while ((str = in.readLine()) != null) {
                solution = solution + str+",";
            }
            //  System.out.println(str);
        } catch (IOException e) {
            System.out.println("The file could not be found !!");
        }
        solution = solution.substring(0,solution.length() - 1);
        return solution;
    }
    //Deal file data and and to body
      public void handleFAddToBody(String[]args){
         // httpc.getConnection(args);
          String filePath = pickDAddBodyPart(args,"-f");
          String addBody = getFileContent(filePath);
          httpc.setRequestBody(addBody);//------------------------------------
          httpc.getConnection(args);
          addInfoToBody(addBody,args);
         // System.out.println(httpc.getBody());
         // System.out.println("*********");
         // System.out.println(httpc.getRequest());
      }

      // parse the request and execute the request
    public void parse(String[] args){
        if(isNoConnection(args)) handleNoConnection(args);
        else if(isNoVerboseConnection(args)) handleNoVerboseConnection(args);
        if(isGetHConnection(args)) handleHConnection(args);
        if(isGetVConnection(args)) handleVConnection(args);
        if(isDAddBody(args,"-d")) handleDAddBody(args);
        else if (isDAddBody(args,"-f")) handleFAddToBody(args);
    }

}
