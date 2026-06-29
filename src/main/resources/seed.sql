-- ShareSpace reference data
-- Still needs to be discussed to what reference data we want.
-- Admin panel might be used to add reference data if we want as well.

INSERT OR IGNORE INTO categories (name, description)
VALUES
  ('Electronics', 'Phones, TVs and other electronics.'),
  ('Tools', 'Power tools and hand tools.'),
  ('Gaming', 'Enter a world of adventure and exitement.'),
  ('Pets', 'Show your animal friends some love.'),
  ('Outdoor', 'Get into the great outdoors.'),
  ('Fashion', 'Find your style.'),
  ('Travel', 'Discover the world'),
  ('Home', 'Transform your space into a work of art'),
  ('Music', 'Turn it up'),
  ('Designer Goods', 'Surround yourself with luxury'),
  ('Plants', 'Transform your home into a oasis'),
  ('Cooking', 'Cook up something special'),
  ('Toys & Collectibles', 'Bring home a new friend'),
  ('Driveables', 'Drive your mood'),
  ('Real Estate', 'Find your new home'),
  ('Books', 'Read to your desire'),
  ('Health & Beauty', 'Take care of yourself'),
  ('Jewelry & Watches', 'Luxury lifestyle'),
  ('Sporting', 'Experience greatness'),
  ('Baby', 'Baby supplies'),    
  ('Everything else', 'Discover hidden gems');

INSERT OR IGNORE INTO sub_categories (name, category_id)
WITH v(name, category_name) AS (
  VALUES
    ('Smartphone',       'Electronics'),
    ('TV',               'Electronics'),
    ('Laptop',           'Electronics'),
    ('Camera',           'Electronics'),
    ('Smart Watch',      'Electronics'),
    ('Home Electronics', 'Electronics'),
    ('Desktop Computer', 'Electronics'),
    ('Hammer',           'Tools'),
    ('Drill',            'Tools'),
    ('Saw',              'Tools'),
    ('Lawn Mower',       'Tools'),
    ('Screwdriver',      'Tools'),
SELECT v.name, c.id
FROM v
JOIN categories c ON c.name = v.category_name;

-- Roles
INSERT INTO roles (name) SELECT 'lender' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'lender');
INSERT INTO roles (name) SELECT 'renter' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'renter');

