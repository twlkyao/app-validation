<!--
    作者： 齐士垚
    日期： 2013.11.18
    功能： 处理文件上传，文件审核通过后，将上传文件保存在本地文件夹。
    Author: Qi Shiyao
    Data:   2013.11.18
    Function:   Handle the uploaded file, if the file passed the audit, then store the file into specified folder.
    Problem: The apk uploaded should be deleted, the apk should first be checked whether is in the data base.
-->
<html>
    <head>
        <title>文件上传</title>
        <meta http-equiv="Content-Type" content="text/html;
            charset=UTF-8">
    </head>
        <body>
            <?php
                //Specify the folder and the name of the file to store
                $upload_dir = getcwd()."/upload_apks/"; //Construct the filepath according to the current working directory
                //$upload_file = $upload_dir.iconv("UTF-8", "Gb2312", $_FILES["myapk"]["name"]); //Change the code type of the filename
                $upload_file = $upload_dir. $_FILES['myapk']['name']; //Construct the filepath to save the file
                
				// The file type filter.
                $file_type = "application/vnd.android.package-archive";
                // Deals with the file type.
                if($file_type != $_FILES['myapk']['type']) {
					echo "File is not a apk file.<br>";
					echo "<p><a href='javascript:history.back()'>重新上传</p>";
					return;
				}
                
                //$mysql_handle; //The handle of the MySQL database
                $serverAddress = "localhost"; //The address of the MySQL server
                $username = "root"; //The username of the MySQL server
                $password = "jack"; //The password of the MySQL server
                $databaseName="software"; //The name of the database
                $tableName = "softwareInfo"; //The name of the softwareInfo table
                
                
                //Move the temp file into the specified filepath
                if(0 == $_FILES['myapk']['error']) {

                    echo "<strong>文件上传成功！</strong><hr>";
                    //Show the file infomation
                    echo "文件名：".$_FILES['myapk']['name']."<br>";
                    echo "临时保存文件名：".$_FILES['myapk']['tmp_name']."<br>";
                    echo "文件大小：" .($_FILES['myapk']['size']/1024)."KB<br>";
                    echo "文件种类：" . $_FILES['myapk']['type'] . "<br>";
                    
                    if (file_exists($upload_file)) {   //文件已经存在
                        echo $_FILES['myapk']['name'] . "已经存在！<br>";
                        echo "软件已经通过审核！<br>";
                    } else { //文件不存在
                        move_uploaded_file($_FILES['myapk']['tmp_name'], $upload_file);
                        echo "文件保存路径：" .$upload_file."<br>";
                        
                    /**
                    Store the file information into the database
                    */
                    //Connect to the database server
                    $mysql_handle = mysql_connect($serverAddress, $username, $password)
                        or die("Could not connect to the database server!".mysql_error()."<br>"); //Could not connected to the database server

					mysql_query("set character set 'utf8'"); // Set the charset of reading data base.
					mysql_query("set names 'utf8'"); // Set the charset of writing data base.

                    // Include the get_apk_info script
                    include_once('get_apk_info.php');
                    
                    // Get the apk information
                    $md5_value = md5_file($upload_file); // Get the md5 value of the apk file.
                    $sha1_value = sha1_file($upload_file); // Get the sha1 value of the apk file.
                    
                    $aapt_file = "/opt/android-sdk-linux/build-tools/19.0.0/aapt"; // The aapt file path.
                    $apk_info = readApkInfoFromFile($aapt_file, $upload_file); // Call the function to get the apk info array.
                    
                    $version_name = $apk_info['version']; // Get the apk version.
                    
                    $name = $apk_info['lable']; // Get the apk name.
                    
                    /**
                    Create the database if not exists
                    */
                    // Define the sql create database string for use
                    $mysql_create_database = "CREATE DATABASE IF NOT EXISTS $databaseName"; 
                    
                    // Execute the create database sql
                    mysql_query($mysql_create_database)
                        or die("Could not create the $databaseName database!");
                     
                    /**
                    Select the database
                    */
                    mysql_select_db($databaseName, $mysql_handle)
                        or die("Could not select the $databaseName database!");
                    
                    /**
                    Create the table
                    */
                    /**
                    id: software id
                    name: software name
                    version_name: software version name
                    md5: software md5
                    sha1: software sha1
                    charset: utf8
                    primary key: id, name, version_name
                    */
                    $mysql_create_table = "CREATE TABLE IF NOT EXISTS $tableName (
                        id int(10) AUTO_INCREMENT, 
                        name varchar(40),
                        version_name varchar(40),
                        md5 varchar(32),
                        sha1 varchar(40),
                        PRIMARY KEY(id, name, version_name)                
                    )charset=utf8"; // Define the create table sql
                    //PRIMARY KEY(name, md5, sha1)
                    
                    mysql_query($mysql_create_table, $mysql_handle)
                        or die("Could not create the $tableName table!"); // Execute the create table sql
                    
                    /**
                    Judge if the software is already in the table
                    */
                    
                    // Define the select sql query string to check whether the apk file is exist or not.
                    $mysql_select = "SELECT * FROM $tableName WHERE name = '$name' AND version_name = '$version_name' AND md5 = '$md5_value' AND sha1 = '$sha1_value'"; 
                    $select_result = mysql_query($mysql_select); // Get the value from the table.
                    
                    $result = mysql_fetch_array($select_result); // Get the query result into array 
                    
                    // The apk is not in the data base, insert the information into data base.
                    if(!is_array($result)) {
						/**
						Insert the values into the softwareInfo table
						*/
						//Define the sql insert string for use
						$sql_insert="INSERT IGNORE INTO $tableName (name, version_name, md5,sha1)
							VALUES ('$name', '$version_name', '$md5_value', '$sha1_value')"; // The value string to be inserted into the database on condition that there are no records
						
						//echo $sql_insert."<br>";
						
						mysql_query($sql_insert)
							or die("Could not insert into the $tableName table"); // Execute the insert function
						
						// Echo the table content
						echo "<br><strong>The content of the table $tableName is as followed:</strong><br>";
						
						/**
						Select the values of the softwareInfo table
						*/
						//Define the sql select string for use
						$sql_select = "SELECT * from $tableName";
						
						//Execute the SQL query and return records
						$result = mysql_query($sql_select)
							or die(mysql_error()); //Get the select records

						//Fetch tha data from the database 
						while ($row = mysql_fetch_array($result)) {
							echo "id：".$row{'id'}." name：".$row{'name'}
								." version_name：".$row{'version_name'}." md5：".$row{'md5'}
									." sha1：".$row{'sha1'}."<br>";
						}
					   
						//Close the connection
						mysql_close($mysql_handle);
					} else { // The apk file is already in the database;
						echo "The apk is already published!<br>";
					}
					   
                  }
                    echo "<p><a href='javascript:history.back()'>继续上传</a></p>";
                } else {
                    echo "文件上传失败(".$_FILES["myapk"]["error"].")<br><br>";
                    echo "<p><a href='javascript:history.back()'>重新上传</p>";
                }
            ?>
    </body>
</html>
