# Manual UI checklist

Use this before a public launch. Automated tests do not replace looking at the page.

## Desktop, about 1440 and 1024

- Homepage: one title, the search field, language areas, four tools, learning without fake cards, assistant as a secondary block
- Header groups open and close, and every link reaches a real page
- Search suggestions, filters, empty state, and pagination
- A word page, a grammar rule, a tool, the assistant, a lesson, and a quiz
- Footer links
- Light and dark, with no unreadable muted text

## Phone, about 375 and 320

- No sideways scrolling on a long Arabic title, a Latin URL, or a search result
- Menu opens, scrolls, and closes. Focus does not stay behind it
- Search field remains usable
- Quiz options are easy to tap
- Lesson contents collapse on a small screen

## Keyboard

- Skip link appears and moves to the content
- Tab order follows the header, then the page
- Dialog closes with Escape
- Radio and checkbox labels receive focus
- Theme toggle announces its state

## Arabic

- Headings and paragraphs do not stretch across a wide monitor
- Mixed Arabic and Latin, including a form code, stay in a sensible order
- Numerals in counts stay consistent
- Dark mode does not flash from light after the first paint

## Flows

- Anonymous visitor never sees a signup wall
- Quiz does not reveal the correct option before submit
- Assistant disabled state offers search and tools
- 404 offers home, search, and sections
- A failed load offers retry and no stack trace
