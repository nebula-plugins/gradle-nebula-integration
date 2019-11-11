# Locking ergonomics when using `write-locks` and `update-locks` flags

Given a project is created
And the project uses Gradle core locking
And locks are in use,
When a dependency resolves to a new version
And `--write-locks` and `--update-locks` with a group & artifact are both invoked,
Then the user should be informed that these should not be run at the same time
