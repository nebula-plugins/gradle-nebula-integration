Platform Support
================

Experiment for building a platform.


Requirements
------------

1. Inheriting upstream BOM opinions
2. Adding my own opinions
3. Excluding upstream opinions in favor of my own opinions


To Improve
----------

1. Root configuration for language plugins where recommendations can be applied to everything.
   Otherwise consumers have to migrate to the new configurations or add the bom everywhere it is used.
2. How can I generate an artifact with ComponentMetadataRules from the declarations in the buildfile?


To Dream State
--------------

1. How can I avoid having a plugin consumers must apply?
2. How can I avoid having to create an artifact with ComponentMetadataRules?
3. How can I declare my opinion and derive exclusions where necessary?
