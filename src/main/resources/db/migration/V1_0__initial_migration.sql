DROP TABLE IF EXISTS restaurant_table;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS email_template;

CREATE TABLE restaurant_table (
  number INT PRIMARY KEY,
  min_number_of_seats INT,
  max_number_of_seats INT
);

CREATE TABLE reservation (
    id INT PRIMARY KEY,
    date TIMESTAMP,
    duration INT,
    full_name VARCHAR(255),
    phone VARCHAR(255),
    email VARCHAR(255),
    number_of_seats INT,
    verification_code VARCHAR(255)
);

CREATE TABLE email_template (
    name VARCHAR(255) PRIMARY KEY,
    content VARCHAR(4096)
);

INSERT INTO email_template (name, content) VALUES
('New reservation', 'Hi {{FULLNAME}}, <br>Your reservation number {{ID}} has been successfully made. We will contact you by phone number {{PHONE}} about two hours before reservation date. <br>Here are details of your reservation<br>Table number: {{TABLE_ID}} <br>Reservation date: {{TIME}}'),
('Cancellation request', 'Hi {{FULLNAME}}, <br>We received your request for cancellation of your reservation number {{ID}}<br>To properly cancel reservation you have to enter verification code below<br>Verification code: {{VERIFICATION_CODE}}'),
('Reservation cancelled', 'Hi {{FULLNAME}}, <br>Your reservation number {{ID}} has been cancelled successfully');
