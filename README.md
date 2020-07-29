# Learning2019-PostProcessing
Data post-processing for Learning 2019

## Commands to run code in this repository

You may need to be in the java-src folder to execute the following

```javac -cp mysql-connector-java-8.0.19.jar:out -d out L*.java```
```java -cp mysql-connector-java-8.0.19.jar:./out Learning2019GetAllOutputData "jdbc:mysql://learning-2019.cnnjljsiwvwt.us-east-2.rds.amazonaws.com/Learning2019" reader pwd data-2020-Mar-26.csv```