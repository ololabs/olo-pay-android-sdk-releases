# Olo Pay Android SDK Changelog

## v3.1.2 (Dec 3, 2024)

#### Updates
- `PaymentCardDetailsForm`: Added `setCardBackgroundStyle()` to set the background style of the form view

#### Dependency Updates
- Updated source and target compatibility to Java 17
- Updated target SDK version to 34
- Updated Android Studio Gradle Plugin to v8.4.2
- Updated to Gradle 8.6
- Updated to `com.stripe:stripe-android:20.53.0`
- Updated to `androidx.navigation:navigation-fragment-ktx:2.8.4`
- Updated to `androidx.navigation:navigation-ui-ktx:2.8.4`
- Updated to `androidx.activity:activity-ktx:1.9.3`
- Updated to `androidx.lifecycle:lifecycle-livedata-ktx:2.8.7`
- Updated to `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0`
- Updated to `androidx.navigation:navigation-safe-args-gradle-plugin:2.8.4`

## v3.1.1 (June 20, 2024)

#### Bug Fixes
- `PaymentCardDetailsSingleLineView`: Fixed crash caused by calling `setTextColor` with an empty string
- `PaymentCardDetailsSingleLineView`: Fixed crash caused by calling `setCardBackgroundStyle` with an empty string
- `PaymentCardDetailsSingleLineView`: Fixed crash caused by calling `setErrorBackgroundStyle` with an empty string
- `PaymentCardDetailsSingleLineView`: Fixed crash caused by calling `setErrorTextColor` with an empty string
- `PaymentCardDetailsSingleLineView`: Fixed crash caused by calling `setCursorColor` with an empty string
- `PaymentCardDetailsSingleLineView`: Fixed crash caused by calling `setHintTextColor` with an empty string
- `PaymentCardDetailsMultiLineView`: Fixed crash caused by calling `setCardBackgroundStyle` with an empty string
- `PaymentCardDetailsMultiLineView`: Fixed crash caused by calling `setErrorBackgroundStyle` with an empty string
- `PaymentCardDetailsMultiLineView`: Fixed crash caused by calling `setTextColor` with an empty string
- `PaymentCardDetailsMultiLineView`: Fixed crash caused by calling `setErrorTextColor` with an empty string
- `PaymentCardDetailsMultiLineView`: Fixed crash caused by calling `setCursorColor` with an empty string
- `PaymentCardDetailsMultiLineView`: Fixed crash caused by calling `setHintTextColor` with an empty string
- `PaymentCardDetailsMultiLineView`: Fixed crash caused by calling `setFocusedHintTextColor` with an empty string
- `PaymentCardDetailsMultiLineView`: Fixed crash caused by calling `setFieldUnderlineColors` with an empty string
- `PaymentCardDetailsForm`: Fixed crash caused by calling `setCardBorderColor` with an empty string
- `PaymentCardDetailsForm`: Fixed crash caused by calling `setFieldDividerColor` with an empty string
- `PaymentCardDetailsForm`: Fixed crash caused by calling `setCardBackgroundColor` with an empty string
- `PaymentCardDetailsForm`: Fixed crash caused by calling `setErrorBackgroundStyle` with an empty string
- `PaymentCardDetailsForm`: Fixed crash caused by calling `setTextColor` with an empty string
- `PaymentCardDetailsForm`: Fixed crash caused by calling `setErrorTextColor` with an empty string
- `PaymentCardDetailsForm`: Fixed crash caused by calling `setCursorColor` with an empty string
- `PaymentCardDetailsForm`: Fixed crash caused by calling `setHintTextColor` with an empty string
- `PaymentCardDetailsForm`: Fixed crash caused by calling `setFocusedHintTextColor` with an empty string
- `PaymentCardCvvView`: Fixed crash caused by calling `setHintTextColor` with an empty string
- `PaymentCardCvvView`: Fixed crash caused by calling `setTextColor` with an empty string
- `PaymentCardCvvView`: Fixed crash caused by calling `setCursorColor` with an empty string
- `PaymentCardCvvView`: Fixed crash caused by calling `setErrorTextColor` with an empty string
- `PaymentCardCvvView`: Fixed crash caused by calling `setErrorBackgroundStyle` with an empty string
- `PaymentCardCvvView`: Fixed crash caused by calling `setCvvBackgroundStyle` with an empty string

#### Updates
- `PaymentCardDetailsSingleLineView`: Changed height of text input field from `wrap_content` to `match_parent`
- `PaymentCardDetailsMultiLineView`: Add ability to set the gravity of the built-in error message
- `PaymentCardDetailsForm`: Add ability to set the gravity of the built-in error message
- `PaymentCardCvvView`: Add ability to set the gravity of the built-in error message

