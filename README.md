Original App Design Project - README Template
===

# App Ideas
1. Receipt scanner app
2.

# Receipt Scanner

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
Uses your phone camera to scan physical receipts and save them for future records

### App Evaluation
- **Category:** Utility
- **Mobile:** uses camera, mobile first experience.
- **Story:** Allows users to keep digital records of their receipts. Users can query receipts and organize them by stores, dates etc
- **Market:** People that dislike keeping physical receipts would love this
- **Habit:** User goes to store, buys stuff, gets receipt. User scans receipt with app and receipt information gets extracted by the app and stored in a database.
- **Scope:** User can create receipts, query receipts, sort receipts and filter receipts.

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* User must be able to sign in
* User can scan receipt
* User can make queries for receipts using merchant name, items etc through search bar
* Receipts can be filtered by the merchants, dates, total amounts, tags
* User should be able to use the app while offline
* User can choose to save pictures of receipts in settings page
* Users can create categories based on queries. For example, users can create a category based on a filter to search for only receipts from publix. If they save this categeory, they will then be able to click it to get only receipts from publix
* Users can also tag receipt(different from tagging items under the receipt). By default there will be a reimbursement tag and reimbursement category which will depend on the reimbursement tag. The reimbursements category saves receipts that the user plans to geet reimbursed later

**Optional Nice-to-have Stories**

* Statistics page where user can view various stats about their spendings like most visited merchant, most popular item category
* User can label each item under each receipt (e.g food, electronics etc)
* User can see predictions on their spending for the next cycle(2 weeks, month, quarter etc)
* User can set budgets for a cycle and receive push/email notifications if they surpass their budgets


### 2. Screen Archetypes

* Splash screen
    * User gets quick onboarding of what the app can do
    * Navigates to login screen / register screen
* Login screen / Register screen
* List of receipts screen (Main Screen)
    * User can categorize receipts according to date, merchant, total price etc
    * User can make search queries for receipts - using item name, merchant name - using search bar
* Receipt details screen
    * User can edit receipt
    * User can delete receipt
    * User can tag receipt
    * User can label items under receipt - stretch

* Settings Screen
    * will probably be accessed from a sidebar

* Filter Screen
    * will be accessed from action button on toolbar
* Main Screen drawer
    * Shows user email
    * Shows categories e.g reimbursements
    * Has button to go to settings screen

### 3. Navigation


**Flow Navigation** (Screen to Screen)

* Splash Screen
    * navigate to Login Screen after click through splash screen pages
* Login Screen
    * navigate to Main screen(list of receipts)
* Main screen
    * navigate to Settings screen
    * navigate to Receipt Details screen
    * navigate to Login screen after log out
    * navigate to Filter screen

* Settings Screen
    * Go back to main screen!


## Wireframes
![](https://i.imgur.com/NGMEbof.jpg)
![](https://i.imgur.com/1W8W7S8.jpg)


[Add picture of your hand sketched wireframes in this section]
<img src="YOUR_WIREFRAME_IMAGE_URL" width=600>

### [BONUS] Digital Wireframes & Mockups

### [BONUS] Interactive Prototype

## Schema
[This section will be completed in Unit 9]
### Models
[Add table of models]
### Networking
- [Add list of network requests by screen ]
- [Create basic snippets for each Parse network request]
- [OPTIONAL: List endpoints if using existing API such as Yelp]