# Guestbook Application for Technical Demonstration

A simple Guestbook application, built on [SparkJava](http://www.sparkjava.com) and [AngularJS](http://www.angularjs.org), which can easily be deployed to Heroku. You will be making modifications to this application as part of the Technical Demonstration. It is acceptable for you to follow the below instructions immediately. However, you cannot commit code to this repository until explicitly told so.

[![Deploy to Heroku](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy?template=https://github.com/uscis-td/icy-dew-5284)

## First Time Setup

You will need to do the following to properly use this application:
* Sign up for a [Heroku](https://www.heroku.com/) account
* Install the [Heroku toolbelt](https://toolbelt.heroku.com/)
* Install [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* Install [Maven](https://maven.apache.org/download.cgi)
* Install [NodeJS](https://nodejs.org/)
* Install [grunt](http://gruntjs.com/)
* Install [PostgreSQL](https://www.postgresql.org/). If running OSX 10.7+, [PostreSQL.app](http://postgresapp.com/) contains all you should need.

Start a local instance of PostreSQL Server for use during local runs and automated tests. Run the following commands to prepare your local PostreSQL database.

```sh
$ psql
$ CREATE DATABASE demo_test;
$ CREATE DATABASE live;
$ \l # Check this list to ensure the "demo_test" and "live" databases exist.
$ \q
$
```

## Running Locally

Once you've completed the above, run the following commands to run this application locally.

```sh
$ git clone https://github.com/icy-dew-5284
$ cd icy-dew-5284
$ npm install
$ mvn clean install
$ heroku local
```

The Guestbook application should now be running on [localhost:5000](http://localhost:5000/).

## Running Tests

JUnit tests run every time `mvn clean install` is invoked. You can use the following to run both JUnit and Jasmine tests explicitly, as well as JSLint.

```sh
$ mvn clean install
$ grunt test
```

Jasmine tests can be run independently of JSLint by calling `grunt karma:single`. To run Jasmine tests continually on the JavaScript files, run `grunt karma:continuous`. Once this job is started, Jasmine tests will automatically run after every file save in a JavaScipt file.

## Building Reports

You can build and view JavaDoc and Java Checkstyle reports using Maven with the following commands. The [Google Java Style Guide](https://github.com/google/styleguide) is the style guide for this application.

```sh
$ mvn clean site
$ open target/site/project-reports.html
```

## Deploying to Heroku

You will be deploying this application to Heroku. You should have created a Heroku account and installed the Heroku Toolbelt by now. The following commands should allow you to push your application to Heroku. Alternatively, the button at the top of this document should automatically deploy this application to a new Heroku instance.

```sh
$ heroku login
Enter your Heroku credentials.
Email: <your.email.here@example.com>
Password:
$ heroku create
Creating <heroku-name>... done
http://<heroku-name>.herokuapp.com/ | https://git.heroku.com/<heroku-name>.git
Git remote heroku added
$ git push heroku master
$ heroku open
```
