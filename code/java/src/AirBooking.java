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
		try{
         String query =  "INSERT INTO Passenger(pID,passNum,fullName,"
						+" bdate,country) VALUES (";

		// To get the latest pid
		String getpID = "SELECT MAX(pID) FROM Passenger";
		List<List<String>> pidGot = esql.executeQueryAndReturnResult(getpID);
		int pid = Integer.valueOf(pidGot.get(0).get(0));
		//add 1
		pid = pid + 1;
		String pidGotten = Integer.toString(pid);
		// done
         int verify = 1;
         String input = "temp";
         query += pidGotten;

         String checkPN = "SELECT pID FROM Passenger WHERE passNum = '";
         String tempCheck = checkPN;
		 while(verify == 1){
			 System.out.print("Enter passNum: ");
			 input = in.readLine();
			 checkPN += input;
			 checkPN += "'";
			 List<List<String>> passNumGot = esql.executeQueryAndReturnResult(checkPN);
			 if(input.length() == 10 && passNumGot.size() == 0){
				 verify = 0;
			}
			else {
				checkPN = tempCheck;
				System.out.println("Error: incorrect Passenger");
			}
		}
		query += ",'" + input + "'";

		verify = 1;
		while(verify == 1) {
			System.out.print("Enter Full Name: ");
			input = in.readLine();
			if(input.length() <= 24 && input.length() != 0){
				verify = 0;
			}
		}
		query += ",'" + input + "'";

		verify = 1;
		while(verify == 1) {
			System.out.print("Enter bdate (mm/dd/yyyy): ");
			input = in.readLine();
			if(input.length() == 10) {
				verify = 0;
			}
			else {
				System.out.println("Error invalid date, please re enter date");
			}
		}
		query += ",'" + input + "'";

		verify = 1;
		while(verify == 1){
			System.out.print("Enter country: ");
			input = in.readLine();
			if(input.length() <= 24 && input.length() != 0) {
				verify = 0;
			}
			else {
				System.out.println("Error: please enter less than 24 characters");
			}
		}
		query += ",'" + input + "')";

         esql.executeUpdate(query);

      }catch(Exception e){
         System.err.println (e.getMessage());
      }
	}

	public static void BookFlight(AirBooking esql){//2
		//Book Flight for an existing customer
		try{
         int verify = 1;

         String input = "input";

		String passnum = "0";
         verify = 1;
         // ask user for passport number, will loop until valid
         while(verify == 1){
			 System.out.print("Enter passport number: ");
			 passnum = in.readLine();
			 String validPN = "SELECT P.passNum FROM Passenger P WHERE P.passNum = '" + passnum +"'";
			 List<List<String>> validUSER = esql.executeQueryAndReturnResult(validPN);
			 if(validUSER.size() == 0){
				 System.out.print("invalid passport\n");
			 }
			 else{
				 verify = 0;
			 }
		}
		String validP = "SELECT P.pID FROM Passenger P WHERE P.passNum = '" + passnum +"'";
		List<List<String>> validPID = esql.executeQueryAndReturnResult(validP);
		String pid = validPID.get(0).get(0);
		// This will hold the value of the passenger's pID

			 String origin = "";
			 verify = 1;
			 while(verify == 1){
				 System.out.print("Enter origin: ");
				 origin = in.readLine();

				 String validO = "SELECT F.origin FROM Flight F WHERE F.origin = '" + origin + "'";
				 List<List<String>> validOrigin = esql.executeQueryAndReturnResult(validO);
				 if(validOrigin.size() == 0){
					 System.out.print("invalid origin\n");
				 }
				 else{
					if(origin.length() <= 16){
					verify = 0;
					}
				 }
			}
			// ask user for destination, loops to ensure valid
			String dest = "";
			verify = 1;
			while(verify == 1){
				 System.out.print("Enter destination: ");
				 dest = in.readLine();

				 String validD = "SELECT F.destination FROM Flight F WHERE F.destination = '" + dest + "'";
				 List<List<String>> validDest = esql.executeQueryAndReturnResult(validD);
				 if(validDest.size() == 0){
					 System.out.print("invalid destination\n");
				 }
				 else{
					if(dest.length() <= 16){
					verify = 0;
					}
				 }
			}
			//ask user for their departure date
			verify = 1;
			String date = "date";
			while(verify == 1) {
				System.out.print("Enter departure date(mm/dd/yyyy): ");
				date = in.readLine();
				String month = date.substring(0,2);
				String day = date.substring(3,5);
				int monthVerify = Integer.valueOf(month);
				int dayVerify = Integer.valueOf(day);
				if(date.length() == 10 && monthVerify < 13 && monthVerify > 0 && dayVerify >= 1 && dayVerify < 32) {
					verify = 0;
				}
				else {
					System.out.println("Error invalid date, please re enter date");
				}
			}
			System.out.print("\nAvailable flights: \n");


			String query = "SELECT F.flightNum, F.origin, F.destination,B1.departure, "
						   +"F.seats - (SELECT Count(B.flightNum) FROM Booking B WHERE B.flightNum = F.flightNum)"
						   +" AS availableseats FROM Flight F, Booking B1 WHERE F.origin = ";
			query += "'" + origin + "' AND F.destination = ";
			query += "'" + dest + "' AND B1.departure = ";
			query += "'" + date + "'AND (F.seats - (SELECT Count(B3.flightNum) FROM Booking B3 WHERE B3.flightNum = F.flightNum)) > 0"
				  +" GROUP BY F.flightNum, F.origin, F.destination, B1.departure";


			//execute query to print all available flights

				esql.executeQueryAndPrintResult(query);

				System.out.print("\n Select the flight \n");

				//check for proper flight number input
				String flightNum = "";
				verify = 1;
				 while(verify == 1){
					 System.out.print("Enter flightNum: ");
					 flightNum = in.readLine();

					if(flightNum.length() <= 8){
					verify = 0;
					}
				}
				System.out.print("\n");

				//generate random alphanumeric string
				String bookref="";
				verify = 1;
				while(verify == 1){
					String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
					StringBuilder ref = new StringBuilder();
					Random rnd = new Random();
					while (ref.length() < 10) { // length of the random string.
						int index = (int) (rnd.nextFloat() * CHARS.length());
						ref.append(CHARS.charAt(index));
					}
					bookref = ref.toString();

					String test = "SELECT B.bookRef FROM Booking B WHERE B.bookRef = '"+ bookref +"'";
					 List<List<String>> val_book = esql.executeQueryAndReturnResult(test);
					 if(val_book.size() == 0){
						 verify = 0;
					 }
				}
		String insert =  "INSERT INTO Booking(bookRef,departure,flightNum,pID)"
						+" VALUES ('"+ bookref +"', '"+ date +"', '"+ flightNum +"', '"+ pid +"')";

		esql.executeUpdate(insert);
	}catch(Exception e){
         System.err.println (e.getMessage());
	 }

	}

	public static void TakeCustomerReview(AirBooking esql){//3
		//Insert customer review into the ratings table
		try{
         String query =  "INSERT INTO Ratings(rID, pID, flightNum, score,comment) VALUES (";



        String getID = "SELECT MAX(rID) FROM Ratings";
		List<List<String>> ratingID = esql.executeQueryAndReturnResult(getID);
		int rid = Integer.valueOf(ratingID.get(0).get(0));
		rid = rid + 1;
		String rating = Integer.toString(rid);
		String input = rating;
		query += input;
		query += ",";

         int verify = 1;
         String verifytook = query;
         //Gets input for pID and flightNum, checks if passenger took flight or wrote a rating already.
         while(verify == 1) {
			 String verifyExist = "SELECT passNum FROM Passenger WHERE pID = ";
			 String temp = verifyExist;
			 String verifypidTook = "temp";
			 while(verify == 1) {
				 System.out.print("Enter pID: ");
				 input = in.readLine();
				 verifyExist += input;
				 List<List<String>> verifyIDE = esql.executeQueryAndReturnResult(verifyExist);
				 //Checks if pID exists
				 if(verifyIDE.size() != 0){
					 verify = 0;
				}
				else {
					verifyExist = temp;
					System.out.print("Error pID does not exist\n");
				}
			 }
			 query += input + ",";
			 String pidGot = input;
			 verify = 1;

			 String verifyflightNumExist = "SELECT airId FROM Flight WHERE flightNum = '";
			 temp = verifyflightNumExist;

			 while(verify == 1) {
				 System.out.print("Enter flightNum: ");
				 input = in.readLine();
				 verifyflightNumExist += input;
				 verifyflightNumExist += "'";
				 //Checks if flight num exist
				 List<List<String>> flightExist = esql.executeQueryAndReturnResult(verifyflightNumExist);
				 if(flightExist.size() != 0) {
					 verify = 0;
				 }
				 else {
					 verifyflightNumExist = temp;
					 System.out.print("Error flightNum does no exist\n");
				 }
			 }
			 query += "'" + input + "',";
			 String checkTook = "SELECT departure FROM Booking WHERE flightNum = ";
			 checkTook += "'" + input + "'" + " AND pID = " + pidGot;
			 String checkWrote = "SELECT rID FROM Ratings WHERE flightNum = ";
			 checkWrote += "'" + input + "'" + " AND pID = " + pidGot;

			 List<List<String>> verifyIfExist = esql.executeQueryAndReturnResult(checkTook);
			 List<List<String>> verifyIfWrote = esql.executeQueryAndReturnResult(checkWrote);
			 //Checks to see if the passenger didnt write a rating, and took the flight
			 if(verifyIfExist.size() != 0 && verifyIfWrote.size() == 0) {
				 verify = 0;
			 }
			 else if ( verifyIfWrote.size() != 0 ) {
				 System.out.print("Error the passenger wrote a rating for this flight, please enter info again\n");
				 query = verifytook;
				 verify = 1;
				}
			 else {
				 System.out.print("Error the passenger didnt take the flight, please enter info again\n");
				 query = verifytook;
				 verify = 1;
			 }
		}
		 verify = 1;



		 while(verify == 1) {
			 System.out.print("Enter a score (0-5): ");
			 input = in.readLine();
			 int score = Integer.valueOf(input);
			 if(score >= 0 && score <= 5) {
				 verify = 0;
			 }
			 else
			 {
				 System.out.print("Error invalid score, please enter (0-5)\n");
			}
		}
		 query += input + ",";

		 System.out.print("Enter your comments on flight: ");
		 input = in.readLine();
		 query += "'" + input + "'" + ")";

         esql.executeUpdate(query);
      }catch(Exception e){
         System.err.println (e.getMessage());
      }
	}

	public static void InsertOrUpdateRouteForAirline(AirBooking esql){//4
		//Insert a new route for the airline
	}

	public static void ListAvailableFlightsBetweenOriginAndDestination(AirBooking esql) throws Exception{//5
		//List all flights between origin and distination (i.e. flightNum,origin,destination,plane,duration)
		String origin = "";
		System.out.print("Please enter Origin of flights you would like to see: ");
		String input = in.readLine();
		origin += input;
		while (origin == "" || origin == null) {
			System.out.print("Please enter valid Origin: ");
			input = in.readLine();
			origin += input;
		}
		String destination = "";
		System.out.print("Please enter Destination of flights you would like to see: ");
		String input2 = in.readLine();
		destination += input2;
		while (destination == "" || destination == null) {
			System.out.print("Please enter valid destination: ");
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

			System.out.print("Please enter number of top-rated destinations you want to see: ");
			do {
				input = in.readLine();
				shouldRepeat = 1;
				if (Integer.parseInt(input) <= 0) {
					System.out.print("Please enter a value greater than 0: ");
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
					System.out.print("Cannot leave entry blank. Try again: ");
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
					System.out.print("Cannot leave entry blank. Try again: ");
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
					System.out.print("Enter a value greater than 0: ");
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
		try{
		String query = "SELECT F.flightNum, F.origin, F.destination,B1.departure,"
						+ " (SELECT COUNT(B2.flightNum) FROM Booking B2"
						+ " WHERE B2.flightNum = F.flightNum) as booked,F.seats,"
						+ " (F.seats - (SELECT Count(B.flightNum)FROM Booking B"
						+ " WHERE B.flightNum = F.flightNum)) as available FROM Flight F, Booking B1"
						+ " WHERE B1.departure = '";

		String input = "";
		int shouldRepeat = 1;
		while (shouldRepeat == 1) {
			System.out.print("Please enter the departure date for flights: ");
			input = in.readLine();
			String dates = "SELECT * FROM Booking WHERE departure = '" + input + "'";
			List<List<String>> flights = esql.executeQueryAndReturnResult(dates);
				if (input == "Exit") {
					shouldRepeat = 0;
				}
				 if(flights.size() == 0){
					 System.out.print("There are no available flights for this departure date. Try Again or 'Exit' to exit: ");
				 }
				 else {
					 shouldRepeat = 0;
					 query += input;
					 query += "'";
				 }
		}
		query += " AND  (F.seats - (SELECT Count(B.flightNum) FROM Booking B";
		query += " WHERE B.flightNum = F.flightNum)) > 0" ;
		query += " GROUP BY F.flightNum, F.origin, F.destination, F.plane, B1.departure, F.seats";
		query += " ORDER BY F.origin, F.destination";

		int rows = esql.executeQueryAndPrintResult(query);
		System.out.println ("total row(s): " + rows);

	}catch(Exception e) {
		System.err.println(e.getMessage());
	}

}
