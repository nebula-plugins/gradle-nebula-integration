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

## Results

Results are located in `docs/<grouping>/<scenario>` with 
- an `output.txt` file showing the tasks run, the console output, and assertions we're validating against.
- a folder `input` with the contributing `build.gradle` and other related files, such as generated locks and a local bom 

These results are directly written from test results, so they will regenerate when tests are run with `./gradlew clean build`
