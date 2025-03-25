# Parse SDK for Kotlin

A modern Kotlin SDK for the Parse Platform, built with Ktor and coroutines.

## Features

- Modern Kotlin syntax and features
- Coroutines for asynchronous operations
- Ktor for HTTP networking
- Full Parse Platform API support
- Type-safe query builder
- File upload/download support
- User authentication and management

## Installation

Add the following to your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("org.parseplatform:parse-sdk-kotlin:1.0.0")
}
```

## Usage

### Initialize Parse

```kotlin
Parse.initialize(
    applicationId = "your-application-id",
    clientKey = "your-client-key", // Optional
    masterKey = "your-master-key", // Optional
    serverUrl = "https://api.parse.com/1" // Optional, defaults to Parse.com
)
```

### Create and Save Objects

```kotlin
// Create a new object
val gameScore = ParseObject.create("GameScore")
gameScore["score"] = 1337
gameScore["playerName"] = "Sean Plott"
gameScore["cheatMode"] = false

// Save the object
gameScore.save()
```

### Query Objects

```kotlin
// Create a query
val query = ParseQuery<ParseObject>("GameScore")
    .equalTo("playerName", "Sean Plott")
    .greaterThan("score", 1000)
    .orderByDescending("score")
    .setLimit(10)

// Find objects
val results = query.find()

// Get first result
val firstResult = query.first()

// Count results
val count = query.count()
```

### User Authentication

```kotlin
// Sign up
val user = ParseUser.signUp("username", "password", "email@example.com")

// Log in
val loggedInUser = ParseUser.logIn("username", "password")

// Get current user
val currentUser = ParseUser.getCurrentUser()

// Log out
ParseUser.logOut()

// Request password reset
ParseUser.requestPasswordReset("email@example.com")
```

### File Operations

```kotlin
// Upload a file
val file = ParseFile.fromFile(File("image.jpg"))
file.save()

// Download a file
val data = file.download()

// Save to local path
file.saveTo("downloaded-image.jpg")
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.