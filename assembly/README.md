# Assembly utils

Use the assembly descriptors to package your apps

Example that produces the main `jar` file together with an accompanying `lib/` directory:

```
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-assembly-plugin</artifactId>
            <version>2.4</version>
            <dependencies>
                <dependency>
                    <groupId>com.github.marsik.java-utils</groupId>
                    <artifactId>assembly</artifactId>
                    <version>1.0-SNAPSHOT</version>
                </dependency>
            </dependencies>
            <configuration>
                <descriptorRefs>
                    <descriptorRef>jarwithlib</descriptorRef>
                </descriptorRefs>
            </configuration>
            <executions>
                <execution>
                    <id>make-assembly</id>
                    <phase>package</phase>
                    <goals>
                        <goal>single</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```
