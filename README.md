# E-Commerce-Application

Inventory APIs
1. Create Inventory Item
POST /api/inventory
Takes item details in the request body and creates a new inventory item. Returns the created item with its ID.

3. Add Supply to Item
POST /api/inventory/{itemId}/supply?quantity=10
Increases the quantity of an existing item. You give the item ID and how much quantity to add.

4. Get Item by SKU
GET /api/inventory/sku/{sku}
Fetches the item details using its SKU code.

5. Get All Active Items
GET /api/inventory
Returns a list of all items that are marked as active.

6. Check Item Availability
GET /api/inventory/{itemId}/availability?quantity=5
Checks if the requested quantity is available for the given item ID.

7. Deactivate Item
PUT /api/inventory/{itemId}/deactivate
Marks the item as inactive so it won’t be available for operations.

Reservation APIs
1. Create Reservation
POST /api/reservations
Takes item ID, customer ID, and quantity. Reserves the item if available.

2. Cancel Reservation
PUT /api/reservations/{reservationId}/cancel?customerId=abc123
Cancels an existing reservation. Customer ID is required.

3. Get Reservation by ID
GET /api/reservations/{reservationId}
Fetches reservation details using the reservation ID.
