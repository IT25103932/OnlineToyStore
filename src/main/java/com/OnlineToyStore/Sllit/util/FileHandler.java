package com.OnlineToyStore.Sllit.util;

/**
 * FileHandler — Utility class for file I/O operations.
 *
 * In this project, data is persisted using plain text files (.txt)
 * instead of a database. Each Service class uses BufferedReader and
 * BufferedWriter to read and write data.
 *
 * This class exists as a placeholder demonstrating the Abstraction
 * principle — file handling logic is separated into the util layer
 * so Service classes focus only on business logic.
 *
 * In a production system, this would be replaced by JPA/Hibernate
 * with a relational database.
 */
public class FileHandler {
    // File operations are handled directly in each Service class
    // using BufferedReader and BufferedWriter for simplicity.
}