# Forms, validation, file uploads, localization, accessibility, and responsive behavior

## Forms

Forms are implemented directly in Compose screens with `remember { mutableStateOf(...) }` for local field state and ViewModel callbacks for persistence.

Major forms:

- login email/password;
- add/edit task and subtask;
- add project;
- add/edit product and variant;
- add user;
- profile phone number update;
- return status and notes update;
- chat message draft, group creation, and group settings.

## Validation

Validation is currently distributed between screens and ViewModels:

- `LoginViewModel` rejects blank email/password.
- task and subtask flows require title and due date; subtasks also require a project through the parent task.
- stock forms validate required names/SKUs, numeric price, numeric stock quantity, and variant fields through `StockValidation`.
- profile phone numbers are checked for required and overly long values in the UI.
- chat group creation requires a non-blank name and at least one selected member.

There is no centralized validation framework.

## File uploads

No binary file upload flow is present in the inspected frontend. Product images are represented by an image URL field, not an uploaded file.

## Localization

Localization is implemented in `AppLocalization.kt` with three languages:

- English (`en`);
- Romanian (`ro`);
- German (`de`).

`AppStrings.text()` falls back to English and then to the key itself. Status and role formatting helpers map enum values to localized strings.

The selected language is held in `App()` and initialized from `UserSession.languageFlag`. Profile language changes update UI state, and phone updates persist the modified user session.

## Accessibility

The code uses Material 3 components, which provide baseline semantics. Specific accessibility-related implementation includes radio-button role semantics in the add-user role selector. There is no dedicated accessibility test suite and no documented screen-reader acceptance checklist in the repository.

## Responsive behavior

The UI uses Compose layouts, safe drawing insets, modal drawer navigation, bottom navigation for selected sections, scrollable forms, and reusable list/detail overlays. Responsive behavior is mostly implicit in Compose layout primitives; there are no breakpoint-specific layout abstractions in the inspected code.

