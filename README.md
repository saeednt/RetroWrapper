# RetroWrapper

**This is a wrapper around Retrofit to make your use of library in app-level so much easier. This will make your classes of retrofit implementation with just one annotation and omits the boilerplate for you. ;)**
  
  ### Usage
  **1-** For the configuration of the retrofit create a class which implements the `BaseConfig` class and is annotated with `@RetroConfig`:
```kotlin
@RetroConfig  
class RetroConfig : BaseConfig {  
     override val baseUrl: String  
         get() = "https://myurl.com"  
     override val converters: List<Converter.Factory> = arrayListOf(LoganSquareConverterFactory.create())  
     override val callAdapters: List<CallAdapter.Factory> = arrayListOf(RxJava2CallAdapterFactory.create())  
     override val observeScheduler: Scheduler  
         get() = AndroidSchedulers.mainThread()  
}
```
  
**2-**   For each of your requests make an empty interface and annotate it with `@Request`:
```kotlin
@Request(  
       url = "user/{username}/profile",  
       verb = Verb.GET,  
       returnType = User::class  
       )  
interface GetUserProfile
```

And just like this for post requests:
```kotlin
@Request(  
       url = "user/signin",  
       verb = Verb.POST,  
       returnType = User::class,
       bodyType = RequestBody::class
       )  
interface SignIn
```
      
As simple as that. You can call your APIs like this:
```kotlin
var caller = GetUserProfileCaller() //this is the name of the interface + Caller
caller.call("saeednt")
    .subscribe{...}
```
And
```kotlin
var caller = SignInCaller() //this is the name of the interface + Caller
var body = RequestBody()
caller.call(body)
    .subscribe{...}
```
      
  ### Extra configuration:
  In the `@Request` annotation you can find these options:
  
  | Parameter | Usage | Default|
  |-|-|-|
  |**url**|The url part which is added to call function annotation|-|
  |**verb**|The http verb for retrofit call|-|
  |**returnType**|The object which will be returned by the response converters after a successful call|-|
  |**bodyType**|For adding a @Body parameter to your call|`NullType::class`
  |**rxEnabled**|If set to true your call function returns Observable\<returnType>. Otherwise the function returns Call\<returnType>|`true`
  |**pagedRequest**|If your call has pagination and you want the pagination to be handled for you you can set this to true|`false`|
  |**ordered**|If you request has order parameters you can set this to true|`false`|
  |**hasQueryMap**|If you want to pass extra query map parameter to the call you can set this to true|`false`|

### Installation [![Latest Version](https://api.bintray.com/packages/saeednt/RetroWrapper/com.nt.retrowrapper/images/download.svg)](https://bintray.com/saeednt/RetroWrapper/com.nt.retrowrapper/_latestVersion)
For gradle:
```groovy
implementation 'com.nt.retrowrapper:wrapper:latest_version'
kapt 'com.nt.retrowrapper:compiler:latest_version'
```

And for maven:
```xml
<dependency>
 <groupId>com.nt.retrowrapper</groupId>
 <artifactId>wrapper</artifactId>
 <version>latest_version</version>
 <type>pom</type> 
</dependency>
```
and
```xml
<annotationProcessorPath>
 <groupId>com.nt.retrowrapper</groupId>
 <artifactId>compiler</artifactId>
 <version>latest_version</version>
 <type>pom</type> 
</annotationProcessorPath>
```

Or you can download the library and compiler from bintray in .jar format:
[Bintray Repo](https://bintray.com/saeednt/RetroWrapper/com.nt.retrowrapper)

Note that you have to add the compiler dependency using `kapt ...`

And the library dependency using `implementation ...`