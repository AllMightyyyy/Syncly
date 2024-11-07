-- 1. Create 'users' table
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       displayName VARCHAR(255),
                       avatarUrl VARCHAR(255),
                       email VARCHAR(100) NOT NULL UNIQUE,
                       passwordHash VARCHAR(255) NOT NULL,
                       role ENUM('ADMIN', 'USER') NOT NULL DEFAULT 'USER',
                       createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       IsEmailVerified BOOLEAN NOT NULL DEFAULT FALSE,
                       emailVerificationToken VARCHAR(255),
                       resetPasswordToken VARCHAR(255),
                       resetPasswordExpiresAt TIMESTAMP,
                       IsActive BOOLEAN NOT NULL DEFAULT TRUE,
                       twoFactorSecret VARCHAR(255),
                       IsTwoFactorEnabled BOOLEAN NOT NULL DEFAULT FALSE,
                       provider VARCHAR(50),
                       providerId VARCHAR(100)
);

-- 2. Create 'devices' table
CREATE TABLE devices (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         user_id BIGINT NOT NULL,
                         deviceName VARCHAR(255) NOT NULL,
                         deviceType ENUM('DESKTOP', 'MOBILE', 'WEB', 'OTHER') NOT NULL,
                         refreshToken VARCHAR(255) UNIQUE,
                         createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         refreshTokenExpiryDate TIMESTAMP,
                         FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Create 'password_history' table
CREATE TABLE password_history (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  user_id BIGINT NOT NULL,
                                  passwordHash VARCHAR(255) NOT NULL,
                                  changedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. Create 'clipboard_entries' table
CREATE TABLE clipboard_entries (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   user_id BIGINT NOT NULL,
                                   content TEXT NOT NULL,
                                   category VARCHAR(255),
                                   deviceInfo VARCHAR(255) NOT NULL,
                                   timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   isDeleted BOOLEAN NOT NULL DEFAULT FALSE,
                                   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 5. Create 'clipboard_tags' table (for ClipboardEntry tags)
CREATE TABLE clipboard_tags (
                                clipboard_entry_id BIGINT NOT NULL,
                                tag VARCHAR(255) NOT NULL,
                                PRIMARY KEY (clipboard_entry_id, tag),
                                FOREIGN KEY (clipboard_entry_id) REFERENCES clipboard_entries(id) ON DELETE CASCADE
);

-- 6. Create 'paste_bins' table
CREATE TABLE paste_bins (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            user_id BIGINT NOT NULL,
                            name VARCHAR(255) NOT NULL,
                            content TEXT NOT NULL,
                            createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            isDeleted BOOLEAN NOT NULL DEFAULT FALSE,
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 7. Create 'refresh_tokens' table
CREATE TABLE refresh_tokens (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                token VARCHAR(255) NOT NULL UNIQUE,
                                user_id BIGINT NOT NULL,
                                createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                expiryDate TIMESTAMP,
                                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