#### Dependency Updates
- Updated to `androidx.core:core-ktx:1.13.1`
- Updated to `androidx.appcompat:appcompat:1.7.0`
- Updated to `androidx.activity:activity-ktx:1.9.0`
- Updated to `androidx.lifecycle:lifecycle-livedata-ktx:2.8.2`
- Updated to `com.google.android.material:material:1.12.0`
- Updated to `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1`
- Updated to `com.google.android.gms:play-services-wallet:19.4.0`
- Updated to `com.stripe:stripe-android:20.46.0`

## v3.1.0 (Mar 22, 2024)

#### Bug Fixes
- `PaymentCardDetailsSingleLineView`: Card number field correctly renders error state for unsupported card brands
- `PaymentCardDetailsSingleLineView`: Fixed issue with fields not applying error color changes if they are currently in an error state when the change is applied
- `PaymentCardDetailsMultiLineView`: Fixed incorrect tinting of card number and cvv icons
- `PaymentCardDetailsMultiLineView`: Fixed issue with `clearFields()` incorrectly causing the cvv to be displayed instead of the card number icon
- `PaymentCardDetailsMultiLineView`: Card number field correctly renders error state for unsupported card brands
- `PaymentCardDetailsMultiLineView`: Fixed issue with fields not applying error color changes if they are currently in an error state when the change is applied
- `PaymentCardDetailsForm`: Fixed issue with `setCardBackgroundColor()` incorrectly setting background color when the color was passed in as a string
- `PaymentCardDetailsForm`: Fixed issue with hint text getting set incorrectly
- `PaymentCardDetailsForm`: Fixed issue with fields not applying error color changes if they are currently in an error state when the change is applied
- `PaymentCardCvvView`: Updated hint color to use same color as other card input views

