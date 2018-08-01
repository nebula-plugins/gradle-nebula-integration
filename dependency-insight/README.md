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
    - Alignments 
    - Excludes
    - Rejections
    - Replacements
    - Substitutions
    - Alignment

## Results

Results are located in `docs/<grouping>/<scenario>` with 
- an `output.txt` file showing the tasks run, the console output, and assertions we're validating against.
- a folder `input` with the contributing `build.gradle` and other related files, such as generated locks and a local bom 

These results are directly written from test results, so they will regenerate when tests are run with `./gradlew clean build`

#### Force messages with recommendations
Using a bom 
- Forces do not show up when there is a recommendation in place
- ★ `rec-force` is the simplest example. See also:
    - `rec-force-lock`
    - `reject-rec-force`
    - `reject-rec-force-lock`
    - `replacement-rec-force`
    - `replacement-rec-force-lock`
    - `substitute-rec-force`
    - `substitute-rec-force-lock`

#### Force messages with substitutions
Using `substitute module('<module>') because (substitutionMessage) with module('<other-module>')`
- Forces do not show up when there is a substitution in place
- ★ `substitute-static-force` is the simplest example. See also:
    - `substitute-dynamic-force`
    - `substitute-dynamic-force-lock`
    - `substitute-static-force-lock`
    - `substitute-rec-force`
    - `substitute-rec-force-lock`

#### Rejection messages with non-dynamic dependencies
Using `selection.reject(rejectionMessage)`
- Rejections do not show up as a contributer when user has not selected a dynamic version
    - ★ `reject-static` is the simplest example. See also the other failing tests starting with `reject-`

#### Exclude messages
Using `exclude group: '<group>', module: '<name>'`
- Given I have an exclusion, then I would like to see this as a selection reason
- When running `dependencyInsight`, the exclusion selection information does not show up. Would prefer to see something like:
 ```
By constraint : dependency was excluded beacause <reason>
 ```
 whereas we currently see: 
```
No dependencies matching given input were found in configuration ':compileClasspath'
```
- Other information contributing to selection reasons also does not show up.
    - Forces, locks, and more selection reasons do not show up 
    - More importantly, `exclude-substitute-static` (and related projects) would be a great place to see that substitutions contributed, but did not take priority to the resolution
#### Alignment
Using a `ComponentMetadataRule` and `ComponentMetadataDetails.belongsTo(...)`
- Given I have a dependency on `a:a:1.0` and `a:b:2.0` (which brings in `a:a:2.0`)
    - and I have a force on `a:a` to `3.0` 
    - then I am seeing a resolved configuration of `a:a:3.0` and `a:b:2.0`, which is not aligning the dependencies.
    - ★ `alignment-static-direct-force` is the simplest example. See also:
        - `alignment-static-direct-force-lock`
        - `alignment-rec-direct-force`
        - `alignment-rec-direct-force`
- Given I have a dependency on `d:d:5.0` and `d:e:6.0` (which brings in `d:d:6.0`)
    - and I have a force on `d:e` to `4.0` 
    - then I am seeing a resolved configuration of `d:d:5.0` and `d:e:5.0`, which is different than other aligning behavior, making alignment override the force of dependency `d:e`.
    - ★ `alignment-static-transitive-force` is the simplest example. See also:
        - `alignment-static-transitive-force-lock`
        - `alignment-rec-transitive-force`
        - `alignment-rec-transitive-force`
