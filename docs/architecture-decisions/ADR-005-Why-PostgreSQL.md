# ADR-005 — Why PostgreSQL?
## Status

Accepted

## Decision

Use PostgreSQL as the primary relational database.

## Data Candidates

PostgreSQL will initially be considered for:

* Users
* Products
* Orders
* Order Items
* Payments
* Inventory

## Reasons

PostgreSQL provides:

* ACID transactions
* Strong consistency
* Referential integrity
* Foreign keys
* Constraints
* Complex SQL queries
* Mature ecosystem
* Reliable transaction processing

## Example Domain Relationship
    Order
    |
    +---- OrderItem
    |       |
    |       +---- Product
    |
    +---- Payment

A relational database is well suited to transactional relationships such as these.

## Alternatives
* MySQL
* MongoDB

MongoDB will only be introduced where document-oriented modeling provides a genuine benefit.