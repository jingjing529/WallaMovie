# CS 122B Team WJJ

- # General
    - #### Team#: WJJ

    - #### Names: Chengfeng Tang & Jingjing Wang

    - #### Project 5 Video Demo Link: https://youtu.be/mL0VDdQQRtE
    - #### Instruction of deployment:
		1. git clone this git repo to AWS instance: `git clone https://github.com/UCI-Chenli-teaching/s23-122b-wjj.git`
		2. cd into the repo: `cd s23-122b-wjj`
		3. inside your repo, find where the pom.xml file locates:
			1. Single instance: `cd cs122b-project3`
			2. Scaled version instance (master/slave): `cd cs122b-project5`
		4. build the war file
			`mvn clean`
			`mvn package`
		5. copy your newly built war file to tomcat:
			`cp ./target/*.war /var/lib/tomcat10/webapps/`

    - #### Collaborations and Work Distribution:
    Cheng Tang: Connection Pooling, Jmeter testing, Demo
    
    Jingjing Wang: Master-Slave Replication, Load balancer, Readme

- # Connection Pooling
    - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
      - `cs122b-project3/WebContent/META-INF/context.xml` for single instance version
      - `cs122b-project5/WebContent/META-INF/context.xml` for scaled version

    - #### Explain how Connection Pooling is utilized in the Fabflix code.
		1. The servlet's init method initializes the DataSource object by looking up the appropriate resource from the context using JNDI. In Fabflix's code is `java:comp/env/jdbc/read` or `java:comp/env/jdbc/write`.
		2. Inside the `doPost()` or `doGet()` method, a connection is obtained from the connection pool by calling dataSource.getConnection(). This method internally manages the pool of available connections and retrieves a connection from the pool.
		3. The obtained connection is then used to execute a database query.
		4. After executing the query and performing the necessary operations, the connection is closed automatically and returned to the connection pool when it goes out of scope.
    - #### Explain how Connection Pooling works with two backend SQL.
		1. Two data sources are defined in the context.xml file, one for the read/write master database `jdbc/write` and the other for the read-only database `jdbc/read`.
		2. Connection pooling settings are applied to each data source, creating separate connection pools for each database.
		3. When a request is made, the servlet retrieves a connection from the appropriate data source based on the desired operation. Requests for write operations obtain connections from the `jdbc/write` data source, while read requests acquire connections from the `jdbc/read` data source.
		4. The connection pool associated with each data source manages the allocation and reuse of connections from their respective pools.
		5. By utilizing separate connection pools for each data source, requests are automatically directed to the corresponding database, enabling high availability and scalability in the system.

- # Master/Slave
    - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
		- Datasource configuration: 
			- `cs122b-project5/WebContent/META-INF/context.xml`
			- `cs122b-project5/WebContent/WEB-INF/web.xml`

		- Servlets that use read-only datasource:
			- `src/HeroSuggestion.java`
			- `src/IndexServlet.java`
			- `src/LoginEmployeeServlet.java`
			- `src/LoginServlet.java`
			- `src/MovieListServlet.java`
			- `src/SingleMovieServlet.java`
			- `src/SingleStarServlet.java`

		- Servlets that use read/write datasource:
			- `src/DashboardServlet.java`
			- `src/Payment.java`

    - #### How read/write requests were routed to Master/Slave SQL?
		1. In the `context.xml` file, two data sources are defined: one for the read/write master database `jdbc/write` and the other for the read-only database `jdbc/read`.
		2. 	In the case of the read/write master database, it points to
		`jdbc:mysql://mpri.wallamovie.store:3306/moviedb`
		For the read-only database, it uses a load-balanced URL
		`jdbc:mysql:loadbalance://mpri.wallamovie.store:3306,spri.wallamovie.store:3306/moviedb`
		By using the load-balanced JDBC URL, the read requests are automatically distributed among the available database instances, providing a mechanism for achieving high availability and scalability in the system.
		2. In servlets, we manually connect to specific data sources. When inserting data into the database, we obtain a connection from the write data source using JNDI lookup (`java:comp/env/jdbc/write`), corresponding to the master database for read/write operations. On the other hand, when retrieving data, we obtain a connection from the read data source using JNDI lookup (`java:comp/env/jdbc/read`), which can be routed to either the master or the slave database.

