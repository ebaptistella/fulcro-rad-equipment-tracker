:author: Tony Kay
:lang: en
:encoding: UTF-8
:doctype: book
:source-highlighter: coderay
:source-language: clojure
:toc: left
:toclevels: 3
:sectlinks:
:sectanchors:
:leveloffset: 1
:sectnums:
:imagesdir: docs
:scriptsdir: js
:imagesoutdir: docs

ifdef::env-github[]
:tip-caption: :bulb:
:note-caption: :information_source:
:important-caption: :heavy_exclamation_mark:
:caution-caption: :fire:
:warning-caption: :warning:
endif::[]

ifdef::env-github[]
toc::[]
endif::[]

= Dataico Coding Challenge

This repository contains a working Fulcro RAD and Datomic web application which we use for job candidate evaluation.
The idea is that we want to ensure that incoming candidates can work with Clojure, Clojurescript, Datomic, and Fulcro.

You might want to start reading https://www.braveclojure.com/foreword[Clojure for The Brave and True]. There are also free online books for Fulcro and RAD, and also many YouTube Videos.

The challenge for being hired at Dataico is judged by having an interactive session with you on this project, where we will observe the following important aspects:

. Do you know how to structurally edit code reasonably well (see instructions below)
. Did you complete the required tasks?
. Do you understand what you did in the project? Can you do an additional similar thing while pairing?
. What are your general habits and understanding?
. How well do you take direction, and understand/respond to in-person pairing.

NOTE: You may use any tools you want. AI, etc. There are tons of YouTube videos and online documentation for all of this. We expect that the overall challenge will take a varying amount of time. An experience Clojure developer might complete it all in one evening, but someone less experience might work on it for considerably longer. Independent of the tools you use, you will still be interviewed and asked to do live independent work on this project without the help of AI or other tools. So, make sure you understand everything that you do.

== Setup

You will need to install:

* A JDK (e.g. JDK 22 from Oracle)
* IntelliJ Community Edition
** The Cursive plugin
** Other plugins such as the VIM plugin or other editing/keybindings

=== Tools and Usage

A suggested set of structural editing keybindings is included as settings.zip, and reflects what most of the developers at Dataico use.

IMPORTANT: These are MacOS bindings. They will not work for Windows or Linux. DO NOT import those settings on Windows or Linux. You can just use whatever the defaults are in the IDE.

After you've installed the Cursive plugin, you can import these settings
using the `File > Manage IDE Settings > Import Settings ...`. You do NOT need to unzip the file. Just point the import at the zip.

You can use `Help -> Show Cursive Cheat Sheet` to reveal the shortcuts.

== Task 1 - Learn how to Run the project

=== Open the Project

In IntelliJ choose `File > Open...` and navigate to this project's root and pick the `deps.edn` file. If it asks you, choose to open in a new window as a Project.

=== Set Dependencies

Clojure projects can define a list of optional dependencies. When working on this project you need to enable the "optional" dev and test dependencies.

To do this open the Clojure Deps tab (usually on the right edge) and open "Aliases". Turn on the "dev" and "test" alias, and then press the "refresh" button at the top of that panel.

image::clojure-deps.png[]

=== Add a REPL

You need to add a REPL. This is done in Intellij
`Run > Edit Configurations ...`.  You will be adding a Clojure Local REPL.

Make it look like this:

image::clj-repl.png[]

=== Run a REPL

The toolbar at the top of IntelliJ will now include your REPL setup. Choose it (from the dropdown) and press the Play button to run it. This should open up a REPL panel in the IDE where you can run Clojure Code.

image::clj-run.png[]

=== Load and Run Code

REPL means "Read Eval Print Loop". It's an interactive coding tool. Clojure is a dynamic and interactive langauge, and code can be changed on the fly.

