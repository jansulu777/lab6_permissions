# lab6_permissions

Android Permissions practice class work and Jetpack Compose implementation.

## Part A: Theory Questions & Answers

### Question 1
**Context:** A student wants to add photo capture to an app. They immediately wrote the `CAMERA` permission into the manifest and moved on to the dialog code. A teammate says: “First decide whether a permission is needed at all.”  
**Task:** List the four decisions of the permission workflow from the lecture, in order. Give an example of a case that works without any permission, and explain why that is the best option.

**Answer:**
1. **Four Decisions of the Permission Workflow:**
   1. Decide if the feature can be fulfilled without any permission (e.g. system intents/pickers).
   2. Determine the correct permission category (Install-time vs Runtime vs Special).
   3. Declare permission in `AndroidManifest.xml` and implement runtime request logic if required.
   4. Handle user decisions gracefully (Granted, Denied, Rationale, Permanently Denied).
2. **Example working without permission:** Taking a photo using `ActivityResultContracts.TakePicture()` or picking photos using `ActivityResultContracts.PickVisualMedia()`.
3. **Why it is the best option:** Preserves user privacy, builds user trust, requires no runtime permission code or rationale dialogs.

---

### Question 2
**Context:** Your teammate thinks Android permissions are just “one line in the manifest” and writes the same code for every permission.  
**Task:** Name the three permission categories (install-time, runtime, special) and explain how each one is granted. Show why the category decides how much code you have to write.

**Answer:**
1. **Permission Categories & How Granted:**
   - **Install-time Permissions:** Automatically granted by the OS when the app is installed.
   - **Runtime Permissions:** Granted at runtime via in-app system dialogs.
   - **Special Permissions:** Granted manually by the user from system settings screens.
2. **Code Impact:**
   - Install-time requires only 1 line in `AndroidManifest.xml`.
   - Runtime requires manifest declaration + checking permission status + launcher request contracts + rationale dialogs.
   - Special permissions require manifest declaration + system settings intent launchers + lifecycle resume observers.

---

### Question 3
**Context:** A developer added `INTERNET` to the manifest and is now writing a `checkSelfPermission` check before every network request. In another app the same line was forgotten.  
**Task:** Explain why a normal permission needs no runtime check and no dialog. What exception does the app throw when the manifest line is forgotten? Give the three examples of normal permissions from the lecture.

**Answer:**
1. **Why no runtime check is needed:** Normal permissions pose minimal risk to privacy/security and are granted automatically at install time.
2. **Exception thrown:** `java.lang.SecurityException`.
3. **Three Examples:** `INTERNET`, `ACCESS_NETWORK_STATE`, `VIBRATE`.

---

### Question 4
**Context:** A company has two apps: “Bank” and “Wallet”. Wallet wants to bind to the bank's `AccountsService`, which is protected by `uz.ttpu.bank.permission.READ_ACCOUNTS`. A third-party app requests the same permission.  
**Task:** Who is a signature permission granted to? Which `protectionLevel` is written when you declare it in the manifest? What is the result for Wallet and for the stranger app, and is a dialog shown to the user?

**Answer:**
1. **Who granted to:** Only apps signed with the exact same certificate as the declaring app.
2. **protectionLevel:** `android:protectionLevel="signature"`.
3. **Result:** Wallet is granted access automatically. The stranger app is denied access automatically. No dialog is shown to the user.

---

### Question 5
**Context:** A student believes install-time permissions must also be requested through a dialog while the app is running.  
**Task:** Explain when and where install-time permissions are shown to the user, and when the system grants them. Which two kinds of permissions belong to this category?

**Answer:**
1. **When/Where shown & Granted:** Shown in the app store listing / app settings before downloading. Granted automatically at installation time.
2. **Two Kinds:** Normal permissions and Signature permissions.

---

## Part B: Hands-on Jetpack Compose Implementation

This repository contains a full Android Studio project (`lab6_permissions`) built with Jetpack Compose demonstrating:
- **Normal Permissions (`VIBRATE`, `INTERNET`, `ACCESS_NETWORK_STATE`):** Handling install-time permissions.
- **Single Runtime Permission (`CAMERA`):** Requesting single permissions, showing educational rationale dialogs, status badges, and redirecting to System Settings on permanent denial.
- **Multiple Runtime Permissions (`CAMERA`, `RECORD_AUDIO`, `LOCATION`):** Batch requesting multiple permissions with status tracking.
- **Lifecycle Integration:** Auto-refreshing status when returning from System Settings.
