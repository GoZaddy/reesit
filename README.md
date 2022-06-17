Original App Design Project - README Template
===

# App Ideas
1. Receipt scanner app
2. Travel app to get most picture-worthy locations
3. Social media app - close knit friends - volunteer to help with stuff
4. Business app, get daily offers and discounts from businesses close to you

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
    * user logs in or creates account here
    * will be two acitivities not one. or one activity with two fragments
* Receipt Creation/update Screen
    * user takes picture of receipt here
    * user also edits receipts here
    * Should have a button to take pictures(probably a floating action button)
    * user creates receipts here
* List of receipts screen (Main Screen)
    * Should contain a floating action button that you can use to create a receipt
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
* Create Custom categories screen
    * To create custom categories

### 3. Navigation

**Flow Navigation** (Screen to Screen)

* Splash Screen
    * navigate to Login Screen after click through splash screen pages
* Login Screen
    * navigate to Main screen(list of receipts)
    * navigate to Register screen if user has no account
* Main screen
    * navigate to Settings screen
    * navigate to Receipt Details screen
    * navigate to Login screen after log out
    * navigate to Filter screen
    * navigate to Create Custom categories screen
    * navigate to Receipt creation screen

* Settings Screen
    * Go back to main screen!


## Wireframes
![](https://i.imgur.com/NGMEbof.jpg)
![](https://i.imgur.com/1W8W7S8.jpg)

### [BONUS] Digital Wireframes & Mockups

### [BONUS] Interactive Prototype

## Schema
[This section will be completed in Unit 9]
### Models
[Add table of models]
#### User

   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the user |
   | email        | String | user's email |
   | password         | String     | user's password |
   | createdAt     | DateTime | date when user is created (default field) |
   | updatedAt     | DateTime | date when user is last updated (default field) |

#### Receipt

   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the receipt (default field) |
   | userId        | Pointer to User| receipt owner |
   | merchantId      | Pointer to Merchant  | merchant issuing receipt |
   | total       | Number   | total amount paid(plus tax) |
   | savings       | Number   | total amount saved |
   | tags       | Array of Pointers to ReceiptTag  | tags for this receipt |
   | referenceNumber | String   | reference number of receipt(if available) |
   | createdAt     | DateTime | date when receipt is created (default field) |
   | updatedAt     | DateTime | date when receipt is last updated (default field) |
   
  #### Item

   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for item on receipt (default field) |
   | userId        | Pointer to User| receipt owner |
   | receiptId     | Pointer to Receipt | receipt that this item belongs to |
   | itemCount     | Number   | count for this item |
   | pricePerItem  | Number   | how much a singular item costs |
   | totalAmountPaid | Number   | how much was paid for this item |
   | createdAt     | DateTime | date when item is created (default field) |
   | updatedAt     | DateTime | date when item is last updated (default field) | 
   
#### Merchant

   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the merchant (default field) |
   | name          | String   | name of merchant  |
   | logo          | String     | logo of merchant(not required) |
   | createdAt     | DateTime | date when merchant is created (default field) |
   | updatedAt     | DateTime | date when merchant is last updated (default field) |    
   
#### CustomCategory

   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for custom category (default field) |
   | name        | String| name of custom category |
   | filters        | Array     | Array of filter objects defined in json |
   | userId       | Pointer to User   | user who created this custom category |
   | createdAt     | DateTime | date when post is created (default field) |
   | updatedAt     | DateTime | date when post is last updated (default field) |   
   
   #### Receipt Tag

   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for receipt tag (default field) |
   | name        | String| name of receipt tag |
   | createdAt     | DateTime | date when receipt tag is created (default field) |
   | updatedAt     | DateTime | date when receipt tag is last updated (default field) |   
   
   
### Networking
- [Add list of network requests by screen ]
- [Create basic snippets for each Parse network request]
- [OPTIONAL: List endpoints if using existing API such as Yelp]

#### List of network requests by screen
- Main Screen
  - (Read/GET) Query all receipts where user is an owner (have a load more button to load more receipts)
     ```swift
     let query = PFQuery(className:"Receipt")
     query.whereKey("author", equalTo: currentUser)
     query.order(byDescending: "createdAt")
     query.findObjectsInBackground { (receipts: [PFObject]?, error: Error?) in
        if let error = error { 
           print(error.localizedDescription)
        } else if let receipts = receipts {
           print("Successfully retrieved \(receipts.count) receipts.")
       // TODO: Do something with receipts...
        }
     }
     ```
  - (Read/GET) Get Custom Categories

- Receipt creation/edition screen
    - (Create/POST) Create new receipt
    - (Update/PUT) Update new receipt
    - (Delete/DELETE) Delete receipt
    - (Read/GET) Get all receipt tags that have been added by user so we can add them to dropdown

- Settings screen
    - (Read/GET) Get user settings
    - (Create/ POST) Update user settings

-  Register Screen
    -  (Create/POST) Create new user
-  Login Screen
    -  (Read/GET) Get user information to authenticate user
- Create Custom Categories screen
    - (Create/POST) Create Custom Categories

     
  