#### Updates
- Deprecated `SetupParameters.freshSetup`
- Changed `IOloPayApiInitializer.environment` to have a public getter
- `PaymentCardDetailsSingleLineView`: Added full support for XML styling
- `PaymentCardDetailsSingleLineView`: Added methods for styling text and error text using [TextAppearance](https://developer.android.com/reference/android/R.styleable#TextAppearance) styles
- `PaymentCardDetailsSingleLineView`: Added methods for styling the error message background
- `PaymentCardDetailsSingleLineView`: Added `setVerticalSpacing()` to control the spacing between the input field and error message label
- `PaymentCardDetailsSingleLineView`: Added `getFont()` and `getErrorFont()`
- `PaymentCardDetailsSingleLineView`: Added `configurationChangeListener` property
- `PaymentCardDetailsMultiLineView`: Added full support for XML styling
- `PaymentCardDetailsMultiLineView`: Added methods for styling text and error text using [TextAppearance](https://developer.android.com/reference/android/R.styleable#TextAppearance) styles
- `PaymentCardDetailsMultiLineView`: Added `setFocusedHintText()` to allow for different hint text when the fields have focus
- `PaymentCardDetailsMultiLineView`: Added methods for styling the error message background
- `PaymentCardDetailsMultiLineView`: Added `setVerticalSpacing()` to control the spacing between the input field and error message label
- `PaymentCardDetailsMultiLineView`: Added `getFont()` and `getErrorFont()`
- `PaymentCardDetailsMultiLineView`: Added `configurationChangeListener` property
- `PaymentCardDetailsForm`: Added full support for XML styling
- `PaymentCardDetailsForm`: Added methods for styling text and error text using [TextAppearance](https://developer.android.com/reference/android/R.styleable#TextAppearance) styles
- `PaymentCardDetailsForm`: Added `setFocusedHintText()` to allow for different hint text when the fields have focus
- `PaymentCardDetailsForm`: Added methods for styling the error message background
- `PaymentCardDetailsForm`: Added `setVerticalSpacing()` to control the spacing between the input field and error message label
- `PaymentCardDetailsForm`: Added `getFont()` and `getErrorFont()`
- `PaymentCardDetailsForm`: Added `configurationChangeListener` property
- `PaymentCardCvvView`: Added full support for XML styling
- `PaymentCardCvvView`: Added methods for styling text and error text using [TextAppearance](https://developer.android.com/reference/android/R.styleable#TextAppearance) styles
- `PaymentCardCvvView`: Added methods for styling the error message background
- `PaymentCardCvvView`: Added `setVerticalSpacing()` to control the spacing between the input field and error message label
- `PaymentCardDetailsCvvView`: Added `getFont()` and `getErrorFont()`
- `PaymentCardDetailsCvvView`: Added `configurationChangeListener` property
- Test Harness improvements: Submit CVV token to Ordering API, add button to clear focus from `PaymentCardDetailsForm`

#### Dependency Updates
- Updated to compileSdkVersion 34
- Updated Android Studio Gradle Plugin to v8.2.2
- Updated to Kotlin v1.9.10
- Updated to Gradle v8.5
- Updated source and target compatibility from Java 1.8 to Java 11
- Updated to `com.stripe:stripe-android:20.37.1`
- Updated to `androidx.core:core-ktx:1.12.0`
- Updated to `androidx.navigation:navigation-fragment-ktx:2.7.7`
- Updated to `androidx.navigation:navigation-ui-ktx:2.7.7`
- Updated to `androidx.activity:activity-ktx:1.8.2`
- Updated to `androidx.lifecycle:lifecycle-livedata-ktx:2.7.0`
- Updated to `com.google.android.material:material:1.11.0`
- Updated to `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3`
- Updated to `com.google.android.gms:play-services-wallet:19.3.0`

## v3.0.0 (Oct 27, 2023)

#### Breaking Changes
- Changed **ALL** references of `CVC` to `CVV`
- `CardInputListener`: Changed method signature of `onFocusChange()`
- `CardErrorType`: Removed `incorrectNumber` and merged its use case with `invalidNumber`
- `CardErrorType`: Removed `incorrectZip` and merged its use case with `invalidZip`
- `CardErrorType`: Removed `incorrectCVC` and merged its use case with `invalidCVV`

#### Bug Fixes
- `PaymentCardDetailsSingleLineView`: Fix issue with `clearFields()` method causing the following controls to have incorrect state
- `PaymentCardDetailsMultiLineView`: Fix issue with `clearFields()` method causing the following controls to have incorrect state
- `PaymentCardDetailsForm`: Fix issue with `clearFields()` method causing the following controls to have incorrect state

#### Updates
- Added support for CVV tokenization
    - See `PaymentCardCvvView` control
    - See `OloPayAPI.createCvvUpdateToken()`
- `PaymentCardDetailsSingleLineView`: Improved focus change handling
- `PaymentCardDetailsSingleLineView`: Improved logic for displaying error messages
- `PaymentCardDetailsMultiLineView`: Improved focus change handling
- `PaymentCardDetailsMultiLineView`: Improved logic for displaying error messages
- `CardInputListener`: Deprecated `onFieldComplete()`
- `CardInputListener`: Added `onValidStateChanged()`
- `FormValidCallback`: Added `onValidStateChanged()`
- `IPaymentMethod`: Added `environment` property
- Test Harness Improvements: New tabbed interface for each main aspect of the Olo pay SDK: Credit Cards, Google Pay, CVV Tokenization

#### Dependency Updates
- Updated to `com.android.tools.build:gradle:7.4.2`

## v2.0.1 (July 14, 2023)

#### Updates
- `GooglePayContext:`General improvements to the Google Pay flow (same public facing API)

#### Dependency Updates
- Added `androidx.navigation:navigation-fragment-ktx:2.6.0`
- Added `androidx.navigation:navigation-ui-ktx:2.6.0`
- Added `androidx.activity:activity-ktx:1.7.2`
- Added `androidx.lifecycle:lifecycle-livedata-ktx:2.6.1`
- Added `com.google.android.gms:play-services-wallet:19.1.0`
- Added `androidx.lifecycle:lifecycle-extensions:2.2.0`

## v2.0.0 (Jun 16, 2023)

#### Breaking Changes
- Updated minimum SDK to API 23

#### Bug Fixes
- `PaymentCardDetailsSingleLineView:` Fixed issue preventing error messages from being displayed for unsupported card brands
- `PaymentCardDetailsMultiLineView:` Fixed issue preventing error messages from being displayed for unsupported card brands

#### Updates
- `PaymentCardDetailsSingleLineView:` Added methods to customize the background, text and cursor colors, and fonts
- `PaymentCardDetailsSingleLineView:` Added hint text support for all card fields (both in code and xml)
- `PaymentCardDetailsMultiLineView:` Added methods to customize the background, text and cursor colors, and fonts
- `PaymentCardDetailsMultiLineView:` Added hint text support for all card fields (both in code and xml)
- `PaymentCardDetailsForm:` Added methods to customize the background, text and cursor colors, and fonts
- `PaymentCardDetailsForm:` Added hint text support for all card fields (both in code and xml)
- Added `CardBrand.Unsupported` enum type
- Improved caching mechanism when switching between Test and Production environments during development

#### Dependency Updates
- Updated to Kotlin v1.8.0
- Updated to `androidx.core:core-ktx:1.10.1`
- Updated to `androidx.appcompat:appcompat:1.6.1`
- Updated to `com.google.android.material:material:1.9.0`
- Updated to `com.stripe:stripe-android:20.25.5`

## v1.3.0 (Dec 5, 2022)

#### Breaking Changes
- `PaymentCardDetailsSingleLineView:` Removed `postalCodeRequired` and `usZipCodeRequired` properties
- `PaymentCardDetailsMultiLineView:` Removed `postalCodeRequired` and `usZipCodeRequired` properties

#### Updates
- `PaymentCardDetailsSingleLineView:` Added US and CA postal code validation
- `PaymentCardDetailsMultiLineView:` Added US and CA postal code validation

#### Dependency Updates
- Updated to Kotlin v1.7.22
- Updated to `com.stripe:stripe-android:20.16.1`
- Updated to `com.google.android.material:material:1.7.0`

## v1.2.2 (Sep 27, 2022)

#### Dependency Updates
- Update compile and target SDK version to 33
- Updated to Kotlin v1.7.10
- Updated to `com.stripe:stripe-android:20.13.0`
- Updated to `androidx.core:core-ktx:1.9.0`
- Updated to `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4`
- Updated to `androidx.appcompat:appcompat:1.5.1`
- Updated to `com.google.android.material:material:1.6.1`

## v1.2.1 (May 27, 2022)

#### Updates
- Added Maven support

#### Dependency Updates
- Updated to `com.stripe:stripe-android:20.4.0`
- Updated to `com.google.android.material:material:1.6.0`

## v1.2.0 (May 18, 2022)

#### Breaking Changes
- `PaymentCardDetailsSingleLineView.hasErrorMessage` has been changed from a property to a method
- `PaymentCardDetailsMultiLineView.hasErrorMessage` has been changed from a property to a method
- `PaymentCardDetailsSingleLineView.errorMessage` has been replaced with `getErrorMessage()`
- `PaymentCardDetailsMultiLineView.errorMessage` has been replaced with `getErrorMessage()`

#### Bug Fixes
- `PaymentCardDetailsSingleLineView:` Fix layout refresh issue when using the SDK in a React Native app
- Add missing Google Pay configuration parameters to Java Test Harness sample code
- Fix test harness compilation issues due to missing gradle scripts
- Fix code example in `ApplicationInitializer` docs

#### Updates
- Error message now displays for `PaymentCardDetailsSingleLineView` and `PaymentCardDetailsMultiLineView` if `paymentMethodParams` property is accessed and card details are invalid
- `PaymentCardDetailsSingleLineView.displayErrors` can now be set in XML
- `PaymentCardDetailsMultiLineView.displayErrors` can now be set in XML

## v1.1.0 (Feb 28, 2022)

#### Breaking Changes
- Renamed `OloPay.PaymentCardDetailsMultiLineView.TextInputLayout.ErrorTextAppearance` style to `"OloPay.PaymentCardDetailsMultiLineView.ErrorText.TextAppearance"`

#### Updates
- `PaymentCardDetailsSingleLineView:` Improved US postal code validation
- `PaymentCardDetailsMultiLineView:` Improved US postal code validation
- `PaymentCardDetailsMultiLineView:` Add custom error message support

#### Dependency Updates
- Updated to `com.stripe:stripe-android:19.2.0`

## v1.0.1 (Dec 20, 2021)

#### Bug Fixes
- Fixed bug preventing `ApplicationInitializer.initialize()` from being called
- Fixed bug when creating input controls in code rather than inflating via XML layouts
- `PaymentCardDetailsSingleLineView:` Added `postalCodeEnabled`, `postalCodeRequired`, and `usZipCodeRequired` properties
- `PaymentCardDetailsMultiLineView:` Added `postalCodeEnabled`, `postalCodeRequired`, and `usZipCodeRequired` properties
- Fixed a bug that incorrectly mapped `GooglePayErrorType.DeveloperError` as `GooglePayErrorType.NetworkError` and vice-versa

#### Updates
- Added `OloPayEnvironment` enum to support Test and Production environments (set via `SetupParameters.environment`)
- `OloPayApiInitializer:` Added consistent error handling between java/kotlin flows
- Callbacks in the `GooglePayContext` constructor are now optional and can also be set via properties
- Added `IGooglePayContext.readyCallback` and `IGooglePayContext.resultCallback`

#### Dependency Updates
- Changed Compile SDK Version from 30 to 31

## v1.0.0 (Oct 25, 2021)
- Initial release