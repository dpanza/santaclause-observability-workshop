-- sql/init.sql
-- Initialisation de la base de donnees Santa's Wish List

-- Table des voeux
CREATE TABLE IF NOT EXISTS wish (
    id UUID PRIMARY KEY,
    child_name VARCHAR(100) NOT NULL,
    toy_name VARCHAR(200) NOT NULL,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'RECEIVED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    processed_by VARCHAR(50)
);

-- Table du stock (PAS d'index sur toy_name pour simuler requete lente)
CREATE TABLE IF NOT EXISTS toy_inventory (
    id UUID PRIMARY KEY,
    toy_name VARCHAR(200) NOT NULL,
    category VARCHAR(50) NOT NULL,
    stock INTEGER NOT NULL DEFAULT 100,
    warehouse_location VARCHAR(20)
);

-- Table des livraisons
CREATE TABLE IF NOT EXISTS delivery (
    id UUID PRIMARY KEY,
    wish_id UUID REFERENCES wish(id),
    scheduled_date DATE DEFAULT '2024-12-24',
    status VARCHAR(50) DEFAULT 'PENDING'
);

-- Index sur les colonnes frequemment recherchees (sauf toy_name dans toy_inventory - bug intentionnel)
CREATE INDEX IF NOT EXISTS idx_wish_child_name ON wish(child_name);
CREATE INDEX IF NOT EXISTS idx_wish_status ON wish(status);
CREATE INDEX IF NOT EXISTS idx_wish_category ON wish(category);
CREATE INDEX IF NOT EXISTS idx_delivery_wish_id ON delivery(wish_id);
CREATE INDEX IF NOT EXISTS idx_delivery_status ON delivery(status);

-- Donnees initiales : stock de jouets
INSERT INTO toy_inventory (id, toy_name, category, stock, warehouse_location) VALUES
-- PLUSH
(gen_random_uuid(), 'Teddy Bear', 'PLUSH', 150, 'A1'),
(gen_random_uuid(), 'Unicorn Plush', 'PLUSH', 120, 'A2'),
(gen_random_uuid(), 'Dinosaur Rex', 'PLUSH', 80, 'A3'),
(gen_random_uuid(), 'Bunny Soft', 'PLUSH', 100, 'A4'),
(gen_random_uuid(), 'Penguin Cuddle', 'PLUSH', 90, 'A5'),
(gen_random_uuid(), 'Lion King', 'PLUSH', 70, 'A6'),
(gen_random_uuid(), 'Elephant Dumbo', 'PLUSH', 85, 'A7'),
(gen_random_uuid(), 'Panda Bear', 'PLUSH', 95, 'A8'),

-- BOARD_GAME
(gen_random_uuid(), 'Monopoly', 'BOARD_GAME', 60, 'B1'),
(gen_random_uuid(), 'Scrabble', 'BOARD_GAME', 50, 'B2'),
(gen_random_uuid(), 'Catan', 'BOARD_GAME', 45, 'B3'),
(gen_random_uuid(), 'Ticket to Ride', 'BOARD_GAME', 55, 'B4'),
(gen_random_uuid(), 'Carcassonne', 'BOARD_GAME', 40, 'B5'),
(gen_random_uuid(), 'Dixit', 'BOARD_GAME', 65, 'B6'),
(gen_random_uuid(), 'Uno', 'BOARD_GAME', 200, 'B7'),
(gen_random_uuid(), 'Chess Set', 'BOARD_GAME', 30, 'B8'),

-- BOOK
(gen_random_uuid(), 'Harry Potter', 'BOOK', 300, 'C1'),
(gen_random_uuid(), 'Le Petit Prince', 'BOOK', 250, 'C2'),
(gen_random_uuid(), 'Where the Wild Things Are', 'BOOK', 180, 'C3'),
(gen_random_uuid(), 'Matilda', 'BOOK', 150, 'C4'),
(gen_random_uuid(), 'Charlie and the Chocolate Factory', 'BOOK', 170, 'C5'),
(gen_random_uuid(), 'The Hobbit', 'BOOK', 120, 'C6'),

-- ELECTRONIC (problematiques)
(gen_random_uuid(), 'Nintendo Switch', 'ELECTRONIC', 25, 'D1'),
(gen_random_uuid(), 'PlayStation Controller', 'ELECTRONIC', 40, 'D2'),
(gen_random_uuid(), 'Robot Dog', 'ELECTRONIC', 15, 'D3'),
(gen_random_uuid(), 'Drone Mini', 'ELECTRONIC', 20, 'D4'),
(gen_random_uuid(), 'VR Headset', 'ELECTRONIC', 10, 'D5'),
(gen_random_uuid(), 'Smart Watch Kids', 'ELECTRONIC', 35, 'D6'),
(gen_random_uuid(), 'RC Car', 'ELECTRONIC', 50, 'D7'),
(gen_random_uuid(), 'Electronic Keyboard', 'ELECTRONIC', 18, 'D8');
