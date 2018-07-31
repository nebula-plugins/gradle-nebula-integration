# Dependency insight investigations 
###### Using core gradle features

## Purpose
Investigating the interactions with dependency selection processes to make sure that all participants, all reasons, and all rejected versions are shown. 

## Selection reasons
The following selection reasons are combined to ensure all selection reasons are shown. 

- Basic version selection reasons
    - Static configuration
    - Dynamic configuration
    - A recommendation (BOM or properties file)
- Enforcement selection reasons
    - Locks
    - Forces
- Selection rules
    - Excludes
    - Rejections
    - Replacements
    - Substitutions

## Results

- Force messages with recommendations
    - Using a bom 
    - Forces do not show up when there is a recommendation in place
    - `rec-force` is the simplest example. See also:
        - `rec-force-lock`
        - `reject-rec-force`
        - `reject-rec-force-lock`
        - `replacement-rec-force`
        - `replacement-rec-force-lock`
        - `substitute-rec-force`
        - `substitute-rec-force-lock`
- Force messages with substitutions
  - Using `substitute module('<module>') because (substitutionMessage) with module('<other-module>')`
  - Forces do not show up when there is a substitution in place
  - `substitute-static-force` is the simplest example. See also:
     - `substitute-dynamic-force`
     - `substitute-dynamic-force-lock`
     - `substitute-static-force-lock`
     - `substitute-rec-force`
     - `substitute-rec-force-lock`
- Rejection messages with non-dynamic dependencies
    - Using `selection.reject(rejectionMessage)`
    - Rejections do not show up as a contributer when user has not selected a dynamic version
        - `reject-static` is the simplest example. See also the other failing tests starting with `reject-`)
- Exclude messages
    - Using `exclude group: '<group>', module: '<name>'`
    - It would be nice to add a reason for the exclusion
    - When running `dependencyInsight`, the exclusion selection information does not show up. Would prefer to see something like "Selection reason - dependency was excluded beacause <reason>", whereas we currently see: 
    ```
    No dependencies matching given input were found in configuration ':compileClasspath'
    ```
    - Other information contributing to selection reasons also does not show up.
        - Forces, locks, and more selection reasons do not show up 
        - More importantly, `exclude-substitute-static` (and related projects) would be a great place to see that substitutions contributed, but did not take priority to the resolution
