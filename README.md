## LookLookum
#### Telegram bot based on [MS Azure Face API](https://azure.microsoft.com/en-us/services/cognitive-services/face/)
You can send bot a picture and he will find human faces on it, send back your picture with founded faces marked and person's description (gender, age, emotions) in a following text message.
### Tech
Looklookum uses a number of open source projects to work properly:
- Java 8
- [TelegramBots](https://github.com/rubenlagus/TelegramBots): Java library to create bots using Telegram Bots API
- [emoji-java](https://github.com/vdurmont/emoji-java): The missing emoji library for Java
- [logback](https://github.com/qos-ch/logback): The reliable, generic, fast and flexible logging framework for Java
- Apache maven
### Installation and run
Application needs some secure info to work with APIs endpoints. By default, it takes this data from `app.properties` file, you need to create it and fill in your credentials, and put to the classpath (e.g. `src/main/resources/app.properties`):
```
#Telegram
botUserName=<your bot name>
botToken=<your bot token>

#Microsoft Azure
faceApiEndPoint=<face API URL>
msAzureSubscriptionKey=<your Azure subscription key>
```
Compile, package and install application:
```sh
$ cd looklookum
$ mvn clean install
```
Run:
```sh
$ java -jar target/looklookum.jar
```
### License
MIT

