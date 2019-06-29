INSERT INTO customer
    (name)
SELECT 'DefaultCustomer'
WHERE
    NOT EXISTS (
        SELECT id FROM customer WHERE name = 'DefaultCustomer'
    );