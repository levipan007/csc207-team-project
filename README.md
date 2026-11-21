# TruckTrack

TruckTrack is our CSC207 app for food truck owners. We're building it to manage
menus, take customer orders, track cooking tickets, and review sales in one place.

Team: Talha Efe Ustun, Yiwei (Levi) Pan, Sophie Shu, and Heng Zhu.

## Team overview

| Lead | Area | Planned features |
| --- | --- | --- |
| Talha | Accounts and truck profiles | Login, sign-up, and editing the owner's profile and truck details. |
| Levi | Menu management and availability | Adding, editing, and archiving menu items, changing availability, and viewing a sales summary. |
| Heng | Recommendations and analytics | Order-priority recommendations, sales insights such as top items and busiest hours, and weekly revenue reports. |
| Sophie | Ordering and cooking workflow | Taking, editing, completing, and cancelling orders; cooking tickets, progress indicators, and ready-order announcements. |

These features will connect through shared menu and order data. Menu management
and the sales-summary screen are runnable, and the `Owner` and `Truck` entities
are in place for Talha's account features. Login, signup, ordering, cooking, and
recommendations are still planned integrations.

## Current implementation

- Add menu items with name, description, positive price, and an optional local image.
- Edit details while keeping a stable item ID.
- Mark items available or sold out.
- Archive items while retaining their records; show archived items with a filter.
- View a date-range sales summary: revenue, completed orders, units sold, and item totals.
- Save/reload menu data with JSON persistence; keep purchase prices independent of menu edits.
- Run the Swing screens independently of the team's upcoming login/order integration.
- Define `Owner` and `Truck` entities with tests as the starting point for accounts and truck profiles.

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

## Clean Architecture

```text
src/main/java/
  entity/               MenuItem, Owner, Truck, and immutable completed-sale snapshots
  use_case/             One action per package, with boundary and data-access interfaces
  interface_adapter/    Controllers, presenters, screen state, ViewModels and listeners
  data_access/          JSON persistence implementations
  view/                 Swing panels
  app/                  Main and AppBuilder assemble the module
src/test/java/           Domain, use-case, persistence, adapter, view, and integration tests
```

The Entity and Use Case layers do not import Swing, concrete presenters, JSON, or
concrete data-access implementations. The project uses the custom listener style
from the current course `ca-login` example. Full interface and data contracts are
in [the module guide](docs/levi-menu-module.md).

Domain objects currently implemented:

- `MenuItem`: ID, truck ID, name, description, integer price in cents, image reference,
  availability, and archived status; immutable detail/availability/archive operations.
- `CompletedSale`: sale/truck IDs, completion timestamp, and immutable sale lines.
- `SaleLine`: item ID, purchase-time name and unit price, quantity, and exact line total.
- `Owner`: ID, username, password, display name, email, and truck ID.
- `Truck`: ID, owner ID, name, and cuisine.

`CompletedSale` is the read-only reporting input. It does not implement order-taking
or the cooking workflow. The team can replace its file adapter with one over the
actual order store through `ViewSalesSummaryDataAccessInterface`.

## Remaining team integrations

- Supply authenticated user/truck context from Talha's feature.
- Connect Sophie's order catalogue and completed sales to the shared storage contracts.
- Agree shared sales calculations with Heng's insights/weekly report implementation.
- Choose and demonstrate the team's external API. The current app stores data in
  local JSON files; external API integration is still to come.
- Implement and explain the agreed additional design pattern. The proposed order
  State pattern belongs to the order lifecycle; `MenuState` here is screen data.

Reference structure: [official course ca-login](https://github.com/CSC207-2026F-UofT/ca-login)
and [Chapter 11](https://github.com/CSC207-UofT/207-course-notes/blob/main/11-clean-architecture.md).
