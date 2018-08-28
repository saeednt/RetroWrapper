# RetroWrapper

**This is a wrapper around Retrofit to make your use of library in app-level so much easier. This will make your classes of retrofit implementation with just one annotation and omits the boilerplate for you. ;)**
  
  ### Problem: (Don't do these steps)
  **1-** First you want to create a class which is responsible for holding an instance of retrofit which will create your caller classes from your interfaces:
```kotlin
object RetroKeeper {  
          val retro: Retrofit by lazy{  
          val rconfig = RetroConfig() //this class is for holding the configuration of the retrofit  
                  val r = Retrofit.Builder()  
                  r.baseUrl(rconfig.baseUrl)  
                  rconfig.converters.forEach({r.addConverterFactory(it)})  
                  rconfig.callAdapters.forEach({r.addCallAdapterFactory(it)})  
                  r.build()  
                  }  
}
```
  
  **2-** Then let's say You want to call a server API with the class below:
```kotlin
interface GetUserProfileCall {  
          @GET("user/{username}/profile")  
          fun call(@Path("username") username: String): Observable<User>  
}
```
      
  
  **3-** Then you might want a class responsible for creating the Call and handling the response and progress like this:
```kotlin
class GetUserProfileCaller {  
  	    private val api: GetUserProfileCall = RetroKeeper.retro.create(GetUserProfileCall::class.java)  
    
  	    private val obsScheduler: Scheduler = RetroConfig().observeScheduler  
    
  	    private lateinit var username: String  
    
  	    private var progress: ProgressHandler? = null  
    
  	    fun call(@Path("username") username: String, progress: ProgressHandler? = null): Observable<User> {  
  	        this.username = username  
  	        this.progress = progress  
  	        return api.call(username)  
  	        .observeOn(obsScheduler)  
  	        .subscribeOn(Schedulers.io())  
  	        .doOnSubscribe{ progress?.showProgress() }  
  	        .doOnError{ progress?.showError() }
  	        .doOnNext{ progress?.hideProgress() }  
  	    }  
  	}
```
      
  ### Solution: (Do these steps instead)
  You don't need to do any of the above. For the configuration of the retrofit you just have to create a config class which implements the `BaseConfig` class and is annotated with `@RetroConfig`:
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
      
  And for making all those classes above just create an empty interface and annotate it with `@Request`:
```kotlin
@Request(  
          url = "user/{username}/profile",  
          verb = Verb.GET,  
          returnType = User::class  
          )  
interface GetUserProfile
```
      
  ### Extra configuration:
  In the `@Request` annotation you can find these options:
  
  | Paremeter | Usage | Default|
  |-|-|-|
  |**url**|The url part which is added to call function annotation|-|
  |**verb**|The http verb for retrofit call|-|
  |**returnType**|The object which will be returned by the response converters after a successful call|-|
  |**bodyType**|For adding a @Body parameter to your call|`NullType::class`
  |**rxEnabled**|If set to true your call function returns Observable\<returnType>. Otherwise the function returns Call\<returnType>|`true`
  |**pagedRequest**|If your call has pagination and you want the pagination to be handled for you you can set this to true|`false`|
  |**ordered**|If you request has order parameters you can set this to true|`false`|
  |**hasQueryMap**|If you want to pass extra query map parameter to the call you can set this to true|`false`|
**