- # JMeter TS/TJ Time Logs
    #### Logs
    - Single-instance cases:
		- [Log #1](/logs/http%2010T%20NOCP) Use HTTP, without using Connection Pooling, 10 threads in JMeter.
		- [Log #2](logs/http%201T) Use HTTP, 1 thread in JMeter.
		- [Log #3](/logs/http%2010T) Use HTTP, 10 threads in JMeter.
		- [Log #4](/logs/https%2010T) Use HTTPS, 10 threads in JMeter.
    - Scaled-version cases:
		- [Log #5](/logs/http%2010T%20gcppub%20NOCP) Use HTTP, without using Connection Pooling, 10 threads in JMeter.
		- [Log #6](/logs/http%201T%20gcppub) Use HTTP, 1 thread in JMeter.
		- [Log #7](/logs/http%2010T%20gcppub) Use HTTP, 10 threads in JMeter.

    #### Instructions of how to use the `log_processing.*` script to process the JMeter logs.
    - Name the log file "test" or change the script to match the log name
    - Make sure the log file is in the same directory of the log_processing script
    - Make sure you have the python3 installed
    - inside your repo, cd into the corresponding location where log_processing.py locates:
		1. Single instance: `cd cs122b-project3`
		2. Scaled version instance (master/slave): `cd cs122b-project5`
    - Run log_processing.py: `python3 log_processing.py` 


- # JMeter TS/TJ Time Measurement Report

| **Single-instance Version Test Plan**          | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | ![img 1](/img/http%201T.png)   | 235 ms                         | 153.68701550387595 ms                                  | 153.60271317829458 ms                        | With only one thread, the server processes requests sequentially, which accounts for the low average times. The times for Search Servlet and JDBC are almost the same, indicating that most of the servlet's time is spent executing the database query.           |
| Case 2: HTTP/10 threads                        | ![img 2](/img/http%2010T.png)   | 882 ms                         | 794.6528803545052 ms                                  | 794.5686853766617 ms                        | With 10 concurrent threads, the average times increase substantially. This is because the server must now handle multiple requests simultaneously. Connection pooling helps manage this concurrency efficiently.           |
| Case 3: HTTPS/10 threads                       | ![img 3](/img/https%2010T.png)  | 868 ms                         | 795.5774390243903 ms                                  | 795.4926829268293 ms                        | This case is similar to Case 2 but uses HTTPS instead of HTTP. The times are almost the same, suggesting that the added encryption/decryption overhead of HTTPS is negligible compared to the database query time.           |
| Case 4: HTTP/10 threads/No connection pooling  | ![img 4](/img/http%2010T%20NOCP.png)   | 859 ms                         | 780.3653039832285 ms                                  | 682.9661949685535 ms                        | Without connection pooling, the server must create a new database connection for each request and close it after it's not used, increasing the average time. However, closing connection pooling ensures proper resource reclamation and termination, resulting in faster average JDBC performance time.           |

| **Scaled Version Test Plan**                   | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | ![img 5](/img/http%201T%20gcppub.png)   | 270 ms                         | 151.08289241622575 ms                                  | 150.8641975308642 ms                        |  Similar to Case 1 in single-instance version, but running on a more powerful server (indicated by "gcppub", meaning "GCP public ip"). There's a slight increase in the average times, which might be due to differences in the server environment.           |
| Case 2: HTTP/10 threads                        | ![img 6](/img/http%2010T%20gcppub.png)  |   820 ms                       | 707.2462446351931 ms                                   | 707.1397532188842 ms                         | Similar to Case 2 in single-instance version, but running on a more powerful server. The average times are faster than in Case 2, since we enabled the load balancer where the read requests are automatically distributed among the available database instances between master and slave.                                                                                |
| Case 3: HTTP/10 threads/No connection pooling  | ![img 7](/img/http%2010T%20gcppub%20NOCP.png)   |  958ms                         | 842.4223722275796 ms                                  | 754.9048537447766 ms                        | Similar to Case 4 in single-instance version, but running on a more powerful server. Despite the more powerful server, the average times are still higher than when using connection pooling since the server needs to create a new database connection for each request, underscoring the benefits of connection pooling for handling concurrent requests.           |

- # Past Project Contribution List

### P1
Chengfeng Tang - Servlets + Json + Github + MySQL + Debug + Demo

Jingjing Wang - HTML + CSS + AWS + Jump Function + Project setup / management

### P2
Chengfeng Tang - Servlet + Js + HTML + Github + MySQL + Debug

Jingjing Wang - HTML + Session/Session Storage + CSS + AWS + Project setup + Demo

We use Like%input% for all substring mathces(title,director,name) and
COLLATEutf8mb4_general_ci for case insensitivity. Basically all string searche will return results that include the input anywhere in the string.

### P3
Chengfeng Tang - reCAPTCHA + Prepared Statement + XML

Jingjing Wang - HTTPS + Encryption + Dashboard + DEMO

File names with prepared statements: DashboardServlet, insertion, LoginEmployeeServlet, LoginServlet, MovieListServlet, Payment, SingleStarServlet, SingleMovieServlet


All insertion Optimizations:
1. Batch Insertion: Instead of inserting records one by one, batch insertion allows the database to take a group of records and insert them at once. Significantly reduced the amount of overhead in establishing database transactions, which in turn greatly improved the speed of the operation.
2. Created index on table star(name) and movies(title). It significantly sped up the data retrieval operations by allowing the database to find the data associated with a particular value much more quickly. Indexing the 'name' column of the 'star' table and the 'title' column of the 'movies' table allows for faster retrieval of these entities.
3. Use efficient data structures to store and manipulate the data. Arrays allow for fast retrieval of elements at any index, while hash maps allow for fast retrieval of values associated with any given key.
4. BadData: Keeping track of bad data or erroneous can help avoid repeating the same mistakes or wasting time on data that will not be useful. This involves keeping a list of records that have caused errors in the past or have been identified as incorrect or irrelevant.
5. NotFoundStars and NotFoundMovies: These act as caches for stars and movies that have been searched for but not found in the database. By keeping a record of these, the system can avoid wasting time on unsuccessful database queries. If the system tries to look up a star or movie that's not in the database, it can check this list first and avoid a potentially time-consuming database operation if the star or movie is listed there.


862,803 miliseconds without optimization

34,103 miliseconds after optimization


Inconsistent data reports:

12115 Movies, inerted 11690, 52 duplicates, 425 inconsistent (contains typo or invalid data).

6862 Stars, inserted 5969.

inserted 8757 genres_in_movies

inserted 8698 stars_in_movies

12575 stars not found.

417 movies not found.

See reports for details.

### P4

Chengfeng Tang - Full Text Search + Autocomplete + Fuzzy Search

Jingjing Wang - Andriod + Demo


### Website Link
https://wallamovie.store:8443/cs122b/login.html

### Demo Link
https://youtu.be/QHrfIfA03q4

