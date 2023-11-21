import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer; 
import org.apache.xmlrpc.webserver.ServletWebServer;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import java.net.MalformedURLException;
import javax.servlet.*;
import java.util.*;
import java.io.*;
import java.net.InetAddress;
import java.net.URL;

import java.net.NetworkInterface;
import java.util.Enumeration;


public class Drone {

  //private static XmlRpcClient DroneClient;
  
  private static DroneServlet droneServe;
  private static XmlRpcServer xmlRpcServer;
  private static WebServer server;
	
  private static boolean debug; 

  private static final int PORT = 2054;
  private static final int COL_SIZE = 5;

  private static String successor;
  
  /*
   *Colony Table
   *Index 0: 2^0 nodes around ring
   *Index 1: 2^1 nodes around ring
   *Index 2: 2^2 nodes around ring
   *Index 3: 2^3 nodes around ring
   *Index 4: 2^4 nodes around ring
   */
   private static String[] colonyTable = new String[COL_SIZE];

   public Drone(){
    
    // default
   }
  
   public boolean ping() {
    System.out.println("Pinged!");
    return true;
   }

   private String getNextLiveSuccessor(int colonyTableIndex, XmlRpcClient succClient, XmlRpcClientConfigImpl configSucc) {
    // Checks if machine at specified colony table index is available
    // RETURNS IP address if available, and null if unavailable

    String potentialSuccessor = colonyTable[colonyTableIndex];
    if (potentialSuccessor != null && !potentialSuccessor.equals(successor)) {
     try {
      configSucc.setServerURL(new URL("http://" + potentialSuccessor + ":" + PORT));
                succClient.execute("Drone.ping", new Object[]{});
                // If ping is successful, update successor
                successor = potentialSuccessor;
                return successor;
      } catch (Exception ex) {
       return null;
   }
}
return null;
   }

   public String getSuccessor() {
    System.out.println("I made it!");
    XmlRpcClient succClient = new XmlRpcClient();
    XmlRpcClientConfigImpl configSucc = new XmlRpcClientConfigImpl();
    configSucc.setEnabledForExtensions(true);
    succClient.setConfig(configSucc);
    try {
     configSucc.setServerURL(new URL("http://" + successor + ":" + PORT));
	 } catch(MalformedURLException e) {
	  System.out.println("Invalid successor URL.");
	 } 
    
    try {
     succClient.execute("Drone.ping", new Object[]{});

      //If the Successor is online
      String tempSuccessor = successor;
	    successor = droneServe.getClientIpAddress();
	    return tempSuccessor;	
    } catch(Exception e) {
      System.out.println("Did not get a response from successor ping.");
    }
    String tempSuccessor = successor;
    tempSuccessor = getNextLiveSuccessor(1, succClient, configSucc);
    // If successor is not online, try second entry in colonyTable(2^1 nodes around ring)
    if (tempSuccessor == null) {
     
     // second node around ring is not reachable.  Now try third entry in colonyTable(2^2 nodes around ring)
     tempSuccessor = getNextLiveSuccessor(2, succClient, configSucc);
     if (tempSuccessor == null) {
      System.out.println("New bootstrap node needed; ring must be reset.  Exiting.");
      System.exit(1);
     }
   }
   return tempSuccessor;
    //TODO
      //check that successor is valid
      //if valid
        //send successor
        //assign requestor as successor
      //else check
      //if next successor is valid
       //send
       //assign requestor as successor
      //else
       //try index 2 on colony, cutting 1 out of chain
       //if this addr is invalid, exit and request new BS node
	}

     public String getColonyMember(int index) {
             //returns member of colony table at index
        return colonyTable[index];
     }


  private static boolean initializeNetwork(){
     //Load successor and colony table with own IP addr
     
     String IP = getPrivateIP();
     successor = IP;
     System.out.println(IP);
     for(int i = 0; i <  colonyTable.length; i++){
        colonyTable[i]= IP;
     }
     try{
      PropertyHandlerMapping phm = new PropertyHandlerMapping();
      WebServer server  = new WebServer(PORT); // may have to change port
      xmlRpcServer = server.getXmlRpcServer();
      XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
      serverConfig.setEnabledForExtensions(true);
      phm.addHandler("Drone", Drone.class);
      xmlRpcServer.setHandlerMapping(phm);
      server.start();

    } catch (Exception e) {
      // Handle any exceptions during server setup
      System.err.println("Server Initialization Exception: " + e.toString());
      e.printStackTrace();
      System.exit(1);
    }

     return false;
  }


