-- Aligns with OrderEntity fields/status usage
CREATE TABLE IF NOT EXISTS orders (
  id        BIGSERIAL PRIMARY KEY,
  customer  VARCHAR(100) NOT NULL,
  item      VARCHAR(100) NOT NULL,
  quantity  INTEGER      NOT NULL CHECK (quantity > 0),
  status    VARCHAR(20)  NOT NULL,
  created   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
