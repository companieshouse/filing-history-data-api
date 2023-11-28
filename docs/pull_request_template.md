## Describe the changes

### Related Jira tickets
[Jira Ticket](URL)

## Developer check list
### General
- [ ] Is the PR as small as it can be?
- [ ] Has the Jira ticket been updated with any relevant comments?
- [ ] Is the `README` up to date?
- [ ] Has the `POM` been updated for the latest versions of dependencies?
- [ ] Has the code been double-checked against `main`?
- [ ] Does the code adhere standards in this repository, including SonarQube checks?

### Testing
- [ ] Do the code changes have unit and integration tests with code coverage > 80%?
- [ ] Has the code been tested locally and/or passed the API Karate tests on Tilt?
- [ ] Have mandatory manual test cases been run?
- [ ] Are extra Karate tests required?
- [ ] Are all the test data resources up to date?

### Release preparation
_Where possible, add links to the respective Jira tickets when an item is checked_
- [ ] Have these changes been included in a release ticket?
- [ ] Are changes required to the `docker-chs-development` deployment or test data?
- [ ] Are any changes required to the environment added to `chs-configs`?

## Notes to the tester
_Add any testing hints or instructions here._
