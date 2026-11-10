# TruckTrack

## Team Information

### Team Members
- Talha Efe Ustun
- Yiwei (Levi) Pan
- Name
- Name

---

## Project Description

### Domain

**Small Business Management**

Running a food truck means juggling a lot at once: keeping the menu up to date, making sure you don't run out of ingredients halfway through a lunch rush, and figuring out at the end of the week whether you actually made money. TruckTrack is a desktop app that keeps all of this in one place.

An owner can build their menu and set prices, keep track of how much of each ingredient they have, and record sales as they happen. When a sale is recorded, the app automatically subtracts the ingredients that went into those items, so the inventory always reflects what's actually left in the truck. If something starts running low, the app lets the owner know. Owners can also look back at their earnings for the day or the week and see which items are selling best.

---

## User Stories

### User Story 1 – Menu Management
As a food truck owner, I want to add items to my menu and set their prices, so that my menu stays accurate as I change what I offer.

### User Story 2 – Recording Sales
As a food truck owner, I want to quickly record what I sold and how many, so that I have a reliable record of my income without doing the math by hand.

### User Story 3 – Inventory Tracking
As a food truck owner, I want to see how much of each ingredient I have and update it when I restock, so that I don't run out of something in the middle of a busy shift.

### User Story 4 – Earnings Report
As a food truck owner, I want to see how much I earned today or this week and which items sold the most, so that I can tell what's working and what isn't.

---

## Use Cases

### Use Case 1: Add Menu Item
**Actor:** Owner

**Main Flow:**
1. The owner opens the Menu screen and clicks "Add Item".
2. They enter the item's name, its price, and the ingredients it uses along with how much of each.
3. The app checks that the information is valid and adds the item to the menu.
4. The new item shows up in the menu list.

**Alternative Flows:**
- If an item with the same name is already on the menu, the app shows an error and doesn't add it.
- If the price is missing, zero, or negative, the app asks the owner to enter a valid price.

### Use Case 2: Record Sale
**Actor:** Owner

**Main Flow:**
1. The owner opens the Sales screen and picks the items that were sold and how many of each.
2. They click "Record Sale".
3. The app checks that there are enough ingredients in stock to make those items.
4. The sale is saved with the current date and time, and the ingredients used are subtracted from inventory.
5. The app shows the total for the sale and confirms it was recorded.

**Alternative Flows:**
- If there isn't enough of an ingredient, the app tells the owner which one is short, and the sale is not recorded.
- If the sale goes through but brings an ingredient below its low-stock level, the app records the sale and shows a low-stock warning.

### Use Case 3: Update Inventory
**Actor:** Owner

**Main Flow:**
1. The owner opens the Inventory screen and picks an ingredient, or adds a new one.
2. They enter how much they're adding (after restocking) or removing (for example, if something spoiled).
3. The app updates the quantity and saves the change.
4. The inventory list refreshes, with low-stock ingredients highlighted.

**Alternative Flows:**
- If the amount entered isn't a valid number, the app shows an error.
- If removing the amount would leave a negative quantity, the app shows an error and leaves the stock unchanged.

### Use Case 4: View Earnings Report
**Actor:** Owner

**Main Flow:**
1. The owner opens the Earnings screen and chooses a time period: today, this week, or a custom date range.
2. The app adds up the total revenue and number of sales for that period and finds the best-selling items.
3. The results are shown on screen.

**Alternative Flows:**
- If the start date of a custom range comes after the end date, the app asks the owner to fix the dates.
- If there were no sales in the chosen period, the app says so instead of showing an empty report.

---

## Minimal Viable Product (MVP)

Each of us is leading one use case. Together, these four make up the core of the app: items go on the menu, sales use those items and draw down inventory, and the earnings report is built from the recorded sales.

| Team Member      | MVP Use Case |
|------------------|--------------|
| Talha Efe Ustun  | Add Menu Item |
| Yiwei (Levi) Pan | Record Sale |
| Name             | Update Inventory |
| Name             | View Earnings Report |

If we have time after the MVP is working, we'd like to add:
- Login and sign-up, so multiple owners can use the app with their own data
- A weather check using an external weather API, to help owners plan where to park and how much to prep
- Editing and deleting menu items
- Exporting sales history to a CSV file

---

## Entities

### FoodTruck
The main object that holds everything about one truck.
- **Instance variables:** `String truckId`, `String name`, `List<MenuItem> menu`, `List<Ingredient> inventory`, `List<Sale> sales`
- **Methods:** `addMenuItem(MenuItem)`, `getMenuItem(String name)`, `hasMenuItem(String name)`, `addIngredient(Ingredient)`, `getIngredient(String name)`, `addSale(Sale)`, `getSalesBetween(LocalDateTime start, LocalDateTime end)`

### MenuItem
A single item on the menu, along with the ingredients needed to make it.
- **Instance variables:** `String name`, `double price`, `Map<Ingredient, Double> recipe`
- **Methods:** `getName()`, `getPrice()`, `setPrice(double)`, `getRecipe()`

### Ingredient
Something the truck keeps in stock, like buns, patties, or cheese.
- **Instance variables:** `String name`, `String unit`, `double quantity`, `double lowStockThreshold`
- **Methods:** `increase(double amount)`, `decrease(double amount)`, `isLow()`, `hasEnough(double amount)`

### Sale
One completed transaction, which can include several different items.
- **Instance variables:** `List<SaleItem> items`, `LocalDateTime timestamp`
- **Methods:** `getTotal()`, `getItems()`, `getTimestamp()`

### SaleItem
One line in a sale, such as "3 × Classic Burger".
- **Instance variables:** `MenuItem item`, `int quantity`
- **Methods:** `getSubtotal()`

---

## API Usage

**API Name:** CSC207 User/Data API (used in the course's `DBUserDataAccessObject` example)

**Documentation:** [Link]

**How we use it:** We store each food truck's data (its menu, inventory, and sales history) as a JSON object saved under the truck's ID. All four use cases read and write this data through Data Access Objects that call the API, so nothing is lost when the app is closed, and the same data can be loaded from a different computer.

**Status:** Not yet called

---

## Design Diagrams

### UML Class Diagram
Coming soon. We'll keep the PlantUML source in `/docs` so the diagram can be regenerated as the design changes.

### Sequence Diagrams
Coming soon.

---

## How to Run

Instructions will be added once the first runnable version is ready.

---

## Notes

- We're using pull requests for all changes, and each PR is reviewed by at least one other team member before merging.
- If a PR changes the design or the project's status, it should update this README too.