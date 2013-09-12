This repository will soon contain a standalone server that can be used
to host and test applications built using the Link SDK. For this
sample server we use glassfish v3.1.2.2 and JavaEE 6.

The Link SDK DOES NOT have any particular dependencies on the server
environment - anything from plain old apache to .NET to PHP, etc. will
work - so view this repository as an illustrative example of how to
build a complete application using the Link SDK, including both the
front end (built in Javascript/CSS3/HTML5) and a server back end.

This repository is also useful for Mobile Helix customers who are
interested in integrating a custom server of their own with the Mobile
Helix authentication infrastructure. There are several parts to this
puzzle, with more documentation to follow:

1) A web service must be created on your custom server. That web
service should receive binary data in the BSON format
(http://bsonspec.org/) that deserializes into an object of type
ApplicationServerCreateSessionRequest. See
ApplicationServerWebServices/src/java/com/mobilehelix/services/objects.

2) You must implement custom session management to secure access to
your custom applications. See the SessionManager, Session, and
SessionFilter objects for an example
(ApplicationServer-ejbPublic/src/java/com/mobilehelix/appserver/session).

We are happy to answer any questions while our documentation remains a
work in progress.