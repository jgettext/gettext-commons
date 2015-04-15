You don't have to add a custom maven repository as Gettext commons is [available on maven central](http://search.maven.org/#search|ga|1|g%3A%22com.googlecode.gettext-commons%22)

# Adding the Gettext Commons library #

Add the following lines to your pom.xml file to add the Gettext Commons library as a dependency to your project:

```
<project>
 ...
 <dependencies> 
  ...
  <dependency>
      <groupId>com.googlecode.gettext-commons</groupId>
      <artifactId>gettext-commons</artifactId>
      <version>0.9.8</version>
  </dependency>
 </dependencies>
```

# Adding the Maven plugin #

Add the following lines to your pom.xml file to make use of the maven gettext plugin for generating messages templates and deploying messages bundles.

```
<project>
 <plugins> 
  ...
  <plugin>
    <groupId>com.googlecode.gettext-commons</groupId>
    <artifactId>gettext-maven-plugin</artifactId>
    <version>1.2.4</version>
    <executions>
        <execution>
            <id>convert-po-class</id>
            <phase>compile</phase>
            <goals>
                <goal>dist</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
      <poDirectory>${basedir}/po</poDirectory>
      <targetBundle>my.package.Messages</targetBundle>
      <outputFormat>properties</outputFormat>
    </configuration>
  </plugin>
 </plugins>
```