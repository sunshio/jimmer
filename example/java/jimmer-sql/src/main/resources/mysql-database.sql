/*
 Navicat Premium Data Transfer

 Source Server         : 127.0.0.1
 Source Server Type    : MySQL
 Source Server Version : 50728 (5.7.28-log)
 Source Host           : localhost:3306
 Source Schema         : q

 Target Server Type    : MySQL
 Target Server Version : 50728 (5.7.28-log)
 File Encoding         : 65001

 Date: 15/05/2023 23:46:28
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for author
-- ----------------------------
DROP TABLE IF EXISTS `author`;
CREATE TABLE `author` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `first_name` varchar(25) COLLATE utf8mb4_unicode_ci NOT NULL,
  `last_name` varchar(25) COLLATE utf8mb4_unicode_ci NOT NULL,
  `gender` char(1) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_time` datetime NOT NULL,
  `modified_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of author
-- ----------------------------
BEGIN;
INSERT INTO `author` (`id`, `first_name`, `last_name`, `gender`, `created_time`, `modified_time`) VALUES (1, 'Eve', 'Procello', 'F', '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `author` (`id`, `first_name`, `last_name`, `gender`, `created_time`, `modified_time`) VALUES (2, 'Alex', 'Banks', 'M', '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `author` (`id`, `first_name`, `last_name`, `gender`, `created_time`, `modified_time`) VALUES (3, 'Dan', 'Vanderkam', 'M', '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `author` (`id`, `first_name`, `last_name`, `gender`, `created_time`, `modified_time`) VALUES (4, 'Boris', 'Cherny', 'M', '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `author` (`id`, `first_name`, `last_name`, `gender`, `created_time`, `modified_time`) VALUES (5, 'Samer', 'Buna', 'M', '2023-05-15 23:43:58', '2023-05-15 23:43:58');
COMMIT;

-- ----------------------------
-- Table structure for book
-- ----------------------------
DROP TABLE IF EXISTS `book`;
CREATE TABLE `book` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `edition` int(11) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `store_id` bigint(20) DEFAULT NULL,
  `tenant` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_time` datetime NOT NULL,
  `modified_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of book
-- ----------------------------
BEGIN;
INSERT INTO `book` (`id`, `name`, `edition`, `price`, `store_id`, `tenant`, `created_time`, `modified_time`) VALUES (1, 'Learning GraphQL', 1, 50.00, 1, 'a', '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `book` (`id`, `name`, `edition`, `price`, `store_id`, `tenant`, `created_time`, `modified_time`) VALUES (2, 'Learning GraphQL', 2, 55.00, 1, 'b', '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `book` (`id`, `name`, `edition`, `price`, `store_id`, `tenant`, `created_time`, `modified_time`) VALUES (3, 'Learning GraphQL', 3, 51.00, 1, 'a', '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `book` (`id`, `name`, `edition`, `price`, `store_id`, `tenant`, `created_time`, `modified_time`) VALUES (4, 'Effective TypeScript', 1, 73.00, 1, 'b', '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `book` (`id`, `name`, `edition`, `price`, `store_id`, `tenant`, `created_time`, `modified_time`) VALUES (5, 'Effective TypeScript', 2, 69.00, 1, 'a', '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `book` (`id`, `name`, `edition`, `price`, `store_id`, `tenant`, `created_time`, `modified_time`) VALUES (6, 'Effective TypeScript', 3, 88.00, 1, 'b', '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `book` (`id`, `name`, `edition`, `price`, `store_id`, `tenant`, `created_time`, `modified_time`) VALUES (7, 'Programming TypeScript', 1, 47.50, 1, 'a', '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `book` (`id`, `name`, `edition`, `price`, `store_id`, `tenant`, `created_time`, `modified_time`) VALUES (8, 'Programming TypeScript', 2, 45.00, 1, 'b', '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `book` (`id`, `name`, `edition`, `price`, `store_id`, `tenant`, `created_time`, `modified_time`) VALUES (9, 'Programming TypeScript', 3, 48.00, 1, 'a', '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `book` (`id`, `name`, `edition`, `price`, `store_id`, `tenant`, `created_time`, `modified_time`) VALUES (10, 'GraphQL in Action', 1, 80.00, 2, 'b', '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `book` (`id`, `name`, `edition`, `price`, `store_id`, `tenant`, `created_time`, `modified_time`) VALUES (11, 'GraphQL in Action', 2, 81.00, 2, 'a', '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `book` (`id`, `name`, `edition`, `price`, `store_id`, `tenant`, `created_time`, `modified_time`) VALUES (12, 'GraphQL in Action', 3, 80.00, 2, 'b', '2023-05-15 23:43:58', '2023-05-15 23:43:58');
COMMIT;

-- ----------------------------
-- Table structure for book_author_mapping
-- ----------------------------
DROP TABLE IF EXISTS `book_author_mapping`;
CREATE TABLE `book_author_mapping` (
  `book_id` bigint(20) NOT NULL,
  `author_id` bigint(20) NOT NULL,
  PRIMARY KEY (`book_id`,`author_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of book_author_mapping
-- ----------------------------
BEGIN;
INSERT INTO `book_author_mapping` (`book_id`, `author_id`) VALUES (1, 1);
INSERT INTO `book_author_mapping` (`book_id`, `author_id`) VALUES (1, 2);
INSERT INTO `book_author_mapping` (`book_id`, `author_id`) VALUES (2, 1);
INSERT INTO `book_author_mapping` (`book_id`, `author_id`) VALUES (2, 2);
INSERT INTO `book_author_mapping` (`book_id`, `author_id`) VALUES (3, 1);
INSERT INTO `book_author_mapping` (`book_id`, `author_id`) VALUES (3, 2);
INSERT INTO `book_author_mapping` (`book_id`, `author_id`) VALUES (4, 3);
INSERT INTO `book_author_mapping` (`book_id`, `author_id`) VALUES (5, 3);
INSERT INTO `book_author_mapping` (`book_id`, `author_id`) VALUES (6, 3);
INSERT INTO `book_author_mapping` (`book_id`, `author_id`) VALUES (7, 4);
INSERT INTO `book_author_mapping` (`book_id`, `author_id`) VALUES (8, 4);
INSERT INTO `book_author_mapping` (`book_id`, `author_id`) VALUES (9, 4);
INSERT INTO `book_author_mapping` (`book_id`, `author_id`) VALUES (10, 5);
INSERT INTO `book_author_mapping` (`book_id`, `author_id`) VALUES (11, 5);
INSERT INTO `book_author_mapping` (`book_id`, `author_id`) VALUES (12, 5);
COMMIT;

-- ----------------------------
-- Table structure for book_store
-- ----------------------------
DROP TABLE IF EXISTS `book_store`;
CREATE TABLE `book_store` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `website` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_time` datetime NOT NULL,
  `modified_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `business_key_book_store` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of book_store
-- ----------------------------
BEGIN;
INSERT INTO `book_store` (`id`, `name`, `website`, `created_time`, `modified_time`) VALUES (1, 'O\'REILLY', NULL, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `book_store` (`id`, `name`, `website`, `created_time`, `modified_time`) VALUES (2, 'MANNING', NULL, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
COMMIT;

-- ----------------------------
-- Table structure for tree_node
-- ----------------------------
DROP TABLE IF EXISTS `tree_node`;
CREATE TABLE `tree_node` (
  `node_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  `created_time` datetime NOT NULL,
  `modified_time` datetime NOT NULL,
  PRIMARY KEY (`node_id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of tree_node
-- ----------------------------
BEGIN;
INSERT INTO `tree_node` (`node_id`, `name`, `parent_id`, `created_time`, `modified_time`) VALUES (1, 'Home', NULL, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `tree_node` (`node_id`, `name`, `parent_id`, `created_time`, `modified_time`) VALUES (2, 'Food', 1, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `tree_node` (`node_id`, `name`, `parent_id`, `created_time`, `modified_time`) VALUES (3, 'Drinks', 2, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `tree_node` (`node_id`, `name`, `parent_id`, `created_time`, `modified_time`) VALUES (4, 'Coca Cola', 3, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `tree_node` (`node_id`, `name`, `parent_id`, `created_time`, `modified_time`) VALUES (5, 'Fanta', 3, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `tree_node` (`node_id`, `name`, `parent_id`, `created_time`, `modified_time`) VALUES (6, 'Bread', 2, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `tree_node` (`node_id`, `name`, `parent_id`, `created_time`, `modified_time`) VALUES (7, 'Baguette', 6, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `tree_node` (`node_id`, `name`, `parent_id`, `created_time`, `modified_time`) VALUES (8, 'Ciabatta', 6, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `tree_node` (`node_id`, `name`, `parent_id`, `created_time`, `modified_time`) VALUES (9, 'Clothing', 1, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `tree_node` (`node_id`, `name`, `parent_id`, `created_time`, `modified_time`) VALUES (10, 'Woman', 9, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `tree_node` (`node_id`, `name`, `parent_id`, `created_time`, `modified_time`) VALUES (11, 'Casual wear', 10, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `tree_node` (`node_id`, `name`, `parent_id`, `created_time`, `modified_time`) VALUES (12, 'Dress', 11, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `tree_node` (`node_id`, `name`, `parent_id`, `created_time`, `modified_time`) VALUES (13, 'Miniskirt', 11, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `tree_node` (`node_id`, `name`, `parent_id`, `created_time`, `modified_time`) VALUES (14, 'Jeans', 11, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `tree_node` (`node_id`, `name`, `parent_id`, `created_time`, `modified_time`) VALUES (15, 'Formal wear', 10, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `tree_node` (`node_id`, `name`, `parent_id`, `created_time`, `modified_time`) VALUES (16, 'Suit', 15, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `tree_node` (`node_id`, `name`, `parent_id`, `created_time`, `modified_time`) VALUES (17, 'Shirt', 15, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `tree_node` (`node_id`, `name`, `parent_id`, `created_time`, `modified_time`) VALUES (18, 'Man', 9, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `tree_node` (`node_id`, `name`, `parent_id`, `created_time`, `modified_time`) VALUES (19, 'Casual wear', 18, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `tree_node` (`node_id`, `name`, `parent_id`, `created_time`, `modified_time`) VALUES (20, 'Jacket', 19, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `tree_node` (`node_id`, `name`, `parent_id`, `created_time`, `modified_time`) VALUES (21, 'Jeans', 19, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `tree_node` (`node_id`, `name`, `parent_id`, `created_time`, `modified_time`) VALUES (22, 'Formal wear', 18, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `tree_node` (`node_id`, `name`, `parent_id`, `created_time`, `modified_time`) VALUES (23, 'Suit', 22, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
INSERT INTO `tree_node` (`node_id`, `name`, `parent_id`, `created_time`, `modified_time`) VALUES (24, 'Shirt', 22, '2023-05-15 23:43:58', '2023-05-15 23:43:58');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
