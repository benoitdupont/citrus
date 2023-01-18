The goal of these samples is to illustrate my use case, and how/if Citrus can be used like that.

The flow looks like this:

1. A client does a http get on my SUT: http_get(http://localhost:8080/resource)
2. The SUT is a kind of middleman and will forward the http query to an external server: http_get(http://localhost:8081/login)
3. The external server will provide a Jwt in its response
4. The SUT code then use the token to securely query the resource to the same external server: http_get(http://localhost:8081/someResource?token=someBase64Token)

There are two ways to reproduce the issue:

# The long way, which use a real SUT that needs to be launched manually

1. Launch first the SUT, a Spring Boot simple rest app: be.ben.MySutApplication
2. Launch the test ``GetResourceWithSUTTest``

The test will gives the following stacktrace:
```
Unable to create endpoint for static endpoint adapter type 'class com.consol.citrus.endpoint.adapter.StaticResponseEndpointAdapter'
com.consol.citrus.exceptions.TestCaseFailedException: Unable to create endpoint for static endpoint adapter type 'class com.consol.citrus.endpoint.adapter.StaticResponseEndpointAdapter'
	at com.consol.citrus.DefaultTestCase.executeAction(DefaultTestCase.java:143)
	at com.consol.citrus.DefaultTestCaseRunner.run(DefaultTestCaseRunner.java:125)
	at com.consol.citrus.GherkinTestActionRunner.then(GherkinTestActionRunner.java:66)
	at AssertOnTheMock_Fails_Test.testThatTheMockWillAnswer(AssertOnTheMock_Fails_Test.java:48)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:566)
	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:725)
	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60)
	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131)
```

# Or the simple way, without the SUT

I tried to simplify the use case which I think is not dependent of my Sut:

Execute both tests in ``GetResourcesWithoutSUTTest``, they will show that
1. It works fine when no assertions on the httpMockServer is done 
2. It fails with the above stacktrace when we assert on the httpMockServer