insertionQuery
==============

Non-dom-event way to catch nodes showing up. And it uses selectors.

**It's not just for wider browser support, It can be better than DOMMutationObserver for certain things.**

## Why?

- Because DOM Events slow down the browser and insertionQuery doesn't
- Because DOM Mutation Observer has less browser support than insertionQuery
- Because with insertionQuery you can filter DOM changes using selectors without performance overhead!

## Widespread support!

IE10+ and mostly anything else (including mobile)

Details: http://caniuse.com/#feat=css-animation

## Installation

    npm install insertion-query

or

    bower install insertionQuery

or just download the `insQ.min.js` file.

## Basic usage

	insertionQ('selector').every(function(element){
		//callback
	});

Runs the callback whenever an element matching the selector is created in the document. This means it handles DOM insertions of new nodes.

Changing an existing element to match the selector won't run the callback. Showing an element that was not displayed before won't run the callback. You can disable preventing those situations with configuration option `insertionQ.config({ strictlyNew:false })`, but it's not recommended.

## Insertion summary

    insertionQ('selector').summary(function(arrayOfInsertedNodes){
		//callback
	});

Runs the callback with an array of newly inserted nodes that contain element(s) matching the selector. For multiple nodes matching the selector, if they were inserted in a wrapper, the wrapper will be returned in the array. The array will contain the smallest set of nodes containing all the changes that happened to document's body.

## Config options

You can change insertionQuery options by calling the config method:

    insertionQ.config({
        strictlyNew : true,
        timeout : 20
    });

- `strictlyNew` Keep track of nodes that existed at the moment of defining a new insertionQuery. Defaults to `true`.
- `timeout` Time in miliseconds to wait before insertionQuery starts listening to events. If DOM already contained elements matching the selector, animation events will be triggered and there's some latency. Defaults to `20`. 20ms was safe in testing, you can change it to 0 if you know what you're doing. (or if you don't mind getting events from existing nodes matching the selector)

## Technical notes:

 - run after DOM is ready or you'll get all the callbacks from HTML elements there. (thank you capt. Obvious)
 - the implementation is based on **CSS animations NOT DOM events**. So no consequences for performance.
 - because it's done with CSS you get the selectors for free, no javascript work is done matching that, not even a `querySelector` call
 - to make sure you won't get calls from elements that are there, the callbacks start working some miliseconds after you call insertionQ, so if you add elements in the same function call that you initiated insertionQuery, you won't get callbacks. This can be changed in config.
 - it actually takes a few miliseconds before the callback runs after element is added (I measured upto 30ms in Firefox)
