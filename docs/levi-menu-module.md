# Levi's menu module

This implementation follows the course's `ca-login` package structure and custom
ViewModel listener convention. It covers menu creation, editing, archiving,
availability, menu viewing, and a read-only sales summary. Authentication,
order-taking/cooking, recommendations, and weekly reporting are separate team work.

## Running and checking

Use JDK 11 or newer and Maven. The code targets Java 11.

```sh
mvn verify
mvn exec:java
```

The standalone launcher uses `data/` and truck ID `truck-1`. To choose a different
local data directory or truck:

```sh
mvn exec:java -Dexec.args="--data-dir demo-data --truck-id truck-1"
```

Do not run multiple application processes against the same data directory.
JSON writes replace complete files atomically; the in-process lock does not
provide transactions between independent application processes.

## One use case from input to output

1. `MenuView` gets the owner's input and calls `AddMenuItemController`.
2. The controller creates `AddMenuItemInputData` and calls the input boundary.
3. `AddMenuItemInteractor` validates the request and creates an immutable `MenuItem`.
4. It saves through `AddMenuItemDataAccessInterface`.
5. `JsonMenuDataAccessObject` writes the JSON file. A failed write is a use-case
   failure; it does not produce a successful visible item.
6. The interactor sends `AddMenuItemOutputData` through its output boundary.
7. `AddMenuItemPresenter` updates `MenuState` and calls `notifyStateChanged()`.
8. `MenuView`, a registered listener, refreshes its display.

`AppBuilder` creates and connects these objects. The interactor does not import
the concrete presenter, Swing, or JSON classes. Edit/archive/availability share
the menu ViewModel and storage. An interface is a method contract, not another
runtime object that forwards requests.

## Menu behaviour

- Prices are positive integer cents internally. Input uses a decimal dollar
  amount with at most two decimal places. Values are never silently rounded.
- IDs and truck ownership stay fixed when details change.
- Availability is temporary; archiving keeps the historical record and makes
  the item unavailable. Archived items are visible in the archived filter but
  cannot be edited or made available. There is no permanent delete operation.
- Images are optional local file references. Keep those files in place. A moved
  or unreadable image shows a placeholder; it does not prevent reading the menu.
  A shared image upload/storage API is not part of this standalone module.
- Distinct menu items may have the same display name; their IDs distinguish them.
- The ordering feature must use both `!item.isArchived()` and `item.isAvailable()`
  when accepting a new order, including a fresh check at order submission.

## Team integration contracts

Talha's login/truck flow should supply the selected truck ID when assembling the
controllers. The standalone truck-ID argument is a development context, not
authentication or authorization. Changing it does not log a user in.

`JsonMenuDataAccessObject(Path directory)` implements the use-case-specific menu
interfaces. `getAllForTruck(truckId)` includes archived items so the management
screen can display history. `get(itemId)` returns `null` if absent. `save(item)`
adds/replaces an immutable snapshot and prevents moving an existing item ID to
another truck. Use cases separately reject access to another truck's item.

Sophie can read this menu through a narrow ordering data-access interface backed
by the same store, or the team can substitute a shared API-backed adapter. Store
the item name and unit price on each order line at purchase time: later menu
changes must not alter old orders.

`ViewSalesSummaryDataAccessInterface.findCompletedSales(truckId)` is the summary's
integration point. The supplied `JsonCompletedSalesDataAccessObject` reads
`completed-sales.json`. The ordering feature should supply only completed,
non-cancelled sales; planned/cooking/ready orders are not revenue in this report.
Alternatively implement that interface over the team's order store. The summary
does not create orders or mark them complete.

Heng can reuse the same completed-sale snapshots and aggregation rules when
implementing weekly reports. Keep one agreed definition of qualifying sales,
day/week boundaries, cancellations, taxes/discounts, and revenue. This initial
summary totals quantity times purchase unit price; it does not calculate profit,
taxes, discounts, or refunds.

## Sales JSON contract

```json
{
  "schemaVersion": 1,
  "completedSales": [
    {
      "id": "sale-1",
      "truckId": "truck-1",
      "completedAt": "2026-11-20T17:00:00Z",
      "lines": [
        {"itemId": "burger-1", "nameAtPurchase": "Burger", "unitPriceCents": 950, "quantity": 2}
      ]
    }
  ]
}
```

Dates are inclusive using the `America/Toronto` time zone. A missing sales file
means no completed sales; malformed data shows an error and is never reset.
The report shows revenue, completed-order count, units sold, and item totals.
Items are ranked by units sold, then revenue, then ID; the latest qualifying
purchase name is displayed. A menu item need not still be active for its past
sales to count.

For an isolated demonstration, copy `docs/fixtures/completed-sales.example.json`
to a separate data directory as `completed-sales.json`, then use that directory
with `--data-dir`. This is explicitly sample data, never loaded by default. The
inclusive range 2026-11-19 through 2026-11-20 should show **$32.50, two completed
orders, four units**: three burgers ($28.50) and one fries ($4.00).

## Validation evidence

`mvn verify` runs JUnit, creates a JaCoCo report at
`target/site/jacoco/index.html`, and applies the repository's basic Checkstyle
rules. Tests cover invalid input, missing/foreign/archived items, failed storage,
notification callbacks, persisted reloads, date boundaries, historical prices,
malformed files, and the dependency rule. Check the generated reports for the
actual current counts and coverage; passing tests alone does not verify the
other teammates' features or the course's external API requirement.

## Design-pattern distinction

The ViewModel notifications follow the Observer style used by `ca-login`.
`MenuState` is only a screen-data holder. This module does not implement the
proposed order State pattern; that belongs to the order lifecycle feature.
