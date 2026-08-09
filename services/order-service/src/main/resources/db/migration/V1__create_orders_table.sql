CREATE TABLE orders (
                        id UUID PRIMARY KEY,
                        event_id UUID NOT NULL,
                        user_id UUID NOT NULL,
                        status VARCHAR(20) NOT NULL,
                        requested_at TIMESTAMPTZ NOT NULL,
                        updated_at TIMESTAMPTZ
);

CREATE TABLE order_seats (
                             order_id UUID NOT NULL REFERENCES orders(id),
                             seat_id VARCHAR(20) NOT NULL,
                             PRIMARY KEY (order_id, seat_id)
);

CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_seats_order_id ON order_seats(order_id);