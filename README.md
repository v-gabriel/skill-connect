# Skill Connect
Android Jetpack Compose chat application built around Azure Communication Services and Firebase.

<br>

## Architecture

<img src="https://github.com/v-gabriel/skill-connect/assets/72694712/e57f6947-0e76-400f-a9a6-7efaa778155f" width=50% height=50%>

<br>
<br>

**Step 1**: user either logins or registers using the [Firebase Authentication](https://firebase.google.com/docs/auth) service

**Step 2**: a communication identifier and token is generated (or refreshed) using the [Azure Functions](https://learn.microsoft.com/en-us/azure/azure-functions/functions-overview?pivots=programming-language-csharp) which serve a identity management for the reserved [Azure Communication Services](https://azure.microsoft.com/en-us/products/communication-services/)

**Step 3**: user data is mapped and saved in the [Firebase Firestore](https://firebase.google.com/docs/firestore), along with other needed relations and entities.

**Step 4**: the app instantiates needed services, some of which listen for chat client and chat thread client events. If the activity changes its state to paused, those services are propagated to the foreground service which can then manage notifications and update the UI state when the activity becomes available again.

<br>

## Tech

**IDEs:** [Android Studio](https://developer.android.com/studio), [Visual Studio](https://visualstudio.microsoft.com/)

**Languages:** [C#](https://learn.microsoft.com/en-us/dotnet/csharp/), [Kotlin](https://kotlinlang.org/)

**Other:** [Azure Communication Services](https://azure.microsoft.com/en-us/products/communication-services/), [Azure Functions](https://learn.microsoft.com/en-us/azure/azure-functions/functions-overview?pivots=programming-language-csharp), [Jetpack Compose](https://developer.android.com/jetpack/compose) 

[![My Skills](https://skillicons.dev/icons?i=androidstudio,kotlin,visualstudio,dotnet,azure)](https://skillicons.dev)

<br>

## Authors

- [@v-gabriel](https://github.com/v-gabriel)

