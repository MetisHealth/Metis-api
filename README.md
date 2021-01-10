# METIS

<p align="center">
  <img width="128" height="128" src="/src/main/resources/public/images/metis.png">
  <br/>
  <p>
    <img src="https://travis-ci.com/yigitcolakoglu/Metis.svg?branch=main">
    <img src="https://heroku-badge.herokuapp.com/?app=metis-app">
  </p>
</p>

Metis is a web application that allows medical clinic owners to easily manage their appointments and their patients. It is primarily designed for use in Turkey, with several specific features. It runs with SpringBoot, uses MySQL to store data and redis to manage sessions. 

## Screenshots

![Login Page](/imgs/screenshots/login.png)
![Appointment Page](/imgs/screenshots/calendar.png)
![Patient Page](/imgs/screenshots/patients.png)

## How to Install?  

*(Those steps assume you have MySQL and Redis installed on your system)*
* Grab the source code or the latest build.
* Edit `config/application.properties` according to your setup. (Create the file and folder if it does not exist)
* Run gradle bootRun. This will create the clinic database.
* Enter to the mysql console and run the command.
    ```mysql
	INSERT INTO `clinic`.`user` (`id`,
	                             `hes_code`,
	                             `tc_no`, 
	                             `email`,   
	                             `enabled`,   
	                             `expired`,   
	                             `expired_creds`,  
	                             `locked`,  
	                             `name`,  
	                             `password`,  
	                             `phone`,  
	                             `role`)  
	VALUES ('1',  
            '111111',  
            '1111111111',   
            'yigitcolakoglu@hotmail.com',
            b'1',   
            b'0',   
            b'0',   
            b'0',   
            'Yigit Colakoglu',   
            '$2y$12$/lDkf3rn/Qe4foYzc4/qVehKDICmlB9acNSRphPCCQmMCnLtcvrsC', 
            b'1111111',   
            'ADMIN'); 
    ```
* Visit localhost:8080 and login using `yigitcolakoglu@hotmail.com` and `password`
 
## What's in the oven?

* Life Fits into Home integration.
* Admin page
* Automatic receipt generation
* Automatic zoom link generation
* Profile page and personalization
