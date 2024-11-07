CREATE TABLE paste_bins (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            user_id BIGINT NOT NULL,
                            name VARCHAR(100) NOT NULL,
                            content TEXT NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            is_deleted BOOLEAN DEFAULT FALSE,
                            FOREIGN KEY (user_id) REFERENCES users(id)
);