So, now that we have that working you should play a little bit with Clojure.
Open `src/dev/user.clj`.
This file is auto-loaded by the REPL on startup (so don't leave garbage in it that can crash your startup!).

IMPORTANT: You MUST have properly set your dependencies earlier or this won't work! If you need to go back and do that, then you will also need to stop/start your REPL.

Move to the bottom of the file and create a structured comment. That looks like this:

[source]
----
(comment
  )
----

Anything that is structurally sound (e.g. doesn't have syntax errors) is legal within such a block and will be ignored. But beware, it is structural, so syntax errors will make things fail.

Add the following expression to the comment block:

[source]
----
(comment
  (* (+ 2 2) (+ 5 5))
  )
----

Clojure expressions are lists (parenthetical) where the first item in the list is "what to do". Everything in Clojure in this position is a function (yes, functions can have symbols for names).

So, the above expression means "Call the multiply function on the sub-expressions..." where the subexpressions are each themselves calls of functions that add some numbers together.

Now, you can send any or all of this expression to your active REPL to see what happens. If you look at the Cursive Cheat Sheet you want to memorize TWO shortcuts (which may be different that the items shown below):

* Send Top Form to REPL
* Send Form Before Caret to REPL

image::send-repl.png[]

Play with those two keyboard shortcuts until you understand exactly what they do. They are the most common operation you will do when working interactively with Clojure.


== Task 2 – Basic Clojure and Structural Editing

If you've never worked with a LISP variant like Clojure you will find that your editing experience will be dramatically improved by first learning about structural editing.
If you try to treat Clojure like more common languages you will find the experience very frustrating.

Clojure is written using data structures, which themselves have structure. The code is NOT line-centric. It is structural. Thus, line-based habits will hurt you. Trying to "count parenthesis" will also hurt you. In order to work with Clojure effectively you have to change how you think about the code.

Good Clojure editors will all have structural editing. In IntelliJ with Cursive you have two main options:

. Parinfer: This mode, which you can enable, automatically tries to infer the proper data structure bracing (e.g. parenthesis placement) based on your indentation of the code. Most people find this pretty useful.
. Paredit: This mode tries to enforce matching braces, which some people love and others hate.  I do not recommend this mode for beginners.
. Structural Off: The editor does no automatic structural maintenance.

So, you should either choose Parinfer or "Structural Off".

In ANY mode you always have access to structural editing *commands*, which you *must* learn how to use in order to be most effective.

The Cursive Cheat Sheet lists these under "Structural Navigation" and "Structural Editing".

Your first task is to go to existing source files and learn what a few of these do.

The ones you should memorize first are:

* Slurp and Barf (forwards and backwards)
* Wrap with ... (parens, braces, etc.)
* Split
* Splice
* Join
* Raise
* Kill Sexp
* Close () and newline
* Move form up (and down)

Others will make more sense as you learn the language, but the above list will dramatically improve your experience.

Use an online resource such as https://www.braveclojure.com/foreword/[Clojure for the Brave And True]
to learn some Clojure basics, but use this project and the REPL we just got working to play with examples. While doing those, be sure to practice structural editing.

== Task 3 - Basic Datomic

If you open the `src/dev/development.clj` file you will find functions that can start and stop the web server for this project.

Make sure your REPL is running, and use the keyboard shortcuts to do the following:

* Switch REPL NS to current File
* Load File in REPL

image::switch-and-load.png[]

NOTE: Your key bindings may be different.

By moving your REPL to point to the development namespace and loading it, you're ready to start the server. In order to do that you need to run the `(start)` function. The comment block at the end of the file has those expressions.

[source]
-----
(comment
  ;; Eval the next line to start the server:
  (start)
  ;; Eval the next line to stop the server, reload any stale code, and start it up again
  (restart)
...
-----

=== Try Some Datomic Queries and Transactions

You can do a basic Datomic tutorial to get the basic idea. A good place to start is https://max-datom.com/[Max Datom]

There are additional examples at the bottom of the `development` namespace in the comment block that will work once you've started the server.

This shows you how you can dynamically play with the database in your local running server to better understand the schema, and how Datomic works.

You should work on the tutorial, and then satisfy your own curiosity about the schema of the project and how you go about doing basic CRUD operations on Datomic data.

== Task 4 - Add Database Schema

Now that you have some basic Clojure knowledge and the ability to edit code reasonably well, we're going to start adding some things to this project.

The database for this project is represented by RAD model code in the `src/main/com/example/model` folder.
You should read the https://book.fulcrologic.com/RAD.html[Fulcro RAD book] and understand how things are defined and related by attributes.

The Datomic plugin for RAD is installed, and once you define the model extensions properly the project will auto-create the schema and network resolvers on project startup.

The procedure in this project is basically:

. Define new attributes/entities via `defattr` in the model folder.
. Gather those together into the `src/main/com/example/model.cljc` file
. (Re)start the server, which will cause it to attempt to generate the schema in Datomic.

The development system uses an in-memory database, and that database should
persist as long as you don't kill your REPL.

You are going to create a new schema to track equipment, and to track
which accounts in the system have been assigned the use of that equipment.

There will be one entity per piece of equipment, and then assignment
entities that track the relation (including historically) of who has the
equipment.

.Equipment Tracking Database Schema
[cols="1,2,1,3"]
|===
|Table |Column |Type |Description

.4+|equipment
|id |uuid |Primary key
|kind |keyword|One of :laptop, :monitor, :keyboard, :mouse, :furniture (required)
|serial |string|Serial number (required)
|description |string |description

.5+|assignment
|id |uuid |Primary key
|account |REF |Target of account (required)
|equipment |REF |Target of equipment (required)
|assigned-on |timestamp| When it was assigned (required)
|returned-on |timestamp| When it was returned
|===

An assignment entity that has no return date is the thing that marks
a bit of equipment as "currently assigned". In your code you will
need to enforce the rule that equipment cannot be assigned to more than
one account at a time.

* Add a model equipment namespace, define the attributes, and compose them into the model
* Add a model assignment namespace, define the attributes, and compose them into the model

It is a good idea to manually load your namespace as you go to make sure it compiles. So, keep a running REPL. Once you finish composing a model into the primary model namespace, be sure to reload the entire web server to put that model into service, and double-check it with queries like the ones we have in the development namespace comment.

=== Seed Data

Follow the patterns for that the other entities used and create seed functions and such for the development namespace so that some equipment and assignments will be in the database any time you start from scratch (e.g. start a new REPL) so that you don't have to go through manual entry every time.

Restart your REPL and then use queries to verify that you're seeding the data correctly.

== Task 5 – Add Reports

Now that you have schema and data, it's time to be able to see it in the UI. Add UI namespaces and follow the existing code patterns to create reports for equipment and assignments so you can see the content of the database without having to do queries. Reports need a top-level source attribute and a hand-written resolver that can return at least the ID fields of the entities in question. Follow the patters for the other entities and look for `defresolver` to figure out the best way to do this.

NOTE: You'll need to add the report components to the router and menu UI.
See `src/main/com/example/ui.cljs`. It has both of those things.

When dealing with things like `defsc`, routing, and DOM, you will be using Fulcro library constructs. See https://book.fulcrologic.com[Fulcro Developers Guide] for details about these.

=== Notes on Resolvers

Pathom can resolve whatever you query for in the database if it has the type and UUID of the entity.
This means that most top-level resolvers that you write will return something like:

[source]
-----
{:equipment/all [{:equipment/id some-id} {:equipment/id other-id}]}
-----

and Pathom will automatically use the auto-generated resolvers (from the defattr schema) to populate other attributes that the network API requests.

== Task 6 – Forms

Add forms to your application for creating/editing equipment and assignments. The assignments form will be a little tricky because you have to pick equipment and accounts. You'll have to read up on pickers and look at the invoice item examples.

Be sure to link together your report UI and form UI so that you can try it all out.

You will also need to make a new resolver for equipment (for your picker) so that you only show unassigned equipment when creating a new assignment.

== Summary

You can feel free to continue adding additional functionality to the application. Here are some things to try:

* When disabling an account on the account report, drop all equipment assignments for that account.
* Make assignments non-editable. E.g. the form will allow you to edit everything on a new assignment, but only the return date on an existing one if it is missing.
* Add a button on the equipment report that will allow you to "return" a bit of equipment.
* Add a column on the equipment report that shows the current (if any) assignee.
* Add a filter control on the equipment report to show only the equipment assigned to an account that you pick.
* Add a resolver and column to the account report that indicates how many bits of equipment are assigned to each account.
Extra credit, clicking on the number shows the equipment report pre-filtered by that account.

Some additional fun things to try:

* Customize the UI. See the multimethod rendering in the RAD book.
