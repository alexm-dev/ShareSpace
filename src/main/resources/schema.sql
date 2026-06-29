-- ShareSpace database schema

CREATE TABLE IF NOT EXISTS users (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    username      TEXT    NOT NULL UNIQUE,
    email         TEXT    NOT NULL UNIQUE,
    password_hash TEXT    NOT NULL,
    created_time  TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status        TEXT,
    first_name    TEXT,
    last_name     TEXT,
    location_id   INTEGER REFERENCES locations(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS roles (
    id   INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE CHECK (name IN ('lender', 'renter'))
);

CREATE TABLE IF NOT EXISTS locations (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    city           TEXT NOT NULL,
    postal_code    TEXT NOT NULL,
    district       TEXT,
    street_address TEXT NOT NULL,
    country        TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS categories (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE IF NOT EXISTS sub_categories (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL,
    category_id INTEGER NOT NULL,

    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    UNIQUE (name, category_id)
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,

    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS assets (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    owner_id          INTEGER NOT NULL,
    sub_category_id   INTEGER NOT NULL,
    model             TEXT    NOT NULL,
    description       TEXT,
    condition         TEXT,
    asset_location_id INTEGER NOT NULL,
    daily_rate        REAL    NOT NULL,
    metadata          TEXT,

    FOREIGN KEY (owner_id)          REFERENCES users(id)          ON DELETE CASCADE,
    FOREIGN KEY (sub_category_id)   REFERENCES sub_categories(id) ON DELETE RESTRICT,
    FOREIGN KEY (asset_location_id) REFERENCES locations(id)      ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS asset_images (
    asset_id   INTEGER PRIMARY KEY,
    image_data BLOB    NOT NULL,
    mime_type  TEXT,

    FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_images (
    user_id    INTEGER PRIMARY KEY,
    image_data BLOB    NOT NULL,
    mime_type  TEXT,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bookings (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    asset_id     INTEGER NOT NULL,
    renter_id    INTEGER NOT NULL,
    start_time   TEXT    NOT NULL,
    end_time     TEXT    NOT NULL,
    status       TEXT    NOT NULL DEFAULT 'pending'
                         CHECK (status IN ('pending', 'confirmed', 'completed', 'cancelled')),
    total_cost   REAL    NOT NULL CHECK (total_cost >= 0),
    created_time TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CHECK (end_time > start_time),
    FOREIGN KEY (asset_id)  REFERENCES assets(id) ON DELETE RESTRICT,
    FOREIGN KEY (renter_id) REFERENCES users(id)  ON DELETE RESTRICT
);

-- single-row table, id is always 1
CREATE TABLE IF NOT EXISTS sessions (
    id         INTEGER PRIMARY KEY CHECK (id = 1),
    user_id    INTEGER NOT NULL,
    created_at TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ratings (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    booking_id    INTEGER NOT NULL,
    reviewer_id   INTEGER NOT NULL,
    rated_user_id INTEGER,
    rating        INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment       TEXT,
    created_time  TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (booking_id)    REFERENCES bookings(id) ON DELETE RESTRICT,
    FOREIGN KEY (reviewer_id)   REFERENCES users(id)    ON DELETE RESTRICT,
    FOREIGN KEY (rated_user_id) REFERENCES users(id)    ON DELETE RESTRICT
);

