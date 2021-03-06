# ribac - Right Based Access Control
ribac is a micro service that...
* ...manages your users rights
* ...answers questions about the rights of a user
* ...replaces access control specific business logic

Its goal is that the code to check if a user has the right to do a specific action in your system is as simple as 'Has the user _ the right to do _?'

## Installation
0. Download the creation script for the ribac database:
    ```bash
    curl --silent https://raw.githubusercontent.com/JonasTaulienSolutions/ribac/master/ribac.sql \
         --output ./ribac.sql
    ```

0. Create a file `docker-compose.yaml` or add the following to your existing compose file:
    ```yaml
    version: '3.7'
    networks:
      ribacnet:
        external: false
    services:
      ribac-db:
        image: mysql:8.0
        ports:
        - 3306:3306
        restart: always
        networks:
        - ribacnet
        command: --default-authentication-plugin=mysql_native_password
        environment:
          MYSQL_ROOT_PASSWORD: root-password
          MYSQL_DATABASE: ribac
          MYSQL_USER: ribac
          MYSQL_PASSWORD: ribac-password
        volumes:
        - ./ribac.sql:/docker-entrypoint-initdb.d/ribac.sql:ro
    
      ribac:
        image: jonastauliensolutions/ribac:0.1.0-SNAPSHOT
        ports:
        - 8080:8080
        restart: always
        networks:
        - ribacnet
        depends_on:
        - ribac-db
        environment:
          RIBAC_DB_HOST: ribac-db
          RIBAC_DB_PORT: 3306
          RIBAC_DB_NAME: ribac
          RIBAC_DB_USER: ribac
          RIBAC_DB_PASSWORD: ribac-password
    ```

0. Start ribac
    ```bash
    docker-compose up --detached
    ```

## Usage
### 1. Add ribac-client as dependency
```xml
<dependency>
    <groupId>jonastauliensolutions</groupId>
    <artifactId>ribac-client</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### 2. Create Client
```java
Ribac ribac = Ribac.create("localhost", 8080);
```

### 3. Add Users to ribac
In ribac a User IS the id you gave the user in your database.
```java
RibacUser user123 = ribac.createUser("user123");
```

### 4. Add Groups to ribac
A Group is a set of Users. Before you can add Users to a Group you have to create the Group:
```java
RibacGroup administrators = ribac.createGroup("Administrators");
```

### 5. Add Users to Groups
You can add Users to zero or as many Groups as you want:
```java
ribac.addUser("user123").toGroup("Administrators");
```

### 6. Add Rights to ribac
A Right is an action a User or an Group of Users is allowed to make on your system.
Before you can assign a Right, you have to add it:
```java
RibacRight createAdministrators = ribac.createRight("Create Administrators");
```

### 7. Assign Rights
You can assign a Right to a single User
```java
ribac.giveUser("user123").right("Create Administrators");
```

Or you can assign a Right to a Group of Users
```java
ribac.giveGroup("Administrators").right("Create Administrators");
```

### 8. Create RightSets
You probably not only want to give multiple Users a single Right but also to give a single User multiple Rights.  
That's what RightSets are for:

0. Create RightSet
    ```java
    RibacRightSet viewPublishedProducts = ribac.createRightSet("View Published Products");
    ```

0. Add Rights to RightSet (you have to create them before doing this)
    ```java
    ribac.addRight("View Product With Id 456").toRightSet("View Published Products");
    ```

0. Assign RightSets
    * To Users
        ```java
        ribac.giveUser("user123").rightSet("View Published Products");
        ```
    * Or to Groups
        ```java
        ribac.giveGroup("Administrators").rightSet("View Published Products");
        ```


### 9. Query ribac
Ask if a User has a Right:
```java
boolean hasRight = ribac.hasUser("user123").right("Create Administrators");
// Or
boolean hasRight = user123.hasRight("View Product With Id 457");
```