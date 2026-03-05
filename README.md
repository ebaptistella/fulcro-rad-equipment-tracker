# Fulcro RAD Equipment Tracker

## Why this exists

This project is a Fulcro RAD + Datomic web app that demonstrates equipment tracking: modeling equipment and assignments, reports and forms, and the rule that each piece of equipment is assigned to at most one account at a time. It serves as a reference for building full-stack Clojure/ClojureScript applications with RAD and Datomic.

## How it works

The app is a full-stack Clojure/ClojureScript stack: **Fulcro** on the frontend, **Fulcro RAD** for rapid app development (forms, reports, auth), and **Datomic** as the database. The schema is defined in the RAD model layer (`src/main/com/example/model`) via `defattr`; the Datomic plugin turns those attributes into schema and Pathom resolvers on startup. The UI is built from RAD reports (list views with a top-level resolver returning entity IDs) and forms (create/edit with pickers where needed). The development environment uses an in-memory Datomic DB that persists for the life of the REPL; seeding runs on `(start)` so the app starts with sample data.

## Core concepts

- **Equipment & Assignment**: One entity per equipment item (id, kind, serial, description); assignment entities link account ↔ equipment with `assigned-on` and optional `returned-on`. No `returned-on` means “currently assigned”; the app must ensure at most one active assignment per equipment.
- **RAD model**: Attributes and entities are defined with `defattr` in the model folder and composed in `model.cljc`; the RAD Datomic adapter creates schema and resolvers from that.
- **Reports**: A report needs a top-level source attribute (e.g. `:equipment/all`) and a resolver that returns at least the ID of each entity; Pathom fills the rest from the attribute definitions.
- **Pickers**: For assignment forms, account and equipment are chosen via pickers; the equipment picker for new assignments should list only unassigned equipment (custom resolver).
- **Structural editing**: Clojure code is edited by structure (forms), not by lines; learning slurp/barf, wrap, splice, etc. in Cursive is recommended.

For the full task list, setup, and step-by-step instructions, see [docs/README.md](docs/README.md).
