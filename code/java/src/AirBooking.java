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

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class AirBooking{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public AirBooking(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
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
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
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
	}
	
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
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
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
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
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
		if (rs.next()) return rs.getInt(1);
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
				"Usage: " + "java [-classpath <classpath>] " + AirBooking.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		AirBooking esql = null;
		
		try{
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new AirBooking (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Passenger");
				System.out.println("2. Book Flight");
				System.out.println("3. Review Flight");
				System.out.println("4. Insert or Update Flight");
				System.out.println("5. List Flights From Origin to Destination");
				System.out.println("6. List Most Popular Destinations");
				System.out.println("7. List Highest Rated Destinations");
				System.out.println("8. List Flights to Destination in order of Duration");
				System.out.println("9. Find Number of Available Seats on a given Flight");
				System.out.println("10. < EXIT");
				
				switch (readChoice()){
					case 1: AddPassenger(esql); break;
					case 2: BookFlight(esql); break;
					case 3: TakeCustomerReview(esql); break;
					case 4: InsertOrUpdateRouteForAirline(esql); break;
					case 5: ListAvailableFlightsBetweenOriginAndDestination(esql); break;
					case 6: ListMostPopularDestinations(esql); break;
					case 7: ListHighestRatedRoutes(esql); break;
					case 8: ListFlightFromOriginToDestinationInOrderOfDuration(esql); break;
					case 9: FindNumberOfAvailableSeatsForFlight(esql); break;
					case 10: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

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
	
	public static void AddPassenger(AirBooking esql){//1
		//Add a new passenger to the database
	}
	
	public static void BookFlight(AirBooking esql){//2
		//Book Flight for an existing customer
		
	}
	
	public static void TakeCustomerReview(AirBooking esql){//3
		//Insert customer review into the ratings table
	}
	
	public static void InsertOrUpdateRouteForAirline(AirBooking esql){//4
		//Insert a new route for the airline
	}
	
	public static void ListAvailableFlightsBetweenOriginAndDestination(AirBooking esql) throws Exception{//5
		//List all flights between origin and distination (i.e. flightNum,origin,destination,plane,duration) 
		String origin = "";
		System.out.println("Please enter Origin of flights you would like to see:");
		String input = in.readLine();
		origin += input;
		while (origin == "" || origin == null) {
			System.out.println("Please enter valid Origin");
			input = in.readLine();
			origin += input;
		}
		String destination = "";
		System.out.println("Please enter Destination of flights you would like to see:");
		String input2 = in.readLine();
		destination += input2;
		while (destination == "" || destination == null) {
			System.out.println("Please enter valid destination");
			input2 = in.readLine();
			destination += input2;
		}
		try{
			String query1 = "SELECT * FROM flight WHERE origin = '";
			String query2 = "' AND destination = '";
			esql.executeQueryAndPrintResult(query1 + origin + query2 + destination + "'");
		} catch (SQLException e){
			System.out.println(e);
		} 
	}
	
	public static void ListMostPopularDestinations(AirBooking esql){//6
		//Print the k most popular destinations based on the number of flights offered to them (i.e. destination, choices)
		try {
			String input = "";
			String input2 = "";
			Integer shouldRepeat = 1;
			List<List<String>> popularDestinations;
			List<List<String>> totalNumberOfFlights;
			String totalFlights = "SELECT COUNT(*) FROM Flight GROUP BY destination";
			totalNumberOfFlights = esql.executeQueryAndReturnResult(totalFlights);

			System.out.println("Please enter number of top-rated destinations you want to see");
			do { 
				input = in.readLine();
				shouldRepeat = 1;
				if (Integer.parseInt(input) <= 0) {
					System.out.println("Please enter a value greater than 0");
					shouldRepeat = 0;
				}
			} while(shouldRepeat == 0);
			String query = "SELECT destination,COUNT(*) as count FROM Flight GROUP BY destination ORDER BY count DESC LIMIT " + input + ";";
			popularDestinations = esql.executeQueryAndReturnResult(query);
			for (int i = 0; i < popularDestinations.size(); i++) {
				String dest2print = popularDestinations.get(i).get(0).replaceAll("\\s+","");
				System.out.println("Destination: " + dest2print);
				String flight2print = popularDestinations.get(i).get(1);
				System.out.println("Number of flights: " + flight2print);
				System.out.println("__________________________________________________");
			}
		}
		catch(Exception e){
			 System.err.println (e.getMessage());
		}
	}
	
	public static void ListHighestRatedRoutes(AirBooking esql){//7
		//List the k highest rated Routes (i.e. Airline Name, flightNum, Avg_Score)
		try{
			System.out.print("Please enter number of highest rated routes you want: ");
			Integer shouldRepeat = 1;
			
			String numRoutes = in.readLine();
			do {
				shouldRepeat = 1;
				if (Integer.parseInt(numRoutes) <= 0)
				{
					System.out.print("Please enter a number higher than 0: ");
					numRoutes = in.readLine();
					shouldRepeat = 0;
				}	
			} while (shouldRepeat == 0);

			String query = "";
			String select = "SELECT Ratings.flightNum, AVG(Ratings.score) as avg, COUNT(Ratings.flightNum) as total ";
			String from = "FROM Ratings GROUP BY Ratings.flightNum ";
			String orderby = "ORDER BY avg DESC, total DESC LIMIT " + numRoutes;
			query = query + select + from + orderby;

			String display = "SELECT Airline.name, Flight.flightNum, Flight.origin, Flight.destination, Flight.plane, a.avg ";
			display += "FROM Airline, Flight ";
			display += "INNER JOIN (";
			display += query + ") AS a ON Flight.flightNum = a.flightNum WHERE Flight.airID = Airline.airID ORDER BY a.avg DESC, a.total DESC";

			esql.executeQueryAndPrintResult(display);
		}catch(Exception e)
		{
			System.err.println(e.getMessage());
		}
	}
	
	public static void ListFlightFromOriginToDestinationInOrderOfDuration(AirBooking esql){//8
		//List flight to destination in order of duration (i.e. Airline name, flightNum, origin, destination, duration, plane)
		try { 
			String origin = ""; 
			String dest = "";
			boolean shouldRepeat = true; 
			
			do { 
				System.out.print("Enter the flight origin: "); 
				origin = in.readLine(); 
				if(origin.replaceAll("\\s+","") == "" || origin == null) { 
					shouldRepeat = true;
					System.out.print("Cannot leave entry blank. Try again."); 
					origin = in.readLine(); 
				}
				else {
					shouldRepeat = false;
				} 

			} while (shouldRepeat);
			
			shouldRepeat = true; 
			do { 
				System.out.print("Enter the flight destination: "); 
				dest = in.readLine(); 
				if(dest.replaceAll("\\s+","") == "" || dest.replaceAll("\\s+","") == null) { 
					System.out.print("Cannot leave entry blank. Try again."); 
					shouldRepeat = true;
					dest = in.readLine(); 
				} 
				else { 
					shouldRepeat = false; 
				} 
			} while (shouldRepeat); 
			
			String query = "SELECT A.name, F.flightNum, F.origin, F.destination, F.duration, F.plane FROM Airline A, FLight F WHERE F.airId = A.airID AND origin = '";
			query += origin + "' AND destination = '" + dest + "' ORDER BY F.duration ASC"; 
			
			shouldRepeat = true;
			String numFlights = "";
			do {
				System.out.print("Enter the number of flights you would like to see: ");
				numFlights = in.readLine();
				if(Integer.parseInt(numFlights) <= 0 || numFlights.replaceAll("\\s+","") == "") {
					System.out.print("Enter a value greater than 0");
					shouldRepeat = true;
					numFlights = in.readLine();
				}
				else {
					shouldRepeat = false;
				} 
			} while (shouldRepeat);
			
			List<List<String>> flightsResult = esql.executeQueryAndReturnResult(query); 
			
			
			if(flightsResult.size() == 0) {
					System.out.print("There are no such flights\n");
			}
			else { 
			System.out.print("Airline \t Flight Number \t Origin \t Destination \t Duration \t Plane \n"); 
				for(int i = 0; i < Integer.parseInt(numFlights); i++) { 
					for(int j = 0; j < flightsResult.get(i).size(); j++) { 
						System.out.print(flightsResult.get(i).get(j));
						System.out.print("\t");
					}
					System.out.print("\n");
					if (i + 1 >= flightsResult.size()) {
						break;
					}
				}
		} 

		} catch(Exception e) { 
			System.err.println(e.getMessage()); 
		}
	}
	
	public static void FindNumberOfAvailableSeatsForFlight(AirBooking esql){//9
		//
		
	}
	
}
