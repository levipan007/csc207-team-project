# TruckTrack

TruckTrack is our CSC207 app for food truck owners. We're building it to manage
menus, take customer orders, track cooking tickets, and review sales in one place.

Team: Talha Efe Ustun, Yiwei (Levi) Pan, Sophie Shu, and Heng Zhu.

## Team overview

| Lead | Area | Planned features |
| --- | --- | --- |
| Talha | Accounts, cooking screen and audio | Login, sign-up, and editing the owner's profile and truck details; the cooking screen of open tickets; spoken ready-order announcements. |
| Levi | Menu management and sales summary | Adding, editing, and archiving menu items, changing availability, categories and prep times, and viewing a sales summary. |
| Heng | Recommendations and analytics | Order-priority recommendations, ranked items, sales insights such as busiest hours, and weekly earnings reports. |
| Sophie | Ordering and order lifecycle | Taking, editing, cancelling, and completing orders; moving orders through cooking to ready; the app's visual design. |

These features connect through shared menu and order data. Menu management and
the sales-summary screen are runnable. The shared entities, including `Order`
with its lifecycle, are in place. Login, signup, ordering screens, the cooking
screen and recommendations are still in progress.

## Current implementation

- Add menu items with name, description, positive price, and an optional local image.
- Edit details while keeping a stable item ID.
- Mark items available or sold out.
- Archive items while retaining their records; show archived items with a filter.
- View a date-range sales summary: revenue, completed orders, units sold, and item totals.
- Save/reload menu data with JSON persistence; keep purchase prices independent of menu edits.
- Run the Swing screens independently of the team's upcoming login/order integration.
- Define `Owner` (password stored as a hash) and `Truck` entities for accounts and truck profiles.
- Define `Order`, whose lifecycle uses the State pattern, with tests for every valid and invalid transition.

## Run

Requires JDK 11 or newer and Maven.

```sh
mvn verify
mvn exec:java
```

`mvn verify` runs the tests and Checkstyle and writes the JaCoCo coverage report to
`target/site/jacoco/index.html`. The standalone app uses the local `data/` directory
and truck ID `truck-1` by default:

```sh
mvn exec:java -Dexec.args="--data-dir demo-data --truck-id truck-1"
```

