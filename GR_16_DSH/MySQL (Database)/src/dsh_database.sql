-- MySQL dump 10.13  Distrib 8.0.20, for Win64 (x86_64)
--
-- Host: localhost    Database: dsh_database
-- ------------------------------------------------------
-- Server version	8.0.20

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `account_request`
--

DROP TABLE IF EXISTS `account_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account_request` (
  `rid` int NOT NULL AUTO_INCREMENT,
  `rfirstname` varchar(32) NOT NULL,
  `rlastname` varchar(32) NOT NULL,
  `rbirthdate` date NOT NULL,
  `rgender` enum('F','M') NOT NULL,
  `rusername` varchar(32) NOT NULL,
  `rsid` char(12) NOT NULL,
  `rpassword` varchar(32) NOT NULL,
  `ryear` enum('First','Second','Third') NOT NULL,
  `rdepartment` varchar(64) NOT NULL,
  PRIMARY KEY (`rid`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account_request`
--

LOCK TABLES `account_request` WRITE;
/*!40000 ALTER TABLE `account_request` DISABLE KEYS */;
/*!40000 ALTER TABLE `account_request` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin` (
  `aid` int NOT NULL AUTO_INCREMENT,
  `ausername` varchar(32) NOT NULL,
  `apassword` varchar(32) NOT NULL,
  `afirstname` varchar(32) NOT NULL,
  `alastname` varchar(32) NOT NULL,
  PRIMARY KEY (`aid`),
  UNIQUE KEY `ausername` (`ausername`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES (2,'admin','admin','Admin','Admin');
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `consultation`
--

DROP TABLE IF EXISTS `consultation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `consultation` (
  `cid` int NOT NULL AUTO_INCREMENT,
  `pid` varchar(32) NOT NULL,
  `subid` int NOT NULL,
  `cstart` datetime NOT NULL,
  `cend` datetime NOT NULL,
  `cdescription` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`cid`),
  KEY `subid` (`subid`,`pid`),
  CONSTRAINT `consultation_ibfk_1` FOREIGN KEY (`subid`, `pid`) REFERENCES `professor_subject` (`subid`, `pid`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consultation`
--

LOCK TABLES `consultation` WRITE;
/*!40000 ALTER TABLE `consultation` DISABLE KEYS */;
/*!40000 ALTER TABLE `consultation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `professor`
--

DROP TABLE IF EXISTS `professor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `professor` (
  `pid` char(12) NOT NULL,
  `pfirstname` varchar(32) NOT NULL,
  `plastname` varchar(32) NOT NULL,
  `pusername` varchar(32) NOT NULL,
  `pbirthdate` date NOT NULL,
  `pgender` enum('F','M') NOT NULL,
  `ppassword` varchar(32) NOT NULL,
  PRIMARY KEY (`pid`),
  UNIQUE KEY `pusername` (`pusername`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `professor`
--

LOCK TABLES `professor` WRITE;
/*!40000 ALTER TABLE `professor` DISABLE KEYS */;
/*!40000 ALTER TABLE `professor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `professor_subject`
--

DROP TABLE IF EXISTS `professor_subject`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `professor_subject` (
  `subid` int NOT NULL,
  `pid` varchar(32) NOT NULL,
  PRIMARY KEY (`subid`,`pid`),
  KEY `pid` (`pid`),
  CONSTRAINT `professor_subject_ibfk_1` FOREIGN KEY (`subid`) REFERENCES `subject` (`subid`),
  CONSTRAINT `professor_subject_ibfk_2` FOREIGN KEY (`pid`) REFERENCES `professor` (`pid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `professor_subject`
--

LOCK TABLES `professor_subject` WRITE;
/*!40000 ALTER TABLE `professor_subject` DISABLE KEYS */;
/*!40000 ALTER TABLE `professor_subject` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student`
--

DROP TABLE IF EXISTS `student`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student` (
  `sid` char(12) NOT NULL,
  `sfirstname` varchar(32) NOT NULL,
  `slastname` varchar(32) NOT NULL,
  `sbirthdate` date NOT NULL,
  `sgender` enum('F','M') NOT NULL,
  `susername` varchar(32) NOT NULL,
  `spassword` char(32) NOT NULL,
  `syear` enum('First','Second','Third') NOT NULL,
  `sdepartment` varchar(64) NOT NULL,
  PRIMARY KEY (`sid`),
  UNIQUE KEY `susername` (`susername`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student`
--

LOCK TABLES `student` WRITE;
/*!40000 ALTER TABLE `student` DISABLE KEYS */;
/*!40000 ALTER TABLE `student` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subject`
--

DROP TABLE IF EXISTS `subject`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subject` (
  `subid` int NOT NULL AUTO_INCREMENT,
  `subname` varchar(32) NOT NULL,
  `subyear` enum('First','Second','Third') DEFAULT NULL,
  PRIMARY KEY (`subid`),
  UNIQUE KEY `subname` (`subname`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subject`
--

LOCK TABLES `subject` WRITE;
/*!40000 ALTER TABLE `subject` DISABLE KEYS */;
/*!40000 ALTER TABLE `subject` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-06-12 23:28:24
