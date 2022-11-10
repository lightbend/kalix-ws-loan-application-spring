# Kalix Workshop - Loan application - Spring
Not supported by Lightbend in any conceivable way, not open for contributions.
## Prerequisite
Java 17<br>
Apache Maven 3.6 or higher<br>
[Kalix CLI](https://docs.kalix.io/kalix/install-kalix.html) <br>
Docker 20.10.8 or higher (client and daemon)<br>
Container registry with public access (like Docker Hub)<br>
Access to the `gcr.io/kalix-public` container registry<br>
cURL<br>
IDE / editor<br>

## Create kickstart maven project

```
mvn \
archetype:generate \
-DarchetypeGroupId=io.kalix \
-DarchetypeArtifactId=kalix-spring-boot-archetype \
-DarchetypeVersion=LATEST
```
Define value for property 'groupId': `io.kx`<br>
Define value for property 'artifactId': `loan-application-spring`<br>
Define value for property 'version' 1.0-SNAPSHOT: :<br>
Define value for property 'package' io.kx: : `io.kx.loanapp`<br>

## Import generated project in your IDE/editor

## Update main class
1. Move `io.kx.Main` to `io.kx` package
2. Change default annotation for `ACL` to: `@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))`

##Update pom.xml
In `pom.xml`:
1. In `<mainClass>io.kx.Main</mainClass>` replace `io.kx.Main` with `io.kx.Main`
2. In `<dockerImage>my-docker-repo/${project.artifactId}</dockerImage>` replace `my-docker-repo` with the your `dockerId`


# Loan application service

## Define persistence (domain)
1. Create package `io.kx.loanapp.doman`
2. Create enum `LoanAppDomainStatus` 
3. Create Java Record `LoanAppDomainState` and add parameters
4. Create Java Interface `LoanAppDomainEvent` and add Java records for events `Submitted`, `Approved`, `Declined` and Jackson annotations for polymorph serialization
5. In `LoanAppDomainState` Java Record implement `empty`, `onSubmitted`, `onApproved` and `onDeclined` methods

<i><b>Tip</b></i>: Check content in `loan-app-step-1` git branch

## Define API data structure and endpoints
1. Create package `io.kx.loanapp.api`<br>
2. Create Java Interface `LoanAppApi` and add Java Records for requests and responses
3. Create class `LoanAppService` extending `EventSourcedEntity<LoanAppDomainState>`
    1. add class level annotations (event sourcing entity configuration):
   ```
   @EntityKey("loanAppId")
   @EntityType("loanapp")
   @RequestMapping("/loanapp/{loanAppId}")
   ```
    2. add class level annotations (path prefix):
   ```
   @RequestMapping("/loanapp/{loanAppId}")
   ```
    3. Override `emptyState` and return `LoanAppDomainState.empty()`, set loanAppId via `EventSourcedEntityContext` injected through the constructor
    4. Implement each request method and event handlers

<i><b>Tip</b></i>: Check content in `loan-app-step-1` git branch


## Implement unit test
1. Create  `src/test/java` <br>
2. Create  `io.kx.loanapp.LoanAppServiceTest` class<br>
3. Implement `happyPath`
   <i><b>Tip</b></i>: Check content in `loan-app-step-1` git branch

## Run unit test
```
mvn test
```
## Implement integration test
1. Edit `io.kx.loanapp.IntegrationTest` class<br>
3. Implement `happyPath`
<i><b>Tip</b></i>: Check content in `loan-app-step-1` git branch

## Run integration test
```
mvn -Pit verify
```

<i><b>Note</b></i>: Integration tests uses [TestContainers](https://www.testcontainers.org/) to span integration environment so it could require some time to download required containers.
Also make sure docker is running.

## Run locally

In project root folder there is `docker-compose.yaml` for running `kalix proxy` and (optionally) `google pubsub emulator`.
<i><b>Tip</b></i>: You can comment out google pubsub emulator from `docker-compose.yaml` because it is not used here
```
docker-compose up
```

Start the service:

```
mvn exec:exec
```

## Test service locally
Submit loan application:
```
curl -XPOST -d '{
  "clientId": "12345",
  "clientMonthlyIncomeCents": 60000,
  "loanAmountCents": 20000,
  "loanDurationMonths": 12
}' http://localhost:9000/loanapp/1/submit -H "Content-Type: application/json"
```

Get loan application:
```
curl -XGET http://localhost:9000/loanapp/1 -H "Content-Type: application/json"
```

Approve:
```
curl -XPOST http://localhost:9000/loanapp/1/approve -H "Content-Type: application/json"
```

## Register for Kalix account or Login with existing account
[Register](https://console.kalix.io/register)

## kalix CLI
Login (need to be logged in the Kalix Console in web browser):
```
kalix auth login
```
Create new project:
```
kalix projects new loan-application --region gcp-us-east1
```
<i><b>Note</b></i>: Replace `<REGION>` with desired region

List projects:
```
kalix projects list
```
Set project:
```
kalix config set project loan-application
```

## Package & Deploy

<i><b>Note</b></i>: Make sure you have replaced `my-docker-repo` with the your `dockerId` in `<dockerImage>my-docker-repo/${project.artifactId}</dockerImage>`

```
mvn deploy
```


## Expose service
```
kalix services expose loan-application-spring
```
Result:
`
Service 'loan-application' was successfully exposed at: <some_host>.us-east1.kalix.app
`
## Test service in production
Submit loan application:
```
curl -XPOST -d '{
  "clientId": "12345",
  "clientMonthlyIncomeCents": 60000,
  "loanAmountCents": 20000,
  "loanDurationMonths": 12
}' https://<somehost>.kalix.app/loanapp/1/submit -H "Content-Type: application/json"
```
Get loan application:
```
curl -XGET https://<somehost>.kalix.app/loanapp/1 -H "Content-Type: application/json"
```
Approve:
```
curl -XPOST https://<somehost>.kalix.app/loanapp/1/approve -H "Content-Type: application/json"
```

# Loan application processing service
## Create loan application processing packages
Create package `io.kx.loanproc` in `main` and `test` <br>

## Define persistence (domain) data structure  (GRPC)
1. Create package `io.kx.loanproc.doman`<br>
2. Create enum `LoanProcDomainStatus`
3. Create Java Record `LoanProcDomainState`
4. Create Java Interface `LoanProcDomainEvent` and add Java records for events `ReadyForReview`, `Approved`, `Declined` and Jackson annotations for polymorph serialization
5. In `LoanProcDomainState` Java Record implement `empty`, `onReadyForReview`, `onApproved` and `onDeclined` methods

<i><b>Tip</b></i>: Check content in `loan-proc-step-2` git branch

## Define API data structure and endpoints (GRPC)
1. Create package `io.kx.loanproc.api`<br>
2. Create Java Interface `LoanProcApi` and add Java records for requests and responses
3. Create class `LoanProcService` extending `EventSourcedEntity<LoanProcDomainState>`
   1. add class level annotations (event sourcing entity configuration):
   ```
   @EntityKey("loanAppId")
   @EntityType("loanproc")
   @RequestMapping("/loanproc/{loanAppId}")
   ```
   2. add class level annotations (path prefix):
   ```
   @RequestMapping("/loanproc/{loanAppId}")
   ```
   2. Override `emptyState` and return `LoanProcDomainState.empty()`, set loanAppId via `EventSourcedEntityContext` injected through the constructor
   3. Implement each request method and event handlers
      
<i><b>Tip</b></i>: Check content in `loan-proc-step-2` git branch


## Implement unit test
1. Create  `src/test/java` 
2. Create  `io.kx.loanproc.LoanProcServiceTest` class<br>
3. Create `happyPath`
   <i><b>Tip</b></i>: Check content in `loan-proc-step-2` git branch

## Run unit test
```
mvn test
```
## Implement integration test
1. Edit `io.kx.loanproc.IntegrationTest` class<br>
2.
<i><b>Tip</b></i>: Check content in `loan-proc-step-2` git branch

## Run integration test
```
mvn -Pit verify
```

<i><b>Note</b></i>: Integration tests uses [TestContainers](https://www.testcontainers.org/) to span integration environment so it could require some time to download required containers.
Also make sure docker is running.

## Run locally

In project root folder there is `docker-compose.yaml` for running `kalix proxy` and (optionally) `google pubsub emulator`.
<i><b>Tip</b></i>: You can comment out google pubsub emulator from `docker-compose.yaml` because it is not used here
```
docker-compose up
```

Start the service:

```
mvn exec:exec
```
## Test service locally
Start processing:
```
curl -XPOST http://localhost:9000/loanproc/1/process -H "Content-Type: application/json"
```

Get loan processing:
```
curl -XGET http://localhost:9000/loanproc/1 -H "Content-Type: application/json"
```

Approve:
```
curl -XPOST -d '{"reviewerId":"9999"}' http://localhost:9000/loanproc/1/approve -H "Content-Type: application/json"
```
## Package & Deploy
```
mvn deploy
```
## Test service in production
Start processing:
```
curl -XPOST https://<somehost>.kalix.app/loanproc/1/process -H "Content-Type: application/json"
```

Get loan processing:
```
curl -XGET https://<somehost>.kalix.app/loanproc/1 -H "Content-Type: application/json"
```

Approve:
```
curl -XPOST -d '{"reviewerId":"9999"}' https://<somehost>.kalix.app/loanproc/1/approve -H "Content-Type: application/json"
```

# Views
## Create a view
1. Create package `io.kx.loanproc.view`
2. Create Java Interface `io.kx.loanproc.view.LoanProcViewModel` with Java records for `ViewRecord` and `ViewRequest`
3. Create `io.kx.loanproc.viewLoanProcByStatusView` class extending `View`
   1. Add class level annotation for table name: `@Table("loanproc_by_status")`
   2. Implement getLoanProcByStatus with `@Query` and `@PostMapping` annotations
   3. Implement event handler methods for each domain event

<i><b>Tip</b></i>: Check content in `views-step-3` git branch

##Unit test
Because of the nature of views only Integration tests are done.

## Create integration tests for view
In `io.kx.loanproc.IntegrationTest` copy `loanProcHappyPathWith` test to `loanProcHappyPathWithView` and add view query via `webClient`
<i><b>Tip</b></i>: Check content in `views-step-3` git branch

## Run integration test
```
mvn -Pit verify
```
## Package & Deploy
```
mvn deploy
```
## Test service in production
```
curl -XPOST -d {"statusId":"STATUS_APPROVED"} https://<somehost>.kalix.app/loanproc/views/by-status -H "Content-Type: application/json"
```