The standalone truck selector is a development context, not authentication.
A missing completed-sales file produces an empty report. No sales are invented.
For the documented sample fixture and integration instructions, see
[Levi's module guide](docs/levi-menu-module.md).

## Levi's user story and use cases

As a truck owner, I want to manage my menu and item availability and inspect my
sales summary, so that the menu reflects what I can sell and I can review sales.

| Use case | Trigger and successful result | Alternatives |
| --- | --- | --- |
| Add menu item | Owner enters details and presses Add; item is saved and listed. | Missing name, invalid/nonpositive price, or storage failure shows an error and preserves input. |
| Edit menu item | Owner selects an active item and saves revised details; the same ID is retained. | Missing/foreign/archived item or invalid details fail without changing the saved record. |
| Archive menu item | Owner selects and confirms archive; item is hidden from the active menu and kept in history. | Cancelling the dialog does nothing; an already archived item remains archived. |
| Change availability | Owner toggles an active item between available and sold out. | Archived/missing/foreign items cannot be changed. |
| View sales summary | Owner chooses an inclusive date range; completed-sale totals are displayed. | Empty data shows zeros; invalid dates or unreadable data show errors. |

Precondition: the enclosing application supplies the owner's selected truck ID.
The standalone launcher supplies it for development; authentication is a separate
team integration. For add/edit/availability/archive, the postcondition is visible
success only after persistence succeeds.

## Talha's part: accounts and truck profiles

User story: As a food truck owner, I want to sign up and log in so that I only see
my own truck's menu, orders and sales. I also want to update my profile and my
truck's details.

Planned use cases:

| Use case | What happens | When it fails |
| --- | --- | --- |
| Sign up | Owner enters a username, password, name and truck name. A new account and truck are saved. | Username already taken, password too short, or missing fields. |
| Log in | Owner enters username and password. The app opens with their truck. | Wrong username or password. |
| Edit profile | Owner changes their name or email. | Invalid email or empty name. |
| Edit truck | Owner changes the truck name or cuisine. | Empty truck name. |

Progress so far: `Owner` and `Truck` entities with tests. Next is the Sign up use case.

## Sophie's part: ordering and the order lifecycle

User story: As a truck owner, I want to take an order from the current menu,
change it while it's being made, and move it through to pickup, so that what we
cook matches what the customer asked for and they know when it's ready.

Planned use cases:

| Use case | What happens | When it fails |
| --- | --- | --- |
| Place order | Owner picks items and quantities. A new order is saved as placed, with the next order number. | Empty order, or an item that is sold out, archived or missing. |
| Edit order | Owner adds, removes or changes items and notes. | The order is already ready or finished, or it would be left with no items. |
| Cancel order | The order is cancelled and never counts as a sale. | The order is already ready or finished. |
| Start cooking | The order moves from placed to in progress. | The order isn't placed. |
| Mark ready | The order moves to ready and its number is announced. | The order isn't in progress. |
| Finish order | The order is handed over and saved as a completed sale. | The order isn't ready. |

Progress so far: the `Order` entity, with its lifecycle using the State pattern.
Next is saving orders and the Place order use case.

## Heng's part: recommendations and analytics

User story: As a truck owner, I want the app to suggest which order to cook next
and to show my best-selling items, busiest hours and weekly earnings, so that
customers don't wait too long and I can plan stock and staffing.

Planned use cases:

| Use case | What happens | When it fails |
| --- | --- | --- |
| Tentative priority | Open orders are ranked by which to cook next, each with a short reason. | No open orders shows an empty list. |
| Rank items | Menu items are ranked by units sold or revenue over a date range. | No sales shows an empty list; invalid dates show an error. |
| Sales insights | Orders and revenue per hour, and average order value per day. | No sales shows zeros. |
| Weekly earnings | Revenue per day for a chosen week, the total, and the change from last week. | No previous week shows "no prior data". |

Analytics read completed sales only, so cancelled or unfinished orders never
count. Progress so far: a `RankItem` use-case outline and a placeholder Analysis
tab.

## Clean Architecture

```text
src/main/java/
  entity/               MenuItem, Owner, Truck, and immutable completed-sale snapshots
  use_case/             One action per package, with boundary and data-access interfaces
  interface_adapter/    Controllers, presenters, screen state, ViewModels and listeners
  data_access/          JSON persistence implementations
  view/                 Swing panels
  app/                  Main and AppBuilder; each area will wire its screens in its own module
src/test/java/           Domain, use-case, persistence, adapter, view, and integration tests
```

The Entity and Use Case layers do not import Swing, concrete presenters, JSON, or
concrete data-access implementations. The project uses the custom listener style
from the current course `ca-login` example. Full interface and data contracts are
in [the module guide](docs/levi-menu-module.md).

Domain objects currently implemented:

- `MenuItem`: ID, truck ID, name, description, integer price in cents, image reference,
  availability, archived status, category, and prep time; immutable
  detail/availability/archive operations.
- `CompletedSale`: sale/truck IDs, completion timestamp, and immutable sale lines.
- `SaleLine`: item ID, purchase-time name and unit price, quantity, and exact line total.
- `Owner`: ID, username, password hash, display name, email, and truck ID.
- `Truck`: ID, owner ID, name, and cuisine.
- `Order` and `OrderItem`: an order's lines (name and price copied at order time),
  number, timestamps, and status. Status changes follow the State pattern:
  `PLACED → IN_PROGRESS → READY → COMPLETED`, or `CANCELLED` from the first two.
  A completed order becomes a `CompletedSale`.

`CompletedSale` is the read-only reporting input for the sales summary and
analytics. Finishing an order saves one to `completed-sales.json`, which the sales
summary already reads.

## Remaining team integrations

- Split app wiring into one module per area, and supply the logged-in truck to
  every controller through a shared session (Talha's login replaces the
  development truck selector).
- Save orders and completed sales from Sophie's order workflow.
- Use `CompletedSale` totals in both the sales summary and Heng's analytics, so
  they always agree.
- Choose the team's external API and cloud storage. The app currently stores data
  in local JSON files.
- Additional design pattern: the State pattern on the order lifecycle (in
  `entity/Order`). `MenuState` is unrelated screen data.

Reference structure: [official course ca-login](https://github.com/CSC207-2026F-UofT/ca-login)
and [Chapter 11](https://github.com/CSC207-UofT/207-course-notes/blob/main/11-clean-architecture.md).
