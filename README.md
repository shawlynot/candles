### Running
1. Run `mvn clean install`. 
2. Start a Kafka instance. The application uses localhost:9092 by default, and assumes admin permissions. [I used docker](https://kafka.apache.org/quickstart).
3. Run the producer jar.
4. Run the consumer jar.

### Configuration
See the relevant `application.yaml`s. The kafka servers, the topic and the symbol can all be overriden using the
standard Spring Boot mechanisms (e.g. env vars or Java properties).