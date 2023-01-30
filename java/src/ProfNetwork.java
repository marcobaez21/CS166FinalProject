/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 * By:
 * Cody Steimle (862137374)
 * Marco Baez (862040802)
 *
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

import java.sql.Timestamp;

//public static int MSGID = 27811;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class ProfNetwork {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Messenger
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public ProfNetwork (String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");
	 //static int MSGID = 27811;
         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end ProfNetwork

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
	 if(outputHeader){
	    for(int i = 1; i <= numCol; i++){
		System.out.print(rsmd.getColumnName(i) + "\t");
	    }
	    System.out.println();
	    outputHeader = false;
	 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
          List<String> record = new ArrayList<String>();
         for (int i=1; i<=numCol; ++i)
            record.add(rs.getString (i));
         result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       if(rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            ProfNetwork.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      ProfNetwork esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Messenger object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new ProfNetwork (dbname, dbport, user, "");
	
         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.print("\n");
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. Goto Friend List");
                System.out.println("2. Update Profile");
                System.out.println("3. Messages");
		        System.out.println("4. Requests");
		        System.out.println("5. Search People");
                System.out.println("6. Change Password");
                System.out.println(".........................");
                System.out.println("9. Log out");
                System.out.print("\n");
                switch (readChoice()){
                    case 1: FriendList(esql, authorisedUser); break;
                    case 2: UpdateProfile(esql,authorisedUser); break;
		            case 3: MessagesSubMenu(esql, authorisedUser); break;
                    case 4: RequestsSubMenu(esql, authorisedUser); break;
		            case 5: SearchPeople(esql,authorisedUser); break;
                    case 6: ChangePassword(esql, authorisedUser); break;
                    case 9: usermenu = false; break;
                    default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "        Welcome To Cody and Marco's Messenger              \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    * An empty block and contact list would be generated and associated with a user
    **/
   public static void CreateUser(ProfNetwork esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         //String password = in.readLine();
         char passwordArray[] = System.console().readPassword("");
	 String password = String.valueOf(passwordArray);
         System.out.print("\tEnter user email: ");
         String email = in.readLine();

	 //Creating empty contact\block lists for a user
//	 String query = String.format("INSERT INTO USR (userId, password, email, contact_list) VALUES ('%s','%s','%s')", login, password, email);
	 String query = String.format("INSERT INTO USR(userID, password, email) VALUES ('%s', '%s', '%s')", login, password, email);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end

   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(ProfNetwork esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         //String password = in.readLine();
	 char passwordArray[] = System.console().readPassword(""); 
	 String password = String.valueOf(passwordArray);

         String query = String.format("SELECT * FROM USR WHERE userId = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end



// Rest of the functions definition go in here

public static void MessagesSubMenu(ProfNetwork esql, String authorisedUser){
    try{
        boolean messagesubmenu = true;
        while(messagesubmenu) {
            System.out.print("\n");
            System.out.println("--------------Messages--------------");
            System.out.println("1. View Recieved Messages");
            System.out.println("2. View Sent Messages");
            System.out.println(".........................");
            System.out.println("9. Back");
            System.out.print("\n");
            switch (readChoice()){
                case 1: ViewMessages(esql, authorisedUser); break;
                case 2: ViewSentMessages(esql, authorisedUser); break;
                case 9: messagesubmenu = false; break;
                default : System.out.println("Unrecognized choice!"); break;
            }
        }
        System.out.print("\n");
     }catch(Exception e){
        System.err.println (e.getMessage ());
     }
}

public static void RequestsSubMenu(ProfNetwork esql, String authorisedUser){
    try{
        boolean requestsubmenu = true;
        while(requestsubmenu) {
            System.out.print("\n");
            System.out.println("--------------Requests--------------");
            System.out.println("1. View Incoming Requests");
            System.out.println("2. View Outgoing Requests");
            System.out.println(".........................");
            System.out.println("9. Back");
            System.out.print("\n");
            switch (readChoice()){
                case 1: ViewIncomingRequests(esql, authorisedUser); break;
                case 2: ViewOutgoingRequests(esql, authorisedUser); break;
                case 9: requestsubmenu = false; break;
                default : System.out.println("Unrecognized choice!"); break;
            }
        }
        System.out.print("\n");
     }catch(Exception e){
        System.err.println (e.getMessage ());
     }
}



public static void FriendList(ProfNetwork esql, String authorisedUser){
    try{
        Integer currlvl = 0;
        boolean freindmenu = true;
        while(freindmenu) {
            ViewFriends(esql,authorisedUser);

            System.out.println("1. View Profile");
            System.out.println(".........................");
            System.out.println("9. Back");
            System.out.print("\n");
            switch (readChoice()){
            case 1: SelectProfile(esql,authorisedUser, currlvl); break;
            case 9: freindmenu = false; break;
            default : System.out.println("Unrecognized choice!"); break;
            }
        }
     }catch(Exception e){
        System.err.println (e.getMessage ());
     }
}

public static void SelectProfile(ProfNetwork esql, String authorisedUser, Integer currlvl){
    try{
        System.out.print("\tEnter user id to view profile: ");
        String viewUser = in.readLine();

        //Freind check
        if(FriendCheck(esql, authorisedUser, viewUser)){
            currlvl = 0;
            ViewProfile(esql, viewUser, authorisedUser, currlvl);
        }
        else {
            currlvl ++;
            ViewProfile(esql, viewUser, authorisedUser, currlvl);
        }


     }catch(Exception e){
        System.err.println (e.getMessage ());
     }
}


public static boolean FriendCheck(ProfNetwork esql, String user1, String user2){
    try{
        String query = String.format("SELECT * FROM CONNECTION_USR C WHERE C.status = 'Accept' AND ((C.userId = '%s' AND C.connectionId ='%s') OR (C.connectionId = '%s' AND C.userId ='%s'))", user1,user2, user1,user2);

        //Freind check
        if(esql.executeQuery(query)>0){
            return true;
        }
        else return false;

    }catch(Exception e){
        System.err.println (e.getMessage ());
        return false;
    }
}


public static void ViewProfile(ProfNetwork esql, String viewUser, String authorisedUser, Integer currlvl){
    try{
        boolean profilemenu = true;
        while(profilemenu) {
            System.out.println("\n");
            System.out.println("--------------"+viewUser+"'s Profile--------------");
            String query = String.format("SELECT U.userId, U.email, U.name, U.dateofBirth FROM USR U WHERE U.userId = '%s'", viewUser);
            esql.executeQueryAndPrintResult(query);
    
            ViewFriends(esql,viewUser);

            //System.out.println("Current friend level: " + (currlvl+1));

            System.out.println("1. Send Friend Request");
            System.out.println("2. Send Message");
            System.out.println("3. View a Friend of "+viewUser+"'s profile");
            System.out.println(".........................");
            System.out.println("9. Back");
            System.out.print("\n");
            switch (readChoice()){
            case 1: SendRequest(esql, authorisedUser, viewUser, currlvl); break;
            case 2: SendMessage(esql, authorisedUser, viewUser); break;
            case 3: SelectProfile(esql,authorisedUser, currlvl); break;
            case 9: profilemenu = false; break;
            default : System.out.println("Unrecognized choice!"); break;
            }
            System.out.println("\n");
        }

    }catch(Exception e){
        System.err.println (e.getMessage ());
    }
}


public static void ViewFriends(ProfNetwork esql, String thisUser){
    try{
        System.out.println("\n");
        System.out.println("--------------"+thisUser+"'s Friends--------------");
        String query = String.format("SELECT U.userId, U.email, U.name, U.dateofBirth FROM USR U, (SELECT C1.connectionId FROM CONNECTION_USR C1 WHERE C1.status = 'Accept' AND C1.userId = '%s' UNION SELECT C2.userId FROM CONNECTION_USR C2 WHERE C2.status = 'Accept' AND C2.connectionId = '%s') AS FRIEND WHERE U.userId = FRIEND.connectionId", thisUser,thisUser);
        esql.executeQueryAndPrintResult(query);
        System.out.println("\n");
    }catch(Exception e){
        System.err.println (e.getMessage ());
    }
}


public static void UpdateProfile(ProfNetwork esql, String authorisedUser){
    try{
        System.out.print("\n");
        System.out.print("\tEnter your full name: ");
        String name = in.readLine();
        System.out.print("\tEnter your date of birth: ");
        String dob = in.readLine();

        String query = String.format("UPDATE USR SET name = '%s', dateofBirth= '%s' WHERE userId = '%s'", name, dob, authorisedUser);

        esql.executeUpdate(query);
        System.out.println ("User account successfully updated!");
        System.out.print("\n");

     }catch(Exception e){
        System.err.println (e.getMessage ());
     }
}


public static void SendMessage(ProfNetwork esql, String authorisedUser, String viewUser){
	try{
        System.out.print("\n");
		System.out.print("Enter Message: ");
		String message = in.readLine();
		long now = System.currentTimeMillis();
		Timestamp currtime = new Timestamp(now);
		String stat = "Sent";
        String values =  String.format("'%s','%s','%s','%s','%d','%s'", authorisedUser, viewUser, message, currtime, 0, stat);
		String query = "INSERT INTO MESSAGE(senderId, recieverId, contents, sendTime, deleteStatus, status) VALUES ("+values+")";
		esql.executeUpdate(query);
		System.out.print("Message Sent!");
        System.out.print("\n");
	}catch(Exception e){
		System.err.println (e.getMessage ());
	}
}


public static void ViewMessages(ProfNetwork esql, String authorisedUser){
    try{
        boolean msgmenu = true;
        while(msgmenu) {
            System.out.print("\n");
            String query = String.format("SELECT msgId, senderId, contents, sendTime FROM MESSAGE WHERE recieverId = '%s' AND (deleteStatus = 0 OR deleteStatus = 1)", authorisedUser);
            esql.executeQueryAndPrintResult(query);

            System.out.print("\nWould you like to delete an messages?");
            System.out.print("\n1. Delete Message");
            System.out.println(".........................");
            System.out.println("9. Back");
            System.out.print("\n");
            switch(readChoice()){
                case 1: DeleteMessage(esql, authorisedUser); break;
                case 9: msgmenu = false; break;
                default: System.out.print("Unrecognized choice!"); break;
            }
            System.out.print("\n");
        }

    }catch(Exception e){
        System.err.println (e.getMessage ());
    }
}


public static void ViewSentMessages(ProfNetwork esql, String authorisedUser){
	try{
        boolean msgmenu = true;
        while(msgmenu) {
            System.out.print("\n");
            String query = String.format("SELECT msgId, recieverId, contents, sendTime FROM MESSAGE WHERE senderId = '%s' AND (deleteStatus = 0 OR deleteStatus = 2)", authorisedUser);
            esql.executeQueryAndPrintResult(query);

            System.out.print("\nWould you like to delete an messages?");
            System.out.print("\n1. Delete Message");
            System.out.println(".........................");
            System.out.println("9. Back");
            System.out.print("\n");
            switch(readChoice()){
                case 1: DeleteSentMessage(esql, authorisedUser); break;
                case 9: msgmenu = false; break;
                default: System.out.print("Unrecognized choice!"); break;
            }
            System.out.print("\n");
        }

    }catch(Exception e){
        System.err.println (e.getMessage ());
    }
}


public static void DeleteMessage(ProfNetwork esql, String authorisedUser){
    try{
        System.out.print("\n");
        System.out.print("Enter msgID for message to delete: ");
        int messageID = Integer.parseInt(in.readLine());

        String query = String.format("SELECT * FROM MESSAGE WHERE msgId = '%s' AND recieverId = '%s'", messageID, authorisedUser);

        //Check if message is allowed to be deleted
        if(esql.executeQuery(query) > 0){
            //Check the status of the message, if 0 set to 1, otherwise set to 3
            String status_query = String.format("SELECT * FROM MESSAGE WHERE msgId = '%s' AND deleteStatus = 0", messageID);
            if(esql.executeQuery(status_query) == 1) query = String.format("UPDATE MESSAGE SET deleteStatus = 2 WHERE msgId = '%d'", messageID);
            else query = String.format("UPDATE MESSAGE SET deleteStatus = 3 WHERE msgId = '%d'", messageID);

            esql.executeUpdate(query);
            System.out.print("\nDeleted Message!");
        }
        else System.out.print("\nInvalid Message ID.");
        System.out.print("\n");

    }catch(Exception e){
        System.err.println (e.getMessage ());
    }
}


public static void DeleteSentMessage(ProfNetwork esql, String authorisedUser){
    try{
        System.out.print("\n");
        System.out.print("Enter msgID for message to delete: ");
        int messageID = Integer.parseInt(in.readLine());

        String query = String.format("SELECT * FROM MESSAGE WHERE msgId = '%s' AND senderId = '%s'", messageID, authorisedUser);

        //Check if message is allowed to be deleted
        if(esql.executeQuery(query) > 0){
            //Check the status of the message, if 0 set to 1, otherwise set to 3
            String status_query = String.format("SELECT * FROM MESSAGE WHERE msgId = '%s' AND deleteStatus = 0", messageID);
            if(esql.executeQuery(status_query) == 1) query = String.format("UPDATE MESSAGE SET deleteStatus = 1 WHERE msgId = '%d'", messageID);
            else query = String.format("UPDATE MESSAGE SET deleteStatus = 3 WHERE msgId = '%d'", messageID);

            esql.executeUpdate(query);
            System.out.print("\nDeleted Message!");
        }
        else System.out.print("\nInvalid Message ID.");
        System.out.print("\n");

    }catch(Exception e){
        System.err.println (e.getMessage ());
    }
 }


public static void SendRequest(ProfNetwork esql, String authUser, String otherUser, Integer currlvl){
	try{
        String freindquery = String.format("SELECT * FROM CONNECTION_USR C WHERE C.status = 'Accept' AND (C.userId = '%s' OR C.connectionId = '%s')", authUser, authUser);
        //System.out.println(freindquery);

        //Less than 5 friends, then ingnore the level check
        Integer numFriends = esql.executeQueryAndReturnResult(freindquery).size();        
        if (currlvl<3 || numFriends<5){  
            //System.out.println("Num friends: " + numFriends);  
            String stat = "Request";
            String query = String.format("INSERT INTO CONNECTION_USR(userId, connectionid, status) VALUES ('%s', '%s', '%s')", authUser, otherUser, stat );
            esql.executeUpdate(query);
            System.out.print("Connection Request Sent!");
        }
        else {
            System.out.print("Error: connection level requiremnent not met.");
        }
	}catch(Exception e){
		System.err.println (e.getMessage ());
	}
}


public static void ViewIncomingRequests(ProfNetwork esql, String authorisedUser){
    try{
        boolean reqmenu = true;
        while(reqmenu){
            System.out.print("\n");
            String query = String.format("SELECT userid, status from CONNECTION_USR WHERE connectionId='%s' AND status = 'Request'", authorisedUser);
            esql.executeQueryAndPrintResult(query);
    
            System.out.print("\nWould you like to change status on any request?");
            System.out.print("\n1. Change Status");
            System.out.println(".........................");
            System.out.println("9. Back");
            System.out.print("\n");

            switch(readChoice()){
                case 1: ChangeRequest(esql, authorisedUser); break;
                case 9: reqmenu = false; break;
                default: System.out.print("Unrecognized choice!"); break;
            }
        }
        System.out.print("\n");
    }catch(Exception e){
        System.err.println(e.getMessage ());
    }
}

public static void ViewOutgoingRequests(ProfNetwork esql, String authorisedUser){
    try{
        System.out.print("\n");
        String query = String.format("SELECT connectionId, status from CONNECTION_USR WHERE userId='%s' AND status = 'Request'", authorisedUser);
        esql.executeQueryAndPrintResult(query);
        System.out.print("\n");
    }catch(Exception e){
        System.err.println(e.getMessage ());
    }
}


public static void ChangeRequest(ProfNetwork esql, String authorisedUser){
    try{
        System.out.print("Enter the userid of the request you would like to change: ");
        String user = in.readLine();
        System.out.print("Press (1) to Accept or 2 to (Reject): ");
        int choice = Integer.parseInt(in.readLine());
        String status = "Request";
        if(choice==1){status = "Accept";}
        else if (choice==2){status = "Reject";}
        String query = String.format("UPDATE CONNECTION_USR SET status = '%s' WHERE userid = '%s' AND connectionid = '%s'", status, user, authorisedUser);
        esql.executeUpdate(query);
        System.out.println("Updated connection!");
    }catch(Exception e){
        System.err.println(e.getMessage ());
    }
}


public static void SearchPeople(ProfNetwork esql, String authorisedUser){
	try{
        boolean searchmenu = true;
        while(searchmenu) {
            System.out.print("\n");
            Integer currlvl = 9;
            System.out.print("Enter the name or userId to search for: ");
            String name = in.readLine();
            String query = "SELECT name, userId FROM USR WHERE name LIKE '%" + name + "%' OR userId LIKE '%" + name + "%'";
            esql.executeQueryAndPrintResult(query);	
            System.out.print("\n");
            
            System.out.println("1. View Profile");
            System.out.println("2. Search Again");
            System.out.println(".........................");
            System.out.println("9. Back");
            System.out.print("\n");
            switch (readChoice()){
                case 1: SelectProfile(esql,authorisedUser,currlvl); break;
                case 2: break;
                case 9: searchmenu = false; break;
                default : System.out.println("Unrecognized choice!"); break;
            }
        }
	}catch(Exception e){
		System.err.println (e.getMessage ());
	}
}


public static void ChangePassword(ProfNetwork esql, String authorisedUser){
    try{
        System.out.print("\tEnter new password: ");
        String pass1 = in.readLine();
        System.out.print("\tRe-enter new password: ");
        String pass2 = in.readLine();

        if(pass1.compareTo(pass2) == 0){
            String query = String.format("UPDATE USR SET password = '%s' WHERE userId = '%s'", pass1, authorisedUser);
            esql.executeUpdate(query);
            System.out.println ("User password updated successfully!");    
        }
        else {
            System.out.println ("Error the passwords do not match."); 
        }

     }catch(Exception e){
        System.err.println (e.getMessage ());
     }
}

}//end ProfNetwork
