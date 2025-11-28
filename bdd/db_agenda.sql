/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50505
Source Host           : localhost:3366
Source Database       : db_agenda

Target Server Type    : MYSQL
Target Server Version : 50505
File Encoding         : 65001

Date: 2025-11-28 15:38:38
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for c_categoria
-- ----------------------------
DROP TABLE IF EXISTS `c_categoria`;
CREATE TABLE `c_categoria` (
  `pk_i_categoria` int(11) NOT NULL AUTO_INCREMENT,
  `d_v_categoria` varchar(20) NOT NULL,
  `c_v_usucap` varchar(20) NOT NULL,
  `d_f_fechcap` datetime NOT NULL,
  `w_v_status` varchar(1) NOT NULL,
  PRIMARY KEY (`pk_i_categoria`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

-- ----------------------------
-- Records of c_categoria
-- ----------------------------
INSERT INTO `c_categoria` VALUES ('1', 'CITA', 'CHARLY', '2025-11-26 20:36:00', 'A');
INSERT INTO `c_categoria` VALUES ('2', 'JUNTA', 'CHARLY', '2025-11-26 20:36:00', 'A');
INSERT INTO `c_categoria` VALUES ('3', 'ENTREGA DE PROYECTO', 'CHARLY', '2025-11-26 20:36:00', 'A');
INSERT INTO `c_categoria` VALUES ('4', 'EXAMEN', 'CHARLY', '2025-11-26 20:36:00', 'A');
INSERT INTO `c_categoria` VALUES ('5', 'OTRO', 'CHARLY', '2025-11-26 20:36:00', 'A');

-- ----------------------------
-- Table structure for c_estatus
-- ----------------------------
DROP TABLE IF EXISTS `c_estatus`;
CREATE TABLE `c_estatus` (
  `pk_i_estatus` int(11) NOT NULL AUTO_INCREMENT,
  `d_v_estatus` varchar(20) NOT NULL,
  `c_v_usucap` varchar(20) NOT NULL,
  `d_f_fechcap` datetime NOT NULL,
  `w_v_status` varchar(1) NOT NULL,
  PRIMARY KEY (`pk_i_estatus`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

-- ----------------------------
-- Records of c_estatus
-- ----------------------------
INSERT INTO `c_estatus` VALUES ('1', 'REALIZADO', 'CHARLY', '2025-11-26 20:09:00', 'A');
INSERT INTO `c_estatus` VALUES ('2', 'PENDIENTE', 'CHARLY', '2025-11-26 20:09:22', 'A');
INSERT INTO `c_estatus` VALUES ('3', 'APLAZADO', 'CHARLY', '2025-11-26 20:09:42', 'A');

-- ----------------------------
-- Table structure for d_lugar
-- ----------------------------
DROP TABLE IF EXISTS `d_lugar`;
CREATE TABLE `d_lugar` (
  `pk_i_evento` int(11) NOT NULL AUTO_INCREMENT,
  `c_n_longitud` longtext NOT NULL,
  `c_n_latitud` longtext NOT NULL,
  `c_v_usucap` varchar(20) DEFAULT NULL,
  `d_f_fechcap` datetime NOT NULL,
  `w_v_status` varchar(1) NOT NULL,
  PRIMARY KEY (`pk_i_evento`) USING BTREE,
  CONSTRAINT `fk_d_lugar_d_lugar_1` FOREIGN KEY (`pk_i_evento`) REFERENCES `m_evento` (`pk_i_evento`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

-- ----------------------------
-- Records of d_lugar
-- ----------------------------

-- ----------------------------
-- Table structure for m_evento
-- ----------------------------
DROP TABLE IF EXISTS `m_evento`;
CREATE TABLE `m_evento` (
  `pk_i_evento` int(11) NOT NULL AUTO_INCREMENT,
  `d_f_fechevento` date DEFAULT NULL,
  `d_t_horaevento` time DEFAULT NULL,
  `d_v_evento` varchar(20) NOT NULL,
  `pk_i_categoria` int(11) DEFAULT NULL,
  `pk_i_estatus` int(11) DEFAULT NULL,
  `c_v_usucap` varchar(20) NOT NULL,
  `d_f_fechcap` datetime NOT NULL,
  `w_v_status` varchar(1) NOT NULL,
  PRIMARY KEY (`pk_i_evento`) USING BTREE,
  KEY `fk_m_evento_m_evento_1` (`pk_i_categoria`),
  KEY `fk_m_evento_m_evento_2` (`pk_i_estatus`),
  CONSTRAINT `fk_m_evento_m_evento_1` FOREIGN KEY (`pk_i_categoria`) REFERENCES `c_categoria` (`pk_i_categoria`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_m_evento_m_evento_2` FOREIGN KEY (`pk_i_estatus`) REFERENCES `c_estatus` (`pk_i_estatus`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

-- ----------------------------
-- Records of m_evento
-- ----------------------------
