/*
 Navicat MySQL Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 50744
 Source Host           : localhost:3306
 Source Schema         : library

 Target Server Type    : MySQL
 Target Server Version : 50744
 File Encoding         : 65001

 Date: 09/01/2026 21:55:22
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for books
-- ----------------------------
DROP TABLE IF EXISTS `books`;
CREATE TABLE `books`  (
  `book_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '图书ID',
  `book_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `author` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `isbn` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `category` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `branch_id` int(11) NOT NULL COMMENT '所属分馆ID',
  `book_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `total_num` int(11) NOT NULL DEFAULT 0 COMMENT '总数量',
  `available_num` int(11) NOT NULL DEFAULT 0 COMMENT '可借数量',
  `status` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`book_id`) USING BTREE,
  UNIQUE INDEX `isbn`(`isbn`) USING BTREE,
  INDEX `idx_book_name`(`book_name`) USING BTREE,
  INDEX `idx_isbn`(`isbn`) USING BTREE,
  INDEX `idx_branch_category`(`branch_id`, `category`) USING BTREE,
  CONSTRAINT `books_ibfk_1` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`branch_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '图书信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for borrow_records
-- ----------------------------
DROP TABLE IF EXISTS `borrow_records`;
CREATE TABLE `borrow_records`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `book_id` bigint(20) NOT NULL,
  `borrow_time` datetime(6) NOT NULL,
  `branch_id` int(11) NOT NULL,
  `due_time` datetime(6) NOT NULL,
  `overdue_days` int(11) NULL DEFAULT NULL,
  `reminder_count` int(11) NULL DEFAULT NULL,
  `return_time` datetime(6) NULL DEFAULT NULL,
  `status` enum('BORROWED','OVERDUE','RETURNED') CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_branch_status`(`user_id`, `branch_id`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for branches
-- ----------------------------
DROP TABLE IF EXISTS `branches`;
CREATE TABLE `branches`  (
  `branch_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '分馆ID',
  `branch_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`branch_id`) USING BTREE,
  UNIQUE INDEX `branch_name`(`branch_name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '分馆信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of branches
-- ----------------------------
INSERT INTO `branches` VALUES (1, '校本部', '', '2026-01-09 00:37:27');
INSERT INTO `branches` VALUES (2, '黄塘', '', '2026-01-09 00:37:27');
INSERT INTO `branches` VALUES (3, '程江', '', '2026-01-09 00:37:27');
INSERT INTO `branches` VALUES (4, '江南', '', '2026-01-09 00:37:27');
INSERT INTO `branches` VALUES (5, '丰顺', '', '2026-01-09 00:37:27');

-- ----------------------------
-- Table structure for email_notifications
-- ----------------------------
DROP TABLE IF EXISTS `email_notifications`;
CREATE TABLE `email_notifications`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `create_time` datetime(6) NOT NULL,
  `email` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `error_message` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `send_time` datetime(6) NULL DEFAULT NULL,
  `status` enum('FAILED','PENDING','SENT') CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `subject` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `type` enum('OVERDUE_REMINDER','RESERVATION_AVAILABLE','RESERVATION_EXPIRY','RESERVATION_NOTIFY_LENDER') CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for fines
-- ----------------------------
DROP TABLE IF EXISTS `fines`;
CREATE TABLE `fines`  (
  `fine_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '罚款ID',
  `record_id` int(11) NOT NULL COMMENT '关联借阅记录ID',
  `fine_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '罚款金额（1元/天）',
  `pay_status` enum('unpaid','paid') CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT 'unpaid' COMMENT '支付状态：未支付/已支付',
  `pay_time` datetime(0) NULL DEFAULT NULL COMMENT '支付时间',
  PRIMARY KEY (`fine_id`) USING BTREE,
  INDEX `idx_record_id`(`record_id`) USING BTREE,
  INDEX `idx_pay_status`(`pay_status`) USING BTREE,
  INDEX `idx_pay_status_record`(`pay_status`, `record_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '逾期罚款记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for reservations
-- ----------------------------
DROP TABLE IF EXISTS `reservations`;
CREATE TABLE `reservations`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `book_id` bigint(20) NOT NULL,
  `branch_id` int(11) NOT NULL,
  `expiry_time` datetime(6) NOT NULL,
  `reserve_time` datetime(6) NOT NULL,
  `status` enum('CANCELLED','COMPLETED','PENDING','READY') CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_book_user_status`(`book_id`, `user_id`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for role_permissions
-- ----------------------------
DROP TABLE IF EXISTS `role_permissions`;
CREATE TABLE `role_permissions`  (
  `role_id` bigint(20) NOT NULL,
  `permission` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  INDEX `FKn5fotdgk8d1xvo8nav9uv3muc`(`role_id`) USING BTREE,
  CONSTRAINT `FKn5fotdgk8d1xvo8nav9uv3muc` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role_permissions
-- ----------------------------
INSERT INTO `role_permissions` VALUES (1, 'user:read');
INSERT INTO `role_permissions` VALUES (1, 'borrow:read');
INSERT INTO `role_permissions` VALUES (1, 'user:create');
INSERT INTO `role_permissions` VALUES (1, 'user:delete');
INSERT INTO `role_permissions` VALUES (1, 'user:update');
INSERT INTO `role_permissions` VALUES (1, 'book:read');
INSERT INTO `role_permissions` VALUES (1, 'book:create');
INSERT INTO `role_permissions` VALUES (1, 'book:delete');
INSERT INTO `role_permissions` VALUES (1, 'book:update');
INSERT INTO `role_permissions` VALUES (1, 'borrow:create');
INSERT INTO `role_permissions` VALUES (1, 'borrow:delete');
INSERT INTO `role_permissions` VALUES (1, 'borrow:update');
INSERT INTO `role_permissions` VALUES (2, 'borrow:update');
INSERT INTO `role_permissions` VALUES (2, 'book:read');
INSERT INTO `role_permissions` VALUES (2, 'book:update');
INSERT INTO `role_permissions` VALUES (2, 'user:create');
INSERT INTO `role_permissions` VALUES (2, 'user:read');
INSERT INTO `role_permissions` VALUES (2, 'borrow:read');
INSERT INTO `role_permissions` VALUES (2, 'borrow:create');
INSERT INTO `role_permissions` VALUES (2, 'book:create');
INSERT INTO `role_permissions` VALUES (2, 'user:update');
INSERT INTO `role_permissions` VALUES (3, 'book:read');
INSERT INTO `role_permissions` VALUES (3, 'borrow:read');
INSERT INTO `role_permissions` VALUES (3, 'borrow:create');
INSERT INTO `role_permissions` VALUES (4, 'book:read');
INSERT INTO `role_permissions` VALUES (4, 'borrow:read');
INSERT INTO `role_permissions` VALUES (4, 'borrow:create');

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `system_role` bit(1) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKofx66keruapi6vyqpv6f2or37`(`name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of roles
-- ----------------------------
INSERT INTO `roles` VALUES (1, '系统管理员', 'ROLE_SYSTEM_ADMIN', b'1');
INSERT INTO `roles` VALUES (2, '分馆管理员', 'ROLE_BRANCH_ADMIN', b'1');
INSERT INTO `roles` VALUES (3, '教师', 'ROLE_TEACHER', b'1');
INSERT INTO `roles` VALUES (4, '学生', 'ROLE_STUDENT', b'1');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` int(11) NULL DEFAULT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `phone` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `real_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `status` enum('ACTIVE','DELETED','INACTIVE','LOCKED') CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `update_time` datetime(6) NULL DEFAULT NULL,
  `user_type` enum('BRANCH_ADMIN','STUDENT','SYSTEM_ADMIN','TEACHER') CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `username` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `role_id` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK6dotkott2kjsp8vw4d0m25fb7`(`email`) USING BTREE,
  UNIQUE INDEX `UKr43af9ap4edm43mmtq01oddj6`(`username`) USING BTREE,
  INDEX `FKp56c1712k691lhsyewcssf40f`(`role_id`) USING BTREE,
  CONSTRAINT `FKp56c1712k691lhsyewcssf40f` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 1, '2026-01-08 23:40:02.047687', 'admin@library.com', '$2a$10$XsL/dnY5KjNDzBxN4t/Zwu1D3lPjuffo3eVw5T5qoc4QtJgz68UlO', NULL, '系统管理员', 'ACTIVE', '2026-01-08 23:40:02.047687', 'SYSTEM_ADMIN', 'admin', 1);
INSERT INTO `users` VALUES (2, 1, '2026-01-08 23:40:02.142406', 'branchadmin1@library.com', '$2a$10$gyIGQrtK5liZNaOwpMdCg.HBK1jQxbmkI/PVlkIIy.r7JDOkWl2Ge', NULL, '校本部管理员', 'ACTIVE', '2026-01-08 23:40:02.142406', 'BRANCH_ADMIN', 'branchadmin1', 2);
INSERT INTO `users` VALUES (3, 2, '2026-01-08 23:40:02.239476', 'branchadmin2@library.com', '$2a$10$ODoEgRQGf4UpA5fqISsT/uvRLOwNmjmYpZSQSWGQN3rNMhVf4I/XC', NULL, '黄塘校区管理员', 'ACTIVE', '2026-01-08 23:40:02.239476', 'BRANCH_ADMIN', 'branchadmin2', 2);
INSERT INTO `users` VALUES (4, 3, '2026-01-08 23:40:02.333263', 'branchadmin3@library.com', '$2a$10$ZSKovxNp/.wox4m87un.lONKVFkFdCsX1cw/MKS9bQIU3wy/V7I0S', NULL, '程江校区管理员', 'ACTIVE', '2026-01-08 23:40:02.333263', 'BRANCH_ADMIN', 'branchadmin3', 2);
INSERT INTO `users` VALUES (5, 4, '2026-01-08 23:40:02.427606', 'branchadmin4@library.com', '$2a$10$DjTryC2L0sH1Lhagq8iF5eMv5CFp03TUyx8d5YeqfGrba/glqiCmi', NULL, '江南校区管理员', 'ACTIVE', '2026-01-08 23:40:02.427606', 'BRANCH_ADMIN', 'branchadmin4', 2);
INSERT INTO `users` VALUES (6, 5, '2026-01-08 23:40:02.521900', 'branchadmin5@library.com', '$2a$10$Xiw0.0Nk4pChg2yGbjt4IO205xgPIrHNA0eC9Qs/IViwxRWNvJKt6', NULL, '丰顺校区管理员', 'ACTIVE', '2026-01-08 23:40:02.521900', 'BRANCH_ADMIN', 'branchadmin5', 2);
INSERT INTO `users` VALUES (7, 1, '2026-01-08 23:40:02.617390', 'teacher1@school.com', '$2a$10$EZxOFbLK9YRqwCCkowqKo.0kcfPOHcV2xwvDngRDb6ojssPLfcXUO', NULL, '张老师', 'ACTIVE', '2026-01-08 23:40:02.617390', 'TEACHER', 'teacher1', 3);
INSERT INTO `users` VALUES (8, 2, '2026-01-08 23:40:02.713260', 'teacher2@school.com', '$2a$10$AuFot8NLLupPhkbbh9VQkelmzIcLTejl0gMTbZ03CMM3OYjyko.8K', NULL, '王老师', 'ACTIVE', '2026-01-08 23:40:02.713815', 'TEACHER', 'teacher2', 3);
INSERT INTO `users` VALUES (9, 3, '2026-01-08 23:40:02.807977', 'teacher3@school.com', '$2a$10$GA9jiTCVc2Yt8q8xWx0zFOaVj00nrw/juG49.QZzOFS/M0zYU3i7y', NULL, '李老师', 'ACTIVE', '2026-01-08 23:40:02.807977', 'TEACHER', 'teacher3', 3);
INSERT INTO `users` VALUES (10, 1, '2026-01-08 23:40:02.902001', 'student1@school.com', '$2a$10$uH4GP6g57.tRLVx4ZKcfuua1utwo8Phn7fS3GdXQnG0liGpwD8IjO', NULL, '李同学', 'ACTIVE', '2026-01-08 23:40:02.902001', 'STUDENT', 'student1', 4);
INSERT INTO `users` VALUES (11, 2, '2026-01-08 23:40:02.997868', 'student2@school.com', '$2a$10$tnaHR2KP93Xp/2gouQ6Fv.MWlglfoicOLBtkOkx5xSBuyFI2yPlmi', NULL, '王同学', 'ACTIVE', '2026-01-08 23:40:02.997868', 'STUDENT', 'student2', 4);
INSERT INTO `users` VALUES (12, 3, '2026-01-08 23:40:03.093989', 'student3@school.com', '$2a$10$98cO4Q8eSFGn7zkSrl9XcedqKvBh/ABChJVu/PResGxmjSqS3NwQG', NULL, '张同学', 'ACTIVE', '2026-01-08 23:40:03.093989', 'STUDENT', 'student3', 4);
INSERT INTO `users` VALUES (13, 4, '2026-01-08 23:40:03.190411', 'student4@school.com', '$2a$10$S31rAYTIy0zW178r0SadZuzIQ9gQhSKmPxFjqyfMsWYdB5dPwySPe', NULL, '赵同学', 'ACTIVE', '2026-01-08 23:40:03.190411', 'STUDENT', 'student4', 4);
INSERT INTO `users` VALUES (14, 5, '2026-01-08 23:40:03.284784', 'student5@school.com', '$2a$10$SCZyKzqCxBWfT8otEtLF/.q9P39ATAKiF3WB8V3AwbKsz7Su66Kny', NULL, '刘同学', 'ACTIVE', '2026-01-08 23:40:03.284784', 'STUDENT', 'student5', 4);

-- ----------------------------
-- Procedure structure for CalculateOverdueFine
-- ----------------------------
DROP PROCEDURE IF EXISTS `CalculateOverdueFine`;
delimiter ;;
CREATE PROCEDURE `CalculateOverdueFine`(IN recordId BIGINT, OUT fineAmount DECIMAL(10,2))
BEGIN
    DECLARE dueTime DATETIME(6);
    DECLARE returnTime DATETIME(6);
    DECLARE overdueDays INT;
    
    -- 获取借阅记录的应还时间和实际还书时间
    SELECT due_time, return_time INTO dueTime, returnTime 
    FROM borrow_records WHERE id = recordId;
    
    -- 计算逾期天数（还书时间>应还时间才计算）
    IF returnTime > dueTime THEN
        SET overdueDays = DATEDIFF(returnTime, dueTime);
        SET fineAmount = overdueDays * 1.00; -- 1元/天
    ELSE
        SET fineAmount = 0.00;
    END IF;
    
    -- 插入/更新罚款记录
    INSERT INTO fines (record_id, fine_amount, pay_status)
    VALUES (recordId, fineAmount, 'unpaid')
    ON DUPLICATE KEY UPDATE fine_amount = fineAmount;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for GetUserUnpaidFines
-- ----------------------------
DROP PROCEDURE IF EXISTS `GetUserUnpaidFines`;
delimiter ;;
CREATE PROCEDURE `GetUserUnpaidFines`(IN userId BIGINT, OUT totalFine DECIMAL(10,2))
BEGIN
    SELECT IFNULL(SUM(f.fine_amount), 0.00) INTO totalFine
    FROM fines f
    JOIN borrow_records br ON f.record_id = br.id
    WHERE br.user_id = userId AND f.pay_status = 'unpaid';
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
