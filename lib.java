CREATE DATABASE LibraryDB;

   USE LibraryDB;

   CREATE TABLE Books (
       id INT PRIMARY KEY AUTO_INCREMENT,
       title VARCHAR(255) NOT NULL,
       author VARCHAR(255) NOT NULL,
       genre VARCHAR(255),
       isAvailable BOOLEAN NOT NULL
   );

   CREATE TABLE Patrons (
       id INT PRIMARY KEY AUTO_INCREMENT,
       name VARCHAR(255) NOT NULL,
       email VARCHAR(255) NOT NULL
   );

   CREATE TABLE BorrowRecords (
       id INT PRIMARY KEY AUTO_INCREMENT,
       bookId INT,
       patronId INT,
       borrowDate DATE,
       returnDate DATE,
       FOREIGN KEY (bookId) REFERENCES Books(id),
       FOREIGN KEY (patronId) REFERENCES Patrons(id)
   );
   <!-- Maven Dependency -->
   <dependency>
       <groupId>mysql</groupId>
       <artifactId>mysql-connector-java</artifactId>
       <version>8.0.25</version>
   </dependency>
   import java.sql.Connection;
   import java.sql.DriverManager;
   import java.sql.SQLException;

   public class DatabaseUtil {
       private static final String URL = "jdbc:mysql://localhost:3306/LibraryDB";
       private static final String USER = "root";
       private static final String PASSWORD = "your_password";

       public static Connection getConnection() throws SQLException {
           return DriverManager.getConnection(URL, USER, PASSWORD);
       }
   }
   import java.sql.*;
   import java.time.LocalDate;
   import java.util.ArrayList;
   import java.util.List;
   import java.util.stream.Collectors;

   public class Library {
       public Library() {
           try (Connection connection = DatabaseUtil.getConnection()) {
               // Ensure tables exist (you can use a migration tool or do this manually)
               String createBooksTable = "CREATE TABLE IF NOT EXISTS Books (id INT PRIMARY KEY AUTO_INCREMENT, title VARCHAR(255) NOT NULL, author VARCHAR(255) NOT NULL, genre VARCHAR(255), isAvailable BOOLEAN NOT NULL)";
               String createPatronsTable = "CREATE TABLE IF NOT EXISTS Patrons (id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255) NOT NULL, email VARCHAR(255) NOT NULL)";
               String createBorrowRecordsTable = "CREATE TABLE IF NOT EXISTS BorrowRecords (id INT PRIMARY KEY AUTO_INCREMENT, bookId INT, patronId INT, borrowDate DATE, returnDate DATE, FOREIGN KEY (bookId) REFERENCES Books(id), FOREIGN KEY (patronId) REFERENCES Patrons(id))";

               try (Statement statement = connection.createStatement()) {
                   statement.execute(createBooksTable);
                   statement.execute(createPatronsTable);
                   statement.execute(createBorrowRecordsTable);
               }
           } catch (SQLException e) {
               e.printStackTrace();
           }
       }

       public void addBook(Book book) {
           String query = "INSERT INTO Books (title, author, genre, isAvailable) VALUES (?, ?, ?, ?)";
           try (Connection connection = DatabaseUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {
               preparedStatement.setString(1, book.getTitle());
               preparedStatement.setString(2, book.getAuthor());
               preparedStatement.setString(3, book.getGenre());
               preparedStatement.setBoolean(4, book.isAvailable());
               preparedStatement.executeUpdate();
           } catch (SQLException e) {
               e.printStackTrace();
           }
       }

       public void addPatron(Patron patron) {
           String query = "INSERT INTO Patrons (name, email) VALUES (?, ?)";
           try (Connection connection = DatabaseUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {
               preparedStatement.setString(1, patron.getName());
               preparedStatement.setString(2, patron.getEmail());
               preparedStatement.executeUpdate();
           } catch (SQLException e) {
               e.printStackTrace();
           }
       }

       public void borrowBook(int bookId, int patronId) {
           String updateBookQuery = "UPDATE Books SET isAvailable = FALSE WHERE id = ?";
           String insertBorrowRecordQuery = "INSERT INTO BorrowRecords (bookId, patronId, borrowDate) VALUES (?, ?, ?)";
           try (Connection connection = DatabaseUtil.getConnection();
                PreparedStatement updateBookStmt = connection.prepareStatement(updateBookQuery);
                PreparedStatement insertBorrowRecordStmt = connection.prepareStatement(insertBorrowRecordQuery)) {
               updateBookStmt.setInt(1, bookId);
               updateBookStmt.executeUpdate();

               insertBorrowRecordStmt.setInt(1, bookId);
               insertBorrowRecordStmt.setInt(2, patronId);
               insertBorrowRecordStmt.setDate(3, Date.valueOf(LocalDate.now()));
               insertBorrowRecordStmt.executeUpdate();
           } catch (SQLException e) {
               e.printStackTrace();
           }
       }

       public void returnBook(int bookId, int patronId) {
           String updateBookQuery = "UPDATE Books SET isAvailable = TRUE WHERE id = ?";
           String updateBorrowRecordQuery = "UPDATE BorrowRecords SET returnDate = ? WHERE bookId = ? AND patronId = ? AND returnDate IS NULL";
           try (Connection connection = DatabaseUtil.getConnection();
                PreparedStatement updateBookStmt = connection.prepareStatement(updateBookQuery);
                PreparedStatement updateBorrowRecordStmt = connection.prepareStatement(updateBorrowRecordQuery)) {
               updateBookStmt.setInt(1, bookId);
               updateBookStmt.executeUpdate();

               updateBorrowRecordStmt.setDate(1, Date.valueOf(LocalDate.now()));
               updateBorrowRecordStmt.setInt(2, bookId);
               updateBorrowRecordStmt.setInt(3, patronId);
               updateBorrowRecordStmt.executeUpdate();
           } catch (SQLException e) {
               e.printStackTrace();
           }
       }

       public double calculateFine(int patronId) {
           String query = "SELECT borrowDate, returnDate FROM BorrowRecords WHERE patronId = ? AND returnDate IS NULL";
           try (Connection connection = DatabaseUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {
               preparedStatement.setInt(1, patronId);
               ResultSet resultSet = preparedStatement.executeQuery();

               double fine = 0.0;
               while (resultSet.next()) {
                   Date borrowDate = resultSet.getDate("borrowDate");
                   LocalDate dueDate = borrowDate.toLocalDate().plusDays(14);
                   if (dueDate.isBefore(LocalDate.now())) {
                       fine += 5.0;
                   }
               }
               return fine;
           } catch (SQLException e) {
               e.printStackTrace();
               return 0.0;
           }
       }

       public List<Book> searchBooks(String query) {
           String searchQuery = "SELECT * FROM Books WHERE title LIKE ?";
           List<Book> books = new ArrayList<>();
           try (Connection connection = DatabaseUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(searchQuery)) {
               preparedStatement.setString(1, "%" + query + "%");
               ResultSet resultSet = preparedStatement.executeQuery();
               while (resultSet.next()) {
                   books.add(new Book(
                           resultSet.getInt("id"),
                           resultSet.getString("title"),
                           resultSet.getString("author"),
                           resultSet.getString("genre"),
                           resultSet.getBoolean("isAvailable")
                   ));
               }
           } catch (SQLException e) {
               e.printStackTrace();
           }
           return books;
       }

       public void generateReport() {
           String booksQuery = "SELECT * FROM Books";
           String patronsQuery = "SELECT * FROM Patrons";
           try (Connection connection = DatabaseUtil.getConnection();
                Statement booksStmt = connection.createStatement();
                Statement patronsStmt = connection.createStatement();
                ResultSet booksResultSet = booksStmt.executeQuery(booksQuery);
                ResultSet patronsResultSet = patronsStmt.executeQuery(patronsQuery)) {

               System.out.println("Library Report:");
               System.out.println("Books:");
               while (booksResultSet.next()) {
                   System.out.println(new Book(
                           booksResultSet.getInt("id"),
                           booksResultSet.getString("title"),
                           booksResultSet.getString("author"),
                           booksResultSet.getString("genre"),
                           booksResultSet.getBoolean("isAvailable")
                   ));
               }

               System.out.println("Patrons:");
               while (patronsResultSet.next()) {
                   System.out.println(new Patron(
                           patronsResultSet.getInt("id"),
                           patronsResultSet.getString("name"),
                           patronsResultSet.getString("email")
                   ));
               }
           } catch (SQLException e) {
               e.printStackTrace();
           }
       }

       private Book getBookById(int id) {
           String query = "SELECT * FROM Books WHERE id = ?";
           try (Connection connection = DatabaseUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {
               preparedStatement.setInt(1, id);
               ResultSet resultSet = preparedStatement.executeQuery();
               if (resultSet.next()) {


                   return new Book(
                           resultSet.getInt("id"),
                           resultSet.getString("title"),
                           resultSet.getString("author"),
                           resultSet.getString("genre"),
                           resultSet.getBoolean("isAvailable")
                   );
               }
           } catch (SQLException e) {
               e.printStackTrace();
           }
           return null;
       }

       private Patron getPatronById(int id) {
           String query = "SELECT * FROM Patrons WHERE id = ?";
           try (Connection connection = DatabaseUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {
               preparedStatement.setInt(1, id);
               ResultSet resultSet = preparedStatement.executeQuery();
               if (resultSet.next()) {
                   return new Patron(
                           resultSet.getInt("id"),
                           resultSet.getString("name"),
                           resultSet.getString("email")
                   );
               }
           } catch (SQLException e) {
               e.printStackTrace();
           }
           return null;
       }
   }