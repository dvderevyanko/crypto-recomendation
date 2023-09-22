# CryptoRecomendation
CryptoRecommendation service is a web based back-end service (API) in java that helps investors to make right choice in buying crypto. It allows:
1. Upload currency prices
2. Provide the currency with the best range
3. Provide the currency details

Service stores the currency data in Redis. Besides storing day prices service aggregates prices into the monthly/yearly/total 
currency summary that can be accessible in O(1) constant time for better performance. These data can be used in future 
for more complicated analysis. For example, currency can have some strategies to better forecast or some rules 
that keep range values and exclude the currency from recommended to invest.  

## Usage 
1. `POST /crypto` - add currency to the set of supported currencies. This set is used to validate all requests. 
2. `POST /crypto/{currency}/upload` - upload files one by one with prices. Same file can be uploaded several time, data will be replaced.
3. `GET /crypto/max?year=&month&day=` - get the crypto with the highest normalized range for a specific day 
4. `GET /crypto/{currency}` - get the oldest/newest/min/max values for a requested crypto
5. `GET /crypto` - get a descending sorted list of all the cryptos, comparing the normalized range (i.e. (max-min)/min)

## Notes

- Build script uses arm64 docker image `arm64v8/openjdk:18`. In case of using x86-64 platform it requires to change image to `openjdk:18` in the build.gradle.
- Requests are limited : only 10 requests are allowed from the same ip address in one hour

## Run application

`./gradlew composeUpRedis` starts redis only

`./gradlew bootRun` starts standalone service that connects to Redis container

## Run application in container

`./gradlew composeUp -PwithBuild` builds the service image and start application with Redis in containers (Execution command without flag `withBuild` runs image that was built before).

`./gradlew composeDown` stops the containers.

## Swagger

When application runs the descriptions are be available at the path [/v3/api-docs](http://localhost:8080/v3/api-docs) and [swagger](http://localhost:8080/swagger-ui/index.html)