CREATE TABLE clipboard_entries (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   user_id BIGINT NOT NULL,
                                   content TEXT NOT NULL,
                                   timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   device_info VARCHAR(100) NOT NULL,
                                   is_deleted BOOLEAN DEFAULT FALSE,
                                   FOREIGN KEY (user_id) REFERENCES users(id)
);
