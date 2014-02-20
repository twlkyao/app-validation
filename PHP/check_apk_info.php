<?php
    header("Content-Type: text/html; charset=utf-8") ;

    session_start(); // start the seesion
  
	$md5 = $_POST["md5"]; // The md5 checksum of the apk file.
	$sha1 = $_POST["sha1"]; // The sha1 checksum of the apk file.
	
	$serverAddress = "localhost";       // database server address
    $databaseUser = "root";               // database username
    $databasePassword = "jack";        // database password
    $databaseName = "software";            // database name
    $tableName = "softwareInfo";                   // table name
    
    $mysql_handle = mysql_connect($serverAddress, $databaseUser, $databasePassword)
        or die("Can't connect to the database server!". mysql_error()."<br>"); // connect to the database server
    
    mysql_query("set character set 'utf8'"); // Set the charset when read data base. 
	mysql_query("set names 'utf8'"); // Set the charset when write data base.
	
	
	/**
    Create the database
    */
    // Define the sql create database string for use
    $mysql_create_database = "CREATE DATABASE IF NOT EXISTS $databaseName"; 
                    
    // Execute the create database sql
    mysql_query($mysql_create_database)
        or die("Could not create the $databaseName database!".mysql_error()."<br>");
                     
    /**
    Select the database
    */
    mysql_select_db($databaseName, $mysql_handle)
        or die("Could not select the $databaseName database!".mysql_error()."<br>");

    // check whether the username and the password are correct
    $mysql_select = "SELECT * FROM $tableName WHERE md5 = '$md5' AND sha1 = '$sha1'"; // define the select sql query string
    $select_result= mysql_query($mysql_select)
        or die("Could not execute the SELECT operation! ".mysql_error()."<br>"); // get the SELECT result
    $result = mysql_fetch_array($select_result); // get the query result into array 
        
    
    $sessionid = session_id();	// Get the session id

    if(!is_array($result)){ // The file information is not in the database, that is the md5 and sha1 checksum is not identical.
        
        $result_array = array(  
            'flag' => 'false'
           // 'sessionid'=>$sessionid  
        ); 
        echo json_encode($result_array);	// Return the result encoded in JSON.
        
    } else { // The file information is already in the database and the md5 and sha1 checksum is identical.
	   
        $result_array = array(  
			'flag' => 'true'
            //'sessionid'=>$sessionid
        ); 
        echo json_encode($result_array); // Return the result encoded in JSON.
    }

    mysql_close($mysql_handle); // close the connection
?>