  private static String getPublicIP(){
     String urlString = "http://checkip.amazonaws.com/";
     try {
	URL url = new URL(urlString);
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
	return br.readLine();
     }catch(Exception e){
        System.out.print("Unable to contact IP server");
	return null;
     }
}
  public static String getPrivateIP() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress ia = inetAddresses.nextElement();
                    if (!ia.isLoopbackAddress() && ia.isSiteLocalAddress()) {
                        return ia.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting private IP: " + e.getMessage());
        }
        return null;
    }

  

  private static boolean joinNetwork(String bootstrap_IP){
     //Start server
     //Connect to specefied peer or bootstrap
     //Set successor
     //Generate colony table
    //Connect to BS peer
    try{
      XmlRpcClient bootstrapClient = new XmlRpcClient();
      XmlRpcClientConfigImpl configBootstrap = new XmlRpcClientConfigImpl();
      configBootstrap.setEnabledForExtensions(true);
      bootstrapClient.setConfig(configBootstrap);
      configBootstrap.setServerURL(new URL("http://" + bootstrap_IP + ":" + PORT));
      //not sure how xml rpc calls work without params
      successor = (String) bootstrapClient.execute("Drone.getSuccessor", new Object[]{});
      colonyTable[0] = successor;      
    } catch (Exception e) {
      if(debug){
         System.err.println("Bootstrap Client exception: " + e);
      }else{
	      System.out.println("Client Initalization Error");
      }
      System.exit(1);
    }
    //build finger table
    //ask successor for their succesor (Index 0)
    //ask Index 0 for their 2 successor (Index 1)
    if(bootstrap_IP == null){
       initializeNetwork();
    }
    else{
	    updateColony();
    }

    //Start up xml server. 
    try{
      //Setup for server. Still needs to be fixed. 
      //Don't know how the servlet architecture maps on XML Rpc
      //But must be used to get the client IP
      //See DroneServlet.java
      //FIXME
      PropertyHandlerMapping phm = new PropertyHandlerMapping();
      droneServe = new DroneServlet();
      ServletWebServer server  = new ServletWebServer(droneServe, PORT); // may have to change port
      xmlRpcServer = server.getXmlRpcServer();
      XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
      serverConfig.setEnabledForExtensions(true);
      phm.addHandler("Drone", Drone.class);
      xmlRpcServer.setHandlerMapping(phm);
      server.start();

    } catch (Exception e) {
      // Handle any exceptions during server setup
      System.err.println("Server Initialization Exception: " + e);
      System.exit(1);
    }
             
    return true;
  }







  private static void updateColony(){
     try{
     XmlRpcClient updateClient = new XmlRpcClient();
     XmlRpcClientConfigImpl configUpdate = new XmlRpcClientConfigImpl();
     configUpdate.setEnabledForExtensions(true);
     updateClient.setConfig(configUpdate);
     for(int i = 0; i < colonyTable.length; i++){
       configUpdate.setServerURL(new URL("http://" + colonyTable[i] + ":" + PORT));

       try{
          colonyTable[i+1] = (String) updateClient.execute("Drone.getColonyMember", new Object[]{i});
       } catch (Exception e){
          //Error or dead node
          //Try to contact nodes after

          //Acquisition of next node fails
          //If its not the successor
          if(i>0){
             System.out.println("Error: Unable to Find Colony for Node " + i + "\n Attempt 1 Failed");
             //Ask successor to determine node after dead node
             configUpdate.setServerURL(new URL("http://" + colonyTable[0] + ":" + PORT));
             try{
                colonyTable[i+1] = colonyTable[i+1] = (String) updateClient.execute("Drone.getColonyMember", new Object[]{i});
             } catch (Exception f){
                System.out.println("Attempt 2 Failed");
                if(debug){
                      System.out.print("Debug:\n"+f.toString());
                }
                //If this fails, ask successor's successor for the second dead node     
                configUpdate.setServerURL(new URL("http://" + colonyTable[1] + ":" + PORT));
                try{
                   colonyTable[i+1] = colonyTable[i+1] = (String) updateClient.execute("Drone.getColonyMember", new Object[]{i});
                } catch(Exception g){
                   //If all fails, terminate or could jump to next valid node is succesor's colony (can add later)
                   System.out.println("Attempt 3 Failed. Aborting... \n Unable to build colony. Try initializing connection with another seed/peer");
                   if(debug){
                      System.out.print("Debug:\n"+g.toString());
                   }
                   System.exit(0);
                }
             }
          }
          else{
             //i.e. it is the successor that is invalid
             //could cut the third node out because we actually cannot contact it


	  }    
       }
 
  }} catch(MalformedURLException e){
	  System.out.println("Malformed URL");
  }
  }

  private static void dumpColony(){
     int nodeNumber = 1;
     for(int i = 0; i< colonyTable.length; i++){
	System.out.println("Node " + nodeNumber  + ":" + colonyTable[i]);
        nodeNumber*=2;
     }
  }

  public static void main(String[] args) {
    /*Scanner usrIn = new Scanner(System.in);
    System.out.println("Enter Bootstrap IP");
    String boot_ip = usrIn.nextLine();

    initializeConnection(args[1]);
    dumpColony();
    */
    debug = true;
    
    System.out.println(getPublicIP());
    
    initializeNetwork();
    dumpColony();

  }

}
